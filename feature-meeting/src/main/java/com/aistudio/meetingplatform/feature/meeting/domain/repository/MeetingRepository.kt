package com.aistudio.meetingplatform.feature.meeting.domain.repository

import com.aistudio.meetingplatform.feature.meeting.domain.model.MeetingRoom
import com.aistudio.meetingplatform.feature.meeting.domain.model.Participant
import kotlinx.coroutines.flow.StateFlow

interface MeetingRepository {
    val publicRooms: StateFlow<List<MeetingRoom>>
    val currentRoom: StateFlow<MeetingRoom?>
    val participants: StateFlow<List<Participant>>
    
    suspend fun createRoom(title: String, topic: String, hostName: String): MeetingRoom
    suspend fun joinRoom(roomId: String, participantName: String, isHost: Boolean)
    suspend fun leaveRoom()
}
