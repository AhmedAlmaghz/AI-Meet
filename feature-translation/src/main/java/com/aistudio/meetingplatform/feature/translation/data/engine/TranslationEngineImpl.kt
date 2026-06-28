package com.aistudio.meetingplatform.feature.translation.data.engine

import com.aistudio.meetingplatform.core.event.EventBus
import com.aistudio.meetingplatform.core.event.TranslationEvent
import com.aistudio.meetingplatform.feature.translation.data.api.Content
import com.aistudio.meetingplatform.feature.translation.data.api.GeminiApiService
import com.aistudio.meetingplatform.feature.translation.data.api.GenerateContentRequest
import com.aistudio.meetingplatform.feature.translation.data.api.Part
import com.aistudio.meetingplatform.feature.translation.domain.engine.TranslationEngine
import com.aistudio.meetingplatform.feature.translation.domain.model.Subtitle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TranslationEngineImpl @Inject constructor(
    private val apiService: GeminiApiService,
    @Named("GEMINI_API_KEY") private val apiKey: String,
    private val eventBus: EventBus
) : TranslationEngine {

    private val _subtitles = MutableStateFlow<List<Subtitle>>(emptyList())
    override val subtitles: StateFlow<List<Subtitle>> = _subtitles.asStateFlow()
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            eventBus.events.collect { event ->
                if (event is com.aistudio.meetingplatform.core.event.ChatEvent.MessageSent) {
                    simulateSpeechReceived(event.senderId, event.senderName, event.message)
                }
            }
        }
    }

    override suspend fun start() {
        // Initialization if needed
    }

    override suspend fun stop() {
        _subtitles.value = emptyList()
        scope.cancel()
    }

    override suspend fun simulateSpeechReceived(participantId: String, participantName: String, text: String) {
        val translation = try {
            if (apiKey.isBlank() || apiKey.contains("MY_GEMINI_API_KEY")) {
                "API Key not configured. Simulate: [Translated $text]"
            } else {
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = "Translate this to English (if already English, translate to Spanish): $text")))
                    )
                )
                val response = apiService.generateContent("gemini-3.5-live-translate-preview", apiKey, request)
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: "No translation"
            }
        } catch (e: Exception) {
            "Translation error: ${e.message}"
        }

        val subtitle = Subtitle(
            id = UUID.randomUUID().toString(),
            participantId = participantId,
            participantName = participantName,
            originalText = text,
            translatedText = translation,
            timestamp = System.currentTimeMillis()
        )

        _subtitles.update { current ->
            (current + subtitle).takeLast(5) // keep last 5 subtitles
        }

        eventBus.emit(
            TranslationEvent.SubtitleGenerated(
                participantId = participantId,
                participantName = participantName,
                text = text,
                translation = translation
            )
        )
    }
}
