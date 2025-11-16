package com.example.budgetmanager

import com.example.budgetmanager.utils.FirestoreHelper

object ExpenseDataManager {
    private val expenses = mutableListOf<ExpenseItem>()
    private val incomes = mutableListOf<IncomeItem>()

    init {
        loadDataFromFirestore()
    }

    private fun loadDataFromFirestore() {
        // Load expenses from Firestore
        FirestoreHelper.getExpenses(
            onSuccess = { firestoreExpenses ->
                expenses.clear()
                expenses.addAll(firestoreExpenses)
            },
            onError = { error ->
                // If no data in Firestore, use sample data
                addSampleData()
            }
        )

        // Load incomes from Firestore
        FirestoreHelper.getIncomes(
            onSuccess = { firestoreIncomes ->
                incomes.clear()
                incomes.addAll(firestoreIncomes)
            },
            onError = { error ->
                // If no data in Firestore, use sample data
                addSampleIncomeData()
            }
        )
    }

    // Expense methods
    fun addExpense(expense: ExpenseItem) {
        expenses.add(expense)

        // Save to Firestore
        FirestoreHelper.addExpense(
            expense = expense,
            onSuccess = {
                // Successfully saved to Firestore
            },
            onError = { error ->
                // Handle error - you might want to show a message
            }
        )
    }

    fun getExpenses(): List<ExpenseItem> {
        return expenses.toList()
    }

    fun getTotalExpenses(): Double {
        return expenses.sumOf { it.amount }
    }

    // Income methods
    fun addIncome(income: IncomeItem) {
        incomes.add(income)

        // Save to Firestore
        FirestoreHelper.addIncome(
            income = income,
            onSuccess = {
                // Successfully saved to Firestore
            },
            onError = { error ->
                // Handle error
            }
        )
    }

    fun getIncomes(): List<IncomeItem> {
        return incomes.toList()
    }

    fun getTotalIncome(): Double {
        return incomes.sumOf { it.amount }
    }

    fun getBalance(): Double {
        return getTotalIncome() - getTotalExpenses()
    }

    private fun addSampleData() {
        if (expenses.isEmpty()) {
            expenses.add(ExpenseItem(description = "Groceries", amount = 150.0, date = "2024-01-15"))
            expenses.add(ExpenseItem(description = "Transport", amount = 45.0, date = "2024-01-14"))
            expenses.add(ExpenseItem(description = "Entertainment", amount = 75.0, date = "2024-01-13"))
        }
    }

    private fun addSampleIncomeData() {
        if (incomes.isEmpty()) {
            incomes.add(IncomeItem(description = "Salary", amount = 4000.0, date = "2024-01-01"))
        }
    }


    fun refreshData() {
        loadDataFromFirestore()
    }
}