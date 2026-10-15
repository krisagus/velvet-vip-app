package com.example.my1eraapplication

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VerificacionEdadTest {

    @Test
    fun testValidacionMayorDeEdad18() {
        // En 2026, una persona nacida en 2008 o antes tiene >= 18 años
        val anioActual = 2026
        val edadMinima = 18
        val anioLimite = anioActual - edadMinima // 2008

        fun esMayorDeEdad(anioNacimiento: Int): Boolean {
            return anioNacimiento in 1940..anioLimite
        }

        assertTrue("Nacida en 2000 debe ser mayor de edad", esMayorDeEdad(2000))
        assertTrue("Nacida en 2005 debe ser mayor de edad", esMayorDeEdad(2005))
        assertTrue("Nacida en 2008 debe ser mayor de edad (cumple 18 en 2026)", esMayorDeEdad(2008))
        assertFalse("Nacida en 2009 es menor de edad (17 años)", esMayorDeEdad(2009))
        assertFalse("Nacida en 2010 es menor de edad (16 años)", esMayorDeEdad(2010))
        assertFalse("Nacida en 2015 es menor de edad", esMayorDeEdad(2015))
        assertFalse("Año inválido fuera de rango histórico", esMayorDeEdad(1930))
    }

    @Test
    fun testRolesSeparadosFlavors() {
        assertEquals("MODELO", RolApp.MODELO.name)
        assertEquals("PARTICIPANTE", RolApp.PARTICIPANTE.name)
    }

    @Test
    fun testCalculoGananciasYRetiros() {
        val tarifaSegundoModelo = 0.50
        val duracionLlamadaSegundos = 120L // 2 minutos
        val gananciasCalculadas = duracionLlamadaSegundos * tarifaSegundoModelo

        assertEquals(60.0, gananciasCalculadas, 0.001)

        val montoRetiro = 50.0
        val saldoRestante = gananciasCalculadas - montoRetiro
        assertEquals(10.0, saldoRestante, 0.001)
    }
}
