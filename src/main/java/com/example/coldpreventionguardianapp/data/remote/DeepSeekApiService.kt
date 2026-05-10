package com.example.coldpreventionguardianapp.data.remote

import com.example.coldpreventionguardianapp.data.remote.dto.ChatRequest
import com.example.coldpreventionguardianapp.data.remote.dto.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface DeepSeekApiService {
    @POST("chat/completions")
    suspend fun chatCompletions(@Body request: ChatRequest): ChatResponse
}