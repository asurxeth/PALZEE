package com.finrein.pals.di

import com.finrein.pals.data.repository.AuthRepositoryImpl
import com.finrein.pals.domain.repository.AuthRepository
import com.finrein.pals.data.repository.DashboardRepositoryImpl
import com.finrein.pals.domain.repository.DashboardRepository
import com.finrein.pals.data.repository.ChatRepositoryImpl
import com.finrein.pals.domain.repository.ChatRepository
import com.finrein.pals.data.repository.StorageRepositoryImpl
import com.finrein.pals.domain.repository.StorageRepository
import com.finrein.pals.data.repository.GroupRepositoryImpl
import com.finrein.pals.domain.repository.GroupRepository
import com.finrein.pals.data.repository.ActivePalRepositoryImpl
import com.finrein.pals.domain.repository.ActivePalRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
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


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): io.github.jan.supabase.SupabaseClient {
        return com.finrein.pals.PalApplication.supabase
    }

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            install(WebSockets)
        }
    }
}
