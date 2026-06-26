package com.finrein.pals.data.repository

import com.finrein.pals.domain.repository.DashboardRepository
import com.finrein.pals.domain.model.PalItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.boolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : DashboardRepository {

    override suspend fun getCleanHomescreenDashboard(currentUserId: String): Result<List<PalItem>> = withContext(Dispatchers.IO) {
        runCatching {
            // 🚀 Replaces all sequential client queries with one single database transaction
            val rawResponseString = supabaseClient.postgrest.rpc(
                function = "get_clean_homescreen_dashboard",
                parameters = mapOf("current_user_uuid" to currentUserId)
            ).data

            // Parse unified payload array
            val jsonObject = Json.parseToJsonElement(rawResponseString).jsonObject
            val vlogBoxSize = jsonObject["vlog_box_size"]?.jsonPrimitive?.content ?: ""
            val groupsArray = jsonObject["groups"]?.jsonArray ?: kotlinx.serialization.json.JsonArray(emptyList())

            val defaultVlog = PalItem(
                name = "vlog",
                size = vlogBoxSize,
                code = "vlog",
                isVlog = true,
                isCreator = false
            )

            val mappedPals = groupsArray.map { element ->
                val obj = element.jsonObject
                PalItem(
                    name = obj["name"]?.jsonPrimitive?.content ?: "",
                    size = obj["size"]?.jsonPrimitive?.content ?: "1",
                    code = obj["code"]?.jsonPrimitive?.content ?: "",
                    isVlog = false,
                    isCreator = obj["is_creator"]?.jsonPrimitive?.boolean ?: false
                )
            }

            listOf(defaultVlog) + mappedPals
        }
    }
}
