package com.example.budgetmanager

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var btnResetPassword: Button
    private lateinit var btnBackToLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private var retryCount = 0
    private val maxRetries = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = Firebase.auth
        initializeViews()
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        btnResetPassword = findViewById(R.id.btnResetPassword)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)
        progressBar = findViewById(R.id.progressBar)

        // Add a status text view to your layout or remove this if not needed
        // tvStatus = findViewById(R.id.tvStatus)

        btnResetPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (validateEmail(email)) {
                retryCount = 0
                sendPasswordResetEmail(email)
            }
        }

        btnBackToLogin.setOnClickListener {
            goBackToLogin()
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            showToast("Email is required")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address")
            return false
        }
        return true
    }

    private fun sendPasswordResetEmail(email: String) {
        showLoading(true)
        Log.d("PASSWORD_RESET", "Attempt #${retryCount + 1} for: $email")

        val timeoutHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (progressBar.visibility == View.VISIBLE) {
                showLoading(false)
                Log.e("PASSWORD_RESET", "⏰ Timeout occurred")
                handleTimeout(email)
            }
        }

        // Set timeout for 30 seconds
        timeoutHandler.postDelayed(timeoutRunnable, 30000)

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                timeoutHandler.removeCallbacks(timeoutRunnable)
                showLoading(false)

                if (task.isSuccessful) {
                    Log.d("PASSWORD_RESET", "✅ Email sent successfully!")
                    showSuccessMessage(email)
                } else {
                    val error = task.exception
                    Log.e("PASSWORD_RESET", "❌ Failed: ${error?.message}")

                    if (retryCount < maxRetries) {
                        retryCount++
                        Log.d("PASSWORD_RESET", "🔄 Retrying... ($retryCount/$maxRetries)")
                        showToast("Retrying... ($retryCount/$maxRetries)")
                        Handler(Looper.getMainLooper()).postDelayed({
                            sendPasswordResetEmail(email)
                        }, 2000)
                    } else {
                        handleResetError(error)
                    }
                }
            }
            .addOnFailureListener { e ->
                timeoutHandler.removeCallbacks(timeoutRunnable)
                showLoading(false)
                Log.e("PASSWORD_RESET", "💥 Failure: ${e.message}")
                showToast("Network error: ${e.message}")
            }
    }

    private fun handleTimeout(email: String) {
        if (retryCount < maxRetries) {
            retryCount++
            showToast("Timeout. Retrying... ($retryCount/$maxRetries)")
            sendPasswordResetEmail(email)
        } else {
            showToast("Request timeout. Please check your connection and try again.")
        }
    }

    private fun handleResetError(error: Exception?) {
        val errorMessage = error?.message ?: "Failed to send reset email"

        when {
            errorMessage.contains("user-not-found", ignoreCase = true) -> {
                showToast("No account found with this email address")
            }
            errorMessage.contains("invalid-email", ignoreCase = true) -> {
                showToast("Invalid email address format")
            }
            errorMessage.contains("network", ignoreCase = true) -> {
                showToast("Network error. Check your internet connection.")
            }
            errorMessage.contains("too-many-requests", ignoreCase = true) -> {
                showToast("Too many attempts. Please try again in 1 hour.")
            }
            errorMessage.contains("quota", ignoreCase = true) -> {
                showToast("Service temporarily unavailable. Please try again later.")
            }
            else -> {
                showToast("Error: $errorMessage")
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnResetPassword.isEnabled = !show
        btnResetPassword.text = if (show) "Sending..." else "Send Reset Link"
        btnBackToLogin.isEnabled = !show
    }

    private fun showSuccessMessage(email: String) {
        Toast.makeText(
            this,
            "✅ Password reset link sent to:\n$email\n\nCheck your inbox and spam folder.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun goBackToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}