package com.example.my1eraapplication.webrtc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

/**
 * Vista nativa acelerada por hardware (OpenGL ES / EglBase) para renderizar transmisiones de video WebRTC
 * en Jetpack Compose con alto rendimiento y cero latencia.
 */
@Composable
fun WebRtcVideoView(
    videoTrack: VideoTrack?,
    eglContext: EglBase.Context?,
    modifier: Modifier = Modifier,
    mirror: Boolean = false,
    scalingType: RendererCommon.ScalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL,
    overlay: Boolean = false
) {
    var boundTrack by remember { mutableStateOf<VideoTrack?>(null) }

    AndroidView(
        factory = { ctx ->
            SurfaceViewRenderer(ctx).apply {
                if (overlay) {
                    setZOrderMediaOverlay(true)
                }
                if (eglContext != null) {
                    init(eglContext, null)
                    setMirror(mirror)
                    setScalingType(scalingType)
                    setEnableHardwareScaler(true)
                }
            }
        },
        update = { renderer ->
            renderer.setMirror(mirror)
            renderer.setScalingType(scalingType)
            if (boundTrack != videoTrack) {
                boundTrack?.removeSink(renderer)
                videoTrack?.addSink(renderer)
                boundTrack = videoTrack
            }
        },
        onRelease = { renderer ->
            boundTrack?.removeSink(renderer)
            boundTrack = null
            try {
                renderer.release()
            } catch (_: Exception) {}
        },
        modifier = modifier
    )
}
