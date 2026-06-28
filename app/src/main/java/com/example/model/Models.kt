package com.example.model

import com.example.R

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    ARABIC("ar", "العربية", "🇸🇦"),
    ENGLISH("en", "English", "🇬🇧")
}

enum class ThemePalette(val displayName: String, val icon: String) {
    SLATE_DARK("الداكن الفاخر (Slate)", "🌙"),
    EMERALD_LIGHT("الفاتح الهادئ (Emerald)", "☀️"),
    CYBER_NEON("سايبر نيون (Cyber)", "⚡"),
    SUNSET_GLOW("غروب الشمس (Sunset)", "🌅")
}

data class User(
    val id: String = "u_me",
    val name: String = "أحمد المهندس",
    val email: String = "ahmed@example.com",
    val avatarRes: Int = R.drawable.img_avatar_student1,
    val isGuest: Boolean = false
)

data class Participant(
    val id: String,
    val name: String,
    val avatarRes: Int,
    val isSpeaking: Boolean = false,
    val isMuted: Boolean = false,
    val isScreenSharing: Boolean = false,
    val isVideoOn: Boolean = true,
    val isTranslating: Boolean = false,
    val originalLanguage: String = "en",
    val currentSpeech: String = "",
    val translatedSpeech: String = "",
    val isModerator: Boolean = false,
    val isAI: Boolean = false,
    val isHandRaised: Boolean = false
)

data class MeetingRoom(
    val id: String,
    val title: String,
    val topic: String,
    val category: String,
    val hostName: String,
    val participantCount: Int,
    val isActive: Boolean = true,
    val isClassroom: Boolean = false,
    val hasAIAgent: Boolean = true,
    val passcode: String = ""
)

data class ChatMessage(
    val id: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: Int,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPrivate: Boolean = false,
    val recipientId: String? = null,
    val recipientName: String? = null
)
