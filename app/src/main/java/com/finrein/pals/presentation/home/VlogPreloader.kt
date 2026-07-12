package com.finrein.pals.presentation.home

import android.content.Context
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

object VlogPreloader {
    // Shared memory cache for resolved paths
    private val pathCache = ConcurrentHashMap<String, String>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun preload(context: Context, path: String) {
        if (pathCache.containsKey(path)) return
        
        scope.launch {
            val prefs = getVlogPrefs(context)
            val localPath = prefs.getString("local_path_$path", null)
            val resolved = localPath ?: path
            pathCache[path] = resolved
        }
    }

    fun getResolvedPath(path: String): String = pathCache[path] ?: path
}
