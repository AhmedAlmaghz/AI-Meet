package com.aistudio.meetingplatform.feature.auth.di

import com.aistudio.meetingplatform.feature.auth.data.repository.AuthRepositoryImpl
import com.aistudio.meetingplatform.feature.auth.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
