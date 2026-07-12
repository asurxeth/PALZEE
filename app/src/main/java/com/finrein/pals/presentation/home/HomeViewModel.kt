package com.finrein.pals.presentation.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Dispatchers
import com.finrein.pals.domain.repository.DashboardRepository
import com.finrein.pals.domain.repository.ChatRepository
import com.finrein.pals.domain.repository.ActivePalRepository
import com.finrein.pals.domain.model.PalItem
import com.finrein.pals.domain.model.MessageDbItem
import com.finrein.pals.domain.model.ActivePalState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.serialization.json.jsonPrimitive

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val application: Application,
    private val dashboardRepository: DashboardRepository,
    private val chatRepository: ChatRepository,
    private val activePalRepository: ActivePalRepository
) : ViewModel() {

    private val _createdPals = MutableStateFlow<List<PalItem>>(getInitialCachedPals(application))
    val createdPals: StateFlow<List<PalItem>> = _createdPals

    init {
        android.util.Log.d("PalsDataDebug", "ViewModel init - HashCode: ${this.hashCode()}")
        loadCachedPals()
    }

    private fun loadCachedPals() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sharedPrefs = getVlogPrefs(application)
                val saved = sharedPrefs.getString("created_pals", "") ?: ""
                val initialList = if (saved.isEmpty()) {
                    listOf(PalItem(name = "vlog", size = "12", code = "vlog", isVlog = true))
                } else {
                    saved.split(";;;").mapNotNull { s ->
                        val parts = s.split(":")
                        if (parts.size < 3) null else {
                            PalItem(
                                name = parts[0].replace("\\:", ":"),
                                size = parts.getOrNull(1) ?: "4",
                                code = parts.getOrNull(2) ?: "",
                                isVlog = parts.getOrNull(3)?.toBoolean() ?: false,
                                isCreator = parts.getOrNull(4)?.toBoolean() ?: false
                            )
                        }
                    }
                }
                _createdPals.value = initialList
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private val _currentTab = MutableStateFlow<String?>(null)
    val currentTab: StateFlow<String?> = _currentTab

    private val _palMessages = MutableStateFlow<Map<String, List<MessageDbItem>>>(emptyMap())
    val palMessages: StateFlow<Map<String, List<MessageDbItem>>> = _palMessages

    fun setCurrentTab(tab: String?) {
        _currentTab.value = tab
    }

    fun updateCreatedPals(list: List<PalItem>) {
        _createdPals.value = list
    }

    fun updatePalMessages(key: String, list: List<MessageDbItem>) {
        _palMessages.value = _palMessages.value + (key to list)
    }

    fun removePalMessages(key: String) {
        _palMessages.value = _palMessages.value - key
    }

    private var isInitialized = false

    suspend fun refreshPals(currentUserId: String, force: Boolean = false) {
        android.util.Log.d("PalsDataDebug", "RefreshPals called - HashCode: ${this.hashCode()}")
        if (currentUserId.isEmpty()) return
        val onlyHasPlaceholder = _createdPals.value.size <= 1 && _createdPals.value.firstOrNull()?.code == "vlog"
        if (!force && isInitialized && !onlyHasPlaceholder) return
        val result = dashboardRepository.getCleanHomescreenDashboard(currentUserId)
        result.getOrNull()?.let { remoteList ->
            val vlogItem = PalItem(name = "vlog", size = "12", code = "vlog", isVlog = true, isCreator = false)
            val combinedList = (listOf(vlogItem) + remoteList.filter { it.code != "vlog" })
                .filter { it.code.isNotBlank() }
            android.util.Log.d("PalsDebug", "Remote list size: ${remoteList.size}")
            android.util.Log.d("PalsDataDebug", "Remote list size: ${remoteList.size}")
            remoteList.forEach { item ->
                android.util.Log.d("PalsDataDebug", "Item found: ${item.name}, Code: ${item.code}")
            }
            _createdPals.value = combinedList.distinctBy { it.code }
            isInitialized = true
        }
    }

    suspend fun refreshMessages(palCode: String) {
        val result = chatRepository.getMessages(palCode)
        result.getOrNull()?.let { dbMessages ->
            val oldMsgs = _palMessages.value[palCode] ?: emptyList()
            if (oldMsgs != dbMessages) {
                updatePalMessages(palCode, dbMessages)
            }
        }
    }

    fun sendMessage(palCode: String, userId: String, messageText: String) {
        val tempId = java.util.UUID.randomUUID().toString()
        val localMsg = MessageDbItem(
            id = tempId,
            palCode = palCode,
            userId = userId,
            messageText = messageText,
            createdAt = java.time.Instant.now().toString()
        )
        val currentMsgs = _palMessages.value[palCode] ?: emptyList()
        val updatedMsgs = if (messageText.startsWith("REACTION|||")) {
            val parts = messageText.split("|||")
            val targetPath = parts.getOrNull(3) ?: ""
            currentMsgs.filterNot { msg ->
                msg.content.startsWith("REACTION|||") &&
                msg.content.split("|||").getOrNull(3) == targetPath &&
                msg.userId == userId
            } + localMsg
        } else {
            currentMsgs + localMsg
        }
        _palMessages.value = _palMessages.value + (palCode to updatedMsgs)

        viewModelScope.launch(Dispatchers.IO) {
            val result = chatRepository.postMessage(localMsg.copy(id = null))
            if (result.isSuccess) {
                refreshMessages(palCode)
            }
        }
    }

    fun handleMessageRealtimeAction(action: PostgresAction, activePalCode: String?) {
        viewModelScope.launch {
            try {
                globalSyncMutex.withLock {
                    when (action) {
                        is PostgresAction.Insert, is PostgresAction.Delete -> {
                            val record = when (action) {
                                is PostgresAction.Insert -> action.record
                                is PostgresAction.Delete -> action.oldRecord
                                else -> null
                            }
                            val eventPalCode = record?.get("pal_code")?.jsonPrimitive?.content
                            if (eventPalCode != null && eventPalCode == activePalCode) {
                                refreshMessages(eventPalCode)
                            } else if (action is PostgresAction.Delete && activePalCode != null) {
                                refreshMessages(activePalCode)
                            }
                        }
                        else -> {
                            // Suppressed
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                cacheSubmissionRotations(application, state.submissions)
                _activePalState.value = state
            }
        }
    }
}

private fun getInitialCachedPals(application: android.app.Application): List<PalItem> {
    try {
        val sharedPrefs = getVlogPrefs(application)
        val saved = sharedPrefs.getString("created_pals", "") ?: ""
        if (saved.isEmpty()) {
            return listOf(PalItem(name = "vlog", size = "12", code = "vlog", isVlog = true))
        }
        return saved.split(";;;").mapNotNull { s ->
            val parts = s.split(":")
            if (parts.size < 3) null else {
                PalItem(
                    name = parts[0].replace("\\:", ":"),
                    size = parts.getOrNull(1) ?: "4",
                    code = parts.getOrNull(2) ?: "",
                    isVlog = parts.getOrNull(3)?.toBoolean() ?: false,
                    isCreator = parts.getOrNull(4)?.toBoolean() ?: false
                )
            }
        }
    } catch (e: Exception) {
        return listOf(PalItem(name = "vlog", size = "12", code = "vlog", isVlog = true))
    }
}
