package com.example.budgetmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetmanager.databinding.ActivityIncomeListBinding

class IncomeListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncomeListBinding
    private lateinit var adapter: IncomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun setupRecyclerView() {
        adapter = IncomeAdapter(ExpenseDataManager.getIncomes())
        binding.rvIncomes.layoutManager = LinearLayoutManager(this)
        binding.rvIncomes.adapter = adapter
    }

    private fun updateUI() {
        val incomes = ExpenseDataManager.getIncomes()
        val total = ExpenseDataManager.getTotalIncome()

        adapter.updateIncomes(incomes)
        binding.tvTotalIncome.text = "Total Income: $${String.format("%.2f", total)}"

        // Show message if no incomes
        if (incomes.isEmpty()) {
            binding.tvTotalIncome.text = "No income yet"
        }
    }
}