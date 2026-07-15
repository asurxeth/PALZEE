package com.finrein.pals.core.domain.repository

import com.finrein.pals.core.domain.model.MessageDbItem

interface ChatRepository {
    suspend fun getMessages(palCode: String): Result<List<MessageDbItem>>
    suspend fun postMessage(message: MessageDbItem): Result<Unit>
}
