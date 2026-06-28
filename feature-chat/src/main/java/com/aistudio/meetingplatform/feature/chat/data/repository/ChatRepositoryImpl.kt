package com.aistudio.meetingplatform.feature.chat.data.repository

import com.aistudio.meetingplatform.core.event.EventBus
import com.aistudio.meetingplatform.core.event.ChatEvent
import com.aistudio.meetingplatform.feature.chat.domain.model.ChatMessage
import com.aistudio.meetingplatform.feature.chat.domain.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val eventBus: EventBus
) : ChatRepository {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.Default)

    init {
        repositoryScope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is ChatEvent.MessageSent -> handleIncomingMessage(event)
                    else -> {}
                }
            }
        }
    }

    private fun handleIncomingMessage(event: ChatEvent.MessageSent) {
        // Add to list if it's not from local_user to avoid duplicates (since we add immediately on send)
        // For simplicity, we just add all received events if we were doing actual network.
        // But since emit() is local, if we emit and then add, it duplicates.
        // Let's only add if it's from others.
        if (event.senderId != "local_user") {
            val newMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = event.senderId,
                senderName = event.senderName,
                text = event.message,
                timestamp = System.currentTimeMillis()
            )
            _messages.value = _messages.value + newMessage
        }
    }

    override suspend fun sendMessage(senderId: String, senderName: String, text: String) {
        val newMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            senderName = senderName,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + newMessage
        eventBus.emit(ChatEvent.MessageSent(text, senderId, senderName))
    }

    override suspend fun addReaction(messageId: String, reaction: String) {
        _messages.value = _messages.value.map { msg ->
            if (msg.id == messageId) {
                val currentCount = msg.reactions[reaction] ?: 0
                msg.copy(reactions = msg.reactions + (reaction to currentCount + 1))
            } else {
                msg
            }
        }
    }

    override suspend fun clearChat() {
        _messages.value = emptyList()
    }
}
