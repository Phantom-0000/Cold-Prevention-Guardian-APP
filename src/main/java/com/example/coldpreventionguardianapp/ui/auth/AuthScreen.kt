package com.example.coldpreventionguardianapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.coldpreventionguardianapp.R
import com.example.coldpreventionguardianapp.data.repository.AuthResult
import com.example.coldpreventionguardianapp.data.repository.SessionManager
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.auth_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isLoginMode) stringResource(R.string.auth_login) else stringResource(R.string.auth_register),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Username field — only visible in register mode
        if (!isLoginMode) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.auth_username)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.auth_email)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.auth_password)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (!isLoginMode) ImeAction.Next else ImeAction.Done
            )
        )

        // Confirm password — only in register mode
        if (!isLoginMode) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(R.string.auth_confirm_password)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )
        }

        // Error message
        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Primary action button
        Button(
            onClick = {
                errorMessage = null

                // Validation
                when {
                    email.isBlank() -> {
                        errorMessage = context.getString(R.string.auth_error_empty_email)
                        return@Button
                    }
                    password.isBlank() -> {
                        errorMessage = context.getString(R.string.auth_error_empty_password)
                        return@Button
                    }
                    !isLoginMode && password != confirmPassword -> {
                        errorMessage = context.getString(R.string.auth_error_password_mismatch)
                        return@Button
                    }
                    !isLoginMode && password.length < 6 -> {
                        errorMessage = context.getString(R.string.auth_error_password_short)
                        return@Button
                    }
                    !isLoginMode && username.isBlank() -> {
                        errorMessage = context.getString(R.string.auth_error_empty_username)
                        return@Button
                    }
                }

                isLoading = true
                scope.launch {
                    val result = if (isLoginMode) {
                        SessionManager.login(email, password)
                    } else {
                        SessionManager.register(username, email, password)
                    }

                    isLoading = false
                    when (result) {
                        is AuthResult.Success -> {
                            onLoginSuccess()
                        }
                        is AuthResult.Error -> {
                            errorMessage = result.message
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (isLoginMode) stringResource(R.string.auth_login) else stringResource(R.string.auth_register))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Toggle login/register mode
        TextButton(
            onClick = {
                isLoginMode = !isLoginMode
                errorMessage = null
                username = ""
                confirmPassword = ""
            }
        ) {
            Text(
                if (isLoginMode) stringResource(R.string.auth_no_account) else stringResource(R.string.auth_has_account)
            )
        }
    }
}