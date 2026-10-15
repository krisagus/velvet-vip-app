package com.example.my1eraapplication.payment

import com.example.my1eraapplication.googlepay.PaqueteTiempoVip
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

/**
 * Redes de Blockchain soportadas para transferencias de Criptomonedas (USDT).
 */
enum class RedCripto(
    val id: String,
    val nombre: String,
    val token: String,
    val redNombre: String,
    val tiempoEstimado: String,
    val comisionRed: String,
    val walletPorDefecto: String,
    val exploradorUrl: String
) {
    TRC20(
        id = "trc20",
        nombre = "USDT (Tron TRC-20)",
        token = "USDT",
        redNombre = "Tron Network",
        tiempoEstimado = "1 - 3 min",
        comisionRed = "~$1.00 USD",
        walletPorDefecto = "TYDnyPshWjE9Y3vGkK7e1B6XqXk3vP2jKL",
        exploradorUrl = "https://tronscan.org/#/transaction/"
    ),
    POLYGON(
        id = "polygon",
        nombre = "USDT (Polygon PoS)",
        token = "USDT",
        redNombre = "Polygon Network",
        tiempoEstimado = "< 1 min",
        comisionRed = "< $0.05 USD",
        walletPorDefecto = "0x71C8360f3a8b4f177F8A7D373A8e1545A2594639",
        exploradorUrl = "https://polygonscan.com/tx/"
    ),
    BEP20(
        id = "bep20",
        nombre = "USDT (BNB Smart Chain)",
        token = "USDT",
        redNombre = "BNB Chain (BEP-20)",
        tiempoEstimado = "1 - 2 min",
        comisionRed = "~$0.15 USD",
        walletPorDefecto = "0x71C8360f3a8b4f177F8A7D373A8e1545A2594639",
        exploradorUrl = "https://bscscan.com/tx/"
    )
}

/**
 * Modelo de pago en Criptomonedas para paquetes VIP.
 */
data class CotizacionCripto(
    val paquete: PaqueteTiempoVip,
    val red: RedCripto,
    val montoUsdt: Double,
    val montoMxn: Double,
    val tasaMxnUsdt: Double,
    val walletDestino: String
) {
    val montoUsdtFormateado: String
        get() = String.format(Locale.US, "%.2f", montoUsdt)
}

/**
 * Helper para gestión integral de pagos con Criptomonedas (USDT).
 * Diseñado para cobros globales sin cancelaciones (chargebacks) y libre de restricciones de contenido.
 */
object CryptoPaymentHelper {

    // Tasa de cambio de referencia (1 USDT = 20.00 MXN)
    var tasaCambioMxnPorUsdt: Double = 20.0

    /**
     * Calcula la cotización en USDT para un paquete de tiempo en MXN.
     */
    fun calcularCotizacion(
        paquete: PaqueteTiempoVip,
        red: RedCripto = RedCripto.TRC20,
        tasaMxnUsdt: Double = tasaCambioMxnPorUsdt,
        walletCustom: String? = null
    ): CotizacionCripto {
        require(tasaMxnUsdt > 0.0) { "La tasa de cambio debe ser positiva: $tasaMxnUsdt" }
        require(paquete.precioMxn > 0.0) { "El precio en MXN debe ser positivo" }

        val montoUsdt = BigDecimal(paquete.precioMxn / tasaMxnUsdt)
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()

        val wallet = walletCustom?.takeIf { it.isNotBlank() } ?: red.walletPorDefecto

        return CotizacionCripto(
            paquete = paquete,
            red = red,
            montoUsdt = montoUsdt,
            montoMxn = paquete.precioMxn,
            tasaMxnUsdt = tasaMxnUsdt,
            walletDestino = wallet
        )
    }

    /**
     * Valida sintácticamente una dirección de wallet según la red blockchain.
     */
    fun validarDireccionWallet(address: String, red: RedCripto): Boolean {
        val trimmed = address.trim()
        return when (red) {
            RedCripto.TRC20 -> {
                // Dirección Tron: 34 caracteres, inicia con 'T'
                trimmed.length == 34 && trimmed.startsWith("T") && trimmed.all { it.isLetterOrDigit() }
            }
            RedCripto.POLYGON, RedCripto.BEP20 -> {
                // Dirección EVM: 42 caracteres, inicia con '0x', caracteres hexadecimales
                trimmed.length == 42 &&
                        trimmed.startsWith("0x", ignoreCase = true) &&
                        trimmed.substring(2).all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
            }
        }
    }

    /**
     * Valida sintácticamente un Hash de transacción (TxID o comprobante).
     */
    fun validarTxHash(txHash: String): Boolean {
        val trimmed = txHash.trim()
        if (trimmed.length < 10) return false
        // Acepta hashes hex de 64 caracteres (estándar EVM / Tron) o formatos alfanuméricos de prueba
        val sinPrefijo = if (trimmed.startsWith("0x", ignoreCase = true)) trimmed.substring(2) else trimmed
        return sinPrefijo.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }

    /**
     * Ejecuta una acreditación atómica de pago con Criptomonedas en Cloud Firestore:
     * 1. Registra el recibo con auditoría completa en la colección "transacciones".
     * 2. Incrementa de inmediato los segundos del usuario en "salas_videollamada".
     */
    suspend fun acreditarPagoCripto(
        firestore: FirebaseFirestore,
        uid: String,
        salaId: String,
        cotizacion: CotizacionCripto,
        txHash: String
    ): Result<String> = runCatching {
        require(uid.isNotBlank()) { "UID de usuario no puede estar vacío" }
        require(salaId.isNotBlank()) { "ID de sala no puede estar vacío" }
        require(txHash.isNotBlank()) { "El Hash de transacción no puede estar vacío" }

        val transaccionRef = firestore.collection("transacciones").document()
        val transaccionData = mapOf(
            "uid" to uid.trim(),
            "metodo" to "CRYPTO_USDT",
            "red" to cotizacion.red.id,
            "redNombre" to cotizacion.red.nombre,
            "token" to cotizacion.red.token,
            "montoUsdt" to cotizacion.montoUsdt,
            "montoMxn" to cotizacion.montoMxn,
            "tasaCambio" to cotizacion.tasaMxnUsdt,
            "walletDestino" to cotizacion.walletDestino,
            "txHash" to txHash.trim(),
            "segundosAcreditados" to cotizacion.paquete.segundos,
            "timestamp" to FieldValue.serverTimestamp()
        )

        val salaRef = firestore.collection("salas_videollamada").document(salaId.trim())
        val batch = firestore.batch()

        batch.set(transaccionRef, transaccionData)
        batch.set(
            salaRef,
            mapOf(
                "segundos_participante" to FieldValue.increment(cotizacion.paquete.segundos.toLong()),
                "ultimo_juego" to "🪙 ¡Recarga Cripto USDT Exitosa: +${cotizacion.paquete.segundos}s (${cotizacion.montoUsdtFormateado} USDT)!"
            ),
            SetOptions.merge()
        )

        batch.commit().await()
        transaccionRef.id
    }
}
