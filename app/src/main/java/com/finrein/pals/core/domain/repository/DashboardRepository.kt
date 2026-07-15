package com.finrein.pals.core.domain.repository

import com.finrein.pals.core.domain.model.PalItem

interface DashboardRepository {
    suspend fun getCleanHomescreenDashboard(currentUserId: String): Result<List<PalItem>>
}
