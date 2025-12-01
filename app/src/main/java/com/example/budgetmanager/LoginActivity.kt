package com.example.budgetmanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class   LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth



        // Initialize views safely
        initializeViews()
    }
    // Add this method to LoginActivity
    private fun showCurrentUserInfo() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val email = user.email
            // You can show a message or update UI to indicate who's logged in
            Log.d("LOGIN", "Current user: $email")
            // Optional: Show a small text view with "Currently logged in as: user@email.com"
        }
    }
    private fun initializeViews() {
        try {
            // Find all required views
            val emailEditText = findViewById<EditText>(R.id.etEmail)
            val passwordEditText = findViewById<EditText>(R.id.etPassword)
            val loginButton = findViewById<Button>(R.id.btnLogin)
            val progressBar = findViewById<ProgressBar>(R.id.progressBar)
            val forgotPasswordText = findViewById<TextView>(R.id.tvForgotPassword)
            val signUpText = findViewById<TextView>(R.id.tvSignUp)

            // Setup login button
            loginButton.setOnClickListener {
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString()

                if (validateInputs(email, password)) {
                    attemptLogin(email, password, loginButton, progressBar)
                }
            }

            // Setup forgot password - FIXED: Always allow navigation
            forgotPasswordText.setOnClickListener {
                val intent = Intent(this, ForgotPasswordActivity::class.java)
                startActivity(intent)
            }

            // Setup sign up - FIXED: Always allow navigation
            signUpText.setOnClickListener {
                val intent = Intent(this, SignupActivity::class.java)
                startActivity(intent)
            }

        } catch (e: Exception) {
            // If any view is missing, show error and close app
            Toast.makeText(this, "Layout error: Missing required views", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun attemptLogin(email: String, password: String, loginButton: Button, progressBar: ProgressBar) {
        showLoading(true, loginButton, progressBar)

        Log.d("LOGIN", "Attempting login for: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false, loginButton, progressBar)

                if (task.isSuccessful) {
                    Log.d("LOGIN", "Login successful")
                    handleLoginSuccess()
                } else {
                    val error = task.exception
                    Log.e("LOGIN", "Login failed: ${error?.message}")
                    handleLoginFailure(error)
                }
            }
            .addOnFailureListener { e ->
                showLoading(false, loginButton, progressBar)
                Log.e("LOGIN", "Login onFailure: ${e.message}")
                handleLoginFailure(e)
            }
    }

    private fun handleLoginSuccess() {
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
        navigateToMainActivity()
    }

    private fun handleLoginFailure(error: Exception?) {
        when {
            error is FirebaseNetworkException -> {
                Toast.makeText(this, "Network error. Check your internet connection.", Toast.LENGTH_LONG).show()
            }
            error?.message?.contains("network", ignoreCase = true) == true -> {
                Toast.makeText(this, "Network error. Please check your connection.", Toast.LENGTH_LONG).show()
            }
            error?.message?.contains("timeout", ignoreCase = true) == true -> {
                Toast.makeText(this, "Connection timeout. Try again.", Toast.LENGTH_LONG).show()
            }
            else -> {
                val errorMessage = error?.message ?: "Login failed"
                Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLoading(show: Boolean, loginButton: Button, progressBar: ProgressBar) {
        if (show) {
            loginButton.isEnabled = false
            loginButton.text = "Logging in..."
            progressBar.visibility = View.VISIBLE
        } else {
            loginButton.isEnabled = true
            loginButton.text = "Login"
            progressBar.visibility = View.GONE
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Handle back button press
    override fun onBackPressed() {
        // Exit app when back pressed from login
        finishAffinity()
    }
}