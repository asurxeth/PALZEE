package com.finrein.pals.domain.repository

import com.finrein.pals.domain.model.PalItem

interface DashboardRepository {
    suspend fun getCleanHomescreenDashboard(currentUserId: String): Result<List<PalItem>>
}
