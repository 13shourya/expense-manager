package com.example.budgetmanager

import android.os.Bundle
import android.view.View // Add this import
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetmanager.databinding.ActivityTransactionHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionHistoryBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private val transactionList = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadTransactions()

        // Set up back button
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = binding.recyclerViewTransactions
        adapter = TransactionAdapter(transactionList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        Log.d("TRANSACTION_HISTORY", "RecyclerView setup completed")
    }

    private fun loadTransactions() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("TRANSACTION_HISTORY", "User not authenticated")
            return
        }

        Log.d("TRANSACTION_HISTORY", "Loading transactions for user: $userId")

        db.collection("users")
            .document(userId)
            .collection("transactions")
            .orderBy("date", Query.Direction.DESCENDING) // Changed from "createdAt" to "date"
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TRANSACTION_HISTORY", "Error loading transactions: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    transactionList.clear()

                    for (document in snapshot.documents) {
                        try {
                            // FIXED: Use description instead of title, remove userId and createdAt
                            val transaction = Transaction(
                                id = document.id,
                                type = document.getString("type") ?: "",
                                amount = document.getDouble("amount") ?: 0.0,
                                category = document.getString("category") ?: "",
                                date = document.getString("date") ?: "",
                                description = document.getString("description") ?: "" // Use description instead of title
                            )
                            transactionList.add(transaction)
                            Log.d("TRANSACTION_HISTORY", "Loaded transaction: ${transaction.description} - ${transaction.amount}")
                        } catch (e: Exception) {
                            Log.e("TRANSACTION_HISTORY", "Error parsing document: ${document.id}", e)
                        }
                    }

                    adapter.notifyDataSetChanged()
                    Log.d("TRANSACTION_HISTORY", "Successfully loaded ${transactionList.size} transactions")

                    // Update empty state visibility
                    binding.emptyState.visibility = if (transactionList.isEmpty()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                } else {
                    Log.d("TRANSACTION_HISTORY", "No transactions found")
                    binding.emptyState.visibility = View.VISIBLE
                }
            }
    }
}