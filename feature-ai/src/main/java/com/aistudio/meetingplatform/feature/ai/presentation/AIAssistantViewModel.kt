package com.aistudio.meetingplatform.feature.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.meetingplatform.feature.ai.domain.engine.AIEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIAssistantViewModel @Inject constructor(
    private val aiEngine: AIEngine
) : ViewModel() {

    val summary = aiEngine.summary.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val actionItems = aiEngine.actionItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isProcessing = aiEngine.isProcessing.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _qaAnswers = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val qaAnswers: StateFlow<List<Pair<String, String>>> = _qaAnswers.asStateFlow()

    fun generateSummary() {
        viewModelScope.launch {
            aiEngine.generateSummary()
        }
    }

    fun extractActionItems() {
        viewModelScope.launch {
            aiEngine.extractActionItems()
        }
    }

    fun askQuestion(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            val answer = aiEngine.askQuestion(question)
            _qaAnswers.value = _qaAnswers.value + (question to answer)
        }
    }
}
