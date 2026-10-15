package com.example.my1eraapplication.googlepay

import android.content.Context
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.Locale

/**
 * Catálogo y modelo para los paquetes oficiales de recarga VIP en México.
 */
data class PaqueteTiempoVip(
    val id: String,
    val nombre: String,
    val precioMxn: Double,
    val segundos: Int,
    val badge: String? = null,
    val icon: String = "💎",
    val descripcion: String = ""
) {
    init {
        require(precioMxn > 0.0) { "El precio en MXN debe ser estrictamente positivo: $precioMxn" }
        require(segundos > 0) { "Los segundos acreditados deben ser estrictamente positivos: $segundos" }
    }

    val precioEntero: Int get() = precioMxn.toInt()
    val minutos: Int get() = segundos / 60
}

/**
 * Datos extraídos de la respuesta exitosa de Google Pay API.
 */
data class GooglePayPaymentResult(
    val token: String,
    val cardNetwork: String,
    val cardDetails: String,
    val description: String = ""
)

/**
 * Motor desacoplado de Configuración y Cliente Google Pay API v2 para México.
 */
object GooglePayHelper {

    const val API_VERSION = 2
    const val API_VERSION_MINOR = 0

    const val CURRENCY_CODE_MXN = "MXN"
    const val COUNTRY_CODE_MX = "MX"

    const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991

    // Gateways configurables
    const val GATEWAY_TEST_NAME = "example"
    const val GATEWAY_TEST_MERCHANT_ID = "exampleGatewayMerchantId"

    const val GATEWAY_PRODUCTION_NAME = "mercadopago"
    const val GATEWAY_PRODUCTION_MERCHANT_ID = "velvet_vip_mercadopago_prod"
    const val MERCHANT_ID_PRODUCTION = "12345678901234567890"

    const val DEFAULT_MERCHANT_NAME = "Velvet VIP Live México"

    /**
     * Redes de tarjetas bancarias soportadas en México.
     */
    val ALLOWED_CARD_NETWORKS = listOf("VISA", "MASTERCARD", "AMEX")

    /**
     * Métodos de autenticación admitidos por Google Pay (3DS seguro y PAN directo).
     */
    val ALLOWED_AUTH_METHODS = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")

    /**
     * Catálogo de Paquetes de Tiempo VIP para Gamer (moneda MXN -> segundos).
     */
    val PAQUETES_VIP: List<PaqueteTiempoVip> = listOf(
        PaqueteTiempoVip(
            id = "bronce",
            nombre = "Paquete Bronce",
            precioMxn = 50.0,
            segundos = 300,
            badge = "🥉 BÁSICO",
            icon = "⚡",
            descripcion = "+300s (5 min de videollamada)"
        ),
        PaqueteTiempoVip(
            id = "plata",
            nombre = "Paquete Plata",
            precioMxn = 150.0,
            segundos = 1200,
            badge = "⭐ MÁS VENDIDO",
            icon = "🔥",
            descripcion = "+1,200s (20 min de videollamada)"
        ),
        PaqueteTiempoVip(
            id = "velvet_vip",
            nombre = "Paquete Velvet VIP",
            precioMxn = 300.0,
            segundos = 3000,
            badge = "👑 MEJOR VALOR",
            icon = "👑",
            descripcion = "+3,000s (50 min de videollamada)"
        )
    )

    /**
     * Obtiene la equivalencia exacta de segundos para un precio en MXN.
     * Rechaza de forma estricta montos menores o iguales a cero, nulos o no registrados.
     */
    fun obtenerSegundosPorMonto(montoMxn: Double?): Int {
        if (montoMxn == null || montoMxn.isNaN() || montoMxn.isInfinite() || montoMxn <= 0.0) {
            throw IllegalArgumentException("El monto debe ser estrictamente positivo y no nulo: $montoMxn")
        }
        val paquete = PAQUETES_VIP.find { Math.abs(it.precioMxn - montoMxn) < 0.001 }
            ?: throw IllegalArgumentException("Monto no registrado en catálogo VIP: $montoMxn MXN")
        return paquete.segundos
    }

    /**
     * Busca un paquete por su identificador.
     */
    fun obtenerPaquetePorId(id: String): PaqueteTiempoVip? {
        return PAQUETES_VIP.find { it.id.equals(id, ignoreCase = true) }
    }

    /**
     * Define el método base de tarjeta bancaria conforme a Google Pay API v2.
     */
    fun obtenerBaseCardPaymentMethod(): JSONObject {
        val parameters = JSONObject().apply {
            put("allowedAuthMethods", JSONArray(ALLOWED_AUTH_METHODS))
            put("allowedCardNetworks", JSONArray(ALLOWED_CARD_NETWORKS))
            put("billingAddressRequired", false)
        }
        return JSONObject().apply {
            put("type", "CARD")
            put("parameters", parameters)
        }
    }

    /**
     * Genera la especificación de tokenización PAYMENT_GATEWAY parametrizada.
     */
    fun obtenerTokenizationSpecification(
        environment: Int = WalletConstants.ENVIRONMENT_TEST,
        gateway: String? = null,
        gatewayMerchantId: String? = null
    ): JSONObject {
        val isTest = environment == WalletConstants.ENVIRONMENT_TEST
        val gw = gateway ?: if (isTest) GATEWAY_TEST_NAME else GATEWAY_PRODUCTION_NAME
        val gwId = gatewayMerchantId ?: if (isTest) GATEWAY_TEST_MERCHANT_ID else GATEWAY_PRODUCTION_MERCHANT_ID

        val parameters = JSONObject().apply {
            put("gateway", gw)
            put("gatewayMerchantId", gwId)
        }
        return JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", parameters)
        }
    }

    /**
     * Define el método de tarjeta con tokenización para la solicitud de pago final.
     */
    fun obtenerCardPaymentMethodConTokenizacion(
        environment: Int = WalletConstants.ENVIRONMENT_TEST,
        gateway: String? = null,
        gatewayMerchantId: String? = null
    ): JSONObject {
        return obtenerBaseCardPaymentMethod().apply {
            put("tokenizationSpecification", obtenerTokenizationSpecification(environment, gateway, gatewayMerchantId))
        }
    }

    /**
     * JSON Array de métodos de pago para inicializar el botón nativo PayButton.
     */
    fun obtenerAllowedPaymentMethodsArrayJson(): String {
        return JSONArray().put(obtenerBaseCardPaymentMethod()).toString()
    }

    /**
     * Construye el JSON para IsReadyToPayRequest conforme al estándar Google Pay v2.
     */
    fun obtenerIsReadyToPayRequestJson(): String {
        return JSONObject().apply {
            put("apiVersion", API_VERSION)
            put("apiVersionMinor", API_VERSION_MINOR)
            put("allowedPaymentMethods", JSONArray().put(obtenerBaseCardPaymentMethod()))
        }.toString()
    }

    /**
     * Construye el objeto transactionInfo para México (moneda MXN, país MX, precio formateado).
     */
    fun obtenerTransactionInfoJson(precioMxn: Double): JSONObject {
        require(precioMxn > 0.0) { "El monto en MXN debe ser estrictamente positivo: $precioMxn" }
        val precioFormateado = String.format(Locale.US, "%.2f", precioMxn)
        return JSONObject().apply {
            put("totalPrice", precioFormateado)
            put("totalPriceStatus", "FINAL")
            put("currencyCode", CURRENCY_CODE_MXN)
            put("countryCode", COUNTRY_CODE_MX)
        }
    }

    /**
     * Construye el JSON completo para PaymentDataRequest.
     */
    fun obtenerPaymentDataRequestJson(
        precioMxn: Double,
        environment: Int = WalletConstants.ENVIRONMENT_TEST,
        merchantName: String = DEFAULT_MERCHANT_NAME,
        gateway: String? = null,
        gatewayMerchantId: String? = null,
        merchantId: String? = null
    ): String {
        require(!precioMxn.isNaN() && !precioMxn.isInfinite() && precioMxn > 0.0) { "El precio debe ser estrictamente positivo: $precioMxn" }
        val cardPaymentMethod = obtenerCardPaymentMethodConTokenizacion(environment, gateway, gatewayMerchantId)
        val transactionInfo = obtenerTransactionInfoJson(precioMxn)
        val merchantInfo = JSONObject().apply {
            put("merchantName", merchantName)
            if (environment == WalletConstants.ENVIRONMENT_PRODUCTION || merchantId != null) {
                put("merchantId", merchantId ?: MERCHANT_ID_PRODUCTION)
            }
        }

        return JSONObject().apply {
            put("apiVersion", API_VERSION)
            put("apiVersionMinor", API_VERSION_MINOR)
            put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod))
            put("transactionInfo", transactionInfo)
            put("merchantInfo", merchantInfo)
        }.toString()
    }

    /**
     * Crea un cliente oficial PaymentsClient parametrizado en ENVIRONMENT_TEST o PRODUCTION.
     */
    fun crearPaymentsClient(
        context: Context,
        environment: Int = WalletConstants.ENVIRONMENT_TEST
    ): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(environment)
            .build()
        return Wallet.getPaymentsClient(context, walletOptions)
    }

    /**
     * Consulta asíncronamente si Google Pay está disponible en el dispositivo.
     */
    suspend fun consultarIsReadyToPay(paymentsClient: PaymentsClient): Boolean {
        return try {
            val request = IsReadyToPayRequest.fromJson(obtenerIsReadyToPayRequestJson())
            val task = paymentsClient.isReadyToPay(request)
            task.await() == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Extrae de forma segura el token, red y detalles de tarjeta desde el JSON de Google Pay.
     */
    fun parsearResultadoPago(jsonRespuesta: String): GooglePayPaymentResult {
        require(jsonRespuesta.isNotBlank()) { "El JSON de respuesta de Google Pay no puede estar vacío" }
        val root = JSONObject(jsonRespuesta)
        val paymentMethodData = root.getJSONObject("paymentMethodData")
        val tokenizationData = paymentMethodData.getJSONObject("tokenizationData")
        val token = tokenizationData.getString("token").trim()
        require(token.isNotBlank()) { "El token no puede estar vacío" }

        val info = paymentMethodData.optJSONObject("info")
        val rawCardNetwork = info?.optString("cardNetwork")?.trim()
        val cardNetwork = if (!rawCardNetwork.isNullOrEmpty()) rawCardNetwork else "DESCONOCIDA"

        val rawCardDetails = info?.optString("cardDetails")?.trim()
        val cardDetails = if (!rawCardDetails.isNullOrEmpty()) rawCardDetails else "••••"
        val description = paymentMethodData.optString("description", "")

        return GooglePayPaymentResult(
            token = token,
            cardNetwork = cardNetwork,
            cardDetails = cardDetails,
            description = description
        )
    }

    /**
     * Extrae el resultado a partir de un objeto PaymentData.
     */
    fun parsearPaymentData(paymentData: PaymentData): GooglePayPaymentResult {
        return parsearResultadoPago(paymentData.toJson())
    }

    /**
     * Genera un hash SHA-256 seguro del token de Google Pay para auditoría sin exponer datos sensibles.
     */
    fun generarHashToken(token: String): String {
        require(token.isNotBlank()) { "El token no puede estar vacío para generar el hash" }
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(token.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Construye el mapa de datos auditable de la transacción para Cloud Firestore.
     */
    fun construirDatosTransaccion(
        uid: String,
        resultado: GooglePayPaymentResult,
        montoMxn: Double,
        segundosAcreditados: Int
    ): Map<String, Any?> {
        require(uid.isNotBlank()) { "El UID de usuario no puede estar vacío" }
        require(!montoMxn.isNaN() && !montoMxn.isInfinite() && montoMxn > 0.0) { "El monto debe ser positivo: $montoMxn" }
        require(segundosAcreditados > 0) { "Los segundos deben ser positivos: $segundosAcreditados" }

        return mapOf(
            "uid" to uid.trim(),
            "metodo" to "GOOGLE_PAY",
            "redTarjeta" to resultado.cardNetwork,
            "cardDetails" to resultado.cardDetails,
            "montoMxn" to montoMxn,
            "segundosAcreditados" to segundosAcreditados,
            "tokenHash" to generarHashToken(resultado.token),
            "descripcion" to resultado.description,
            "timestamp" to FieldValue.serverTimestamp()
        )
    }

    /**
     * Ejecuta una escritura atómica en Cloud Firestore:
     * a) Registra el recibo auditable en la colección "transacciones".
     * b) Incrementa los segundos disponibles del Gamer en la sala activa usando FieldValue.increment.
     */
    suspend fun acreditarRecargaGooglePay(
        firestore: FirebaseFirestore,
        uid: String,
        salaId: String,
        resultado: GooglePayPaymentResult,
        montoMxn: Double,
        segundosAcreditados: Int
    ): Result<String> = runCatching {
        require(uid.isNotBlank()) { "El UID de usuario no puede estar vacío" }
        require(salaId.isNotBlank()) { "El ID de la sala no puede estar vacío" }

        val transaccionRef = firestore.collection("transacciones").document()
        val transaccionData = construirDatosTransaccion(
            uid = uid.trim(),
            resultado = resultado,
            montoMxn = montoMxn,
            segundosAcreditados = segundosAcreditados
        )

        val salaRef = firestore.collection("salas_videollamada").document(salaId.trim())
        val batch = firestore.batch()

        batch.set(transaccionRef, transaccionData)
        batch.set(
            salaRef,
            mapOf(
                "segundos_participante" to FieldValue.increment(segundosAcreditados.toLong()),
                "ultimo_juego" to "⚡ ¡Recarga Google Pay VIP: +${segundosAcreditados}s ($$montoMxn MXN)!"
            ),
            com.google.firebase.firestore.SetOptions.merge()
        )

        batch.commit().await()
        transaccionRef.id
    }
}
