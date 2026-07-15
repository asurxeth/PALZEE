package com.finrein.pals.core.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File
import io.github.jan.supabase.gotrue.auth

@OptIn(UnstableApi::class)
object VideoCache {
    private var simpleCache: SimpleCache? = null

    @Synchronized
    fun getCache(context: Context): SimpleCache {
        if (simpleCache == null) {
            val cacheDir = File(context.cacheDir, "exoplayer_video_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024) // 100MB cache
            val databaseProvider = StandaloneDatabaseProvider(context)
            simpleCache = SimpleCache(cacheDir, evictor, databaseProvider)
        }
        return simpleCache!!
    }

    fun getCacheDataSourceFactory(context: Context): CacheDataSource.Factory {
        val headers = mutableMapOf<String, String>()
        headers["apikey"] = com.finrein.pals.BuildConfig.SUPABASE_ANON_KEY
        try {
            val token = com.finrein.pals.PalApplication.supabase.auth.currentAccessTokenOrNull()
            if (!token.isNullOrEmpty()) {
                headers["Authorization"] = "Bearer $token"
            } else {
                headers["Authorization"] = "Bearer ${com.finrein.pals.BuildConfig.SUPABASE_ANON_KEY}"
            }
        } catch (e: Exception) {
            headers["Authorization"] = "Bearer ${com.finrein.pals.BuildConfig.SUPABASE_ANON_KEY}"
        }

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.50 Mobile Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(headers)
        val upstreamFactory = androidx.media3.datasource.DefaultDataSource.Factory(
            context,
            httpDataSourceFactory
        )
        return CacheDataSource.Factory()
            .setCache(getCache(context))
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    fun getLowLatencyLoadControl(): androidx.media3.exoplayer.LoadControl {
        return androidx.media3.exoplayer.DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                100,  // minBufferMs
                500,  // maxBufferMs
                50,   // bufferForPlaybackMs
                50    // bufferForPlaybackAfterRebufferMs
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
    }
}
