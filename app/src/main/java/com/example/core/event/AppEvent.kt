package com.example.core.event

import java.util.UUID

sealed interface AppEvent {
    val id: String
    val timestamp: Long

    // Phase 2: Auth events
    data class UserLoggedIn(
        val userId: String,
        val email: String,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : AppEvent

    data class UserLoggedOut(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : AppEvent

    // Phase 3: Meeting events
    data class MeetingCreated(
        val meetingId: String,
        val title: String,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : AppEvent

    data class UserJoinedMeeting(
        val userId: String,
        val name: String,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : AppEvent

    data class UserLeftMeeting(
        val userId: String,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : AppEvent

    // Phase 4: Media events
    data class AudioStreamStarted(
        val userId: String,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : AppEvent

    data class VideoStreamStarted(
        val userId: String,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : AppEvent

    // Phase 5: Chat events
    data class MessageSent(
        val messageId: String,
        val senderId: String,
        val text: String,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : AppEvent

    // Phase 6: Translation events
    data class TranslationUpdated(
        val originalText: String,
        val translatedText: String,
        val targetLanguage: String,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : AppEvent

    // Phase 7: AI events
    data class AIResponseGenerated(
        val prompt: String,
        val responseText: String,
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis()
    ) : AppEvent
}
