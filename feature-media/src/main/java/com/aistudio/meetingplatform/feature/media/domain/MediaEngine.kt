package com.aistudio.meetingplatform.feature.media.domain

import kotlinx.coroutines.flow.StateFlow

data class MediaState(
    val isMicEnabled: Boolean = false,
    val isCameraEnabled: Boolean = false,
    val isScreenSharing: Boolean = false
)

interface MediaEngine {
    val mediaState: StateFlow<MediaState>
    
    suspend fun toggleAudio(participantId: String)
    suspend fun toggleVideo(participantId: String)
    suspend fun toggleScreenShare(participantId: String)
    suspend fun disconnect()
}
