package com.example.budgetmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetmanager.databinding.ActivityExpenseListBinding

class ExpenseListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseListBinding
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(ExpenseDataManager.getExpenses())
        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = adapter
    }

    private fun updateUI() {
        val expenses = ExpenseDataManager.getExpenses()
        val total = ExpenseDataManager.getTotalExpenses()

        adapter.updateExpenses(expenses)
        binding.tvTotalSpent.text = "Total Spent: $${String.format("%.2f", total)}"

        // Show message if no expenses
        if (expenses.isEmpty()) {
            binding.tvTotalSpent.text = "No expenses yet"
        }
    }
}