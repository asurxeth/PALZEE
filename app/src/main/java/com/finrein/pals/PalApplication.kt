package com.finrein.pals

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin

import io.github.jan.supabase.postgrest.Postgrest

@HiltAndroidApp
class PalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: PalApplication
            private set

        val supabase: SupabaseClient by lazy {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                install(Auth)
                install(ComposeAuth) {
                    googleNativeLogin(serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID)
                }
                install(Postgrest)
            }
        }
    }
}

typealias PALApplication = PalApplication
