package com.example.commute_companion_app

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseTokenProvider {

    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun getIdToken(): String? {
        val currentUser = firebaseAuth.currentUser
            ?: return null

        return try {
            currentUser.getIdToken(false).await()?.token
        } catch (exception: Exception) {
            null
        }
    }

    fun getFirebaseUid(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun getEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    fun getDisplayName(): String? {
        return firebaseAuth.currentUser?.displayName
    }
}