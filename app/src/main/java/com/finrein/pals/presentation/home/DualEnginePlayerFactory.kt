package com.finrein.pals.presentation.home

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo
import androidx.media3.exoplayer.DefaultLoadControl

object DualEnginePlayerFactory {

    private val softwareMediaCodecSelector = object : MediaCodecSelector {
        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
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
            // Sort decoders to prioritize software ones (containing c2.android or omx.google)
            return decoders.sortedWith(compareByDescending { info ->
                val name = info.name.lowercase()
                name.contains("c2.android") || name.contains("omx.google") || name.contains("google")
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
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                100,  // minBufferMs
                500,  // maxBufferMs
                50,   // bufferForPlaybackMs
                50    // bufferForPlaybackAfterRebufferMs
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        return ExoPlayer.Builder(context.applicationContext)
            .setRenderersFactory(renderersFactory)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context.applicationContext)
                    .setDataSourceFactory(VideoCache.getCacheDataSourceFactory(context.applicationContext))
            )
            .setLoadControl(loadControl)
            .build()
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun createStandardPreviewPlayer(context: Context): ExoPlayer {
        val renderersFactory = DefaultRenderersFactory(context.applicationContext).apply {
            setMediaCodecSelector(MediaCodecSelector.DEFAULT)
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        }
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                100,  // minBufferMs
                500,  // maxBufferMs
                50,   // bufferForPlaybackMs
                50    // bufferForPlaybackAfterRebufferMs
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        return ExoPlayer.Builder(context.applicationContext)
            .setRenderersFactory(renderersFactory)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context.applicationContext)
                    .setDataSourceFactory(VideoCache.getCacheDataSourceFactory(context.applicationContext))
            )
            .setLoadControl(loadControl)
            .build()
    }
}
