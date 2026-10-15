package com.example.my1eraapplication.webrtc

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

/**
 * Cliente de señalización WebRTC en la nube mediante Firebase Cloud Firestore (Querétaro).
 * Permite intercambiar SDP Offer, SDP Answer y candidatos ICE con servidores STUN
 * para atravesar NAT y hacer videollamada P2P a través de Internet (4G/5G).
 */
class WebRtcSignalingClient(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    var salaId: String = "sala_vip_1"
) {
    val roomDoc: DocumentReference
        get() = firestore.collection("salas_videollamada").document(salaId)

    /**
     * Publica la oferta SDP en Firestore para el interlocutor remoto.
     */
    fun publishOffer(
        sdp: SessionDescription,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val offerData = hashMapOf(
            "type" to sdp.type.canonicalForm(),
            "sdp" to sdp.description,
            "created_at" to FieldValue.serverTimestamp()
        )
        val payload = hashMapOf(
            "webrtc_offer" to offerData,
            "webrtc_answer" to FieldValue.delete(),
            "webrtc_active" to true,
            "webrtc_cloud_mode" to true
        )
        roomDoc.set(payload, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    /**
     * Publica la respuesta SDP en Firestore.
     */
    fun publishAnswer(
        sdp: SessionDescription,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val answerData = hashMapOf(
            "type" to sdp.type.canonicalForm(),
            "sdp" to sdp.description,
            "created_at" to FieldValue.serverTimestamp()
        )
        val payload = hashMapOf(
            "webrtc_answer" to answerData,
            "webrtc_active" to true
        )
        roomDoc.set(payload, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    /**
     * Publica un candidato ICE obtenido de los servidores STUN de Google.
     */
    fun publishIceCandidate(
        candidate: IceCandidate,
        isCaller: Boolean,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val candidateData = hashMapOf(
            "sdp" to candidate.sdp,
            "sdpMid" to candidate.sdpMid,
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "serverUrl" to (candidate.serverUrl ?: ""),
            "created_at" to FieldValue.serverTimestamp()
        )
        val collectionName = if (isCaller) "caller_candidates" else "callee_candidates"
        roomDoc.collection(collectionName).add(candidateData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    /**
     * Escucha la oferta SDP del creador de la llamada.
     */
    fun listenForOffer(onOfferReceived: (SessionDescription) -> Unit): ListenerRegistration {
        return roomDoc.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            val offerMap = snapshot.get("webrtc_offer") as? Map<*, *> ?: return@addSnapshotListener
            val sdpType = offerMap["type"] as? String ?: return@addSnapshotListener
            val sdpString = offerMap["sdp"] as? String ?: return@addSnapshotListener

            if (sdpType.equals("offer", ignoreCase = true) && sdpString.isNotBlank()) {
                val description = SessionDescription(SessionDescription.Type.OFFER, sdpString)
                onOfferReceived(description)
            }
        }
    }

    /**
     * Escucha la respuesta SDP del receptor de la llamada.
     */
    fun listenForAnswer(onAnswerReceived: (SessionDescription) -> Unit): ListenerRegistration {
        return roomDoc.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            val answerMap = snapshot.get("webrtc_answer") as? Map<*, *> ?: return@addSnapshotListener
            val sdpType = answerMap["type"] as? String ?: return@addSnapshotListener
            val sdpString = answerMap["sdp"] as? String ?: return@addSnapshotListener

            if (sdpType.equals("answer", ignoreCase = true) && sdpString.isNotBlank()) {
                val description = SessionDescription(SessionDescription.Type.ANSWER, sdpString)
                onAnswerReceived(description)
            }
        }
    }

    /**
     * Escucha los candidatos ICE remotos (descubiertos por STUN) para establecer el enlace P2P.
     */
    fun listenForRemoteCandidates(
        isCaller: Boolean,
        onCandidateReceived: (IceCandidate) -> Unit
    ): ListenerRegistration {
        val targetCollection = if (isCaller) "callee_candidates" else "caller_candidates"
        val seenDocIds = mutableSetOf<String>()

        return roomDoc.collection(targetCollection).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            for (doc in snapshot.documents) {
                if (seenDocIds.contains(doc.id)) continue
                seenDocIds.add(doc.id)

                val sdp = doc.getString("sdp") ?: continue
                val sdpMid = doc.getString("sdpMid") ?: ""
                val sdpMLineIndex = doc.getLong("sdpMLineIndex")?.toInt() ?: 0

                val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
                onCandidateReceived(candidate)
            }
        }
    }

    /**
     * Limpia la señalización de la sala al finalizar la llamada.
     */
    fun clearSignaling(onComplete: () -> Unit = {}) {
        val clearData = hashMapOf<String, Any>(
            "webrtc_offer" to FieldValue.delete(),
            "webrtc_answer" to FieldValue.delete(),
            "webrtc_active" to false
        )
        roomDoc.set(clearData, SetOptions.merge()).addOnCompleteListener {
            // Limpieza opcional de subcolecciones de candidatos
            deleteCollection(roomDoc.collection("caller_candidates"))
            deleteCollection(roomDoc.collection("callee_candidates"))
            onComplete()
        }
    }

    private fun deleteCollection(collectionRef: com.google.firebase.firestore.CollectionReference) {
        collectionRef.get().addOnSuccessListener { snapshot ->
            for (doc in snapshot.documents) {
                doc.reference.delete()
            }
        }
    }
}
