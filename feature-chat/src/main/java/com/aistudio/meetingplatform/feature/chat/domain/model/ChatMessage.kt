package com.aistudio.meetingplatform.feature.chat.domain.model

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val reactions: Map<String, Int> = emptyMap()
)
