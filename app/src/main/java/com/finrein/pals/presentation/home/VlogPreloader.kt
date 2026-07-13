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
            var resolved = localPath ?: path
            if (resolved.startsWith("http")) {
                if (resolved.contains("/PALS/", ignoreCase = true)) resolved = resolved.replace("/PALS/", "/pals/", ignoreCase = true)
                if (resolved.contains("/PALS_VLOGS/", ignoreCase = true)) resolved = resolved.replace("/PALS_VLOGS/", "/pals_vlogs/", ignoreCase = true)
                if (resolved.contains("/AVATARS/", ignoreCase = true)) resolved = resolved.replace("/AVATARS/", "/avatars/", ignoreCase = true)
            }
            pathCache[path] = resolved
        }
    }

    fun getResolvedPath(path: String): String = pathCache[path] ?: path
}
