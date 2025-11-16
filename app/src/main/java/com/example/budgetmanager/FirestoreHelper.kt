package com.example.budgetmanager.utils

import com.example.budgetmanager.ExpenseItem
import com.example.budgetmanager.IncomeItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()
    private const val EXPENSES_COLLECTION = "expenses"
    private const val INCOMES_COLLECTION = "incomes"


    fun addExpense(
        expense: ExpenseItem,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = FirebaseAuthHelper.getCurrentUserId()
        if (userId == null) {
            onError("User not logged in")
            return
        }

        val expenseWithUser = expense.copy(userId = userId)
        db.collection(EXPENSES_COLLECTION)
            .add(expenseWithUser)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to add expense")
            }
    }

    fun getExpenses(
        onSuccess: (List<ExpenseItem>) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = FirebaseAuthHelper.getCurrentUserId()
        if (userId == null) {
            onError("User not logged in")
            return
        }

        db.collection(EXPENSES_COLLECTION)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val expenses = documents.map { doc ->
                    doc.toObject(ExpenseItem::class.java).copy(id = doc.id)
                }
                onSuccess(expenses)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to get expenses")
            }
    }


    fun addIncome(
        income: IncomeItem,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = FirebaseAuthHelper.getCurrentUserId()
        if (userId == null) {
            onError("User not logged in")
            return
        }

        val incomeWithUser = income.copy(userId = userId)
        db.collection(INCOMES_COLLECTION)
            .add(incomeWithUser)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to add income")
            }
    }

    fun getIncomes(
        onSuccess: (List<IncomeItem>) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = FirebaseAuthHelper.getCurrentUserId()
        if (userId == null) {
            onError("User not logged in")
            return
        }

        db.collection(INCOMES_COLLECTION)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val incomes = documents.map { doc ->
                    doc.toObject(IncomeItem::class.java).copy(id = doc.id)
                }
                onSuccess(incomes)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to get incomes")
            }
    }
}