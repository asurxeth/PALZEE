package com.finrein.pals

import android.app.Application
import android.util.Log
import androidx.profileinstaller.ProfileInstaller
import dagger.hilt.android.HiltAndroidApp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin

import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime

import io.ktor.client.plugins.HttpTimeout

@HiltAndroidApp
class PalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        com.finrein.pals.core.data.local.SharedContextRegistry.context = this
        
        // Force critical path tracing directly on startup 
        // to stabilize Compose view trees
        Thread {
            try {
                ProfileInstaller.writeProfile(this)
            } catch (e: Exception) {
                Log.e("Profile_Debug", "Failed to write profile", e)
            }
        }.start()
    }

    companion object {
        lateinit var instance: PalApplication
            private set

        @kotlin.OptIn(io.github.jan.supabase.annotations.SupabaseInternal::class)
        val supabase: SupabaseClient by lazy {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                httpConfig {
                    install(HttpTimeout) {
                        requestTimeoutMillis = 120000
                        connectTimeoutMillis = 120000
                        socketTimeoutMillis = 120000
                    }
                }
                install(Auth)
                install(ComposeAuth) {
                    googleNativeLogin(serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID)
                }
                install(Postgrest)
                install(Storage)
                install(Realtime)
            }
        }
    }
}

typealias PALApplication = PalApplication
