package com.aistudio.meetingplatform.feature.ai.di

import com.aistudio.meetingplatform.feature.ai.data.engine.AIEngineImpl
import com.aistudio.meetingplatform.feature.ai.domain.engine.AIEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AIModule {

    @Binds
    @Singleton
    abstract fun bindAIEngine(
        impl: AIEngineImpl
    ): AIEngine
}
