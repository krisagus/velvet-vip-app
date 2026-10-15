package com.example.my1eraapplication.payment

import com.example.my1eraapplication.googlepay.GooglePayHelper
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentGatewayIntegrationTest {

    // ==========================================
    // PRUEBAS DE INTEGRACIÓN: MERCADO PAGO MÉXICO
    // ==========================================

    @Test
    fun testConstruirPreferenciaMercadoPagoEstructuraValida() {
        val paquete = GooglePayHelper.PAQUETES_VIP.first { it.id == "plata" } // $150 MXN, 1200s
        val externalRef = "VELVET-MP-TEST-12345"

        val json = MercadoPagoHelper.construirPreferenciaJson(
            paquete = paquete,
            externalReference = externalRef,
            payerEmail = "gamer.vip@velvet.app"
        )

        assertNotNull(json)
        assertEquals(externalRef, json.getString("external_reference"))
        assertEquals("approved", json.getString("auto_return"))
        assertEquals("VELVET VIP", json.getString("statement_descriptor"))

        // Validar Items
        val items = json.getJSONArray("items")
        assertEquals(1, items.length())
        val item0 = items.getJSONObject(0)
        assertEquals("plata", item0.getString("id"))
        assertEquals(150.0, item0.getDouble("unit_price"), 0.001)
        assertEquals("MXN", item0.getString("currency_id"))
        assertEquals(1, item0.getInt("quantity"))
        assertTrue(item0.getString("title").contains("Plata"))

        // Validar Back URLs
        val backUrls = json.getJSONObject("back_urls")
        assertTrue(backUrls.getString("success").contains("pago_exitoso.html"))
        assertTrue(backUrls.getString("failure").contains("pago_fallido.html"))
        assertTrue(backUrls.getString("pending").contains("pago_pendiente.html"))

        // Validar Payer
        val payer = json.getJSONObject("payer")
        assertEquals("gamer.vip@velvet.app", payer.getString("email"))
    }

    @Test
    fun testPreferenciaTodosLosPaquetesVipMercadoPago() {
        GooglePayHelper.PAQUETES_VIP.forEach { paquete ->
            val ref = "REF-${paquete.id}"
            val json = MercadoPagoHelper.construirPreferenciaJson(paquete, ref)
            val items = json.getJSONArray("items")
            val item0 = items.getJSONObject(0)
            assertEquals(paquete.precioMxn, item0.getDouble("unit_price"), 0.001)
            assertEquals("MXN", item0.getString("currency_id"))
        }
    }

    // ==========================================
    // PRUEBAS DE INTEGRACIÓN: CRIPTOMONEDAS (USDT)
    // ==========================================

    @Test
    fun testCotizacionCriptoUsdtCalculoExactoTasaBase() {
        // Paquete Bronce: $50 MXN -> 2.50 USDT (Tasa 20.0)
        val bronce = GooglePayHelper.PAQUETES_VIP.first { it.id == "bronce" }
        val cotizBronce = CryptoPaymentHelper.calcularCotizacion(bronce, RedCripto.TRC20, tasaMxnUsdt = 20.0)
        assertEquals(2.50, cotizBronce.montoUsdt, 0.001)
        assertEquals("2.50", cotizBronce.montoUsdtFormateado)
        assertEquals("USDT", cotizBronce.red.token)
        assertEquals(RedCripto.TRC20, cotizBronce.red)

        // Paquete Plata: $150 MXN -> 7.50 USDT (Tasa 20.0)
        val plata = GooglePayHelper.PAQUETES_VIP.first { it.id == "plata" }
        val cotizPlata = CryptoPaymentHelper.calcularCotizacion(plata, RedCripto.POLYGON, tasaMxnUsdt = 20.0)
        assertEquals(7.50, cotizPlata.montoUsdt, 0.001)
        assertEquals("7.50", cotizPlata.montoUsdtFormateado)
        assertEquals(RedCripto.POLYGON, cotizPlata.red)

        // Paquete Velvet VIP: $300 MXN -> 15.00 USDT (Tasa 20.0)
        val velvet = GooglePayHelper.PAQUETES_VIP.first { it.id == "velvet_vip" }
        val cotizVelvet = CryptoPaymentHelper.calcularCotizacion(velvet, RedCripto.BEP20, tasaMxnUsdt = 20.0)
        assertEquals(15.00, cotizVelvet.montoUsdt, 0.001)
        assertEquals("15.00", cotizVelvet.montoUsdtFormateado)
        assertEquals(RedCripto.BEP20, cotizVelvet.red)
    }

    @Test
    fun testValidacionDireccionesBilleteraTronTrc20() {
        val direccionValida = "TYDnyPshWjE9Y3vGkK7e1B6XqXk3vP2jKL" // 34 chars, inicia con T
        assertTrue(CryptoPaymentHelper.validarDireccionWallet(direccionValida, RedCripto.TRC20))

        // Direcciones inválidas
        assertFalse("Debe fallar si no empieza con T", CryptoPaymentHelper.validarDireccionWallet("0x71C8360f3a8b4f177F8A7D373A8e1545A2594639", RedCripto.TRC20))
        assertFalse("Debe fallar por longitud incorrecta", CryptoPaymentHelper.validarDireccionWallet("TYDnyPshWjE9Y3", RedCripto.TRC20))
        assertFalse("Debe fallar si contiene caracteres raros", CryptoPaymentHelper.validarDireccionWallet("TYDnyPshWjE9Y3vGkK7e1B6XqXk3vP2j!@", RedCripto.TRC20))
    }

    @Test
    fun testValidacionDireccionesBilleteraPolygonYEVM() {
        val direccionValida = "0x71C8360f3a8b4f177F8A7D373A8e1545A2594639" // 42 chars hex, inicia con 0x
        assertTrue(CryptoPaymentHelper.validarDireccionWallet(direccionValida, RedCripto.POLYGON))
        assertTrue(CryptoPaymentHelper.validarDireccionWallet(direccionValida, RedCripto.BEP20))

        // Direcciones inválidas
        assertFalse("Debe fallar si no empieza con 0x", CryptoPaymentHelper.validarDireccionWallet("71C8360f3a8b4f177F8A7D373A8e1545A2594639", RedCripto.POLYGON))
        assertFalse("Debe fallar si tiene caracteres no hex", CryptoPaymentHelper.validarDireccionWallet("0x71C8360f3a8b4f177F8A7D373A8e1545A25946ZZ", RedCripto.POLYGON))
        assertFalse("Debe fallar por longitud corta", CryptoPaymentHelper.validarDireccionWallet("0x71C8360f", RedCripto.POLYGON))
    }

    @Test
    fun testValidacionTxHashTransaccion() {
        val hashTron = "4a7c8d9e2b1f0c3a8d7e6f5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c"
        val hashEvm = "0x4a7c8d9e2b1f0c3a8d7e6f5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c"

        assertTrue(CryptoPaymentHelper.validarTxHash(hashTron))
        assertTrue(CryptoPaymentHelper.validarTxHash(hashEvm))
        assertFalse("Debe fallar con hash muy corto", CryptoPaymentHelper.validarTxHash("123"))
    }

    @Test
    fun testRedesCriptoPropiedadesOficiales() {
        val redes = RedCripto.values()
        assertEquals(3, redes.size)

        val trc20 = RedCripto.TRC20
        assertEquals("USDT", trc20.token)
        assertTrue(trc20.walletPorDefecto.startsWith("T"))
        assertTrue(trc20.exploradorUrl.contains("tronscan"))

        val polygon = RedCripto.POLYGON
        assertEquals("USDT", polygon.token)
        assertTrue(polygon.walletPorDefecto.startsWith("0x"))
        assertTrue(polygon.exploradorUrl.contains("polygonscan"))

        val bep20 = RedCripto.BEP20
        assertEquals("USDT", bep20.token)
        assertTrue(bep20.walletPorDefecto.startsWith("0x"))
        assertTrue(bep20.exploradorUrl.contains("bscscan"))
    }
}
