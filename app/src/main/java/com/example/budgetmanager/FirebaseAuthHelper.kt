package com.example.budgetmanager.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

object FirebaseAuthHelper {
    private val auth = FirebaseAuth.getInstance()


    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }


    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }


    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }


    fun registerWithEmail(
        email: String,
        password: String,
        username: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update user profile with username
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                onSuccess()
                            } else {
                                onError(profileTask.exception?.message ?: "Profile update failed")
                            }
                        }
                } else {
                    onError(task.exception?.message ?: "Registration failed")
                }
            }
    }


    fun loginWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Login failed")
                }
            }
    }


    fun logout() {
        auth.signOut()
    }
}