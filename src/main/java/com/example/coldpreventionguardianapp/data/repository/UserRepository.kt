package com.example.coldpreventionguardianapp.data.repository

import com.example.coldpreventionguardianapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class UserRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun register(username: String, email: String, password: String): AuthResult {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return AuthResult.Error("注册失败：无法获取用户信息")

            val now = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

            val user = User(
                uid = firebaseUser.uid,
                username = username,
                email = email,
                language = "zh-CN",
                ageGroup = "成年人",
                registeredAt = now
            )

            database.getReference("users").child(firebaseUser.uid)
                .setValue(user.toMap()).await()

            AuthResult.Success(user)
        } catch (e: Exception) {
            val message = when (e) {
                is FirebaseAuthUserCollisionException -> "该邮箱已被注册"
                is FirebaseAuthWeakPasswordException -> "密码强度不足，请使用6位以上密码"
                else -> e.message ?: "注册失败，请稍后重试"
            }
            AuthResult.Error(message)
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return AuthResult.Error("登录失败：无法获取用户信息")

            // Fetch user data from database
            val snapshot = database.getReference("users")
                .child(firebaseUser.uid).get().await()
            val user = if (snapshot.exists()) {
                User.fromMap(firebaseUser.uid, snapshot.value as Map<String, Any?>)
            } else {
                User(
                    uid = firebaseUser.uid,
                    username = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: email
                )
            }

            AuthResult.Success(user)
        } catch (e: Exception) {
            val message = when {
                e.message?.contains("invalid credentials", ignoreCase = true) == true -> "邮箱或密码错误"
                e.message?.contains("user not found", ignoreCase = true) == true -> "用户不存在"
                e.message?.contains("network", ignoreCase = true) == true -> "网络连接失败"
                else -> e.message ?: "登录失败，请稍后重试"
            }
            AuthResult.Error(message)
        }
    }

    fun logout() {
        auth.signOut()
    }

    /**
     * Observe user data changes from Firebase Realtime Database.
     * Returns a Flow that emits User whenever the data changes.
     */
    fun observeUser(uid: String): Flow<User> = callbackFlow {
        val reference = database.getReference("users").child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val map = snapshot.value as Map<String, Any?>
                    val user = User.fromMap(uid, map)
                    trySend(user)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Silently ignore — channel will remain open
            }
        }
        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
        }
    }

    suspend fun updateLanguage(uid: String, language: String) {
        database.getReference("users").child(uid).child("language")
            .setValue(language).await()
    }

    suspend fun updateAgeGroup(uid: String, ageGroup: String) {
        database.getReference("users").child(uid).child("ageGroup")
            .setValue(ageGroup).await()
    }
}