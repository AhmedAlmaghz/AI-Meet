package com.aistudio.meetingplatform.feature.meeting.data.repository

import com.aistudio.meetingplatform.core.event.EventBus
import com.aistudio.meetingplatform.core.event.MediaEvent
import com.aistudio.meetingplatform.feature.meeting.domain.model.MeetingRoom
import com.aistudio.meetingplatform.feature.meeting.domain.model.Participant
import com.aistudio.meetingplatform.feature.meeting.domain.repository.MeetingRepository
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
class MeetingRepositoryImpl @Inject constructor(
    private val eventBus: EventBus
) : MeetingRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.Default)

    init {
        repositoryScope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is MediaEvent.AudioStreamStarted -> updateParticipantMic(event.participantId, true)
                    is MediaEvent.AudioStreamStopped -> updateParticipantMic(event.participantId, false)
                    is MediaEvent.VideoStreamStarted -> updateParticipantCamera(event.participantId, true)
                    is MediaEvent.VideoStreamStopped -> updateParticipantCamera(event.participantId, false)
                    else -> {}
                }
            }
        }
    }

    private fun updateParticipantMic(participantId: String, isMicOn: Boolean) {
        _participants.value = _participants.value.map {
            if (it.id == participantId) it.copy(isMicOn = isMicOn) else it
        }
    }

    private fun updateParticipantCamera(participantId: String, isCameraOn: Boolean) {
        _participants.value = _participants.value.map {
            if (it.id == participantId) it.copy(isCameraOn = isCameraOn) else it
        }
    }

    private val _publicRooms = MutableStateFlow<List<MeetingRoom>>(listOf(
        MeetingRoom("room_1", "Weekly Standup", "Engineering Sync", "Alice", 4),
        MeetingRoom("room_2", "Design Review", "UI/UX Discussion", "Bob", 2)
    ))
    override val publicRooms: StateFlow<List<MeetingRoom>> = _publicRooms.asStateFlow()

    private val _currentRoom = MutableStateFlow<MeetingRoom?>(null)
    override val currentRoom: StateFlow<MeetingRoom?> = _currentRoom.asStateFlow()

    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    override val participants: StateFlow<List<Participant>> = _participants.asStateFlow()

    override suspend fun createRoom(title: String, topic: String, hostName: String): MeetingRoom {
        val newRoom = MeetingRoom(
            id = UUID.randomUUID().toString(),
            title = title,
            topic = topic,
            hostName = hostName,
            participantCount = 1
        )
        _publicRooms.value = _publicRooms.value + newRoom
        return newRoom
    }

    override suspend fun joinRoom(roomId: String, participantName: String, isHost: Boolean) {
        val room = _publicRooms.value.find { it.id == roomId }
        if (room != null) {
            _currentRoom.value = room
            _participants.value = listOf(
                Participant(
                    id = "local_user",
                    name = participantName,
                    isHost = isHost,
                    isMicOn = true,
                    isCameraOn = true
                ),
                Participant(
                    id = "ai_agent",
                    name = "🤖 الأستاذ ذكاء (AI)",
                    isHost = false,
                    isMicOn = true,
                    isCameraOn = true,
                    isSpeaking = false
                ),
                Participant(
                    id = "p_alex",
                    name = "Alex Johnson",
                    isHost = false,
                    isMicOn = true,
                    isCameraOn = true,
                    isSpeaking = true
                ),
                Participant(
                    id = "p_satoshi",
                    name = "Satoshi Tanaka",
                    isHost = false,
                    isMicOn = true,
                    isCameraOn = true,
                    isSpeaking = false
                ),
                Participant(
                    id = "p_carlos",
                    name = "Carlos Rodriguez",
                    isHost = false,
                    isMicOn = false,
                    isCameraOn = false,
                    isSpeaking = false
                )
            )
        }
    }

    override suspend fun leaveRoom() {
        _currentRoom.value = null
        _participants.value = emptyList()
    }
}
