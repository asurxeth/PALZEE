package com.finrein.pals.presentation.home

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector

object DualEnginePlayerFactory {

    private val playerPool = java.util.concurrent.ConcurrentLinkedQueue<ExoPlayer>()

    private val softwareMediaCodecSelector = object : MediaCodecSelector {
        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        override fun getDecoderInfos(
            mimeType: String,
            requiresSecureDecoder: Boolean,
            requiresTunnelingDecoder: Boolean
        ): List<androidx.media3.exoplayer.mediacodec.MediaCodecInfo> {
            val decoders = MediaCodecSelector.DEFAULT.getDecoderInfos(
                mimeType, requiresSecureDecoder, requiresTunnelingDecoder
            )
            return decoders.sortedWith(compareByDescending { info ->
                val name = info.name.lowercase()
                !name.contains("c2.android") && !name.contains("omx.google")
            })
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getPooledInstance(context: Context): ExoPlayer {
        return playerPool.poll() ?: ExoPlayer.Builder(context.applicationContext).apply {
            val renderersFactory = DefaultRenderersFactory(context.applicationContext).apply {
                setMediaCodecSelector(softwareMediaCodecSelector)
                setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                setEnableDecoderFallback(true)
            }
            setRenderersFactory(renderersFactory)
            
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(100, 500, 100, 100)
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()
            setLoadControl(loadControl)
        }.build()
    }

    fun releaseIntoPool(player: ExoPlayer) {
        try {
            player.stop()
            player.clearMediaItems()
            if (playerPool.size < 10) {
                playerPool.offer(player)
            } else {
                player.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try { player.release() } catch (ex: Exception) {}
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun createMultiStreamSoftwarePlayer(context: Context): ExoPlayer = getPooledInstance(context)

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun createStandardPreviewPlayer(context: Context): ExoPlayer = getPooledInstance(context)
}
