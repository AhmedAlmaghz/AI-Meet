package com.aistudio.meetingplatform.feature.meeting.di

import com.aistudio.meetingplatform.feature.meeting.data.repository.MeetingRepositoryImpl
import com.aistudio.meetingplatform.feature.meeting.domain.repository.MeetingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MeetingModule {

    @Binds
    abstract fun bindMeetingRepository(
        meetingRepositoryImpl: MeetingRepositoryImpl
    ): MeetingRepository
}
