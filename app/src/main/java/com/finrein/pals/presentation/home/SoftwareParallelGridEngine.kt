package com.finrein.pals.presentation.home

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import java.util.concurrent.ConcurrentHashMap

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
object SoftwareParallelGridEngine {

    private val softwareMediaCodecSelector = object : MediaCodecSelector {
        override fun getDecoderInfos(
            mimeType: String,
            requiresSecureDecoder: Boolean,
            requiresTunnelingDecoder: Boolean
        ): List<MediaCodecInfo> {
            val decoders = MediaCodecSelector.DEFAULT.getDecoderInfos(
                mimeType,
                requiresSecureDecoder,
                requiresTunnelingDecoder
            )
            // Prioritize software decoders (c2.android, omx.google, google)
            return decoders.sortedWith(compareByDescending { info ->
                val name = info.name.lowercase()
                name.contains("c2.android") || name.contains("omx.google") || name.contains("google")
            })
        }
    }

    /**
     * Safely creates an ultra-lightweight software-decoded player instance
     * with completely disabled audio tracks to prevent deadlocks and memory starvation.
     */
    fun createSoftwareGridPlayer(context: Context): ExoPlayer {
        val appContext = context.applicationContext
        
        // 1. Force Software-Only Decoding with Fallback
        val renderersFactory = DefaultRenderersFactory(appContext).apply {
            setMediaCodecSelector(softwareMediaCodecSelector)
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            setEnableDecoderFallback(true)
        }

        // 2. Prevent Memory Starvation (Low-Overhead Load Control)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1500, // minBufferMs
                3000, // maxBufferMs
                500,  // bufferForPlaybackMs
                1000  // bufferForPlaybackAfterRebufferMs
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        // 3. Forced Audio Muting via Track Selector (completely disables audio decoding pipeline)
        val trackSelector = DefaultTrackSelector(appContext).apply {
            parameters = buildUponParameters()
                .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true)
                .build()
        }

        return ExoPlayer.Builder(appContext)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(loadControl)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(appContext)
                    .setDataSourceFactory(VideoCache.getCacheDataSourceFactory(appContext))
            )
            .build()
    }

    /**
     * Batch coordinator for managing parallel software players
     */
    class GridCoordinator(private val context: Context) {
        private val playersMap = ConcurrentHashMap<Int, ExoPlayer>()

        @Synchronized
        fun getOrCreatePlayer(slotIndex: Int, videoPath: String): ExoPlayer {
            // Release existing player in this slot if any
            playersMap.remove(slotIndex)?.let { oldPlayer ->
                try {
                    oldPlayer.stop()
                    oldPlayer.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val player = createSoftwareGridPlayer(context).apply {
                repeatMode = ExoPlayer.REPEAT_MODE_ALL
                volume = 0f // Extra safety mute
                
                val targetPath = if (videoPath.startsWith("http")) {
                    var res = videoPath
                    if (res.contains("/PALS/", ignoreCase = true)) res = res.replace("/PALS/", "/pals/", ignoreCase = true)
                    if (res.contains("/PALS_VLOGS/", ignoreCase = true)) res = res.replace("/PALS_VLOGS/", "/pals_vlogs/", ignoreCase = true)
                    if (res.contains("/AVATARS/", ignoreCase = true)) res = res.replace("/AVATARS/", "/avatars/", ignoreCase = true)
                    res
                } else {
                    videoPath
                }
                
                val cleanPath = when {
                    targetPath.startsWith("file://") -> targetPath.substring(7)
                    else -> targetPath
                }
                
                val mediaItem = if (targetPath.startsWith("http") || targetPath.startsWith("content://")) {
                    MediaItem.fromUri(android.net.Uri.parse(targetPath))
                } else {
                    val file = java.io.File(cleanPath)
                    if (file.exists()) {
                        MediaItem.fromUri(android.net.Uri.fromFile(file))
                    } else {
                        MediaItem.fromUri(android.net.Uri.parse(targetPath))
                    }
                }
                
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
            
            playersMap[slotIndex] = player
            return player
        }

        @Synchronized
        fun releasePlayer(slotIndex: Int) {
            playersMap.remove(slotIndex)?.let { player ->
                try {
                    player.stop()
                    player.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        @Synchronized
        fun releaseAll() {
            val keys = playersMap.keys().toList()
            for (key in keys) {
                releasePlayer(key)
            }
            playersMap.clear()
        }
    }
}
