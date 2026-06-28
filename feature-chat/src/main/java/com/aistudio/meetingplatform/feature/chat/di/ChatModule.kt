package com.aistudio.meetingplatform.feature.chat.di

import com.aistudio.meetingplatform.feature.chat.data.repository.ChatRepositoryImpl
import com.aistudio.meetingplatform.feature.chat.domain.repository.ChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {

    @Binds
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository
}
