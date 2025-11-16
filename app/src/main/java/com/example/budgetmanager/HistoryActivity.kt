package com.example.budgetmanager

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetmanager.databinding.ActivityHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private val transactionsList = mutableListOf<TransactionItem>()
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Setup RecyclerView
        adapter = TransactionAdapter(transactionsList)
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter

        // Load history data
        loadHistoryData()

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadHistoryData() {
        val user = auth.currentUser
        if (user == null) {
            Log.e("HISTORY", "User not logged in")
            return
        }

        val userId = user.uid

        Log.d("HISTORY", " Loading history for user: $userId")

        // Clear previous data
        transactionsList.clear()

        // Load expenses
        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { expensesSnapshot ->
                Log.d("HISTORY", " Found ${expensesSnapshot.size()} expenses")

                for (document in expensesSnapshot) {
                    val title = document.getString("title") ?: ""
                    val amount = document.getDouble("amount") ?: 0.0
                    val category = document.getString("category") ?: ""
                    val date = document.getString("date") ?: "Unknown Date"

                    transactionsList.add(TransactionItem(
                        description = "$title ($category)",
                        amount = -amount, // Negative for expenses
                        date = date,
                        type = "Expense"
                    ))
                }

                // Load incomes
                db.collection("incomes")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { incomesSnapshot ->
                        Log.d("HISTORY", " Found ${incomesSnapshot.size()} incomes")

                        for (document in incomesSnapshot) {
                            val title = document.getString("title") ?: ""
                            val amount = document.getDouble("amount") ?: 0.0
                            val source = document.getString("source") ?: ""
                            val date = document.getString("date") ?: "Unknown Date"

                            transactionsList.add(TransactionItem(
                                description = "$title ($source)",
                                amount = amount, // Positive for income
                                date = date,
                                type = "Income"
                            ))
                        }

                        // Sort by date (newest first)
                        transactionsList.sortByDescending { it.date }

                        Log.d("HISTORY", " Total transactions loaded: ${transactionsList.size}")

                        // Update UI
                        updateUI()
                    }
                    .addOnFailureListener { e ->
                        Log.e("HISTORY", " Failed to load incomes: ${e.message}")
                        updateUI()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("HISTORY", " Failed to load expenses: ${e.message}")
                updateUI()
            }
    }

    private fun updateUI() {
        if (transactionsList.isEmpty()) {
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
            binding.rvTransactions.visibility = android.view.View.GONE
        } else {
            binding.tvEmptyState.visibility = android.view.View.GONE
            binding.rvTransactions.visibility = android.view.View.VISIBLE
            adapter.updateTransactions(transactionsList)
        }
    }
}