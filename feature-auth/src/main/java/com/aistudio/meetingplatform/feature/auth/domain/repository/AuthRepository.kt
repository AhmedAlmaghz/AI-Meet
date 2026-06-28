package com.aistudio.meetingplatform.feature.auth.domain.repository

import com.aistudio.meetingplatform.feature.auth.domain.model.UserSession
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<UserSession?>
    suspend fun login(name: String, email: String, isGuest: Boolean): Result<Unit>
    suspend fun logout()
}
