package com.example.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.model.*
import com.example.sdk.core.PlatformSDK
import com.example.sdk.plugins.ClassroomAnalyticsPlugin
import com.example.sdk.plugins.GeminiTranslationPlugin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = AppRepository(database.chatDao(), database.roomDao())
    val platformSDK = PlatformSDK(repository)

    private val _appLanguage = MutableStateFlow(AppLanguage.ARABIC)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _themePalette = MutableStateFlow(ThemePalette.SLATE_DARK)
    val themePalette: StateFlow<ThemePalette> = _themePalette.asStateFlow()

    private val _isLiveTranslationActive = MutableStateFlow(true)
    val isLiveTranslationActive: StateFlow<Boolean> = _isLiveTranslationActive.asStateFlow()

    private val _isTTSAudioEnabled = MutableStateFlow(true)
    val isTTSAudioEnabled: StateFlow<Boolean> = _isTTSAudioEnabled.asStateFlow()

    private val _notificationAlert = MutableStateFlow<String?>(null)
    val notificationAlert: StateFlow<String?> = _notificationAlert.asStateFlow()

    private var ttsEngine: TextToSpeech? = null

    init {
        // Register Enterprise Microkernel Plugins
        platformSDK.pluginManager.registerPlugin(GeminiTranslationPlugin(), viewModelScope)
        platformSDK.pluginManager.registerPlugin(ClassroomAnalyticsPlugin(), viewModelScope)

        ttsEngine = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.language = Locale("ar")
            }
        }
        // Start simulation speech rotation loop
        viewModelScope.launch {
            while (true) {
                delay(8000)
                if (repository.currentRoom.value != null && _isLiveTranslationActive.value) {
                    repository.triggerLiveTranslationCycle(_appLanguage.value.displayName, viewModelScope)
                    // Speak out translated speech if TTS enabled
                    if (_isTTSAudioEnabled.value) {
                        val speakingPart = repository.participants.value.find { it.id != repository.currentUser.value.id && it.translatedSpeech.isNotBlank() }
                        if (speakingPart != null) {
                            ttsEngine?.speak(speakingPart.translatedSpeech, TextToSpeech.QUEUE_FLUSH, null, "tts_trans")
                        }
                    }
                }
            }
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _appLanguage.value = lang
        ttsEngine?.language = if (lang == AppLanguage.ARABIC) Locale("ar") else Locale.ENGLISH
        if (repository.currentRoom.value != null) {
            repository.triggerLiveTranslationCycle(lang.displayName, viewModelScope)
            showNotification(if (lang == AppLanguage.ARABIC) "تم تغيير لغة الترجمة الفورية إلى العربية 🇸🇦" else "Live AI Voice Translation switched to English 🇬🇧")
        }
    }

    fun setTheme(palette: ThemePalette) {
        _themePalette.value = palette
    }

    fun toggleLiveTranslation() {
        val newVal = !_isLiveTranslationActive.value
        _isLiveTranslationActive.value = newVal
        showNotification(if (newVal) "تم تفعيل الترجمة الفورية للصوت (Gemini AI)" else "تم إيقاف الترجمة الفورية")
    }

    fun toggleTTSAudio() {
        val newVal = !_isTTSAudioEnabled.value
        _isTTSAudioEnabled.value = newVal
        if (!newVal) ttsEngine?.stop()
        showNotification(if (newVal) "تم تفعيل القراءة الصوتية للترجمة 🔊" else "تم كتم القراءة الصوتية للترجمة 🔇")
    }

    fun showNotification(msg: String) {
        _notificationAlert.value = msg
        viewModelScope.launch {
            delay(3500)
            if (_notificationAlert.value == msg) _notificationAlert.value = null
        }
    }

    fun login(name: String, email: String, isGuest: Boolean) {
        platformSDK.auth.login(name, email, isGuest)
        showNotification("أهلاً بك يا ${name.ifBlank { "ضيف" }} في منصة AI Meet 🚀")
    }

    fun joinRoom(room: MeetingRoom) {
        platformSDK.meeting.joinRoom(room, viewModelScope)
        showNotification("انضممت إلى غرفة: ${room.title}")
        // Immediately run first translation
        repository.triggerLiveTranslationCycle(_appLanguage.value.displayName, viewModelScope)
    }

    fun leaveRoom() {
        ttsEngine?.stop()
        platformSDK.meeting.leaveRoom()
        showNotification("غادرت الغرفة")
    }

    fun sendMessage(text: String, isPrivate: Boolean, recipient: Participant?) {
        repository.sendMessage(text, isPrivate, recipient, viewModelScope)
    }

    override fun onCleared() {
        ttsEngine?.shutdown()
        platformSDK.pluginManager.detachAll()
        super.onCleared()
    }
}
