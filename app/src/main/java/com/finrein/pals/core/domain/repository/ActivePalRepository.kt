package com.finrein.pals.core.domain.repository

import com.finrein.pals.core.domain.model.ActivePalState

interface ActivePalRepository {
    suspend fun getActivePalDetails(
        palCode: String,
        currentUserId: String,
        currentDisplayName: String,
        firstName: String,
        currentAvatarUrl: String?,
        locallyDeletedSubmissions: Set<String>,
        syncMutex: kotlinx.coroutines.sync.Mutex,
        resolveAvatarUrl: suspend () -> String?
    ): Result<ActivePalState>
}
