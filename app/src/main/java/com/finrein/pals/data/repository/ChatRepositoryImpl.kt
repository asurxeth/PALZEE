package com.finrein.pals.data.repository

import com.finrein.pals.domain.repository.ChatRepository
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject
import javax.inject.Singleton

import com.finrein.pals.domain.model.MessageDbItem
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ChatRepository {

    override suspend fun getMessages(palCode: String): Result<List<MessageDbItem>> = withContext(Dispatchers.IO) {
        runCatching {
            supabaseClient.postgrest.from("messages")
                .select {
                    filter {
                        eq("pal_code", palCode)
                    }
                    order(column = "created_at", order = io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }
                .decodeList<MessageDbItem>()
        }
    }

    override suspend fun postMessage(message: MessageDbItem): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            supabaseClient.postgrest.from("messages").insert(message)
            Unit
        }
    }
}
