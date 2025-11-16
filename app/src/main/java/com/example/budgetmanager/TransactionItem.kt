package com.example.budgetmanager

data class TransactionItem(
    val description: String,
    val amount: Double,
    val date: String,
    val type: String
)