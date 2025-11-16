package com.example.budgetmanager

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetmanager.databinding.ActivityAddIncomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class AddIncomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddIncomeBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // DEBUG: Check if binding works
        Log.d("INCOME_DEBUG", " AddIncomeActivity started")

        binding.btnSaveIncome.setOnClickListener {
            Log.d("INCOME_DEBUG", " SAVE INCOME BUTTON CLICKED")
            saveIncome()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveIncome() {
        // DEBUG: Check if fields exist
        try {
            val title = binding.etIncomeTitle.text.toString().trim()
            val amountText = binding.etIncomeAmount.text.toString().trim()
            val source = binding.etIncomeSource.text.toString().trim()

            Log.d("INCOME_DEBUG", " Income Form Data - Title: '$title', Amount: '$amountText', Source: '$source'")

            // Validation
            if (title.isEmpty() || amountText.isEmpty() || source.isEmpty()) {
                Log.e("INCOME_DEBUG", " Income validation failed - empty fields")
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Log.e("INCOME_DEBUG", " Income validation failed - invalid amount: $amountText")
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return
            }

            val user = auth.currentUser
            if (user == null) {
                Log.e("INCOME_DEBUG", " User not logged in for income")
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d("INCOME_DEBUG", " User is logged in: ${user.uid}")

            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            Log.d("INCOME_DEBUG", " Creating income object...")

            val income = hashMapOf(
                "title" to title,
                "amount" to amount,
                "source" to source,
                "date" to date,
                "userId" to user.uid,
                "type" to "income",
                "createdAt" to System.currentTimeMillis()
            )

            Log.d("INCOME_DEBUG", " Income data: $income")
            Log.d("INCOME_DEBUG", " Attempting to save INCOME to Firestore...")

            // Save to Firestore
            db.collection("incomes")
                .add(income)
                .addOnSuccessListener { documentReference ->
                    Log.d("INCOME_DEBUG", " SUCCESS: Income saved with ID: ${documentReference.id}")
                    Toast.makeText(this, "Income saved successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e("INCOME_DEBUG", " FAILED to save income: ${e.message}")
                    Toast.makeText(this, "Error saving income: ${e.message}", Toast.LENGTH_LONG).show()
                }

        } catch (e: Exception) {
            Log.e("INCOME_DEBUG", " CRASH in saveIncome: ${e.message}")
            e.printStackTrace()
        }
    }
}