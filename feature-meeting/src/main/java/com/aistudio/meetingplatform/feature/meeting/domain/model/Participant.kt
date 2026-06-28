package com.aistudio.meetingplatform.feature.meeting.domain.model

data class Participant(
    val id: String,
    val name: String,
    val isHost: Boolean,
    val isMicOn: Boolean = false,
    val isCameraOn: Boolean = false,
    val isSpeaking: Boolean = false
)
