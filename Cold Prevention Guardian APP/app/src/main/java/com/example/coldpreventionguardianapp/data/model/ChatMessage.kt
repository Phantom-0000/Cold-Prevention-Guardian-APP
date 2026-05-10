package com.example.coldpreventionguardianapp.data.model

data class ChatMessage(
    val role: String,  // "system", "user", "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)