package com.example.my1eraapplication.webrtc

import org.junit.Test
import org.junit.Assert.*
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.IceCandidate

class WebRtcSmokeTest {
    @Test
    fun testWebRtcImports() {
        val sdpOffer = SessionDescription(SessionDescription.Type.OFFER, "v=0\r\no=- 0 0 IN IP4 127.0.0.1\r\n")
        assertEquals(SessionDescription.Type.OFFER, sdpOffer.type)
        assertEquals("offer", sdpOffer.type.canonicalForm())

        val sdpAnswer = SessionDescription(SessionDescription.Type.ANSWER, "v=0\r\no=- 0 0 IN IP4 127.0.0.1\r\n")
        assertEquals(SessionDescription.Type.ANSWER, sdpAnswer.type)
        assertEquals("answer", sdpAnswer.type.canonicalForm())

        val candidate = IceCandidate("audio", 0, "candidate:1 1 UDP 2122260223 34.120.10.5 5000 typ srflx raddr 10.0.0.1 rport 5000")
        assertEquals("audio", candidate.sdpMid)
        assertEquals(0, candidate.sdpMLineIndex)
        assertTrue(candidate.sdp.contains("srflx"))
    }

    @Test
    fun testIceServerBuilder() {
        val stunServer = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        assertNotNull(stunServer)
        assertEquals("stun:stun.l.google.com:19302", stunServer.uri)
    }

    @Test
    fun testStunServersList() {
        assertTrue(WebRtcSessionManager.STUN_SERVERS.isNotEmpty())
        assertEquals(5, WebRtcSessionManager.STUN_SERVERS.size)
        for (server in WebRtcSessionManager.STUN_SERVERS) {
            assertTrue(server.startsWith("stun:stun"))
            assertTrue(server.endsWith(":19302"))
            assertTrue(server.contains("google.com"))
        }
    }

    @Test
    fun testCallStatesEnum() {
        val expectedStates = listOf(
            "DISCONNECTED",
            "INITIALIZING",
            "GATHERING_STUN",
            "EXCHANGING_SDP",
            "CONNECTING",
            "CONNECTED",
            "FAILED"
        )
        for (stateName in expectedStates) {
            val state = WebRtcCallState.valueOf(stateName)
            assertEquals(stateName, state.name)
        }
    }
}
