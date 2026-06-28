package com.aistudio.meetingplatform.feature.translation.domain.engine

import com.aistudio.meetingplatform.feature.translation.domain.model.Subtitle
import kotlinx.coroutines.flow.StateFlow

interface TranslationEngine {
    val subtitles: StateFlow<List<Subtitle>>
    
    suspend fun start()
    suspend fun stop()
    
    // Simulate streaming audio/text for translation
    suspend fun simulateSpeechReceived(participantId: String, participantName: String, text: String)
}
