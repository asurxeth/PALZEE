package com.finrein.pals.data.repository

import com.finrein.pals.domain.repository.GroupRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : GroupRepository {

    override suspend fun leaveGroup(userId: String, groupId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            supabaseClient.postgrest.from("user_pals").delete {
                filter {
                    eq("user_id", userId)
                    eq("pal_code", groupId)
                }
            }
            Unit
        }
    }

    override suspend fun deleteGroup(groupId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            supabaseClient.postgrest.from("pals").delete {
                filter {
                    eq("code", groupId)
                }
            }
            Unit
        }
    }
}
