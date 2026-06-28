package com.example.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.event.AppEvent
import com.example.core.event.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel : ViewModel() {

    private val _recentEvents = MutableStateFlow<List<AppEvent>>(emptyList())
    val recentEvents: StateFlow<List<AppEvent>> = _recentEvents.asStateFlow()

    val moduleStates: StateFlow<List<ModuleState>> = ArchitectureMonitor.modules
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Collect published events and keep a rolling history in the UI state
        viewModelScope.launch {
            EventBus.events.collect { event ->
                _recentEvents.value = (listOf(event) + _recentEvents.value).take(100)
            }
        }

        // Emit a baseline system initialization event
        viewModelScope.launch {
            EventBus.publish(
                AppEvent.MeetingCreated(
                    meetingId = "sys-init",
                    title = "AI Collaboration System Core Online"
                )
            )
        }
    }

    fun triggerAuthEvent() {
        viewModelScope.launch {
            val userId = "user_${(1000..9999).random()}"
            EventBus.publish(
                AppEvent.UserLoggedIn(
                    userId = userId,
                    email = "engineer@example.com"
                )
            )
            ArchitectureMonitor.updateModuleStatus(ModuleName.AUTH, ModuleStatus.ACTIVE)
        }
    }

    fun triggerMeetingEvent() {
        viewModelScope.launch {
            val meetingId = "room_${(100..999).random()}"
            EventBus.publish(
                AppEvent.MeetingCreated(
                    meetingId = meetingId,
                    title = "Sprint Planning meeting"
                )
            )
            EventBus.publish(
                AppEvent.UserJoinedMeeting(
                    userId = "user_4921",
                    name = "Sarah Connor (Host)"
                )
            )
            ArchitectureMonitor.updateModuleStatus(ModuleName.MEETING, ModuleStatus.ACTIVE)
        }
    }

    fun triggerMediaEvent() {
        viewModelScope.launch {
            EventBus.publish(
                AppEvent.AudioStreamStarted(
                    userId = "user_4921"
                )
            )
            EventBus.publish(
                AppEvent.VideoStreamStarted(
                    userId = "user_4921"
                )
            )
            ArchitectureMonitor.updateModuleStatus(ModuleName.MEDIA, ModuleStatus.ACTIVE)
        }
    }

    fun triggerChatEvent() {
        viewModelScope.launch {
            val msgs = listOf(
                "Can everyone hear me well?",
                "Yes, crystal clear!",
                "Great! Let's review the AI features.",
                "Let's double-check the real-time speech subtitles."
            )
            EventBus.publish(
                AppEvent.MessageSent(
                    messageId = UUID.randomUUID().toString(),
                    senderId = "user_4921",
                    text = msgs.random()
                )
            )
            ArchitectureMonitor.updateModuleStatus(ModuleName.CHAT, ModuleStatus.ACTIVE)
        }
    }

    fun triggerTranslationEvent() {
        viewModelScope.launch {
            val translations = listOf(
                Pair("Hello everyone, welcome to the sprint review.", "مرحباً بالجميع، أهلاً بكم في مراجعة دورة العمل."),
                Pair("The real-time translation module uses Gemini Live API.", "وحدة الترجمة الفورية تستخدم واجهة برمجة تطبيقات جيميناي الحية."),
                Pair("We are now demonstrating Phase 1 foundation.", "نحن الآن نستعرض أساسيات المرحلة الأولى.")
            ).random()
            EventBus.publish(
                AppEvent.TranslationUpdated(
                    originalText = translations.first,
                    translatedText = translations.second,
                    targetLanguage = "Arabic (AR)"
                )
            )
            ArchitectureMonitor.updateModuleStatus(ModuleName.TRANSLATION, ModuleStatus.ACTIVE)
        }
    }

    fun triggerAIEvent() {
        viewModelScope.launch {
            val promptsAndResponses = listOf(
                Pair("Summarize Sarah's points", "Sarah has initiated the stream and is guiding the sprint planning meeting."),
                Pair("Show me action items", "1. Complete Phase 1 compilation\n2. Prepare auth views for Phase 2\n3. Verify WebRTC insets in Phase 4"),
                Pair("Who joined?", "Sarah Connor has joined the room as host.")
            ).random()
            EventBus.publish(
                AppEvent.AIResponseGenerated(
                    prompt = promptsAndResponses.first,
                    responseText = promptsAndResponses.second
                )
            )
            ArchitectureMonitor.updateModuleStatus(ModuleName.AI_ASSISTANT, ModuleStatus.ACTIVE)
        }
    }

    fun clearLogs() {
        _recentEvents.value = emptyList()
        // Reset statuses to INITIALIZED
        ModuleName.values().forEach {
            ArchitectureMonitor.updateModuleStatus(it, ModuleStatus.INITIALIZED)
        }
    }
}
