package com.example.coldpreventionguardianapp.data.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

data class HealthAssessment(
    @StringRes val titleRes: Int,
    @StringRes val analysisRes: Int,
    @StringRes val causesRes: Int,
    @StringRes val complicationsRes: Int,
    @StringRes val adviceRes: Int,
    val themeColor: Color
)
