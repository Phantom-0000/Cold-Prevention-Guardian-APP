package com.example.coldpreventionguardianapp.data.repository

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.coldpreventionguardianapp.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object SessionManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val repository = UserRepository()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // Restore session if Firebase Auth already has a logged-in user
        val uid = repository.getCurrentUserId()
        if (uid != null) {
            scope.launch {
                repository.observeUser(uid).collect { user ->
                    _currentUser.value = user
                }
            }
        }
    }

    suspend fun register(username: String, email: String, password: String): AuthResult {
        val result = repository.register(username, email, password)
        if (result is AuthResult.Success) {
            // Start observing this user's data
            observeUserData(result.user.uid)
        }
        return result
    }

    suspend fun login(email: String, password: String): AuthResult {
        val result = repository.login(email, password)
        if (result is AuthResult.Success) {
            // Start observing this user's data
            observeUserData(result.user.uid)
        }
        return result
    }

    fun logout() {
        _currentUser.value = null
        repository.logout()
    }

    suspend fun updateLanguage(language: String) {
        val uid = _currentUser.value?.uid ?: return
        repository.updateLanguage(uid, language)

        // Apply locale immediately so all stringResource() calls refresh instantly
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language)
        )
    }

    suspend fun updateAgeGroup(ageGroup: String) {
        val uid = _currentUser.value?.uid ?: return
        repository.updateAgeGroup(uid, ageGroup)
    }

    private fun observeUserData(uid: String) {
        scope.launch {
            repository.observeUser(uid).collect { user ->
                _currentUser.value = user
            }
        }
    }
}