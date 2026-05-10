package com.example.coldpreventionguardianapp.data.model

import androidx.annotation.StringRes

data class Medication(
    @StringRes val nameRes: Int,
    @StringRes val regionRes: Int,
    @StringRes val descRes: Int,
    @StringRes val typeRes: Int,     // R.string.med_type_preventive or R.string.med_type_treatment
    @StringRes val warningRes: Int? = null
)
