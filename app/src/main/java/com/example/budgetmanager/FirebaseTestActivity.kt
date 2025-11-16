package com.example.budgetmanager

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetmanager.databinding.ActivityFirebaseTestBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFirebaseTestBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirebaseTestBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnTestAuth.setOnClickListener {
            testFirebaseAuth()
        }

        binding.btnTestFirestore.setOnClickListener {
            testFirestore()
        }

        binding.btnCheckConfig.setOnClickListener {
            checkFirebaseConfig()
        }


        checkFirebaseConfig()
    }

    private fun checkFirebaseConfig() {
        val status = StringBuilder()


        val apps = FirebaseApp.getApps(this)
        status.append("Firebase Apps: ${apps.size}\n")


        val defaultApp = FirebaseApp.getInstance()
        status.append("Project ID: ${defaultApp.options.projectId}\n")
        status.append("Application ID: ${defaultApp.options.applicationId}\n")
        status.append("API Key: ${defaultApp.options.apiKey?.take(10)}...\n")

        binding.tvStatus.text = status.toString()
        Log.d("FIREBASE_DEBUG", "Config: $status")
    }

    private fun testFirebaseAuth() {
        val testEmail = "test${System.currentTimeMillis()}@test.com"
        val testPassword = "123456"

        binding.tvStatus.text = "Creating user: $testEmail"

        auth.createUserWithEmailAndPassword(testEmail, testPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val message = """
                   
                    User: ${user?.email}
                    UID: ${user?.uid}
                    Verified: ${user?.isEmailVerified}
                    """.trimIndent()
                    binding.tvStatus.text = message
                    Log.d("FIREBASE_DEBUG", "Auth Success: $message")
                } else {
                    val error = task.exception?.message ?: "Unknown error"
                    val message = " AUTH FAILED:\n$error"
                    binding.tvStatus.text = message
                    Log.e("FIREBASE_DEBUG", "Auth Failed: $error")
                }
            }
    }

    private fun testFirestore() {
        val testData = hashMapOf(
            "message" to "Test from Android App",
            "timestamp" to System.currentTimeMillis(),
            "test" to true,
            "app" to "BudgetManager"
        )

        binding.tvStatus.text = "Writing to Firestore..."

        db.collection("test_data")
            .document("test_doc_${System.currentTimeMillis()}")
            .set(testData)
            .addOnSuccessListener {
                val message = " FIRESTORE SUCCESS!\nData written successfully"
                binding.tvStatus.text = message
                Log.d("FIREBASE_DEBUG", "Firestore Success: $message")
            }
            .addOnFailureListener { e ->
                val message = " FIRESTORE FAILED:\n${e.message}"
                binding.tvStatus.text = message
                Log.e("FIREBASE_DEBUG", "Firestore Failed: ${e.message}")
            }
    }
}