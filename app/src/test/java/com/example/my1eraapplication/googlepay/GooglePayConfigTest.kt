package com.example.my1eraapplication.googlepay

import com.example.my1eraapplication.viewmodel.EstadoGooglePay
import com.example.my1eraapplication.viewmodel.GooglePayViewModel
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GooglePayConfigTest {

    @Test
    fun testIsReadyToPayRequestCumpleConApiVersion2YParametrosMexico() {
        val jsonStr = GooglePayHelper.obtenerIsReadyToPayRequestJson()
        assertNotNull(jsonStr)
        assertTrue(jsonStr.isNotBlank())

        val root = JSONObject(jsonStr)
        assertEquals("apiVersion debe ser 2", 2, root.getInt("apiVersion"))
        assertEquals("apiVersionMinor debe ser 0", 0, root.getInt("apiVersionMinor"))

        val allowedPaymentMethods = root.getJSONArray("allowedPaymentMethods")
        assertEquals(1, allowedPaymentMethods.length())

        val cardMethod = allowedPaymentMethods.getJSONObject(0)
        assertEquals("CARD", cardMethod.getString("type"))

        val parameters = cardMethod.getJSONObject("parameters")
        val allowedAuthMethods = parameters.getJSONArray("allowedAuthMethods")
        val authList = (0 until allowedAuthMethods.length()).map { allowedAuthMethods.getString(it) }
        assertTrue("Debe incluir PAN_ONLY", authList.contains("PAN_ONLY"))
        assertTrue("Debe incluir CRYPTOGRAM_3DS", authList.contains("CRYPTOGRAM_3DS"))

        val allowedCardNetworks = parameters.getJSONArray("allowedCardNetworks")
        val networkList = (0 until allowedCardNetworks.length()).map { allowedCardNetworks.getString(it) }
        assertTrue("Debe incluir VISA", networkList.contains("VISA"))
        assertTrue("Debe incluir MASTERCARD", networkList.contains("MASTERCARD"))
        assertTrue("Debe incluir AMEX", networkList.contains("AMEX"))
    }

    @Test
    fun testPaymentDataRequestCumpleConApiVersion2MonedaMxnYPaisMx() {
        val precioPrueba = 150.0
        val jsonStr = GooglePayHelper.obtenerPaymentDataRequestJson(
            precioMxn = precioPrueba,
            environment = WalletConstants.ENVIRONMENT_TEST
        )
        assertNotNull(jsonStr)

        val root = JSONObject(jsonStr)
        assertEquals("apiVersion debe ser 2", 2, root.getInt("apiVersion"))
        assertEquals("apiVersionMinor debe ser 0", 0, root.getInt("apiVersionMinor"))

        // Validar transactionInfo para México
        val transactionInfo = root.getJSONObject("transactionInfo")
        assertEquals("currencyCode debe ser MXN", "MXN", transactionInfo.getString("currencyCode"))
        assertEquals("countryCode debe ser MX", "MX", transactionInfo.getString("countryCode"))
        assertEquals("totalPriceStatus debe ser FINAL", "FINAL", transactionInfo.getString("totalPriceStatus"))
        assertEquals("totalPrice debe tener 2 decimales", "150.00", transactionInfo.getString("totalPrice"))

        // Validar tokenizationSpecification para ENVIRONMENT_TEST
        val allowedPaymentMethods = root.getJSONArray("allowedPaymentMethods")
        val cardMethod = allowedPaymentMethods.getJSONObject(0)
        val tokenizationSpec = cardMethod.getJSONObject("tokenizationSpecification")
        assertEquals("PAYMENT_GATEWAY", tokenizationSpec.getString("type"))

        val tokenParams = tokenizationSpec.getJSONObject("parameters")
        assertEquals("example", tokenParams.getString("gateway"))
        assertEquals("exampleGatewayMerchantId", tokenParams.getString("gatewayMerchantId"))

        // Validar merchantInfo
        val merchantInfo = root.getJSONObject("merchantInfo")
        assertEquals("Velvet VIP Live México", merchantInfo.getString("merchantName"))
    }

    @Test
    fun testPaymentDataRequestConfiguracionProduccionParametrizada() {
        val jsonProd = GooglePayHelper.obtenerPaymentDataRequestJson(
            precioMxn = 300.0,
            environment = WalletConstants.ENVIRONMENT_PRODUCTION
        )
        val rootProd = JSONObject(jsonProd)
        val tokenParams = rootProd.getJSONArray("allowedPaymentMethods")
            .getJSONObject(0)
            .getJSONObject("tokenizationSpecification")
            .getJSONObject("parameters")

        assertEquals("mercadopago", tokenParams.getString("gateway"))
        assertEquals("velvet_vip_mercadopago_prod", tokenParams.getString("gatewayMerchantId"))

        // Validar que en producción merchantInfo incluya obligatoriamente merchantId
        val merchantInfo = rootProd.getJSONObject("merchantInfo")
        assertEquals(GooglePayHelper.DEFAULT_MERCHANT_NAME, merchantInfo.getString("merchantName"))
        assertEquals(GooglePayHelper.MERCHANT_ID_PRODUCTION, merchantInfo.getString("merchantId"))
    }

    @Test
    fun testPaymentDataRequestPermitePersonalizarMerchantId() {
        val customMerchantId = "BCR2DN6TVIPTEST"
        val jsonCustom = GooglePayHelper.obtenerPaymentDataRequestJson(
            precioMxn = 50.0,
            environment = WalletConstants.ENVIRONMENT_PRODUCTION,
            merchantId = customMerchantId
        )
        val rootCustom = JSONObject(jsonCustom)
        val merchantInfo = rootCustom.getJSONObject("merchantInfo")
        assertEquals(customMerchantId, merchantInfo.getString("merchantId"))
    }

    @Test
    fun testAllowedPaymentMethodsArrayParaBotonComposePayButton() {
        val arrayStr = GooglePayHelper.obtenerAllowedPaymentMethodsArrayJson()
        val array = JSONArray(arrayStr)
        assertEquals(1, array.length())
        val item = array.getJSONObject(0)
        assertEquals("CARD", item.getString("type"))
    }

    @Test
    fun testMapeoDePaquetesDePreciosEnMxnASegundosEsExacto() {
        // Paquete Bronce: $50.00 MXN -> 300s
        assertEquals(300, GooglePayHelper.obtenerSegundosPorMonto(50.0))

        // Paquete Plata: $150.00 MXN -> 1,200s
        assertEquals(1200, GooglePayHelper.obtenerSegundosPorMonto(150.0))

        // Paquete Velvet VIP: $300.00 MXN -> 3,000s
        assertEquals(3000, GooglePayHelper.obtenerSegundosPorMonto(300.0))

        // Catálogo VIP completo
        val catalogo = GooglePayHelper.PAQUETES_VIP
        assertEquals(3, catalogo.size)
        assertEquals("bronce", catalogo[0].id)
        assertEquals("plata", catalogo[1].id)
        assertEquals("velvet_vip", catalogo[2].id)

        for (paquete in catalogo) {
            assertTrue("Precio debe ser mayor a 0", paquete.precioMxn > 0)
            assertTrue("Segundos deben ser mayores a 0", paquete.segundos > 0)
            assertTrue("Nombre no debe estar vacío", paquete.nombre.isNotBlank())
            assertTrue("Descripción no debe estar vacía", paquete.descripcion.isNotBlank())
        }
    }

    @Test
    fun testMapeoDePreciosRechazaMontosNegativosONulos() {
        // Nulo explícito
        assertThrows(IllegalArgumentException::class.java) {
            GooglePayHelper.obtenerSegundosPorMonto(null)
        }

        // Cero
        assertThrows(IllegalArgumentException::class.java) {
            GooglePayHelper.obtenerSegundosPorMonto(0.0)
        }

        // Negativo
        assertThrows(IllegalArgumentException::class.java) {
            GooglePayHelper.obtenerSegundosPorMonto(-50.0)
        }

        // Negativo fraccionario
        assertThrows(IllegalArgumentException::class.java) {
            GooglePayHelper.obtenerSegundosPorMonto(-0.01)
        }

        // NaN e Infinitos
        assertThrows(IllegalArgumentException::class.java) {
            GooglePayHelper.obtenerSegundosPorMonto(Double.NaN)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GooglePayHelper.obtenerSegundosPorMonto(Double.POSITIVE_INFINITY)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GooglePayHelper.obtenerSegundosPorMonto(Double.NEGATIVE_INFINITY)
        }

        // Monto no registrado en catálogo VIP
        assertThrows(IllegalArgumentException::class.java) {
            GooglePayHelper.obtenerSegundosPorMonto(999.0)
        }
    }

    @Test
    fun testParserResultadoPagoExtraeTokenRedYDetallesCorrectamente() {
        val jsonMock = """
            {
              "apiVersion": 2,
              "apiVersionMinor": 0,
              "paymentMethodData": {
                "description": "Mastercard •••• 8888",
                "tokenizationData": {
                  "type": "PAYMENT_GATEWAY",
                  "token": "{\"id\":\"tok_mx_googlepay_sample\",\"status\":\"ACTIVE\"}"
                },
                "type": "CARD",
                "info": {
                  "cardNetwork": "MASTERCARD",
                  "cardDetails": "8888"
                }
              }
            }
        """.trimIndent()

        val resultado = GooglePayHelper.parsearResultadoPago(jsonMock)
        assertEquals("{\"id\":\"tok_mx_googlepay_sample\",\"status\":\"ACTIVE\"}", resultado.token)
        assertEquals("MASTERCARD", resultado.cardNetwork)
        assertEquals("8888", resultado.cardDetails)
        assertEquals("Mastercard •••• 8888", resultado.description)
    }

    @Test
    fun testParserManejaCamposVaciosYInfoFaltante() {
        // Red y detalles con espacios o cadenas vacías
        val jsonConCamposVacios = """
            {
              "apiVersion": 2,
              "paymentMethodData": {
                "tokenizationData": {
                  "token": "tok_fallback_test"
                },
                "info": {
                  "cardNetwork": "   ",
                  "cardDetails": ""
                }
              }
            }
        """.trimIndent()

        val resultadoVacio = GooglePayHelper.parsearResultadoPago(jsonConCamposVacios)
        assertEquals("tok_fallback_test", resultadoVacio.token)
        assertEquals("DESCONOCIDA", resultadoVacio.cardNetwork)
        assertEquals("••••", resultadoVacio.cardDetails)

        // Sin objeto info en paymentMethodData
        val jsonSinInfo = """
            {
              "apiVersion": 2,
              "paymentMethodData": {
                "tokenizationData": {
                  "token": "tok_no_info"
                }
              }
            }
        """.trimIndent()

        val resultadoSinInfo = GooglePayHelper.parsearResultadoPago(jsonSinInfo)
        assertEquals("tok_no_info", resultadoSinInfo.token)
        assertEquals("DESCONOCIDA", resultadoSinInfo.cardNetwork)
        assertEquals("••••", resultadoSinInfo.cardDetails)
    }

    @Test
    fun testParserManejaJsonIncompletoOErroneo() {
        // Cadena vacía
        assertThrows(IllegalArgumentException::class.java) {
            GooglePayHelper.parsearResultadoPago("")
        }

        // Sin tokenizationData
        val sinToken = """
            {
              "paymentMethodData": {
                "type": "CARD"
              }
            }
        """.trimIndent()
        assertThrows(Exception::class.java) {
            GooglePayHelper.parsearResultadoPago(sinToken)
        }
    }

    @Test
    fun testGeneradorHashTokenSha256() {
        val token = "tok_test_secure_12345"
        val hash = GooglePayHelper.generarHashToken(token)

        assertNotNull(hash)
        assertEquals(64, hash.length) // SHA-256 produce 64 caracteres hexadecimales

        // Determinismo
        val hashRepetido = GooglePayHelper.generarHashToken(token)
        assertEquals(hash, hashRepetido)

        // Rechazo de cadena vacía o en blanco
        assertThrows(IllegalArgumentException::class.java) {
            GooglePayHelper.generarHashToken("")
        }
        assertThrows(IllegalArgumentException::class.java) {
            GooglePayHelper.generarHashToken("   ")
        }
    }

    @Test
    fun testConstruirDatosTransaccionParaAuditoriaFirestore() {
        val resultado = GooglePayPaymentResult(
            token = "tok_google_mxn_sample",
            cardNetwork = "VISA",
            cardDetails = "1234",
            description = "Visa terminada en 1234"
        )

        val datos = GooglePayHelper.construirDatosTransaccion(
            uid = "gamer_test_001",
            resultado = resultado,
            montoMxn = 150.0,
            segundosAcreditados = 1200
        )

        assertEquals("gamer_test_001", datos["uid"])
        assertEquals("GOOGLE_PAY", datos["metodo"])
        assertEquals("VISA", datos["redTarjeta"])
        assertEquals("1234", datos["cardDetails"])
        assertEquals(150.0, datos["montoMxn"])
        assertEquals(1200, datos["segundosAcreditados"])
        assertEquals(GooglePayHelper.generarHashToken("tok_google_mxn_sample"), datos["tokenHash"])
        assertEquals("Visa terminada en 1234", datos["descripcion"])
        assertNotNull(datos["timestamp"])

        // Validación de invariantes
        assertThrows(IllegalArgumentException::class.java) {
            GooglePayHelper.construirDatosTransaccion("", resultado, 150.0, 1200)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GooglePayHelper.construirDatosTransaccion("gamer_1", resultado, -50.0, 1200)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GooglePayHelper.construirDatosTransaccion("gamer_1", resultado, 150.0, 0)
        }
    }

    @Test
    fun testGooglePayViewModelFlujoReactivo() {
        val viewModel = GooglePayViewModel()

        // Estado reactivo inicial
        assertFalse(viewModel.isReadyToPay.value)

        // Verificación simulada
        viewModel.setIsReadyToPay(true)
        assertTrue(viewModel.isReadyToPay.value)

        // Selección de paquete
        val paqueteOro = GooglePayHelper.PAQUETES_VIP[2]
        viewModel.seleccionarPaquete(paqueteOro)
        assertEquals("velvet_vip", viewModel.paqueteSeleccionado.value.id)

        // Ciclo de estados de pago
        viewModel.setProcesando()
        assertTrue(viewModel.estadoPago.value is EstadoGooglePay.Procesando)

        val mockResult = GooglePayPaymentResult("tok", "AMEX", "0005", "Amex")
        viewModel.notificarExito("txn_123", paqueteOro, mockResult)
        val exito = viewModel.estadoPago.value as EstadoGooglePay.Exito
        assertEquals("txn_123", exito.transaccionId)
        assertEquals("velvet_vip", exito.paquete.id)

        viewModel.reiniciarEstado()
        assertTrue(viewModel.estadoPago.value is EstadoGooglePay.Idle)
    }
}
