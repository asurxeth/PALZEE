package com.finrein.pals.domain.repository

import com.finrein.pals.domain.model.MessageDbItem

interface ChatRepository {
    suspend fun getMessages(palCode: String): Result<List<MessageDbItem>>
}
