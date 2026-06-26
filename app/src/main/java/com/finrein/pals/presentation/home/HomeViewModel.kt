package com.finrein.pals.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import com.finrein.pals.domain.repository.DashboardRepository
import com.finrein.pals.domain.repository.ChatRepository
import com.finrein.pals.domain.repository.ActivePalRepository
import com.finrein.pals.domain.model.PalItem
import com.finrein.pals.domain.model.MessageDbItem
import com.finrein.pals.domain.model.ActivePalState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val chatRepository: ChatRepository,
    private val activePalRepository: ActivePalRepository
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

    private val _activePalState = MutableStateFlow<ActivePalState?>(null)
    val activePalState: StateFlow<ActivePalState?> = _activePalState

    val globalSyncMutex = Mutex()

    private val pendingProfileInserts = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

    fun removePendingProfileInsert(palCode: String) {
        pendingProfileInserts.remove(palCode)
    }

    fun clearActivePalState() {
        _activePalState.value = null
    }

    fun refreshActivePalDetails(
        palCode: String,
        currentUserId: String,
        currentDisplayName: String,
        firstName: String,
        currentAvatarUrl: String?,
        locallyDeletedSubmissions: Set<String>,
        resolveAvatarUrl: suspend () -> String?
    ) {
        if (currentUserId.isEmpty() || palCode == "vlog" || palCode.isBlank()) return
        viewModelScope.launch {
            val result = activePalRepository.getActivePalDetails(
                palCode = palCode,
                currentUserId = currentUserId,
                currentDisplayName = currentDisplayName,
                firstName = firstName,
                currentAvatarUrl = currentAvatarUrl,
                locallyDeletedSubmissions = locallyDeletedSubmissions,
                syncMutex = globalSyncMutex,
                resolveAvatarUrl = {
                    if (pendingProfileInserts[palCode] != true) {
                        pendingProfileInserts[palCode] = true
                        try {
                            resolveAvatarUrl()
                        } finally {
                            pendingProfileInserts.remove(palCode)
                        }
                    } else {
                        null
                    }
                }
            )
            result.getOrNull()?.let { state ->
                _activePalState.value = state
            }
        }
    }
}
