package com.aistudio.meetingplatform.feature.ai.domain.engine

import kotlinx.coroutines.flow.StateFlow

interface AIEngine {
    val transcript: StateFlow<String>
    val summary: StateFlow<String>
    val actionItems: StateFlow<List<String>>
    val isProcessing: StateFlow<Boolean>
    
    suspend fun generateSummary()
    suspend fun extractActionItems()
    suspend fun askQuestion(question: String): String
}
