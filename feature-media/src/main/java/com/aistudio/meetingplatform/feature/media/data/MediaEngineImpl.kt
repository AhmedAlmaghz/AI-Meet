package com.aistudio.meetingplatform.feature.media.data

import com.aistudio.meetingplatform.core.event.EventBus
import com.aistudio.meetingplatform.core.event.MediaEvent
import com.aistudio.meetingplatform.feature.media.domain.MediaEngine
import com.aistudio.meetingplatform.feature.media.domain.MediaState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaEngineImpl @Inject constructor(
    private val eventBus: EventBus
) : MediaEngine {

    private val _mediaState = MutableStateFlow(MediaState())
    override val mediaState: StateFlow<MediaState> = _mediaState.asStateFlow()

    override suspend fun toggleAudio(participantId: String) {
        val currentState = _mediaState.value.isMicEnabled
        val newState = !currentState
        _mediaState.value = _mediaState.value.copy(isMicEnabled = newState)
        
        if (newState) {
            eventBus.emit(MediaEvent.AudioStreamStarted(participantId))
        } else {
            eventBus.emit(MediaEvent.AudioStreamStopped(participantId))
        }
    }

    override suspend fun toggleVideo(participantId: String) {
        val currentState = _mediaState.value.isCameraEnabled
        val newState = !currentState
        _mediaState.value = _mediaState.value.copy(isCameraEnabled = newState)
        
        if (newState) {
            eventBus.emit(MediaEvent.VideoStreamStarted(participantId))
        } else {
            eventBus.emit(MediaEvent.VideoStreamStopped(participantId))
        }
    }

    override suspend fun toggleScreenShare(participantId: String) {
        val currentState = _mediaState.value.isScreenSharing
        val newState = !currentState
        _mediaState.value = _mediaState.value.copy(isScreenSharing = newState)
        
        if (newState) {
            eventBus.emit(MediaEvent.ScreenShareStarted(participantId))
        } else {
            eventBus.emit(MediaEvent.ScreenShareStopped(participantId))
        }
    }

    override suspend fun disconnect() {
        _mediaState.value = MediaState()
    }
}
