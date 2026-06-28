package com.aistudio.meetingplatform.feature.media.di

import com.aistudio.meetingplatform.feature.media.data.MediaEngineImpl
import com.aistudio.meetingplatform.feature.media.domain.MediaEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaModule {

    @Binds
    abstract fun bindMediaEngine(
        mediaEngineImpl: MediaEngineImpl
    ): MediaEngine
}
