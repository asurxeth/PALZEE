package com.finrein.pals.presentation.home

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.DefaultLoadControl

object DualEnginePlayerFactory {

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
            // Prioritize reliable software fallbacks to handle massive concurrent playback instances smoothly
            return decoders.sortedWith(compareByDescending { info ->
                val name = info.name.lowercase()
                name.contains("c2.android") || name.contains("omx.google")
            })
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun createMultiStreamSoftwarePlayer(context: Context): ExoPlayer {
        val renderersFactory = DefaultRenderersFactory(context.applicationContext).apply {
            setMediaCodecSelector(softwareMediaCodecSelector)
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            setEnableDecoderFallback(true)
        }
        
        // Instant Playback Adjustments: Drastically low values force immediate frame rendering
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                50,   // minBufferMs (Start playing almost instantly)
                200,  // maxBufferMs
                25,   // bufferForPlaybackMs
                25    // bufferForPlaybackAfterRebufferMs
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        return ExoPlayer.Builder(context.applicationContext)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(loadControl)
            .build()
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun createStandardPreviewPlayer(context: Context): ExoPlayer {
        val renderersFactory = DefaultRenderersFactory(context.applicationContext).apply {
            setMediaCodecSelector(MediaCodecSelector.DEFAULT)
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            setEnableDecoderFallback(true)
        }
        // Standard Preview load control settings
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(100, 500, 50, 50)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        return ExoPlayer.Builder(context.applicationContext)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(loadControl)
            .build()
    }
}
