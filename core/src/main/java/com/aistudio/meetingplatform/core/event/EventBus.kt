package com.aistudio.meetingplatform.core.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventBus @Inject constructor() {
    private val _events = MutableSharedFlow<AppEvent>()
    val events = _events.asSharedFlow()

    suspend fun emit(event: AppEvent) {
        _events.emit(event)
    }
}

interface AppEvent

sealed class MediaEvent : AppEvent {
    data class AudioStreamStarted(val participantId: String) : MediaEvent()
    data class AudioStreamStopped(val participantId: String) : MediaEvent()
    data class VideoStreamStarted(val participantId: String) : MediaEvent()
    data class VideoStreamStopped(val participantId: String) : MediaEvent()
    data class ScreenShareStarted(val participantId: String) : MediaEvent()
    data class ScreenShareStopped(val participantId: String) : MediaEvent()
}

sealed class MeetingEvent : AppEvent {
    data class UserJoined(val participantId: String, val name: String) : MeetingEvent()
    data class UserLeft(val participantId: String) : MeetingEvent()
}

sealed class ChatEvent : AppEvent {
    data class MessageSent(val message: String, val senderId: String, val senderName: String) : ChatEvent()
}

sealed class TranslationEvent : AppEvent {
    data class SubtitleGenerated(val participantId: String, val participantName: String, val text: String, val translation: String) : TranslationEvent()
}

sealed class NetworkEvent : AppEvent {
    object ConnectionLost : NetworkEvent()
    object ConnectionRecovered : NetworkEvent()
}

