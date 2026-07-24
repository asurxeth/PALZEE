package com.finrein.pals.core.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.media.*
import android.opengl.*
import android.util.Log
import android.view.Surface
import androidx.core.content.res.ResourcesCompat
import com.finrein.pals.R
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class VideoProcessor {
    companion object {
        private const val TAG = "VideoProcessor"
        private const val TIMEOUT_USEC = 10000L

        fun processVideo(
            context: Context,
            inputPath: String,
            outputPath: String,
            vlogText: String,
            timeText: String,
            captionText: String,
            roundedCorners: Boolean,
            exportBackground: String = "black",
            isMuted: Boolean = false,
            callback: (Boolean) -> Unit
        ) {
            processVideoList(
                context = context,
                inputPaths = listOf(inputPath),
                outputPath = outputPath,
                vlogTexts = listOf(vlogText),
                timeTexts = listOf(timeText),
                captionTexts = listOf(captionText),
                roundedCorners = roundedCorners,
                exportBackground = exportBackground,
                isMutedList = listOf(isMuted),
                callback = callback
            )
        }

        fun processVideoList(
            context: Context,
            inputPaths: List<String>,
            outputPath: String,
            vlogTexts: List<String>,
            timeTexts: List<String>,
            captionTexts: List<String>,
            roundedCorners: Boolean,
            exportBackground: String = "black",
            isMutedList: List<Boolean>? = null,
            callback: (Boolean) -> Unit
        ) {
            Thread {
                var success = false
                try {
                    success = transcodeList(
                        context = context,
                        inputPaths = inputPaths,
                        outputPath = outputPath,
                        vlogTexts = vlogTexts,
                        timeTexts = timeTexts,
                        captionTexts = captionTexts,
                        roundedCorners = roundedCorners,
                        exportBackground = exportBackground,
                        isMutedList = isMutedList
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error transcoding video list", e)
                }
                callback(success)
            }.start()
        }

        // We rotate the vertex positions in screen-space (geometry rotation)
        // instead of texture coordinates, so that uSTMatrix's vertical flip and crop
        // is always applied to the standard texture coordinate orientation first.
        private fun getVerticesData(rotation: Int): FloatArray {
            return when (rotation) {
                90 -> floatArrayOf(
                    -1.0f,  1.0f, 0.0f, 0.0f, 0.0f, // BL vertex maps to Top-Left in screen space
                    -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // BR vertex maps to Bottom-Left in screen space
                     1.0f,  1.0f, 0.0f, 0.0f, 1.0f, // TL vertex maps to Top-Right in screen space
                     1.0f, -1.0f, 0.0f, 1.0f, 1.0f  // TR vertex maps to Bottom-Right in screen space
                )
                180 -> floatArrayOf(
                     1.0f,  1.0f, 0.0f, 0.0f, 0.0f, // BL vertex maps to Top-Right in screen space
                    -1.0f,  1.0f, 0.0f, 1.0f, 0.0f, // BR vertex maps to Top-Left in screen space
                     1.0f, -1.0f, 0.0f, 0.0f, 1.0f, // TL vertex maps to Bottom-Right in screen space
                    -1.0f, -1.0f, 0.0f, 1.0f, 1.0f  // TR vertex maps to Bottom-Left in screen space
                )
                270 -> floatArrayOf(
                     1.0f, -1.0f, 0.0f, 0.0f, 0.0f, // BL vertex maps to Bottom-Right in screen space
                      1.0f,  1.0f, 0.0f, 1.0f, 0.0f, // BR vertex maps to Top-Right in screen space
                    -1.0f, -1.0f, 0.0f, 0.0f, 1.0f, // TL vertex maps to Bottom-Left in screen space
                    -1.0f,  1.0f, 0.0f, 1.0f, 1.0f  // TR vertex maps to Top-Left in screen space
                )
                else -> floatArrayOf(
                    -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                     1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                    -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,
                     1.0f,  1.0f, 0.0f, 1.0f, 1.0f
                )
            }
        }

        private fun transcodeList(
            context: Context,
            inputPaths: List<String>,
            outputPath: String,
            vlogTexts: List<String>,
            timeTexts: List<String>,
            captionTexts: List<String>,
            roundedCorners: Boolean,
            exportBackground: String = "black",
            isMutedList: List<Boolean>? = null
        ): Boolean {
            if (inputPaths.isEmpty()) {
                Log.e(TAG, "Input paths list is empty")
                return false
            }

            // Filter valid inputs
            val validInputs = inputPaths.indices.mapNotNull { i ->
                val path = inputPaths[i]
                if (path == "EMPTY_BOX") {
                    Triple("EMPTY_BOX", i, File(""))
                } else {
                    val cleanPath = when {
                        path.startsWith("file://") -> path.substring(7)
                        else -> path
                    }
                    val file = File(cleanPath)
                    if (file.exists() && file.length() > 0) {
                        Triple(cleanPath, i, file)
                    } else {
                        null
                    }
                }
            }

            if (validInputs.isEmpty()) {
                Log.e(TAG, "No valid input files found for transcoding")
                return false
            }

            // Set up EGL/Encoder using the first video's format details
            var firstVideoFormat: MediaFormat? = null
            val firstRealInput = validInputs.firstOrNull { it.first != "EMPTY_BOX" }
            if (firstRealInput != null) {
                val tempExtractor = MediaExtractor()
                try {
                    tempExtractor.setDataSource(firstRealInput.first)
                    for (i in 0 until tempExtractor.trackCount) {
                        val format = tempExtractor.getTrackFormat(i)
                        val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                        if (mime.startsWith("video/")) {
                            firstVideoFormat = format
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading first video track format", e)
                } finally {
                    tempExtractor.release()
                }
            }

            if (firstVideoFormat == null) {
                firstVideoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280)
            }

            // High quality 9:16 target size (720x1280)
            val outputWidth = 720
            val outputHeight = 1280
            val outVideoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, outputWidth, outputHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, 10000000) // 10 Mbps for ultra-crisp high quality
                setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            val videoEncoder = try {
                MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create video encoder", e)
                return false
            }
            videoEncoder.configure(outVideoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = videoEncoder.createInputSurface()
            videoEncoder.start()

            // Setup EGL & OpenGL Context
            val eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val version = IntArray(2)
            EGL14.eglInitialize(eglDisplay, version, 0, version, 1)

            val attribList = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGLExt.EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, configs.size, numConfigs, 0)
            val eglConfig = configs[0]

            val ctxAttribList = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )
            val eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, ctxAttribList, 0)

            val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
            val eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, inputSurface, surfaceAttribs, 0)
            EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)

            // Compile Shaders
            val renderProgram = createProgram()
            val overlayProgram = createOverlayProgram()

            // Setup Texture and SurfaceTexture for decoding
            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            val texID = textures[0]
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texID)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            val surfaceTexture = android.graphics.SurfaceTexture(texID)
            val decoderSurface = Surface(surfaceTexture)

            var frameAvailable = false
            surfaceTexture.setOnFrameAvailableListener {
                synchronized(surfaceTexture) {
                    frameAvailable = true
                }
            }

            // Create temporary overlay texture ID
            val overlayTextures = IntArray(1)
            GLES20.glGenTextures(1, overlayTextures, 0)
            val overlayTextureId = overlayTextures[0]
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, overlayTextureId)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            // Vertex Buffer Allocation
            val triangleVertices = ByteBuffer.allocateDirect(20 * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()

            val stMatrix = FloatArray(16)
            android.opengl.Matrix.setIdentityM(stMatrix, 0)

            val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var muxerStarted = false
            var videoOutputTrack = -1
            var audioOutputTrack = -1

            val videoBufferInfo = MediaCodec.BufferInfo()
            var encoderOutputDone = false

            var videoPtsOffsetUs = 0L

            // Process video tracks sequentially
            for (vIndex in validInputs.indices) {
                val (inputPath, originalIndex, _) = validInputs[vIndex]
                val vlogText = vlogTexts.getOrNull(originalIndex) ?: ""
                val timeText = timeTexts.getOrNull(originalIndex) ?: ""
                val captionText = captionTexts.getOrNull(originalIndex) ?: ""

                if (inputPath == "EMPTY_BOX") {
                    try {
                        val overlayBitmap = generateOverlayBitmap(context, outputWidth, outputHeight, vlogText, timeText, captionText, exportBackground)
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, overlayTextureId)
                        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, overlayBitmap, 0)
                        overlayBitmap.recycle()

                        val frameDurationUs = 33333L // 30 fps
                        val numFrames = 60 // 2 seconds
                        val currentVideoPtsOffsetUs = videoPtsOffsetUs
                        var fileMaxVideoPtsUs = 0L

                        for (f in 0 until numFrames) {
                            val ptsUs = f * frameDurationUs
                            val renderPtsUs = currentVideoPtsOffsetUs + ptsUs
                            fileMaxVideoPtsUs = maxOf(fileMaxVideoPtsUs, ptsUs)

                            if (exportBackground == "white") {
                                GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
                            } else {
                                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
                            }
                            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

                            GLES20.glViewport(0, 0, outputWidth, outputHeight)
                            GLES20.glUseProgram(overlayProgram)
                            val moPositionHandle = GLES20.glGetAttribLocation(overlayProgram, "aPosition")
                            val moTextureHandle = GLES20.glGetAttribLocation(overlayProgram, "aTextureCoord")

                            val overlayVertices = getVerticesData(0)
                            val overlayVerticesBuffer = ByteBuffer.allocateDirect(overlayVertices.size * 4)
                                .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                                    put(overlayVertices).position(0)
                                }

                            overlayVerticesBuffer.position(0)
                            GLES20.glVertexAttribPointer(moPositionHandle, 3, GLES20.GL_FLOAT, false, 20, overlayVerticesBuffer)
                            GLES20.glEnableVertexAttribArray(moPositionHandle)

                            overlayVerticesBuffer.position(3)
                            GLES20.glVertexAttribPointer(moTextureHandle, 2, GLES20.GL_FLOAT, false, 20, overlayVerticesBuffer)
                            GLES20.glEnableVertexAttribArray(moTextureHandle)

                            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
                            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, overlayTextureId)
                            GLES20.glUniform1i(GLES20.glGetUniformLocation(overlayProgram, "sTexture"), 1)

                            GLES20.glEnable(GLES20.GL_BLEND)
                            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

                            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
                            GLES20.glDisable(GLES20.GL_BLEND)

                            EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, renderPtsUs * 1000)
                            EGL14.eglSwapBuffers(eglDisplay, eglSurface)

                            var outEncoderIndex = videoEncoder.dequeueOutputBuffer(videoBufferInfo, 0)
                            while (outEncoderIndex >= 0) {
                                val encodedData = videoEncoder.getOutputBuffer(outEncoderIndex)
                                if (encodedData != null) {
                                    if ((videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                        videoBufferInfo.size = 0
                                    }
                                    if (videoBufferInfo.size != 0) {
                                        if (!muxerStarted) {
                                            videoOutputTrack = muxer.addTrack(videoEncoder.outputFormat)
                                            muxer.start()
                                            muxerStarted = true
                                        }
                                        encodedData.position(videoBufferInfo.offset)
                                        encodedData.limit(videoBufferInfo.offset + videoBufferInfo.size)
                                        muxer.writeSampleData(videoOutputTrack, encodedData, videoBufferInfo)
                                    }
                                }
                                videoEncoder.releaseOutputBuffer(outEncoderIndex, false)
                                outEncoderIndex = videoEncoder.dequeueOutputBuffer(videoBufferInfo, 0)
                            }
                        }

                        videoPtsOffsetUs += fileMaxVideoPtsUs + 33000L
                    } catch (e: Exception) {
                        Log.e(TAG, "Error generating empty box segment", e)
                    }
                    continue
                }

                var videoExtractor: MediaExtractor? = null
                var videoDecoder: MediaCodec? = null

                try {
                    videoExtractor = MediaExtractor().apply { setDataSource(inputPath) }
                    var videoInputTrack = -1
                    for (i in 0 until videoExtractor.trackCount) {
                        val format = videoExtractor.getTrackFormat(i)
                        val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                        if (mime.startsWith("video/")) {
                            videoInputTrack = i
                            break
                        }
                    }

                    if (videoInputTrack < 0) {
                        continue
                    }

                    videoExtractor.selectTrack(videoInputTrack)
                    val videoFormat = videoExtractor.getTrackFormat(videoInputTrack)

                    val videoWidth = videoFormat.getInteger(MediaFormat.KEY_WIDTH)
                    val videoHeight = videoFormat.getInteger(MediaFormat.KEY_HEIGHT)

                    // CRITICAL: Set default buffer size to avoid zoom!
                    surfaceTexture.setDefaultBufferSize(videoWidth, videoHeight)

                    // Compute total rotation exactly matching the UI's display logic
                    val fileRotation = if (videoFormat.containsKey(MediaFormat.KEY_ROTATION)) {
                        videoFormat.getInteger(MediaFormat.KEY_ROTATION)
                    } else {
                        0
                    }
                    val rotatedWidth = if (fileRotation == 90 || fileRotation == 270) videoHeight else videoWidth
                    val rotatedHeight = if (fileRotation == 90 || fileRotation == 270) videoWidth else videoHeight

                    val uiRotation = if (rotatedHeight > rotatedWidth) 270 else 0
                    val totalRotation = uiRotation

                    val verticesData = getVerticesData(totalRotation)
                    triangleVertices.clear()
                    triangleVertices.put(verticesData).position(0)

                    videoDecoder = MediaCodec.createDecoderByType(videoFormat.getString(MediaFormat.KEY_MIME) ?: "")
                    videoDecoder.configure(videoFormat, decoderSurface, null, 0)
                    videoDecoder.start()

                    // Generate Overlay Bitmap for this clip
                    val overlayBitmap = generateOverlayBitmap(context, outputWidth, outputHeight, vlogText, timeText, captionText, exportBackground)
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, overlayTextureId)
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, overlayBitmap, 0)
                    overlayBitmap.recycle()

                    val currentVideoPtsOffsetUs = videoPtsOffsetUs
                    var fileMaxVideoPtsUs = 0L

                    var decoderInputDone = false
                    var decoderOutputDone = false

                    while (!decoderOutputDone) {
                        // Feed decoder
                        if (!decoderInputDone) {
                            val inputBufIndex = videoDecoder.dequeueInputBuffer(TIMEOUT_USEC)
                            if (inputBufIndex >= 0) {
                                val inputBuf = videoDecoder.getInputBuffer(inputBufIndex)
                                if (inputBuf != null) {
                                    val sampleSize = videoExtractor.readSampleData(inputBuf, 0)
                                    if (sampleSize < 0) {
                                        videoDecoder.queueInputBuffer(inputBufIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                        decoderInputDone = true
                                    } else {
                                        val presentationTimeUs = videoExtractor.sampleTime
                                        videoDecoder.queueInputBuffer(inputBufIndex, 0, sampleSize, presentationTimeUs, 0)
                                        videoExtractor.advance()
                                    }
                                }
                            }
                        }

                        // Drain decoder & render
                        val decoderBufferInfo = MediaCodec.BufferInfo()
                        val outBufIndex = videoDecoder.dequeueOutputBuffer(decoderBufferInfo, TIMEOUT_USEC)
                        if (outBufIndex >= 0) {
                            val render = decoderBufferInfo.size != 0
                            videoDecoder.releaseOutputBuffer(outBufIndex, render)
                            if (render) {
                                synchronized(surfaceTexture) {
                                    while (!frameAvailable) {
                                        try {
                                            (surfaceTexture as Object).wait(100)
                                        } catch (ie: InterruptedException) {}
                                    }
                                    frameAvailable = false
                                }

                                surfaceTexture.updateTexImage()
                                surfaceTexture.getTransformMatrix(stMatrix)

                                // 1. Draw centered 16:9 video frame
                                if (exportBackground == "white") {
                                    GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
                                } else {
                                    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
                                }
                                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

                                // Viewport is centered 16:9 box inside 720x1280
                                val boxWidth = outputWidth
                                val boxHeight = (outputWidth * (9f / 16f)).toInt()
                                val boxY = (outputHeight - boxHeight) / 2
                                GLES20.glViewport(0, boxY, boxWidth, boxHeight)

                                GLES20.glUseProgram(renderProgram)
                                val muSTMatrixHandle = GLES20.glGetUniformLocation(renderProgram, "uSTMatrix")
                                val maPositionHandle = GLES20.glGetAttribLocation(renderProgram, "aPosition")
                                val maTextureHandle = GLES20.glGetAttribLocation(renderProgram, "aTextureCoord")
                                val muUseRoundedHandle = GLES20.glGetUniformLocation(renderProgram, "uUseRounded")
                                val muSizeHandle = GLES20.glGetUniformLocation(renderProgram, "uSize")
                                val muRadiusHandle = GLES20.glGetUniformLocation(renderProgram, "uRadius")

                                GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, stMatrix, 0)
                                GLES20.glUniform1f(muUseRoundedHandle, if (roundedCorners) 1.0f else 0.0f)
                                GLES20.glUniform2f(muSizeHandle, boxWidth.toFloat(), boxHeight.toFloat())
                                GLES20.glUniform1f(muRadiusHandle, 65.8f * (outputWidth / 720f))

                                triangleVertices.position(0)
                                GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 20, triangleVertices)
                                GLES20.glEnableVertexAttribArray(maPositionHandle)

                                triangleVertices.position(3)
                                GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, 20, triangleVertices)
                                GLES20.glEnableVertexAttribArray(maTextureHandle)

                                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texID)

                                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

                                // 2. Draw Text Overlay on top (non-rotated)
                                GLES20.glViewport(0, 0, outputWidth, outputHeight)
                                GLES20.glUseProgram(overlayProgram)
                                val moPositionHandle = GLES20.glGetAttribLocation(overlayProgram, "aPosition")
                                val moTextureHandle = GLES20.glGetAttribLocation(overlayProgram, "aTextureCoord")

                                val overlayVertices = getVerticesData(0)
                                val overlayVerticesBuffer = ByteBuffer.allocateDirect(overlayVertices.size * 4)
                                    .order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
                                        put(overlayVertices).position(0)
                                    }

                                overlayVerticesBuffer.position(0)
                                GLES20.glVertexAttribPointer(moPositionHandle, 3, GLES20.GL_FLOAT, false, 20, overlayVerticesBuffer)
                                GLES20.glEnableVertexAttribArray(moPositionHandle)

                                overlayVerticesBuffer.position(3)
                                GLES20.glVertexAttribPointer(moTextureHandle, 2, GLES20.GL_FLOAT, false, 20, overlayVerticesBuffer)
                                GLES20.glEnableVertexAttribArray(moTextureHandle)

                                GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
                                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, overlayTextureId)
                                GLES20.glUniform1i(GLES20.glGetUniformLocation(overlayProgram, "sTexture"), 1)

                                GLES20.glEnable(GLES20.GL_BLEND)
                                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

                                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
                                GLES20.glDisable(GLES20.GL_BLEND)

                                val renderPtsUs = currentVideoPtsOffsetUs + decoderBufferInfo.presentationTimeUs
                                fileMaxVideoPtsUs = maxOf(fileMaxVideoPtsUs, decoderBufferInfo.presentationTimeUs)

                                EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, renderPtsUs * 1000)
                                EGL14.eglSwapBuffers(eglDisplay, eglSurface)
                            }

                            if ((decoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                decoderOutputDone = true
                            }
                        }

                        // Drain encoder
                        var outEncoderIndex = videoEncoder.dequeueOutputBuffer(videoBufferInfo, 0)
                        while (outEncoderIndex >= 0) {
                            val encodedData = videoEncoder.getOutputBuffer(outEncoderIndex)
                            if (encodedData != null) {
                                if ((videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                    videoBufferInfo.size = 0
                                }
                                if (videoBufferInfo.size != 0) {
                                    if (!muxerStarted) {
                                        videoOutputTrack = muxer.addTrack(videoEncoder.outputFormat)
                                        // Detect if audio track needs to be created
                                        var firstAudioFormat: MediaFormat? = null
                                        for (idx in validInputs.indices) {
                                            if (isMutedList != null && isMutedList.getOrNull(idx) == true) {
                                                continue
                                            }
                                            val input = validInputs[idx]
                                            val ext = MediaExtractor().apply { setDataSource(input.first) }
                                            for (j in 0 until ext.trackCount) {
                                                val fmt = ext.getTrackFormat(j)
                                                if (fmt.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                                                    firstAudioFormat = fmt
                                                    break
                                                }
                                            }
                                            ext.release()
                                            if (firstAudioFormat != null) break
                                        }
                                        if (firstAudioFormat != null) {
                                            audioOutputTrack = muxer.addTrack(firstAudioFormat)
                                        }
                                        muxer.start()
                                        muxerStarted = true
                                    }
                                    encodedData.position(videoBufferInfo.offset)
                                    encodedData.limit(videoBufferInfo.offset + videoBufferInfo.size)
                                    muxer.writeSampleData(videoOutputTrack, encodedData, videoBufferInfo)
                                }
                            }
                            videoEncoder.releaseOutputBuffer(outEncoderIndex, false)
                            outEncoderIndex = videoEncoder.dequeueOutputBuffer(videoBufferInfo, 0)
                        }
                    }

                    videoPtsOffsetUs += fileMaxVideoPtsUs + 33000L
                } catch (e: Exception) {
                    Log.e(TAG, "Error transcoding video file segment: $inputPath", e)
                } finally {
                    try { videoDecoder?.stop() } catch(e: Exception) {}
                    try { videoDecoder?.release() } catch(e: Exception) {}
                    try { videoExtractor?.release() } catch(e: Exception) {}
                }
            }

            // End of stream for encoder
            try {
                videoEncoder.signalEndOfInputStream()
            } catch (e: Exception) {}

            while (!encoderOutputDone) {
                val outEncoderIndex = videoEncoder.dequeueOutputBuffer(videoBufferInfo, TIMEOUT_USEC)
                if (outEncoderIndex >= 0) {
                    val encodedData = videoEncoder.getOutputBuffer(outEncoderIndex)
                    if (encodedData != null) {
                        if ((videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            videoBufferInfo.size = 0
                        }
                        if (videoBufferInfo.size != 0) {
                            if (!muxerStarted) {
                                videoOutputTrack = muxer.addTrack(videoEncoder.outputFormat)
                                muxer.start()
                                muxerStarted = true
                            }
                            encodedData.position(videoBufferInfo.offset)
                            encodedData.limit(videoBufferInfo.offset + videoBufferInfo.size)
                            muxer.writeSampleData(videoOutputTrack, encodedData, videoBufferInfo)
                        }
                    }
                    videoEncoder.releaseOutputBuffer(outEncoderIndex, false)
                    if ((videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        encoderOutputDone = true
                    }
                }
            }

            // Mux audio tracks sequentially
            if (audioOutputTrack >= 0 && muxerStarted) {
                var audioPtsOffsetUs = 0L
                val maxBufferSize = 256 * 1024
                val audioBuffer = ByteBuffer.allocateDirect(maxBufferSize)
                val audioBufferInfo = MediaCodec.BufferInfo()

                for (vIndex in validInputs.indices) {
                    val (inputPath, _, _) = validInputs[vIndex]
                    if (inputPath == "EMPTY_BOX") {
                        audioPtsOffsetUs += 2000000L + 33000L
                        continue
                    }
                    if (isMutedList != null && isMutedList.getOrNull(vIndex) == true) {
                        val durationUs = try {
                            val retriever = MediaMetadataRetriever()
                            retriever.setDataSource(inputPath)
                            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 2000L
                            retriever.release()
                            durationMs * 1000L
                        } catch (e: Exception) {
                            2000000L
                        }
                        audioPtsOffsetUs += durationUs + 33000L
                        continue
                    }
                    val audioExtractor = MediaExtractor()
                    try {
                        audioExtractor.setDataSource(inputPath)
                        var audioInputTrack = -1
                        for (i in 0 until audioExtractor.trackCount) {
                            val format = audioExtractor.getTrackFormat(i)
                            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                            if (mime.startsWith("audio/")) {
                                audioInputTrack = i
                                break
                            }
                        }

                        if (audioInputTrack >= 0) {
                            audioExtractor.selectTrack(audioInputTrack)
                            var fileMaxAudioPtsUs = 0L

                            while (true) {
                                val sampleSize = audioExtractor.readSampleData(audioBuffer, 0)
                                if (sampleSize < 0) break

                                val presentationTimeUs = audioExtractor.sampleTime
                                audioBufferInfo.offset = 0
                                audioBufferInfo.size = sampleSize
                                audioBufferInfo.flags = audioExtractor.sampleFlags
                                audioBufferInfo.presentationTimeUs = audioPtsOffsetUs + presentationTimeUs

                                fileMaxAudioPtsUs = maxOf(fileMaxAudioPtsUs, presentationTimeUs)

                                muxer.writeSampleData(audioOutputTrack, audioBuffer, audioBufferInfo)
                                audioExtractor.advance()
                            }
                            audioPtsOffsetUs += fileMaxAudioPtsUs + 33000L
                        } else {
                            val durationUs = try {
                                val retriever = MediaMetadataRetriever()
                                retriever.setDataSource(inputPath)
                                val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 2000L
                                retriever.release()
                                durationMs * 1000L
                            } catch (e: Exception) {
                                2000000L
                            }
                            audioPtsOffsetUs += durationUs + 33000L
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error copying audio track for segment: $inputPath", e)
                    } finally {
                        audioExtractor.release()
                    }
                }
            }

            // Cleanup encoder EGL resources
            try {
                videoEncoder.stop()
            } catch(e: Exception) {}
            try {
                videoEncoder.release()
            } catch(e: Exception) {}
            try {
                if (muxerStarted) {
                    muxer.stop()
                }
            } catch(e: Exception) {}
            try {
                muxer.release()
            } catch(e: Exception) {}

            EGL14.eglDestroySurface(eglDisplay, eglSurface)
            EGL14.eglDestroyContext(eglDisplay, eglContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(eglDisplay)

            return true
        }

        private fun createProgram(): Int {
            val vertexShaderCode = """
                uniform mat4 uSTMatrix;
                attribute vec4 aPosition;
                attribute vec4 aTextureCoord;
                varying vec2 vTextureCoord;
                void main() {
                    gl_Position = aPosition;
                    vTextureCoord = (uSTMatrix * aTextureCoord).xy;
                }
            """.trimIndent()

            val fragmentShaderCode = """
                #extension GL_OES_EGL_image_external : require
                precision mediump float;
                varying vec2 vTextureCoord;
                uniform samplerExternalOES sTexture;
                uniform float uUseRounded;
                uniform vec2 uSize;
                uniform float uRadius;
                void main() {
                    vec4 color = texture2D(sTexture, vTextureCoord);
                    if (uUseRounded > 0.5) {
                        vec2 pos = vTextureCoord * uSize;
                        float r = uRadius;
                        vec2 d = vec2(0.0);
                        if (pos.x < r && pos.y < r) {
                            d = pos - vec2(r, r);
                        } else if (pos.x > uSize.x - r && pos.y < r) {
                            d = pos - vec2(uSize.x - r, r);
                        } else if (pos.x < r && pos.y > uSize.y - r) {
                            d = pos - vec2(r, uSize.y - r);
                        } else if (pos.x > uSize.x - r && pos.y > uSize.y - r) {
                            d = pos - vec2(uSize.x - r, uSize.y - r);
                        }
                        if (length(d) > r) {
                            gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
                            return;
                        }
                    }
                    gl_FragColor = color;
                }
            """.trimIndent()

            val vs = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER).apply {
                GLES20.glShaderSource(this, vertexShaderCode)
                GLES20.glCompileShader(this)
            }
            val fs = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER).apply {
                GLES20.glShaderSource(this, fragmentShaderCode)
                GLES20.glCompileShader(this)
            }
            return GLES20.glCreateProgram().apply {
                GLES20.glAttachShader(this, vs)
                GLES20.glAttachShader(this, fs)
                GLES20.glLinkProgram(this)
            }
        }

        private fun createOverlayProgram(): Int {
            val vertexShaderCode = """
                attribute vec4 aPosition;
                attribute vec4 aTextureCoord;
                varying vec2 vTextureCoord;
                void main() {
                    gl_Position = aPosition;
                    vTextureCoord = aTextureCoord.xy;
                }
            """.trimIndent()

            val fragmentShaderCode = """
                precision mediump float;
                varying vec2 vTextureCoord;
                uniform sampler2D sTexture;
                void main() {
                    gl_FragColor = texture2D(sTexture, vec2(vTextureCoord.x, 1.0 - vTextureCoord.y));
                }
            """.trimIndent()

            val vs = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER).apply {
                GLES20.glShaderSource(this, vertexShaderCode)
                GLES20.glCompileShader(this)
            }
            val fs = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER).apply {
                GLES20.glShaderSource(this, fragmentShaderCode)
                GLES20.glCompileShader(this)
            }
            return GLES20.glCreateProgram().apply {
                GLES20.glAttachShader(this, vs)
                GLES20.glAttachShader(this, fs)
                GLES20.glLinkProgram(this)
            }
        }

        private fun generateOverlayBitmap(
            context: Context,
            width: Int,
            height: Int,
            vlogText: String,
            timeText: String,
            captionText: String,
            exportBackground: String = "black"
        ): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.TRANSPARENT)

            // Setup Fonts
            val bricolageFont = try {
                ResourcesCompat.getFont(context, R.font.bricolage_grotesque_variable)
            } catch (e: Exception) {
                Typeface.DEFAULT_BOLD
            }
            val robotoFont = try {
                ResourcesCompat.getFont(context, R.font.roboto_medium_numbers)
            } catch (e: Exception) {
                Typeface.DEFAULT
            }
            val delaFont = try {
                ResourcesCompat.getFont(context, R.font.dela_gothic_one_regular)
            } catch (e: Exception) {
                Typeface.DEFAULT_BOLD
            }

            val scale = width / 306f
            val centerY = height / 2f

            val overlayTextColor = if (exportBackground == "white") Color.BLACK else Color.WHITE
            val shadowColor = if (exportBackground == "white") Color.argb(32, 0, 0, 0) else Color.argb(128, 0, 0, 0)

            // Draw "vlog" watermark left-aligned inside video box
            if (vlogText.isNotEmpty() && vlogText != "EMPTY_BOX_MISSED") {
                val paint = Paint().apply {
                    color = overlayTextColor
                    textSize = 22f * scale
                    typeface = bricolageFont
                    isAntiAlias = true
                    setShadowLayer(3f, 1f, 1f, shadowColor)
                }
                canvas.drawText(
                    vlogText,
                    5.5f * scale,
                    centerY + (paint.textSize / 3f),
                    paint
                )
            }

            // Draw capture time or empty box missed text
            if (vlogText == "EMPTY_BOX_MISSED") {
                // Scheduled time text centered: Dela Gothic One
                val timePaint = Paint().apply {
                    color = overlayTextColor
                    textSize = 15f * scale
                    typeface = delaFont
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    if (exportBackground != "white") {
                        setShadowLayer(2f, 1f, 1f, shadowColor)
                    }
                }
                canvas.drawText(
                    timeText,
                    width / 2f,
                    centerY - (10f * scale),
                    timePaint
                )

                // Missed text centered underneath: exportMissedText in blue (0xFF0F8CFF)
                val missedPaint = Paint().apply {
                    color = Color.parseColor("#0F8CFF")
                    textSize = 22f * scale
                    typeface = bricolageFont
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                    if (exportBackground != "white") {
                        setShadowLayer(2f, 1f, 1f, shadowColor)
                    }
                }
                canvas.drawText(
                    captionText,
                    width / 2f,
                    centerY + (22f * scale),
                    missedPaint
                )
            } else if (timeText.isNotEmpty()) {
                if (vlogText.isEmpty()) {
                    // Preview screen centered layout style
                    val timePaint = Paint().apply {
                        color = overlayTextColor
                        textSize = 20f * scale
                        typeface = delaFont
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                        setShadowLayer(2f, 1f, 1f, shadowColor)
                    }
                    canvas.drawText(
                        timeText,
                        width / 2f,
                        centerY - (10f * scale),
                        timePaint
                    )

                    // Draw caption text underneath time
                    if (captionText.isNotEmpty()) {
                        val captionPaint = Paint().apply {
                            color = overlayTextColor
                            textSize = 15f * scale
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                            textAlign = Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        canvas.drawText(
                            captionText,
                            width / 2f,
                            centerY + (15f * scale),
                            captionPaint
                        )
                    }
                } else {
                    // Vlog screen right-aligned layout style
                    val timePaint = Paint().apply {
                        color = overlayTextColor
                        textSize = 17f * scale
                        typeface = robotoFont
                        textAlign = Paint.Align.RIGHT
                        isAntiAlias = true
                        setShadowLayer(3f, 1f, 1f, shadowColor)
                    }
                    canvas.drawText(
                        timeText,
                        width - (5.5f * scale),
                        centerY + (timePaint.textSize / 3f),
                        timePaint
                    )

                    // Draw caption text in center
                    if (captionText.isNotEmpty()) {
                        val captionPaint = Paint().apply {
                            color = overlayTextColor
                            textSize = 22f * scale
                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                            textAlign = Paint.Align.CENTER
                            isAntiAlias = true
                            setShadowLayer(3f, 1f, 1f, shadowColor)
                        }
                        canvas.drawText(
                            captionText,
                            width / 2f,
                            centerY + (captionPaint.textSize / 3f),
                            captionPaint
                        )
                    }
                }
            }

            return bitmap
        }
    }
}
