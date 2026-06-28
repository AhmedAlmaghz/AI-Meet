package com.aistudio.meetingplatform.feature.translation.domain.model

data class Subtitle(
    val id: String,
    val participantId: String,
    val participantName: String,
    val originalText: String,
    val translatedText: String,
    val timestamp: Long
)
