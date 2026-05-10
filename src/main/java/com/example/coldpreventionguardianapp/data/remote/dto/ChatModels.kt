package com.example.coldpreventionguardianapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// ---- Request ----

data class ChatRequest(
    @SerializedName("model")
    val model: String = "deepseek-chat",
    @SerializedName("messages")
    val messages: List<ChatMessageDto>,
    @SerializedName("temperature")
    val temperature: Double = 0.7,
    @SerializedName("max_tokens")
    val maxTokens: Int = 2000
)

data class ChatMessageDto(
    @SerializedName("role")
    val role: String, // "system", "user", "assistant"
    @SerializedName("content")
    val content: String
)

// ---- Response ----

data class ChatResponse(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("choices")
    val choices: List<Choice>? = null
)

data class Choice(
    @SerializedName("index")
    val index: Int? = null,
    @SerializedName("message")
    val message: ChatMessageDto? = null,
    @SerializedName("finish_reason")
    val finishReason: String? = null
)