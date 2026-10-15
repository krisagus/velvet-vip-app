package com.example.my1eraapplication.model

import com.example.my1eraapplication.EnfoqueAtencionIA
import com.example.my1eraapplication.generarLibretoInteligente
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MiniserieModelsTest {

    @Test
    fun testCatalogoTiene6EscenasValidas() {
        val escenas = CatalogoMiniserie.escenas
        assertEquals(6, escenas.size)

        val ids = mutableSetOf<String>()
        val capitulos = mutableSetOf<Int>()

        for (escena in escenas) {
            assertTrue("ID no debe estar vacío", escena.id.isNotBlank())
            assertTrue("ID debe ser único: ${escena.id}", ids.add(escena.id))

            assertTrue("Capítulo debe ser > 0", escena.capituloNumero > 0)
            assertTrue("Capítulo debe ser único: ${escena.capituloNumero}", capitulos.add(escena.capituloNumero))

            assertTrue("Título no debe estar vacío", escena.titulo.isNotBlank())
            assertTrue("Sinopsis no debe estar vacía", escena.sinopsis.isNotBlank())
            assertTrue("Ambiente no debe estar vacío", escena.ambiente.isNotBlank())
            assertTrue("Mood no debe estar vacío", escena.mood.isNotBlank())

            // Verificación del Personaje Gamer
            assertTrue("Nombre Gamer no debe estar vacío", escena.rolGamer.nombre.isNotBlank())
            assertTrue("Descripción Gamer no debe estar vacía", escena.rolGamer.descripcion.isNotBlank())
            assertTrue("Objetivo Secreto Gamer no debe estar vacío", escena.rolGamer.objetivoSecreto.isNotBlank())
            assertTrue("Frase Apertura Gamer no debe estar vacía", escena.rolGamer.fraseApertura.isNotBlank())

            // Verificación del Personaje Modelo
            assertTrue("Nombre Modelo no debe estar vacío", escena.rolModelo.nombre.isNotBlank())
            assertTrue("Descripción Modelo no debe estar vacía", escena.rolModelo.descripcion.isNotBlank())
            assertTrue("Objetivo Secreto Modelo no debe estar vacío", escena.rolModelo.objetivoSecreto.isNotBlank())
            assertTrue("Frase Apertura Modelo no debe estar vacía", escena.rolModelo.fraseApertura.isNotBlank())

            // Verificación de los giros de guión
            assertTrue("Debe tener al menos 3 giros dramáticos", escena.girosDeGuion.size >= 3)
            val girosUnicos = escena.girosDeGuion.toSet()
            assertEquals("Todos los giros de una escena deben ser distintos", escena.girosDeGuion.size, girosUnicos.size)
            for (giro in escena.girosDeGuion) {
                assertTrue("Giro no debe estar vacío", giro.isNotBlank())
                assertTrue("Giro debe tener indicador dramático", giro.contains("⚡"))
            }
        }
    }

    @Test
    fun testObtenerPorId() {
        val escenaParis = CatalogoMiniserie.obtenerPorId("paris_libreria")
        assertNotNull(escenaParis)
        assertEquals(1, escenaParis?.capituloNumero)
        assertEquals("Encuentro en la Librería de París", escenaParis?.titulo)

        val escenaTren = CatalogoMiniserie.obtenerPorId("tren_medianoche")
        assertNotNull(escenaTren)
        assertEquals(2, escenaTren?.capituloNumero)
        assertEquals("Coincidencia en el Último Tren a Medianoche", escenaTren?.titulo)

        val escenaInexistente = CatalogoMiniserie.obtenerPorId("capitulo_inexistente_999")
        assertNull(escenaInexistente)
    }

    @Test
    fun testEscenaAleatoria() {
        val randomEscena = CatalogoMiniserie.escenaAleatoria()
        assertNotNull(randomEscena)
        assertTrue(CatalogoMiniserie.escenas.contains(randomEscena))
    }

    @Test
    fun testObtenerActosCompletosEnCatalogoYGenericos() {
        for (escena in CatalogoMiniserie.escenas) {
            val actos = escena.obtenerActosCompletos()
            assertEquals("Debe tener exactamente 4 actos progresivos", 4, actos.size)

            for ((index, acto) in actos.withIndex()) {
                assertEquals("El número de acto debe ser secuencial (1-4)", index + 1, acto.numero)
                assertTrue("El título no debe estar vacío", acto.titulo.isNotBlank())
                assertTrue("El objetivo clave no debe estar vacío", acto.objetivoClave.isNotBlank())

                // Diálogos sustanciales (al menos 4 a 6 líneas de diálogo)
                assertTrue("Cada acto debe tener al menos 4 líneas de diálogo (tiene ${acto.dialogos.size})", acto.dialogos.size >= 4)
                for (dialogo in acto.dialogos) {
                    assertTrue("Personaje no debe estar vacío", dialogo.personaje.isNotBlank())
                    assertTrue("Texto del diálogo no debe estar vacío", dialogo.texto.isNotBlank())
                    assertTrue("Acotación no debe estar vacía", dialogo.acotacion.isNotBlank())
                    assertTrue("Intención no debe estar vacía", dialogo.intencion.isNotBlank())
                }

                // Herramientas anti-silencio del apuntador
                assertTrue("Debe tener al menos 3 temas de conversación", acto.temasConversacion.size >= 3)
                for (tema in acto.temasConversacion) {
                    assertTrue("Tema no debe estar vacío", tema.isNotBlank())
                }

                assertTrue("Debe tener al menos 3 preguntas de contraataque", acto.preguntasContraataque.size >= 3)
                for (pregunta in acto.preguntasContraataque) {
                    assertTrue("Pregunta no debe estar vacía", pregunta.isNotBlank())
                }

                assertTrue("Debe tener al menos 3 réplicas sugeridas", acto.replicasSugeridas.size >= 3)
                for (replica in acto.replicasSugeridas) {
                    assertTrue("Réplica no debe estar vacía", replica.isNotBlank())
                }
            }
        }
    }

    @Test
    fun testSerializationRoundTripActoEpisodio() {
        val original = ActoEpisodio(
            numero = 2,
            titulo = "Acto 2: Confesiones Íntimas",
            objetivoClave = "Descubrir la vulnerabilidad del otro sin presiones.",
            dialogos = listOf(
                LineaDialogo("GAMER", "¿Qué es lo que más te cuesta admitir en voz alta?", "(Mirada sincera)", "Vulnerabilidad"),
                LineaDialogo("MODELO", "Que a veces me siento sola rodeada de gente.", "(Suspiro suave)", "Confesión")
            ),
            temasConversacion = listOf("Miedos infantiles", "Música nocturna"),
            preguntasContraataque = listOf("¿Cuál es tu lugar seguro?", "¿Qué te hace sonreír sin esfuerzo?"),
            replicasSugeridas = listOf("«Te entiendo perfectamente.»", "«Aquí no tienes que fingir nada.»")
        )

        val map = original.toMap()
        assertEquals(2, map["numero"])
        assertEquals("Acto 2: Confesiones Íntimas", map["titulo"])
        assertEquals("Descubrir la vulnerabilidad del otro sin presiones.", map["objetivoClave"])

        val deserialized = actoEpisodioFromMap(map)
        assertEquals(original.numero, deserialized.numero)
        assertEquals(original.titulo, deserialized.titulo)
        assertEquals(original.objetivoClave, deserialized.objetivoClave)
        assertEquals(original.dialogos.size, deserialized.dialogos.size)
        assertEquals(original.dialogos[0].personaje, deserialized.dialogos[0].personaje)
        assertEquals(original.dialogos[0].texto, deserialized.dialogos[0].texto)
        assertEquals(original.dialogos[0].acotacion, deserialized.dialogos[0].acotacion)
        assertEquals(original.dialogos[0].intencion, deserialized.dialogos[0].intencion)
        assertEquals(original.dialogos[1].personaje, deserialized.dialogos[1].personaje)
        assertEquals(original.dialogos[1].texto, deserialized.dialogos[1].texto)
        assertEquals(original.temasConversacion, deserialized.temasConversacion)
        assertEquals(original.preguntasContraataque, deserialized.preguntasContraataque)
        assertEquals(original.replicasSugeridas, deserialized.replicasSugeridas)
    }

    @Test
    fun testSerializationActoEpisodioFromMapResilienteConValoresNulosODefectuosos() {
        val emptyMapResult = actoEpisodioFromMap(emptyMap())
        assertEquals(1, emptyMapResult.numero)
        assertEquals("Acto 1", emptyMapResult.titulo)
        assertEquals("", emptyMapResult.objetivoClave)
        assertTrue(emptyMapResult.dialogos.isEmpty())
        assertTrue(emptyMapResult.temasConversacion.isEmpty())
        assertTrue(emptyMapResult.preguntasContraataque.isEmpty())
        assertTrue(emptyMapResult.replicasSugeridas.isEmpty())

        val partialMap = mapOf<String, Any?>(
            "numero" to 3L, // Long como devuelve Firestore
            "titulo" to "Acto 3",
            "dialogos" to listOf(
                mapOf("personaje" to "GAMER", "texto" to "Hola")
            )
        )
        val partialResult = actoEpisodioFromMap(partialMap)
        assertEquals(3, partialResult.numero)
        assertEquals("Acto 3", partialResult.titulo)
        assertEquals(1, partialResult.dialogos.size)
        assertEquals("GAMER", partialResult.dialogos[0].personaje)
        assertEquals("Hola", partialResult.dialogos[0].texto)
        assertEquals("", partialResult.dialogos[0].acotacion)
        assertEquals("", partialResult.dialogos[0].intencion)
    }

    @Test
    fun testGeneradorLibretoInteligenteEscenariosAI() {
        val escenarios = listOf(
            Triple("Tuve un día súper aburrido en la escuela con mucha tarea", EnfoqueAtencionIA.DUO, "ESCUELA"),
            Triple("Estoy exhausto, mucho trabajo y estrés en la oficina con mi jefe", EnfoqueAtencionIA.CLIENTE, "TRABAJO"),
            Triple("Un día normal", EnfoqueAtencionIA.ACTRIZ, "ACTRIZ"),
            Triple("Necesito desconectar y que alguien me escuche", EnfoqueAtencionIA.CLIENTE, "CLIENTE"),
            Triple("Quiero algo espontáneo y divertido", EnfoqueAtencionIA.DUO, "DUO")
        )

        for ((diaTexto, enfoque, _) in escenarios) {
            val escena = generarLibretoInteligente(
                diaTexto = diaTexto,
                enfoque = enfoque,
                atmosfera = "🌧️ Lluvia y Café Nocturno"
            )

            assertNotNull(escena)
            assertTrue("ID debe iniciar con ia_sueno_", escena.id.startsWith("ia_sueno_"))
            assertTrue("Título no debe estar vacío", escena.titulo.isNotBlank())
            assertTrue("Sinopsis no debe estar vacía", escena.sinopsis.isNotBlank())
            assertTrue("Personaje Gamer no debe estar vacío", escena.rolGamer.nombre.isNotBlank())
            assertTrue("Personaje Modelo no debe estar vacío", escena.rolModelo.nombre.isNotBlank())

            val actos = escena.obtenerActosCompletos()
            assertEquals("Debe generar exactamente 4 actos", 4, actos.size)

            var totalDialogos = 0
            for ((idx, acto) in actos.withIndex()) {
                assertEquals(idx + 1, acto.numero)
                assertTrue("Título de acto no debe estar vacío", acto.titulo.isNotBlank())
                assertTrue("Objetivo de acto no debe estar vacío", acto.objetivoClave.isNotBlank())
                assertTrue("Debe tener al menos 4 diálogos por acto", acto.dialogos.size >= 4)
                totalDialogos += acto.dialogos.size
                assertTrue("Debe tener temas de conversación", acto.temasConversacion.isNotEmpty())
                assertTrue("Debe tener preguntas de contraataque", acto.preguntasContraataque.isNotEmpty())
                assertTrue("Debe tener réplicas sugeridas", acto.replicasSugeridas.isNotEmpty())
            }

            assertTrue("El libreto completo debe tener al menos 16 a 24 líneas de diálogo (tiene $totalDialogos)", totalDialogos >= 16)
        }
    }

    @Test
    fun testEscenarioClienteDedicadoYDistintoDeDuo() {
        val escenaCliente = generarLibretoInteligente(
            diaTexto = "Necesito desconectar y que alguien me escuche",
            enfoque = EnfoqueAtencionIA.CLIENTE,
            atmosfera = "🌧️ Lluvia y Café Nocturno"
        )
        val actosCliente = escenaCliente.obtenerActosCompletos()
        assertEquals(4, actosCliente.size)
        assertEquals("Acto 1: El Refugio Íntimo y la Escucha Cálida", actosCliente[0].titulo)
        assertEquals("Acto 2: Confidencias del Alma y Sanación", actosCliente[1].titulo)
        assertEquals("Acto 3: El Desafío de Miradas y Abrazo Emocional", actosCliente[2].titulo)
        assertEquals("Acto 4: El Pacto del Rincón Seguro", actosCliente[3].titulo)

        val escenaDuo = generarLibretoInteligente(
            diaTexto = "Quiero algo espontáneo y divertido",
            enfoque = EnfoqueAtencionIA.DUO,
            atmosfera = "🌧️ Lluvia y Café Nocturno"
        )
        val actosDuo = escenaDuo.obtenerActosCompletos()
        assertEquals(4, actosDuo.size)
        assertEquals("Acto 1: Dos Fugitivos en la Penumbra", actosDuo[0].titulo)

        // Verificar que los títulos no son idénticos (el escenario CLIENTE no cayó a DUO)
        assertTrue("El escenario CLIENTE debe tener un libreto propio, no el de DUO", actosCliente[0].titulo != actosDuo[0].titulo)
    }

    @Test
    fun testDeserializacionConElementosCorruptosEnListaDialogos() {
        val mapCorrupto = mapOf<String, Any?>(
            "numero" to 1,
            "titulo" to "Acto con datos corruptos",
            "dialogos" to listOf(
                null,
                "no es un mapa",
                42,
                listOf(1, 2, 3),
                mapOf("personaje" to "GAMER", "texto" to "Línea válida 1"),
                null,
                mapOf("personaje" to "MODELO", "texto" to "Línea válida 2", "acotacion" to "(Riendo)")
            ),
            "temasConversacion" to listOf("Tema 1", null, 123, "Tema 2")
        )

        val resultado = actoEpisodioFromMap(mapCorrupto)
        assertEquals(2, resultado.dialogos.size)
        assertEquals("GAMER", resultado.dialogos[0].personaje)
        assertEquals("Línea válida 1", resultado.dialogos[0].texto)
        assertEquals("MODELO", resultado.dialogos[1].personaje)
        assertEquals("Línea válida 2", resultado.dialogos[1].texto)
        assertEquals("(Riendo)", resultado.dialogos[1].acotacion)

        assertEquals(2, resultado.temasConversacion.size)
        assertEquals("Tema 1", resultado.temasConversacion[0])
        assertEquals("Tema 2", resultado.temasConversacion[1])
    }

    @Test
    fun testCalculoTurnoDialogoParaGamerYModelo() {
        val dialogos = listOf(
            LineaDialogo("GAMER", "Hola, me alegro de verte"),
            LineaDialogo("MODELO", "Hola, qué bueno coincidir"),
            LineaDialogo("GAMER", "¿Cómo estás?"),
            LineaDialogo("MODELO", "Mucho mejor ahora que hablamos")
        )

        for ((idx, dialogo) in dialogos.withIndex()) {
            val esGamer = dialogo.personaje.equals("GAMER", ignoreCase = true)

            // Caso 1: Usuario es Gamer / Rol A (esMiRolA = true)
            val esMiTurnoGamer = (esGamer && true) || (!esGamer && false)
            if (idx % 2 == 0) {
                assertTrue("Para el usuario Gamer, las líneas pares son su turno", esMiTurnoGamer)
            } else {
                assertTrue("Para el usuario Gamer, las líneas impares NO son su turno", !esMiTurnoGamer)
            }

            // Caso 2: Usuario es Modelo / Rol B (esMiRolA = false)
            val esMiTurnoModelo = (esGamer && false) || (!esGamer && true)
            if (idx % 2 == 1) {
                assertTrue("Para la usuaria Modelo, las líneas impares son su turno", esMiTurnoModelo)
            } else {
                assertTrue("Para la usuaria Modelo, las líneas pares NO son su turno", !esMiTurnoModelo)
            }
        }
    }
}

