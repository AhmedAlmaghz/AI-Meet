package com.aistudio.meetingplatform.feature.chat.domain.repository

import com.aistudio.meetingplatform.feature.chat.domain.model.ChatMessage
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    val messages: StateFlow<List<ChatMessage>>
    
    suspend fun sendMessage(senderId: String, senderName: String, text: String)
    suspend fun addReaction(messageId: String, reaction: String)
    suspend fun clearChat()
}
