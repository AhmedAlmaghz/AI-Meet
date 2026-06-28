package com.aistudio.meetingplatform.feature.auth.domain.model

data class UserSession(
    val id: String,
    val name: String,
    val email: String,
    val isGuest: Boolean
)
