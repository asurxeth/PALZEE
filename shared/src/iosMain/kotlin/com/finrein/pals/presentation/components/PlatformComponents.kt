package com.finrein.pals.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import platform.AVFoundation.AVPlayer
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSURL

@Composable
actual fun PlatformVideoPlayer(
    modifier: Modifier,
    videoUrl: String,
    isMuted: Boolean,
    onVideoEnded: () -> Unit
) {
    val nsUrl = NSURL.URLWithString(videoUrl) ?: return
    val player = AVPlayer.playerWithURL(nsUrl).apply {
        muted = isMuted
    }
    player.play()

    UIKitView(
        factory = {
            val controller = AVPlayerViewController().apply {
                this.player = player
                this.showsPlaybackControls = false
            }
            controller.view
        },
        modifier = modifier
    )
}

@Composable
actual fun PlatformCameraCapture(
    modifier: Modifier,
    isRecording: Boolean,
    onRecordingChange: (Boolean) -> Unit,
    onCaptureSuccess: (String) -> Unit,
    flashMode: String,
    zoomRatio: Float
) {
    // In iOS actual, we can render a simple placeholder or integrate with AVFoundation.
}
