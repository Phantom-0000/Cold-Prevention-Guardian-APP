package com.example.coldpreventionguardianapp.data.model

data class TemperatureRecord(
    val date: String = "",        // yyyy-MM-dd
    val temperature: Double = 0.0,
    val timestamp: Long = 0L      // System.currentTimeMillis() for sorting
)