package com.example.logisticareparto.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()) {

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    suspend fun signIn(email: String, pass: String): Result<FirebaseUser> = try {
        val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
        Result.success(result.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signUp(email: String, pass: String): Result<FirebaseUser> = try {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
        Result.success(result.user!!)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
