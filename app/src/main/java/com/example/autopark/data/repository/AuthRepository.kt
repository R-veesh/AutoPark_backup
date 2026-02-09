package com.example.autopark.data.repository

import com.example.autopark.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val userDataResult = getUserData(firebaseUser.uid)
                userDataResult
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        email: String,
        password: String,
        name: String,
        phoneNumber: String,
        role: String
    ): Result<User> {
        return try {
            // Create user in Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            // Update user profile with name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Create user in Firestore
            val user = User(
                id = firebaseUser.uid,
                email = email,
                name = name,
                phoneNumber = phoneNumber,
                role = role
            )

            db.collection("users").document(firebaseUser.uid).set(user).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getUserData(userId: String): Result<User> {
        return try {
            val document = db.collection("users").document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAuthState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    suspend fun getCurrentUserData(): User? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            getUserData(userId).getOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val docs = db.collection("users").get().await()
            val users = docs.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)?.apply { id = doc.id }
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addUser(user: User): Result<Unit> {
        return try {
            db.collection("users").document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}