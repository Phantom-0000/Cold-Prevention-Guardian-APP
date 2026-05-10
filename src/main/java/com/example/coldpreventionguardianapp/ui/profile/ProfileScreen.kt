package com.example.coldpreventionguardianapp.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.coldpreventionguardianapp.R
import com.example.coldpreventionguardianapp.data.model.User
import com.example.coldpreventionguardianapp.data.repository.SessionManager
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    user: User,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val ageGroupOptions = listOf("儿童", "青少年", "成年人", "老年人")
    val languageOptions = listOf("zh-CN", "en", "de", "fr", "es")

    var selectedAgeGroup by remember { mutableStateOf(user.ageGroup) }
    var selectedLanguage by remember { mutableStateOf(user.language) }
    var showAgeGroupDropdown by remember { mutableStateOf(false) }
    var showLanguageDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome section
        Text(
            text = stringResource(R.string.profile_welcome, user.username),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Age group section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.profile_age_group),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Current selection display with dropdown
                Box {
                    OutlinedButton(
                        onClick = { showAgeGroupDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = getAgeGroupDisplayName(selectedAgeGroup),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                        Text("▼")
                    }

                    DropdownMenu(
                        expanded = showAgeGroupDropdown,
                        onDismissRequest = { showAgeGroupDropdown = false }
                    ) {
                        ageGroupOptions.forEach { ageGroup ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        getAgeGroupDisplayName(ageGroup),
                                        fontWeight = if (ageGroup == selectedAgeGroup)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedAgeGroup = ageGroup
                                    showAgeGroupDropdown = false
                                    scope.launch {
                                        SessionManager.updateAgeGroup(ageGroup)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.profile_language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedButton(
                        onClick = { showLanguageDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = getLanguageDisplayName(selectedLanguage),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                        Text("▼")
                    }

                    DropdownMenu(
                        expanded = showLanguageDropdown,
                        onDismissRequest = { showLanguageDropdown = false }
                    ) {
                        languageOptions.forEach { langCode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        getLanguageDisplayName(langCode),
                                        fontWeight = if (langCode == selectedLanguage)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedLanguage = langCode
                                    showLanguageDropdown = false
                                    scope.launch {
                                        SessionManager.updateLanguage(langCode)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Logout button
        OutlinedButton(
            onClick = {
                SessionManager.logout()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(stringResource(R.string.profile_logout))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun getAgeGroupDisplayName(ageGroup: String): String {
    return when (ageGroup) {
        "儿童" -> stringResource(R.string.age_group_children)
        "青少年" -> stringResource(R.string.age_group_teenagers)
        "成年人" -> stringResource(R.string.age_group_adults)
        "老年人" -> stringResource(R.string.age_group_seniors)
        else -> ageGroup
    }
}

@Composable
private fun getLanguageDisplayName(langCode: String): String {
    return when (langCode) {
        "zh-CN" -> stringResource(R.string.language_chinese)
        "en" -> stringResource(R.string.language_english)
        "de" -> stringResource(R.string.language_german)
        "fr" -> stringResource(R.string.language_french)
        "es" -> stringResource(R.string.language_spanish)
        else -> langCode
    }
}
