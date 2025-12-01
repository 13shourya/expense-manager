package com.example.budgetmanager

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class AddIncomeActivity : AppCompatActivity() {

    private lateinit var amountEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var dateEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var saveButton: Button

    private val db = Firebase.firestore
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_income)

        initializeViews()
        setupDatePicker()
        setupSpinner()
        setupSaveButton()
    }

    private fun initializeViews() {
        // FIXED: Changed eIAmount to etAmount, eIDate to etDate, eIDescription to etDescription
        amountEditText = findViewById(R.id.etAmount)
        categorySpinner = findViewById(R.id.spinnerCategory)
        dateEditText = findViewById(R.id.etDate)
        descriptionEditText = findViewById(R.id.etDescription)
        saveButton = findViewById(R.id.btnSaveIncome)
    }

    private fun setupDatePicker() {
        // Set current date as default
        dateEditText.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

        dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    dateEditText.setText(selectedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

    private fun setupSpinner() {
        val categories = arrayOf("Salary", "Business", "Investment", "Gift", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            saveIncome()
        }
    }

    private fun saveIncome() {
        val amountText = amountEditText.text.toString()
        val category = categorySpinner.selectedItem.toString()
        val date = dateEditText.text.toString()
        val description = descriptionEditText.text.toString()

        if (amountText.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill amount and date", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull() ?: 0.0

        val income = hashMapOf(
            "type" to "income",
            "amount" to amount,
            "category" to category,
            "date" to date,
            "description" to description,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .collection("transactions")
            .add(income)
            .addOnSuccessListener {
                Toast.makeText(this, "Income added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}