package com.aistudio.meetingplatform.feature.meeting.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.meetingplatform.feature.auth.domain.repository.AuthRepository
import com.aistudio.meetingplatform.feature.meeting.domain.model.MeetingRoom
import com.aistudio.meetingplatform.feature.meeting.domain.repository.MeetingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val meetingRepository: MeetingRepository
) : ViewModel() {

    val uiState = combine(
        authRepository.currentUser,
        meetingRepository.publicRooms
    ) { user, rooms ->
        HomeUiState(
            userName = user?.name ?: "",
            publicRooms = rooms
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun createAndJoinRoom(title: String, topic: String, onJoined: (String) -> Unit) {
        viewModelScope.launch {
            val user = authRepository.currentUser.value ?: return@launch
            val room = meetingRepository.createRoom(title, topic, user.name)
            meetingRepository.joinRoom(room.id, user.name, true)
            onJoined(room.id)
        }
    }

    fun joinRoom(roomId: String, onJoined: (String) -> Unit) {
        viewModelScope.launch {
            val user = authRepository.currentUser.value ?: return@launch
            meetingRepository.joinRoom(roomId, user.name, false)
            onJoined(roomId)
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}

data class HomeUiState(
    val userName: String = "",
    val publicRooms: List<MeetingRoom> = emptyList()
)
