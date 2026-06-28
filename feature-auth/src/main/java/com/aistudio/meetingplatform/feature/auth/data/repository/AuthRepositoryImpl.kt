package com.aistudio.meetingplatform.feature.auth.data.repository

import com.aistudio.meetingplatform.feature.auth.domain.model.UserSession
import com.aistudio.meetingplatform.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {
    private val _currentUser = MutableStateFlow<UserSession?>(null)
    override val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    override suspend fun login(name: String, email: String, isGuest: Boolean): Result<Unit> {
        // Simulated network delay or DB operation
        val session = UserSession(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            isGuest = isGuest
        )
        _currentUser.value = session
        return Result.success(Unit)
    }

    override suspend fun logout() {
        _currentUser.value = null
    }
}
