package com.example.autopark.auth

import com.google.firebase.auth.FirebaseAuth

object AuthManager {

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    fun currentUser() = auth.currentUser

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun logout() {
        auth.signOut()
    }
}
