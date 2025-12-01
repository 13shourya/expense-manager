package com.example.budgetmanager

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var btnLogout: Button
    private lateinit var btnSwitchAccount: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Check if user is logged in
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize the new buttons
        btnLogout = findViewById(R.id.btnLogout)
        btnSwitchAccount = findViewById(R.id.btnSwitchAccount)

        // Load user data
        loadUserData()
        setupClickListeners()
        setupLogoutButtons()

        Log.d("MAIN", "MainActivity created - loading initial data")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MAIN", "MainActivity resumed - refreshing data")
        loadUserData()
    }

    private fun setupLogoutButtons() {
        // Logout Button - Completely signs out
        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Switch Account Button - Goes to login screen but keeps Firebase session option
        btnSwitchAccount.setOnClickListener {
            showSwitchAccountDialog()
        }
    }

    // ADD THIS METHOD - Logout Confirmation
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes, Logout") { dialog, which ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ADD THIS METHOD - Switch Account Dialog
    private fun showSwitchAccountDialog() {
        val options = arrayOf("Login with different account", "Cancel")

        AlertDialog.Builder(this)
            .setTitle("Switch Account")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> switchToDifferentAccount()
                    // 1 is Cancel - do nothing
                }
            }
            .show()
    }

    // ADD THIS METHOD - Perform Logout
    private fun performLogout() {
        // Show loading
        val progressDialog = ProgressBar(this)
        progressDialog.visibility = ProgressBar.VISIBLE

        // Sign out from Firebase
        auth.signOut()

        // Delay a bit for smooth transition
        Handler(Looper.getMainLooper()).postDelayed({
            progressDialog.visibility = ProgressBar.GONE
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Go to login screen
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }, 1000)
    }

    // ADD THIS METHOD - Switch to different account
    private fun switchToDifferentAccount() {
        Toast.makeText(this, "Please login with your new account", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupClickListeners() {
        // Add Income button
        findViewById<android.view.View>(R.id.cardIncome).setOnClickListener {
            Toast.makeText(this, "Opening Add Income", Toast.LENGTH_SHORT).show()
            try {
                val intent = Intent(this, AddIncomeActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening Add Income", Toast.LENGTH_LONG).show()
                Log.e("MAIN", "Error opening AddIncomeActivity", e)
            }
        }

        // Add Expense button
        findViewById<android.view.View>(R.id.cardExpense).setOnClickListener {
            Toast.makeText(this, "Opening Add Expense", Toast.LENGTH_SHORT).show()
            try {
                val intent = Intent(this, AddExpenseActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening Add Expense", Toast.LENGTH_LONG).show()
                Log.e("MAIN", "Error opening AddExpenseActivity", e)
            }
        }

        // View All transactions
        findViewById<TextView>(R.id.tvViewAll).setOnClickListener {
            Toast.makeText(this, "Opening Transaction History", Toast.LENGTH_SHORT).show()
            try {
                val intent = Intent(this, TransactionHistoryActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Error opening Transaction History", Toast.LENGTH_LONG).show()
                Log.e("MAIN", "Error opening TransactionHistoryActivity", e)
            }
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            Log.d("MAIN", "Loading data for user: $userId")
            fetchTransactions(userId)
            updateUserName()
        } else {
            Log.e("MAIN", "User ID is null - user not authenticated")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun fetchTransactions(userId: String) {
        db.collection("users").document(userId).collection("transactions")
            .get()
            .addOnSuccessListener { documents ->
                var totalBalance = 0.0
                var totalIncome = 0.0
                var totalExpenses = 0.0

                Log.d("MAIN", "Successfully fetched ${documents.size()} transactions")

                if (documents.isEmpty) {
                    Log.d("MAIN", "No transactions found for user")
                    updateUI(totalBalance, totalIncome, totalExpenses, 0)
                    return@addOnSuccessListener
                }

                for (document in documents) {
                    try {
                        val amount = document.getDouble("amount") ?: 0.0
                        val type = document.getString("type") ?: ""

                        Log.d("MAIN", "Processing transaction: $type - $${amount}")

                        when (type.lowercase()) {
                            "income" -> {
                                totalBalance += amount
                                totalIncome += amount
                            }
                            "expense" -> {
                                totalBalance -= amount
                                totalExpenses += amount
                            }
                            else -> Log.w("MAIN", "Unknown transaction type: $type")
                        }
                    } catch (e: Exception) {
                        Log.e("MAIN", "Error parsing document: ${document.id}", e)
                    }
                }

                updateUI(totalBalance, totalIncome, totalExpenses, documents.size())
            }
            .addOnFailureListener { e ->
                Log.e("MAIN", "Error fetching transactions: ", e)
                Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
                updateUI(0.0, 0.0, 0.0, 0)
            }
    }

    private fun updateUI(totalBalance: Double, totalIncome: Double, totalExpenses: Double, transactionCount: Int) {
        runOnUiThread {
            try {
                val balanceTextView: TextView = findViewById(R.id.tvBalance)
                val incomeTextView: TextView = findViewById(R.id.tvTotalIncome)
                val expenseTextView: TextView = findViewById(R.id.tvTotalExpenses)

                // Format currency properly
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

                balanceTextView.text = currencyFormat.format(totalBalance)
                incomeTextView.text = currencyFormat.format(totalIncome)
                expenseTextView.text = currencyFormat.format(totalExpenses)

                Log.d("MAIN", "UI Updated - Balance: $${totalBalance}, Income: $${totalIncome}, Expense: $${totalExpenses}, Transactions: $transactionCount")
            } catch (e: Exception) {
                Log.e("MAIN", "Error updating UI", e)
            }
        }
    }

    private fun updateUserName() {
        val userNameTextView: TextView = findViewById(R.id.tvUserName)
        val currentUser = auth.currentUser

        currentUser?.let { user ->
            val displayName = user.displayName
            if (!displayName.isNullOrEmpty()) {
                userNameTextView.text = "Hello, $displayName"
            } else {
                user.email?.let { email ->
                    val nameFromEmail = email.substringBefore("@")
                    userNameTextView.text = "Hello, $nameFromEmail"
                } ?: run {
                    userNameTextView.text = "Hello, User"
                }
            }
        } ?: run {
            userNameTextView.text = "Hello, User"
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}