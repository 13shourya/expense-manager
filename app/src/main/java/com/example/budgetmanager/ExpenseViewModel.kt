package com.example.budgetmanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ExpenseViewModel : ViewModel() {

    private val _expenses = MutableLiveData<MutableList<ExpenseItem>>(mutableListOf())
    val expenses: LiveData<MutableList<ExpenseItem>> = _expenses

    private val _totalExpenses = MutableLiveData(0.0)
    val totalExpenses: LiveData<Double> = _totalExpenses

    fun addExpense(expense: ExpenseItem) {
        val currentList = _expenses.value ?: mutableListOf()
        currentList.add(expense)
        _expenses.value = currentList


        updateTotalExpenses()
    }

    fun getExpenses(): List<ExpenseItem> {
        return _expenses.value ?: emptyList()
    }

    private fun updateTotalExpenses() {
        val total = _expenses.value?.sumOf { it.amount } ?: 0.0
        _totalExpenses.value = total
    }


    init {
        addSampleData()
    }

    private fun addSampleData() {
        addExpense(ExpenseItem(description = "Groceries", amount = 150.0, date = "2024-01-15"))
        addExpense(ExpenseItem(description = "Transport", amount = 45.0, date = "2024-01-14"))
        addExpense(ExpenseItem(description = "Entertainment", amount = 75.0, date = "2024-01-13"))
    }
}