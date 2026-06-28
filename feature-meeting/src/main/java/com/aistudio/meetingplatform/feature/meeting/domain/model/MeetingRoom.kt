package com.aistudio.meetingplatform.feature.meeting.domain.model

data class MeetingRoom(
    val id: String,
    val title: String,
    val topic: String,
    val hostName: String,
    val participantCount: Int
)
