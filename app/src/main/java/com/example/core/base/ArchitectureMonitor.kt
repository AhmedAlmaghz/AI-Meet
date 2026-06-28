package com.example.core.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ModuleName {
    AUTH, MEETING, MEDIA, CHAT, TRANSLATION, AI_ASSISTANT
}

enum class ModuleStatus {
    OFFLINE, INITIALIZED, ACTIVE, ERROR
}

data class ModuleState(
    val name: ModuleName,
    val status: ModuleStatus,
    val description: String,
    val phase: Int
)

object ArchitectureMonitor {
    private val _modules = MutableStateFlow(
        listOf(
            ModuleState(ModuleName.AUTH, ModuleStatus.INITIALIZED, "User Credentials & Sessions", 2),
            ModuleState(ModuleName.MEETING, ModuleStatus.INITIALIZED, "Lifecycle & Participants", 3),
            ModuleState(ModuleName.MEDIA, ModuleStatus.INITIALIZED, "WebRTC Audio/Video Engine", 4),
            ModuleState(ModuleName.CHAT, ModuleStatus.INITIALIZED, "Real-Time Message Channel", 5),
            ModuleState(ModuleName.TRANSLATION, ModuleStatus.INITIALIZED, "Gemini Subtitles & Translate", 6),
            ModuleState(ModuleName.AI_ASSISTANT, ModuleStatus.INITIALIZED, "Gemini Action Items & Notes", 7)
        )
    )
    val modules: StateFlow<List<ModuleState>> = _modules.asStateFlow()

    fun updateModuleStatus(name: ModuleName, status: ModuleStatus) {
        _modules.value = _modules.value.map {
            if (it.name == name) it.copy(status = status) else it
        }
    }
}
