package com.example.coldpreventionguardianapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coldpreventionguardianapp.data.model.ChatMessage
import com.example.coldpreventionguardianapp.data.remote.NetworkModule
import com.example.coldpreventionguardianapp.data.remote.dto.ChatMessageDto
import com.example.coldpreventionguardianapp.data.remote.dto.ChatRequest
import com.example.coldpreventionguardianapp.data.repository.SessionManager
import com.example.coldpreventionguardianapp.data.repository.TemperatureRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val apiService = NetworkModule.apiService
    private val temperatureRepository = TemperatureRepository()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    companion object {
        private const val MAX_CONTEXT_SIZE = 10
    }

    /**
     * Send a user message.
     * 1. Append user message to the list
     * 2. Build context (latest temperature + sliding window of last N messages)
     * 3. Call DeepSeek API
     * 4. Append AI response to the list
     */
    fun sendMessage(userContent: String) {
        val userMessage = ChatMessage(role = "user", content = userContent)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Fetch latest temperature for context injection
                val uid = SessionManager.currentUser.value?.uid ?: ""
                val latestTemp = getLatestTemperature(uid)

                // Build the messages list for API
                val apiMessages = buildApiMessages(latestTemp)

                val request = ChatRequest(
                    model = "deepseek-chat",
                    messages = apiMessages,
                    temperature = 0.7,
                    maxTokens = 2000
                )

                val response = apiService.chatCompletions(request)

                val replyContent = response.choices
                    ?.firstOrNull()
                    ?.message
                    ?.content
                    ?: "抱歉，我暂时无法回复，请稍后再试。"

                val assistantMessage = ChatMessage(role = "assistant", content = replyContent)
                _messages.value = _messages.value + assistantMessage
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    role = "assistant",
                    content = "网络请求失败: ${e.localizedMessage ?: "未知错误"}"
                )
                _messages.value = _messages.value + errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    // ---- Private Helpers ----

    /**
     * Read latest temperature from Firebase for the given user.
     * Returns "暂无记录" if no records exist.
     */
    private suspend fun getLatestTemperature(uid: String): String {
        if (uid.isEmpty()) return "暂无记录"
        return try {
            val records = temperatureRepository.getLatestRecord(uid)
            if (records.isNotEmpty()) {
                "${records.first().temperature}°C"
            } else {
                "暂无记录"
            }
        } catch (e: Exception) {
            "暂无记录"
        }
    }

    /**
     * Build the list of ChatMessageDto for the API request.
     * - First: system prompt with temperature context
     * - Then: last MAX_CONTEXT_SIZE non-system messages (user + assistant)
     */
    private fun buildApiMessages(latestTemp: String): List<ChatMessageDto> {
        val systemPrompt = "你是一个资深的健康管理专家。当前用户的最新体温是：$latestTemp。" +
                "请结合对话历史和该体温数据，提供详尽、专业的建议。"

        val systemMessage = ChatMessageDto(role = "system", content = systemPrompt)

        // Sliding window: take last MAX_CONTEXT_SIZE messages, exclude system
        val recentMessages = _messages.value
            .filter { it.role != "system" }
            .takeLast(MAX_CONTEXT_SIZE)
            .map { ChatMessageDto(role = it.role, content = it.content) }

        return listOf(systemMessage) + recentMessages
    }
}