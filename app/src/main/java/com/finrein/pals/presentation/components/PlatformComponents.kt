package com.finrein.pals.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun PlatformVideoPlayer(
    modifier: Modifier,
    videoUrl: String,
    isMuted: Boolean,
    onVideoEnded: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            repeatMode = Player.REPEAT_MODE_ALL
            volume = if (isMuted) 0f else 1f
            prepare()
            playWhenReady = true
        }
    }
    
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                player = exoPlayer
            }
        },
        modifier = modifier
    )
}

@Composable
fun PlatformCameraCapture(
    modifier: Modifier,
    isRecording: Boolean,
    onRecordingChange: (Boolean) -> Unit,
    onCaptureSuccess: (String) -> Unit,
    flashMode: String,
    zoomRatio: Float
) {
    // In Android actual, we can render a simple placeholder or integrate with CameraX.
    // When we move the camera view to commonMain, we will implement this fully.
}
