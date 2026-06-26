package com.finrein.pals.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.finrein.pals.domain.repository.DashboardRepository
import com.finrein.pals.domain.repository.ChatRepository
import com.finrein.pals.domain.model.PalItem
import com.finrein.pals.domain.model.MessageDbItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _createdPals = MutableStateFlow<List<PalItem>>(emptyList())
    val createdPals: StateFlow<List<PalItem>> = _createdPals

    private val _palMessages = MutableStateFlow<Map<String, List<MessageDbItem>>>(emptyMap())
    val palMessages: StateFlow<Map<String, List<MessageDbItem>>> = _palMessages

    fun updateCreatedPals(list: List<PalItem>) {
        _createdPals.value = list
    }

    fun updatePalMessages(key: String, list: List<MessageDbItem>) {
        _palMessages.value = _palMessages.value + (key to list)
    }

    fun removePalMessages(key: String) {
        _palMessages.value = _palMessages.value - key
    }

    suspend fun refreshPals(currentUserId: String) {
        if (currentUserId.isEmpty()) return
        val result = dashboardRepository.getCleanHomescreenDashboard(currentUserId)
        result.getOrNull()?.let { list ->
            _createdPals.value = list.distinctBy { it.code }
        }
    }

    suspend fun refreshMessages(palCode: String) {
        if (palCode == "vlog") return
        val result = chatRepository.getMessages(palCode)
        result.getOrNull()?.let { dbMessages ->
            val oldMsgs = _palMessages.value[palCode] ?: emptyList()
            if (oldMsgs != dbMessages) {
                updatePalMessages(palCode, dbMessages)
            }
        }
    }
}
