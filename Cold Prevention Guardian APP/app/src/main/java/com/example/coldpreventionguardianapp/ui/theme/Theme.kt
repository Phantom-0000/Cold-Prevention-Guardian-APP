package com.example.coldpreventionguardianapp.ui.theme

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import com.example.coldpreventionguardianapp.data.repository.SessionManager
import java.util.Locale

/**
 * Dark-only color scheme matching the web app's design spec.
 */
private val ColdGuardianColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

/**
 * Returns the Locale string ("zh-CN" or "en") derived from the user's language preference.
 * Falls back to system default if no user is logged in.
 */
@Composable
private fun rememberedLocaleTag(): String {
    val user by SessionManager.currentUser.collectAsState()
    return user?.language ?: Locale.getDefault().toLanguageTag()
}

/**
 * Returns the font scale factor: 1.2x for seniors, 1.0x for everyone else.
 */
@Composable
private fun rememberedFontScale(): Float {
    val user by SessionManager.currentUser.collectAsState()
    return if (user?.ageGroup == "老年人") 1.2f else 1.0f
}

/**
 * Applies a locale to the entire AppCompat context hierarchy.
 * Uses AppCompatDelegate.setApplicationLocales for native AndroidX locale switching.
 */
@Composable
private fun ApplyLocale(localeTag: String) {
    val context = LocalContext.current
    val localeList = LocaleListCompat.forLanguageTags(localeTag)

    SideEffect {
        if (AppCompatDelegate.getApplicationLocales() != localeList) {
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    // Also force the Configuration locale for immediate effect in the current activity
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val config = Configuration(activity.resources.configuration)
            val locale = Locale.forLanguageTag(localeTag.replace('-', '-'))
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
        }
    }
}

@Composable
fun ColdPreventionGuardianAPPTheme(
    content: @Composable () -> Unit
) {
    val fontScale = rememberedFontScale()
    val localeTag = rememberedLocaleTag()

    // Apply locale switching
    ApplyLocale(localeTag)

    // Create scaled typography based on user's age group
    val typography = remember(fontScale) {
        createScaledTypography(fontScale)
    }

    // Always use dark theme — matches the web app
    val colorScheme = ColdGuardianColorScheme

    // Set the status bar color and insets for edge-to-edge
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}