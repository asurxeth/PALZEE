package com.finrein.pals.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformVideoPlayer(
    modifier: Modifier,
    videoUrl: String,
    isMuted: Boolean = false,
    onVideoEnded: () -> Unit = {}
)

@Composable
expect fun PlatformCameraCapture(
    modifier: Modifier,
    isRecording: Boolean,
    onRecordingChange: (Boolean) -> Unit,
    onCaptureSuccess: (String) -> Unit,
    flashMode: String,
    zoomRatio: Float
)
