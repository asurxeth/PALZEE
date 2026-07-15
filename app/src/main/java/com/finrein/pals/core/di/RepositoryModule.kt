package com.finrein.pals.core.di

import com.finrein.pals.core.data.repository.AuthRepositoryImpl
import com.finrein.pals.core.domain.repository.AuthRepository
import com.finrein.pals.core.data.repository.DashboardRepositoryImpl
import com.finrein.pals.core.domain.repository.DashboardRepository
import com.finrein.pals.core.data.repository.ChatRepositoryImpl
import com.finrein.pals.core.domain.repository.ChatRepository
import com.finrein.pals.core.data.repository.StorageRepositoryImpl
import com.finrein.pals.core.domain.repository.StorageRepository
import com.finrein.pals.core.data.repository.GroupRepositoryImpl
import com.finrein.pals.core.domain.repository.GroupRepository
import com.finrein.pals.core.data.repository.ActivePalRepositoryImpl
import com.finrein.pals.core.domain.repository.ActivePalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class GroupModule {

    @Binds
    @Singleton
    abstract fun bindGroupRepository(
        groupRepositoryImpl: GroupRepositoryImpl
    ): GroupRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        storageRepositoryImpl: StorageRepositoryImpl
    ): StorageRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ActivePalModule {

    @Binds
    @Singleton
    abstract fun bindActivePalRepository(
        activePalRepositoryImpl: ActivePalRepositoryImpl
    ): ActivePalRepository
}
