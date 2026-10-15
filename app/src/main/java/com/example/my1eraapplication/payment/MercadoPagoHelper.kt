package com.example.my1eraapplication.payment

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.my1eraapplication.googlepay.GooglePayHelper
import com.example.my1eraapplication.googlepay.PaqueteTiempoVip
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Datos del resultado de inicialización de Checkout Pro de Mercado Pago.
 */
data class MercadoPagoCheckoutResult(
    val preferenceId: String,
    val initPointUrl: String,
    val sandboxInitPointUrl: String,
    val externalReference: String
)

/**
 * Helper oficial para procesar pagos con Mercado Pago México (Checkout Pro / API REST).
 * Soporta Tarjetas, Transferencia SPEI (STP), OXXO Pay y Saldo Mercado Pago.
 */
object MercadoPagoHelper {

    // URL base de la API oficial de Mercado Pago
    const val MP_PREFERENCE_URL = "https://api.mercadopago.com/checkout/preferences"

    // Portal web oficial para redirecciones de retorno
    const val DEFAULT_SUCCESS_URL = "https://local-casino-hub-970df.web.app/pago_exitoso.html"
    const val DEFAULT_FAILURE_URL = "https://local-casino-hub-970df.web.app/pago_fallido.html"
    const val DEFAULT_PENDING_URL = "https://local-casino-hub-970df.web.app/pago_pendiente.html"

    // Token por defecto (puede sobreescribirse con el Access Token de producción o prueba del usuario)
    var customAccessToken: String? = null

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Construye el JSON oficial de Preferencia para Mercado Pago Checkout Pro.
     */
    fun construirPreferenciaJson(
        paquete: PaqueteTiempoVip,
        externalReference: String,
        payerEmail: String = "cliente@velvetvip.app",
        successUrl: String = DEFAULT_SUCCESS_URL,
        failureUrl: String = DEFAULT_FAILURE_URL,
        pendingUrl: String = DEFAULT_PENDING_URL
    ): JSONObject {
        val item = JSONObject().apply {
            put("id", paquete.id)
            put("title", "Velvet VIP: ${paquete.nombre} (+${paquete.segundos}s)")
            put("description", paquete.descripcion.ifBlank { "Recarga de tiempo en Velvet VIP" })
            put("quantity", 1)
            put("currency_id", "MXN")
            put("unit_price", paquete.precioMxn)
        }

        val items = JSONArray().apply { put(item) }

        val backUrls = JSONObject().apply {
            put("success", successUrl)
            put("failure", failureUrl)
            put("pending", pendingUrl)
        }

        val payer = JSONObject().apply {
            put("email", payerEmail)
        }

        return JSONObject().apply {
            put("items", items)
            put("payer", payer)
            put("back_urls", backUrls)
            put("auto_return", "approved")
            put("external_reference", externalReference)
            put("statement_descriptor", "VELVET VIP")
        }
    }

    /**
     * Crea de forma asíncrona una preferencia de pago en la API de Mercado Pago.
     * Si no se dispone de un Access Token configurado o no hay conexión con la API,
     * retorna una URL amigable de Checkout con el externalReference generado.
     */
    suspend fun crearPreferenciaCheckout(
        paquete: PaqueteTiempoVip,
        accessToken: String? = customAccessToken
    ): MercadoPagoCheckoutResult = withContext(Dispatchers.IO) {
        val externalRef = "VELVET-MP-" + UUID.randomUUID().toString().take(10).uppercase()

        if (accessToken.isNullOrBlank()) {
            // URL amigable de respaldo
            val fallbackUrl = "https://www.mercadopago.com.mx/checkout/v1/redirect?pref_id=demo_$externalRef"
            return@withContext MercadoPagoCheckoutResult(
                preferenceId = "demo_$externalRef",
                initPointUrl = fallbackUrl,
                sandboxInitPointUrl = fallbackUrl,
                externalReference = externalRef
            )
        }

        val jsonPayload = construirPreferenciaJson(paquete, externalRef)
        val body = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(MP_PREFERENCE_URL)
            .addHeader("Authorization", "Bearer ${accessToken.trim()}")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        try {
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful || responseBody.isBlank()) {
                val fallbackUrl = "https://www.mercadopago.com.mx/checkout/v1/redirect?pref_id=fallback_$externalRef"
                return@withContext MercadoPagoCheckoutResult(
                    preferenceId = "fallback_$externalRef",
                    initPointUrl = fallbackUrl,
                    sandboxInitPointUrl = fallbackUrl,
                    externalReference = externalRef
                )
            }

            val jsonRes = JSONObject(responseBody)
            val prefId = jsonRes.optString("id", "pref_$externalRef")
            val initPoint = jsonRes.optString("init_point", "https://www.mercadopago.com.mx/checkout/v1/redirect?pref_id=$prefId")
            val sandboxInitPoint = jsonRes.optString("sandbox_init_point", initPoint)

            MercadoPagoCheckoutResult(
                preferenceId = prefId,
                initPointUrl = initPoint,
                sandboxInitPointUrl = sandboxInitPoint,
                externalReference = externalRef
            )
        } catch (e: Exception) {
            val fallbackUrl = "https://www.mercadopago.com.mx/checkout/v1/redirect?pref_id=error_$externalRef"
            MercadoPagoCheckoutResult(
                preferenceId = "error_$externalRef",
                initPointUrl = fallbackUrl,
                sandboxInitPointUrl = fallbackUrl,
                externalReference = externalRef
            )
        }
    }

    /**
     * Abre el Checkout de Mercado Pago en el navegador web o app del dispositivo.
     */
    fun abrirCheckoutEnNavegador(context: Context, checkoutUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Registra y acredita la transacción de Mercado Pago de forma atómica en Cloud Firestore.
     */
    suspend fun acreditarPagoMercadoPago(
        firestore: FirebaseFirestore,
        uid: String,
        salaId: String,
        paquete: PaqueteTiempoVip,
        folioReferencia: String,
        metodoDetalle: String = "MERCADO_PAGO_CHECKOUT"
    ): Result<String> = runCatching {
        require(uid.isNotBlank()) { "UID de usuario no puede estar vacío" }
        require(salaId.isNotBlank()) { "ID de sala no puede estar vacío" }

        val transaccionRef = firestore.collection("transacciones").document()
        val transaccionData = mapOf(
            "uid" to uid.trim(),
            "metodo" to "MERCADO_PAGO",
            "metodoDetalle" to metodoDetalle,
            "paqueteId" to paquete.id,
            "montoMxn" to paquete.precioMxn,
            "segundosAcreditados" to paquete.segundos,
            "folio" to folioReferencia,
            "moneda" to "MXN",
            "timestamp" to FieldValue.serverTimestamp()
        )

        val salaRef = firestore.collection("salas_videollamada").document(salaId.trim())
        val batch = firestore.batch()

        batch.set(transaccionRef, transaccionData)
        batch.set(
            salaRef,
            mapOf(
                "segundos_participante" to FieldValue.increment(paquete.segundos.toLong()),
                "ultimo_juego" to "🤝 ¡Recarga Mercado Pago Exitosa: +${paquete.segundos}s ($${paquete.precioEntero} MXN)!"
            ),
            SetOptions.merge()
        )

        batch.commit().await()
        transaccionRef.id
    }
}
