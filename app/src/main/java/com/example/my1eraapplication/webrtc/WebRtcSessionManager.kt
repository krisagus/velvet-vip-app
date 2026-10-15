package com.example.my1eraapplication.webrtc

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.webrtc.*

/**
 * Estados de la videollamada WebRTC en la nube.
 */
enum class WebRtcCallState {
    DISCONNECTED,
    INITIALIZING,
    GATHERING_STUN,
    EXCHANGING_SDP,
    CONNECTING,
    CONNECTED,
    FAILED
}

/**
 * Gestor integral de videollamada WebRTC con STUN de Google y señalización en Firebase Cloud Firestore.
 * Conecta transmisiones de video y audio P2P sobre Internet (redes móviles 4G/5G y Wi-Fi).
 * Maneja tanto la vista previa local por hardware como la sesión WebRTC en la nube.
 */
class WebRtcSessionManager(
    private val context: Context,
    private val signalingClient: WebRtcSignalingClient,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "WebRtcSession"

        val STUN_SERVERS = listOf(
            "stun:stun.l.google.com:19302",
            "stun:stun1.l.google.com:19302",
            "stun:stun2.l.google.com:19302",
            "stun:stun3.l.google.com:19302",
            "stun:stun4.l.google.com:19302"
        )
    }

    private val _callState = MutableStateFlow(WebRtcCallState.DISCONNECTED)
    val callState: StateFlow<WebRtcCallState> = _callState.asStateFlow()

    private val _iceConnectionState = MutableStateFlow(PeerConnection.IceConnectionState.NEW)
    val iceConnectionState: StateFlow<PeerConnection.IceConnectionState> = _iceConnectionState.asStateFlow()

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack.asStateFlow()

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack: StateFlow<VideoTrack?> = _remoteVideoTrack.asStateFlow()

    private var eglBase: EglBase? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null

    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrackRef: VideoTrack? = null
    private var localVideoSource: VideoSource? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private var offerListener: ListenerRegistration? = null
    private var answerListener: ListenerRegistration? = null
    private var candidateListener: ListenerRegistration? = null

    private var isCallerSession = false
    private var remoteSink: VideoSink? = null

    private var lastProcessedOfferSdp: String? = null
    private var lastProcessedAnswerSdp: String? = null
    private val pendingIceCandidates = mutableListOf<IceCandidate>()

    init {
        initPeerConnectionFactory()
    }

    private fun initPeerConnectionFactory() {
        try {
            eglBase = EglBase.create()
            val initOptions = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(initOptions)

            val encoderFactory = DefaultVideoEncoderFactory(
                eglBase?.eglBaseContext,
                true,
                true
            )
            val decoderFactory = DefaultVideoDecoderFactory(eglBase?.eglBaseContext)

            peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .setOptions(PeerConnectionFactory.Options().apply {
                    disableNetworkMonitor = false
                })
                .createPeerConnectionFactory()
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando PeerConnectionFactory: ${e.message}", e)
        }
    }

    fun getEglBaseContext(): EglBase.Context? = eglBase?.eglBaseContext

    /**
     * Inicia la captura de cámara local para previsualización inmediata acelerada por GPU.
     */
    fun startLocalPreview(isFrontCamera: Boolean = true) {
        if (videoCapturer != null && _localVideoTrack.value != null) return

        val factory = peerConnectionFactory ?: return
        val capturer = createCameraCapturer(isFrontCamera) ?: return

        try {
            videoCapturer = capturer
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase?.eglBaseContext)
            localVideoSource = factory.createVideoSource(capturer.isScreencast)
            capturer.initialize(surfaceTextureHelper, context, localVideoSource?.capturerObserver)
            capturer.startCapture(720, 1280, 30)

            val track = factory.createVideoTrack("local_video_track", localVideoSource)
            track.setEnabled(true)
            localVideoTrackRef = track
            _localVideoTrack.value = track
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando captura de cámara: ${e.message}", e)
        }
    }

    private fun ensureLocalTracks(isFrontCamera: Boolean = true) {
        if (_localVideoTrack.value == null) {
            startLocalPreview(isFrontCamera)
        }

        val factory = peerConnectionFactory ?: return
        if (localAudioTrack == null) {
            val audioConstraints = MediaConstraints()
            val audioSource = factory.createAudioSource(audioConstraints)
            localAudioTrack = factory.createAudioTrack("local_audio_track", audioSource)
            localAudioTrack?.setEnabled(true)
        }
    }

    /**
     * Inicia una llamada como Emisor (Gamer)
     */
    fun startCallAsCaller(isFrontCamera: Boolean = true, onLocalTrackReady: ((VideoTrack) -> Unit)? = null) {
        isCallerSession = true
        lastProcessedAnswerSdp = null
        pendingIceCandidates.clear()
        _callState.value = WebRtcCallState.INITIALIZING

        ensureLocalTracks(isFrontCamera)
        setupPeerConnection()

        localAudioTrack?.let { peerConnection?.addTrack(it, listOf("media_stream_vip")) }
        localVideoTrackRef?.let {
            peerConnection?.addTrack(it, listOf("media_stream_vip"))
            onLocalTrackReady?.invoke(it)
        }

        _callState.value = WebRtcCallState.EXCHANGING_SDP
        signalingClient.clearSignaling {
            createAndPublishOffer()
        }

        // Escuchar respuesta SDP del receptor con deduplicación
        answerListener?.remove()
        answerListener = signalingClient.listenForAnswer { answerSdp ->
            coroutineScope.launch(Dispatchers.Main) {
                if (answerSdp.description != lastProcessedAnswerSdp) {
                    lastProcessedAnswerSdp = answerSdp.description
                    handleRemoteAnswer(answerSdp)
                }
            }
        }

        // Escuchar candidatos ICE del receptor
        candidateListener?.remove()
        candidateListener = signalingClient.listenForRemoteCandidates(isCaller = true) { candidate ->
            addOrQueueRemoteCandidate(candidate)
        }
    }

    /**
     * Se une a una llamada como Receptor (Modelo)
     */
    fun joinCallAsCallee(isFrontCamera: Boolean = true, onLocalTrackReady: ((VideoTrack) -> Unit)? = null) {
        isCallerSession = false
        lastProcessedOfferSdp = null
        pendingIceCandidates.clear()
        _callState.value = WebRtcCallState.INITIALIZING

        ensureLocalTracks(isFrontCamera)
        setupPeerConnection()

        localAudioTrack?.let { peerConnection?.addTrack(it, listOf("media_stream_vip")) }
        localVideoTrackRef?.let {
            peerConnection?.addTrack(it, listOf("media_stream_vip"))
            onLocalTrackReady?.invoke(it)
        }

        _callState.value = WebRtcCallState.EXCHANGING_SDP

        // Escuchar oferta SDP del emisor con deduplicación
        offerListener?.remove()
        offerListener = signalingClient.listenForOffer { offerSdp ->
            coroutineScope.launch(Dispatchers.Main) {
                if (offerSdp.description != lastProcessedOfferSdp) {
                    lastProcessedOfferSdp = offerSdp.description
                    handleRemoteOffer(offerSdp)
                }
            }
        }

        // Escuchar candidatos ICE del emisor
        candidateListener?.remove()
        candidateListener = signalingClient.listenForRemoteCandidates(isCaller = false) { candidate ->
            addOrQueueRemoteCandidate(candidate)
        }
    }

    private fun addOrQueueRemoteCandidate(candidate: IceCandidate) {
        coroutineScope.launch(Dispatchers.Main) {
            if (peerConnection?.remoteDescription != null) {
                peerConnection?.addIceCandidate(candidate)
            } else {
                pendingIceCandidates.add(candidate)
            }
        }
    }

    private fun drainPendingCandidates() {
        coroutineScope.launch(Dispatchers.Main) {
            val pc = peerConnection ?: return@launch
            if (pc.remoteDescription != null && pendingIceCandidates.isNotEmpty()) {
                for (cand in pendingIceCandidates) {
                    pc.addIceCandidate(cand)
                }
                pendingIceCandidates.clear()
            }
        }
    }

    private fun setupPeerConnection() {
        val iceServers = STUN_SERVERS.map { url ->
            PeerConnection.IceServer.builder(url).createIceServer()
        }

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            keyType = PeerConnection.KeyType.ECDSA
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection?.close()
        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate?) {
                if (candidate != null) {
                    signalingClient.publishIceCandidate(candidate, isCaller = isCallerSession)
                }
            }

            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}

            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                val state = newState ?: PeerConnection.IceConnectionState.NEW
                _iceConnectionState.value = state
                when (state) {
                    PeerConnection.IceConnectionState.CHECKING -> _callState.value = WebRtcCallState.CONNECTING
                    PeerConnection.IceConnectionState.CONNECTED,
                    PeerConnection.IceConnectionState.COMPLETED -> _callState.value = WebRtcCallState.CONNECTED
                    PeerConnection.IceConnectionState.FAILED -> _callState.value = WebRtcCallState.FAILED
                    PeerConnection.IceConnectionState.DISCONNECTED -> _callState.value = WebRtcCallState.DISCONNECTED
                    else -> {}
                }
            }

            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {
                if (newState == PeerConnection.IceGatheringState.GATHERING) {
                    _callState.value = WebRtcCallState.GATHERING_STUN
                }
            }

            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                val track = receiver?.track()
                if (track is VideoTrack) {
                    _remoteVideoTrack.value = track
                    remoteSink?.let { track.addSink(it) }
                }
            }

            override fun onSignalingChange(newState: PeerConnection.SignalingState?) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(dataChannel: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddStream(stream: MediaStream?) {
                val videoTrack = stream?.videoTracks?.firstOrNull()
                if (videoTrack != null) {
                    _remoteVideoTrack.value = videoTrack
                    remoteSink?.let { videoTrack.addSink(it) }
                }
            }
        })
    }

    private fun createCameraCapturer(isFront: Boolean): CameraVideoCapturer? {
        val enumerator: CameraEnumerator = if (Camera2Enumerator.isSupported(context)) {
            Camera2Enumerator(context)
        } else {
            Camera1Enumerator(true)
        }

        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (isFront && enumerator.isFrontFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if (capturer != null) return capturer
            }
            if (!isFront && enumerator.isBackFacing(deviceName)) {
                val capturer = enumerator.createCapturer(deviceName, null)
                if (capturer != null) return capturer
            }
        }
        for (deviceName in deviceNames) {
            val capturer = enumerator.createCapturer(deviceName, null)
            if (capturer != null) return capturer
        }
        return null
    }

    fun switchCamera(onComplete: ((Boolean) -> Unit)? = null) {
        videoCapturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                onComplete?.invoke(isFrontCamera)
            }

            override fun onCameraSwitchError(errorDescription: String?) {
                Log.e(TAG, "Error cambiando cámara: $errorDescription")
                onComplete?.invoke(false)
            }
        })
    }

    fun attachRemoteSink(sink: VideoSink) {
        remoteSink = sink
        _remoteVideoTrack.value?.addSink(sink)
    }

    fun detachRemoteSink(sink: VideoSink) {
        _remoteVideoTrack.value?.removeSink(sink)
        if (remoteSink == sink) remoteSink = null
    }

    private fun createAndPublishOffer() {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createOffer(object : SdpObserverAdapter() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                if (sdp == null) return
                peerConnection?.setLocalDescription(object : SdpObserverAdapter() {
                    override fun onSetSuccess() {
                        signalingClient.publishOffer(sdp)
                    }
                }, sdp)
            }
        }, constraints)
    }

    private fun handleRemoteOffer(offerSdp: SessionDescription) {
        val pc = peerConnection ?: return
        if (pc.signalingState() == PeerConnection.SignalingState.STABLE ||
            pc.signalingState() == PeerConnection.SignalingState.HAVE_REMOTE_OFFER
        ) {
            pc.setRemoteDescription(object : SdpObserverAdapter() {
                override fun onSetSuccess() {
                    createAndPublishAnswer()
                    drainPendingCandidates()
                }
            }, offerSdp)
        }
    }

    private fun createAndPublishAnswer() {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createAnswer(object : SdpObserverAdapter() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                if (sdp == null) return
                peerConnection?.setLocalDescription(object : SdpObserverAdapter() {
                    override fun onSetSuccess() {
                        signalingClient.publishAnswer(sdp)
                    }
                }, sdp)
            }
        }, constraints)
    }

    private fun handleRemoteAnswer(answerSdp: SessionDescription) {
        val pc = peerConnection ?: return
        if (pc.signalingState() == PeerConnection.SignalingState.HAVE_LOCAL_OFFER) {
            pc.setRemoteDescription(object : SdpObserverAdapter() {
                override fun onSetSuccess() {
                    drainPendingCandidates()
                }
            }, answerSdp)
        }
    }

    /**
     * Cierra la llamada WebRTC y limpia señalización sin destruir la previsualización local.
     */
    fun closeCall() {
        _callState.value = WebRtcCallState.DISCONNECTED
        _iceConnectionState.value = PeerConnection.IceConnectionState.NEW
        _remoteVideoTrack.value = null

        offerListener?.remove()
        offerListener = null
        answerListener?.remove()
        answerListener = null
        candidateListener?.remove()
        candidateListener = null

        lastProcessedOfferSdp = null
        lastProcessedAnswerSdp = null
        pendingIceCandidates.clear()

        signalingClient.clearSignaling()

        try {
            remoteSink?.let { _remoteVideoTrack.value?.removeSink(it) }
            peerConnection?.close()
            peerConnection = null

            localAudioTrack?.dispose()
            localAudioTrack = null
        } catch (e: Exception) {
            Log.e(TAG, "Error cerrando llamada WebRTC: ${e.message}", e)
        }
    }

    /**
     * Libera por completo todos los recursos (cámara, audio, EGL y factoría).
     */
    fun dispose() {
        closeCall()

        try {
            videoCapturer?.stopCapture()
        } catch (_: Exception) {}
        try {
            videoCapturer?.dispose()
            videoCapturer = null
        } catch (_: Exception) {}
        try {
            surfaceTextureHelper?.dispose()
            surfaceTextureHelper = null
        } catch (_: Exception) {}

        try {
            localVideoTrackRef?.dispose()
            localVideoTrackRef = null
            localVideoSource?.dispose()
            localVideoSource = null
            _localVideoTrack.value = null

            peerConnectionFactory?.dispose()
            peerConnectionFactory = null
            eglBase?.release()
            eglBase = null
        } catch (e: Exception) {
            Log.e(TAG, "Error liberando WebRtcSessionManager: ${e.message}", e)
        }
    }
}

/**
 * Adaptador para simplificar callbacks de SdpObserver.
 */
open class SdpObserverAdapter : SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription?) {}
    override fun onSetSuccess() {}
    override fun onCreateFailure(error: String?) {}
    override fun onSetFailure(error: String?) {}
}
