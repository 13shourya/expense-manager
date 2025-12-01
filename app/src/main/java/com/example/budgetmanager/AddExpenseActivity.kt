package com.example.budgetmanager

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetmanager.databinding.ActivityAddExpenseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnSaveExpense.setOnClickListener {
            Log.d("DEBUG", " SAVE EXPENSE BUTTON CLICKED")
            saveExpense()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveExpense() {
        val title = binding.etExpenseTitle.text.toString().trim()
        val amountText = binding.etExpenseAmount.text.toString().trim()
        val category = binding.etExpenseCategory.text.toString().trim()

        Log.d(
            "DEBUG",
            " Expense Form Data - Title: '$title', Amount: '$amountText', Category: '$category'"
        )

        // Validation
        if (title.isEmpty() || amountText.isEmpty() || category.isEmpty()) {
            Log.e("DEBUG", "Expense validation failed - empty fields")
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Log.e("DEBUG", " Expense validation failed - invalid amount: $amountText")
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user == null) {
            Log.e("DEBUG", " User not logged in for expense")
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        Log.d("DEBUG", " Creating expense object for user: ${user.uid}")

        val expense = hashMapOf(
            "title" to title,
            "amount" to amount,
            "category" to category,
            "date" to date,
            "userId" to user.uid,
            "type" to "expense",
            "createdAt" to System.currentTimeMillis()
        )

        Log.d("DEBUG", " Attempting to save EXPENSE to Firestore...")

        // FIX: Change from "expenses" to "users/{userId}/transactions"
        db.collection("users")
            .document(user.uid)
            .collection("transactions")
            .add(expense)
            .addOnSuccessListener { documentReference ->
                Log.d("DEBUG", " SUCCESS: Expense saved with ID: ${documentReference.id}")
                Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("DEBUG", " FAILED to save expense: ${e.message}")
                Toast.makeText(this, "Error saving expense: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}