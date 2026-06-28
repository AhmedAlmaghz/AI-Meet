package com.aistudio.meetingplatform.feature.translation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.meetingplatform.feature.translation.domain.engine.TranslationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SubtitlesViewModel @Inject constructor(
    translationEngine: TranslationEngine
) : ViewModel() {
    val subtitles = translationEngine.subtitles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
