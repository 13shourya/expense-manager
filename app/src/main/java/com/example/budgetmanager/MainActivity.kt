package com.example.budgetmanager

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetmanager.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("DEBUG", " MainActivity created")

        auth = FirebaseAuth.getInstance()


        showSHA1Fingerprint()

        if (auth.currentUser == null) {
            Log.d("DEBUG", " No user found, going to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        Log.d("DEBUG", " User is logged in: ${auth.currentUser?.uid}")
        loadDashboardData()
        setupClickListeners()
    }


    private fun showSHA1Fingerprint() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA-1")
                md.update(signature.toByteArray())
                val digest = md.digest()

                // Convert to readable SHA-1
                val sha1 = digest.joinToString("") { "%02X".format(it) }
                    .chunked(2)
                    .joinToString(":")

                // Show it in Toast and Log
                Toast.makeText(this, "SHA-1: $sha1", Toast.LENGTH_LONG).show()
                println(" SHA-1 FOR FIREBASE: $sha1")
                Log.d("SHA1_DEBUG", "SHA-1: $sha1")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error getting SHA1", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnAddExpense.setOnClickListener {
            Log.d("DEBUG", " Add Expense button clicked")
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        binding.btnAddIncome.setOnClickListener {
            Log.d("DEBUG", " Add Income button clicked")
            startActivity(Intent(this, AddIncomeActivity::class.java))
        }

        binding.btnViewHistory.setOnClickListener {
            Log.d("DEBUG", " View History button clicked")
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("DEBUG", " MainActivity resumed")
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val user = auth.currentUser ?: return
        val userId = user.uid

        Log.d("DEBUG", " Starting to load dashboard data for user: $userId")

        // Load expenses
        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { expensesSnapshot ->
                Log.d("DEBUG", " Successfully loaded ${expensesSnapshot.size()} expenses")
                var totalExpenses = 0.0
                for (document in expensesSnapshot) {
                    val amount = document.getDouble("amount") ?: 0.0
                    totalExpenses += amount
                    Log.d("DEBUG", " Expense: ${document.data}")
                }

                // Load incomes
                db.collection("incomes")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { incomesSnapshot ->
                        Log.d("DEBUG", " Successfully loaded ${incomesSnapshot.size()} incomes")
                        var totalIncome = 0.0
                        for (document in incomesSnapshot) {
                            val amount = document.getDouble("amount") ?: 0.0
                            totalIncome += amount
                            Log.d("DEBUG", " Income: ${document.data}")
                        }

                        val balance = totalIncome - totalExpenses
                        Log.d("DEBUG", " Totals - Income: $$totalIncome, Expenses: $$totalExpenses, Balance: $$balance")

                        // Update UI
                        binding.tvTotalIncome.text = "$${String.format("%.2f", totalIncome)}"
                        binding.tvTotalExpenses.text = "$${String.format("%.2f", totalExpenses)}"
                        binding.tvBalance.text = "$${String.format("%.2f", balance)}"

                        Log.d("DEBUG", " Dashboard UI updated successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("DEBUG", " Failed to load incomes: ${e.message}")
                        Toast.makeText(this, "Error loading incomes", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("DEBUG", " Failed to load expenses: ${e.message}")
                Toast.makeText(this, "Error loading expenses", Toast.LENGTH_SHORT).show()
            }
    }
}