package com.finrein.pals.domain

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime
import com.finrein.pals.domain.repository.AuthRepository
import com.finrein.pals.domain.repository.AuthRepositoryIosImpl

object IOSPlatformSDK {
    lateinit var supabaseClient: SupabaseClient
    lateinit var authRepository: AuthRepository

    fun initialize(url: String, anonKey: String) {
        supabaseClient = createSupabaseClient(url, anonKey) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
        authRepository = AuthRepositoryIosImpl(supabaseClient)
    }
}
