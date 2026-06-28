package com.aistudio.meetingplatform.feature.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.meetingplatform.feature.auth.domain.repository.AuthRepository
import com.aistudio.meetingplatform.feature.chat.domain.model.ChatMessage
import com.aistudio.meetingplatform.feature.chat.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState = combine(
        chatRepository.messages,
        authRepository.currentUser
    ) { messages, user ->
        ChatUiState(
            messages = messages,
            currentUserId = user?.id ?: "local_user",
            currentUserName = user?.name ?: "Me"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatUiState())

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val user = authRepository.currentUser.value
            chatRepository.sendMessage(
                senderId = user?.id ?: "local_user",
                senderName = user?.name ?: "Me",
                text = text
            )
        }
    }

    fun addReaction(messageId: String, reaction: String) {
        viewModelScope.launch {
            chatRepository.addReaction(messageId, reaction)
        }
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentUserId: String = "",
    val currentUserName: String = ""
)
