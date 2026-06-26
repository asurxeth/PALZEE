package com.finrein.pals.data.repository

import com.finrein.pals.domain.repository.StorageRepository
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : StorageRepository {
    // Implementation for file storage and upload operations
}
