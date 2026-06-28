package com.aistudio.meetingplatform.feature.ai.data.engine

import com.aistudio.meetingplatform.core.event.EventBus
import com.aistudio.meetingplatform.core.event.ChatEvent
import com.aistudio.meetingplatform.core.event.TranslationEvent
import com.aistudio.meetingplatform.feature.ai.domain.engine.AIEngine
import com.aistudio.meetingplatform.feature.translation.data.api.Content
import com.aistudio.meetingplatform.feature.translation.data.api.GeminiApiService
import com.aistudio.meetingplatform.feature.translation.data.api.GenerateContentRequest
import com.aistudio.meetingplatform.feature.translation.data.api.Part
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AIEngineImpl @Inject constructor(
    private val apiService: GeminiApiService,
    @Named("GEMINI_API_KEY") private val apiKey: String,
    private val eventBus: EventBus
) : AIEngine {

    private val _transcript = MutableStateFlow("")
    override val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _summary = MutableStateFlow("")
    override val summary: StateFlow<String> = _summary.asStateFlow()

    private val _actionItems = MutableStateFlow<List<String>>(emptyList())
    override val actionItems: StateFlow<List<String>> = _actionItems.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    override val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is ChatEvent.MessageSent -> {
                        appendTranscript("${event.senderName}: ${event.message}")
                    }
                    is TranslationEvent.SubtitleGenerated -> {
                        appendTranscript("${event.participantName} (speech): ${event.text}")
                    }
                }
            }
        }
    }

    private fun appendTranscript(text: String) {
        _transcript.update { current ->
            if (current.isEmpty()) text else "$current\n$text"
        }
    }

    override suspend fun generateSummary() {
        if (_transcript.value.isBlank()) {
            _summary.value = "No transcript available yet."
            return
        }
        
        _isProcessing.value = true
        try {
            val response = callGemini("Summarize the following meeting transcript:\n\n${_transcript.value}")
            _summary.value = response
        } catch (e: Exception) {
            _summary.value = "Failed to generate summary: ${e.message}"
        } finally {
            _isProcessing.value = false
        }
    }

    override suspend fun extractActionItems() {
        if (_transcript.value.isBlank()) {
            _actionItems.value = emptyList()
            return
        }
        
        _isProcessing.value = true
        try {
            val response = callGemini("Extract action items from the following meeting transcript. Return them as a bulleted list starting with '-':\n\n${_transcript.value}")
            val items = response.split("\n")
                .filter { it.trim().startsWith("-") }
                .map { it.trim().removePrefix("-").trim() }
            
            _actionItems.value = items.takeIf { it.isNotEmpty() } ?: listOf(response)
        } catch (e: Exception) {
            _actionItems.value = listOf("Failed to extract action items: ${e.message}")
        } finally {
            _isProcessing.value = false
        }
    }

    override suspend fun askQuestion(question: String): String {
        if (_transcript.value.isBlank()) {
            return "No meeting data available to answer your question."
        }
        
        _isProcessing.value = true
        return try {
            callGemini("Based on the following meeting transcript, answer the question.\n\nTranscript:\n${_transcript.value}\n\nQuestion: $question")
        } catch (e: Exception) {
            "Failed to answer question: ${e.message}"
        } finally {
            _isProcessing.value = false
        }
    }

    private suspend fun callGemini(prompt: String): String {
        if (apiKey.isBlank() || apiKey.contains("MY_GEMINI_API_KEY")) {
            return "Simulated AI Response (API Key not configured):\n$prompt"
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )
        val response = apiService.generateContent("gemini-3.1-flash-lite", apiKey, request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: "No response from AI."
    }
}
