package com.example.budgetmanager

data class IncomeItem(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)