package com.finrein.pals.core.player

import android.content.Context
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import com.finrein.pals.feature.home.getVlogPrefs
import com.finrein.pals.feature.home.getCachedVideoPathSync
import com.finrein.pals.feature.home.ensureVideoCachedLocally

object VlogPreloader {
    // Shared memory cache for resolved paths
    private val pathCache = ConcurrentHashMap<String, String>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun preload(context: Context, path: String) {
        if (pathCache.containsKey(path)) return
        
        scope.launch {
            val cached = getCachedVideoPathSync(context, path)
            if (cached != null) {
                pathCache[path] = cached
            } else if (path.startsWith("http")) {
                ensureVideoCachedLocally(context, path) { localPath ->
                    pathCache[path] = localPath
                }
            } else {
                pathCache[path] = path
            }
        }
    }

    fun getResolvedPath(path: String): String = pathCache[path] ?: path
}
