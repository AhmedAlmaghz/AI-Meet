package com.example.sdk.core

import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Microkernel Plugin Contract
 * يعتمد على مبدأ انعكاس الاعتمادية (Dependency Inversion)
 */
interface MeetingPlugin {
    val id: String
    val name: String
    val version: String
    fun onPluginInitialized(scope: CoroutineScope)
    fun onRoomJoined(room: MeetingRoom)
    fun onParticipantUpdated(participant: Participant)
    fun onPluginDetached()
}

/**
 * Auth SDK Abstraction Contract
 */
interface AuthSDKContract {
    val currentUser: StateFlow<User>
    fun login(name: String, email: String, isGuest: Boolean)
    fun logout()
}

/**
 * Translation SDK Abstraction Contract
 */
interface TranslationSDK {
    val isEnabled: StateFlow<Boolean>
    val isTTSEnabled: StateFlow<Boolean>
    fun toggleTranslation()
    fun toggleTTS()
    suspend fun translateInstant(originalSpeech: String, targetLang: String): String
    
    fun startRealTimeTranslation(targetLanguage: String)
    fun onTranslationReceived(callback: (String) -> Unit)
}

/**
 * Meeting SDK Abstraction Contract
 */
interface MeetingSDKContract {
    val activeRoom: StateFlow<MeetingRoom?>
    val participants: StateFlow<List<Participant>>
    val spotlightId: StateFlow<String?>
    val isModeratorGlobalMute: StateFlow<Boolean>
    val personalMutedIds: StateFlow<Set<String>>

    fun joinRoom(room: MeetingRoom, scope: CoroutineScope)
    fun leaveRoom()
    fun toggleLocalMic()
    fun toggleLocalCamera()
    fun toggleLocalScreenShare()
    fun toggleSpotlight(participantId: String)
    fun togglePersonalMute(participantId: String)
    fun setModeratorGlobalMute(active: Boolean)
    fun muteAllExcept(allowedParticipantIds: Set<String>)
}

/**
 * Chat SDK Abstraction Contract
 */
interface ChatSDKContract {
    fun getRoomMessages(roomId: String): Flow<List<ChatMessage>>
    fun sendMessage(text: String, isPrivate: Boolean, recipient: Participant?, scope: CoroutineScope)
}
