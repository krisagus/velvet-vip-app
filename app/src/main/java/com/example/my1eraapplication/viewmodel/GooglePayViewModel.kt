package com.example.my1eraapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my1eraapplication.googlepay.GooglePayHelper
import com.example.my1eraapplication.googlepay.GooglePayPaymentResult
import com.example.my1eraapplication.googlepay.PaqueteTiempoVip
import com.google.android.gms.wallet.PaymentsClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados del flujo de compra y acreditación de Google Pay.
 */
sealed class EstadoGooglePay {
    object Idle : EstadoGooglePay()
    object Procesando : EstadoGooglePay()
    data class Exito(
        val transaccionId: String,
        val paquete: PaqueteTiempoVip,
        val resultado: GooglePayPaymentResult
    ) : EstadoGooglePay()
    data class Error(val mensaje: String) : EstadoGooglePay()
}

/**
 * ViewModel reactivo para gestionar la disponibilidad de Google Pay, la selección de paquetes
 * y el estado de la transacción.
 */
class GooglePayViewModel : ViewModel() {

    private val _isReadyToPay = MutableStateFlow(false)
    val isReadyToPay: StateFlow<Boolean> = _isReadyToPay.asStateFlow()

    private val _paqueteSeleccionado = MutableStateFlow(GooglePayHelper.PAQUETES_VIP[1]) // Paquete Plata default
    val paqueteSeleccionado: StateFlow<PaqueteTiempoVip> = _paqueteSeleccionado.asStateFlow()

    private val _estadoPago = MutableStateFlow<EstadoGooglePay>(EstadoGooglePay.Idle)
    val estadoPago: StateFlow<EstadoGooglePay> = _estadoPago.asStateFlow()

    /**
     * Consulta de forma asíncrona la disponibilidad de Google Pay en el dispositivo.
     */
    fun verificarDisponibilidad(paymentsClient: PaymentsClient) {
        viewModelScope.launch {
            try {
                val disponible = GooglePayHelper.consultarIsReadyToPay(paymentsClient)
                _isReadyToPay.value = disponible
            } catch (e: Exception) {
                _isReadyToPay.value = false
            }
        }
    }

    /**
     * Permite fijar directamente la disponibilidad (útil para pruebas unitarias y entornos simulados).
     */
    fun setIsReadyToPay(disponible: Boolean) {
        _isReadyToPay.value = disponible
    }

    /**
     * Selecciona un paquete del catálogo VIP.
     */
    fun seleccionarPaquete(paquete: PaqueteTiempoVip) {
        _paqueteSeleccionado.value = paquete
    }

    fun setProcesando() {
        _estadoPago.value = EstadoGooglePay.Procesando
    }

    fun notificarExito(transaccionId: String, paquete: PaqueteTiempoVip, resultado: GooglePayPaymentResult) {
        _estadoPago.value = EstadoGooglePay.Exito(transaccionId, paquete, resultado)
    }

    fun notificarError(mensaje: String) {
        _estadoPago.value = EstadoGooglePay.Error(mensaje)
    }

    fun reiniciarEstado() {
        _estadoPago.value = EstadoGooglePay.Idle
    }
}
