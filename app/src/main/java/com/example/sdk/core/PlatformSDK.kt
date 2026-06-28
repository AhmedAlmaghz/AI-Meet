package com.example.sdk.core

import android.util.Log
import com.example.data.AppRepository
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Platform Plugin Registry & Microkernel Engine
 */
class PluginManager {
    private val plugins = mutableMapOf<String, MeetingPlugin>()

    fun registerPlugin(plugin: MeetingPlugin, scope: CoroutineScope) {
        if (!plugins.containsKey(plugin.id)) {
            plugins[plugin.id] = plugin
            plugin.onPluginInitialized(scope)
            Log.d("PluginEngine", "🔌 Registered Plugin: ${plugin.name} v${plugin.version}")
        }
    }

    fun notifyRoomJoined(room: MeetingRoom) {
        plugins.values.forEach { it.onRoomJoined(room) }
    }

    fun notifyParticipantUpdated(p: Participant) {
        plugins.values.forEach { it.onParticipantUpdated(p) }
    }

    fun detachAll() {
        plugins.values.forEach { it.onPluginDetached() }
        plugins.clear()
    }
}

/**
 * Enterprise Platform SDK Facade
 * يدمج جميع وحدات النظام (Auth, Meeting, Chat, AI Translation, Plugins)
 */
class PlatformSDK(
    private val repository: AppRepository
) {
    val pluginManager = PluginManager()

    val auth: AuthSDKContract = object : AuthSDKContract {
        override val currentUser: StateFlow<User> = repository.currentUser
        override fun login(name: String, email: String, isGuest: Boolean) = repository.login(name, email, isGuest)
        override fun logout() = repository.login("", "", true)
    }

    val meeting: MeetingSDKContract = object : MeetingSDKContract {
        override val activeRoom: StateFlow<MeetingRoom?> = repository.currentRoom
        override val participants: StateFlow<List<Participant>> = repository.participants
        override val spotlightId: StateFlow<String?> = repository.forcedSpotlightId
        override val isModeratorGlobalMute: StateFlow<Boolean> = repository.isModeratorMuteAllActive
        override val personalMutedIds: StateFlow<Set<String>> = repository.localMutedParticipantIds

        override fun joinRoom(room: MeetingRoom, scope: CoroutineScope) {
            repository.joinRoom(room, scope)
            pluginManager.notifyRoomJoined(room)
        }
        override fun leaveRoom() = repository.leaveRoom()
        override fun toggleLocalMic() = repository.toggleLocalMic()
        override fun toggleLocalCamera() = repository.toggleLocalCamera()
        override fun toggleLocalScreenShare() = repository.toggleLocalScreenShare()
        override fun toggleSpotlight(participantId: String) = repository.toggleSpotlight(participantId)
        override fun togglePersonalMute(participantId: String) = repository.toggleParticipantMuteLocal(participantId)
        override fun setModeratorGlobalMute(active: Boolean) {
            if (active) repository.moderatorMuteAllExceptHost()
            else repository.muteAllExcept(participants.value.map { it.id }.toSet())
        }
        override fun muteAllExcept(allowedParticipantIds: Set<String>) = repository.muteAllExcept(allowedParticipantIds)
    }

    val translation: TranslationSDK = GeminiTranslationProvider()

    val chat: ChatSDKContract = object : ChatSDKContract {
        override fun getRoomMessages(roomId: String): Flow<List<ChatMessage>> = repository.getChatMessagesFlow(roomId)
        override fun sendMessage(text: String, isPrivate: Boolean, recipient: Participant?, scope: CoroutineScope) {
            repository.sendMessage(text, isPrivate, recipient, scope)
        }
    }
}
