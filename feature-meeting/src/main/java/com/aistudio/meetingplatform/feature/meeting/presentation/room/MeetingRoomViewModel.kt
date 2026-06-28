package com.aistudio.meetingplatform.feature.meeting.presentation.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.meetingplatform.feature.media.domain.MediaEngine
import com.aistudio.meetingplatform.feature.media.domain.MediaState
import com.aistudio.meetingplatform.feature.meeting.domain.model.MeetingRoom
import com.aistudio.meetingplatform.feature.meeting.domain.model.Participant
import com.aistudio.meetingplatform.feature.meeting.domain.repository.MeetingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingRoomViewModel @Inject constructor(
    private val meetingRepository: MeetingRepository,
    private val mediaEngine: MediaEngine
) : ViewModel() {

    private val _isChatVisible = MutableStateFlow(false)
    private val _isAIAssistantVisible = MutableStateFlow(false)

    val uiState = combine(
        meetingRepository.currentRoom,
        meetingRepository.participants,
        mediaEngine.mediaState,
        combine(_isChatVisible, _isAIAssistantVisible) { chat, ai -> chat to ai }
    ) { room, participants, mediaState, (isChatVisible, isAIAssistantVisible) ->
        MeetingRoomUiState(
            room = room,
            participants = participants,
            mediaState = mediaState,
            isChatVisible = isChatVisible,
            isAIAssistantVisible = isAIAssistantVisible
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MeetingRoomUiState())

    fun leaveRoom(onLeft: () -> Unit) {
        viewModelScope.launch {
            mediaEngine.disconnect()
            meetingRepository.leaveRoom()
            onLeft()
        }
    }
    
    fun toggleMic() {
        viewModelScope.launch {
            mediaEngine.toggleAudio("local_user")
        }
    }
    
    fun toggleCamera() {
        viewModelScope.launch {
            mediaEngine.toggleVideo("local_user")
        }
    }
    
    fun toggleScreenShare() {
        viewModelScope.launch {
            mediaEngine.toggleScreenShare("local_user")
        }
    }
    
    fun toggleChat() {
        _isChatVisible.value = !_isChatVisible.value
    }
    
    fun toggleAIAssistant() {
        _isAIAssistantVisible.value = !_isAIAssistantVisible.value
    }
}

data class MeetingRoomUiState(
    val room: MeetingRoom? = null,
    val participants: List<Participant> = emptyList(),
    val mediaState: MediaState = MediaState(),
    val isChatVisible: Boolean = false,
    val isAIAssistantVisible: Boolean = false
)
