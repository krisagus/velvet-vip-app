package com.example.my1eraapplication

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my1eraapplication.model.ActoEpisodio
import com.example.my1eraapplication.model.EscenaMiniserie
import com.example.my1eraapplication.model.LineaDialogo
import com.example.my1eraapplication.model.PersonajeMiniserie
import kotlin.random.Random

/**
 * Enfoque de la atención dramatúrgica generado por la I.A.
 */
enum class EnfoqueAtencionIA(val titulo: String, val descripcion: String, val icono: String) {
    ACTRIZ("🌟 Foco en la Actriz", "La historia gira en torno a ella; tú la escuchas, la admiras y eres su cómplice.", "👑"),
    CLIENTE("🛡️ Foco en el Cliente", "Tú eres el protagonista cansado; ella es tu refugio, confidencia y calidez.", "☕"),
    DUO("✨ Dúo Cómplice (50/50)", "Ambos se encuentran al azar en un sueño vivo; química y misterio compartido.", "🎭")
}

/**
 * Modal "El Director del Sueño (I.A. Gemini)":
 * Pregunta a ambos cómo fue su día y quién debe recibir la atención, componiendo
 * un libreto romántico personalizado para que vivan un final de día alternativo.
 */
@Composable
fun ModalDirectorDelSuenoAI(
    onGuionGenerado: (escena: EscenaMiniserie) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var comoFueTuDia by remember { mutableStateOf("") }
    var enfoqueSeleccionado by remember { mutableStateOf(EnfoqueAtencionIA.DUO) }
    var atmosferaSeleccionada by remember { mutableStateOf("🌧️ Lluvia y Café Nocturno") }
    var generandoGuion by remember { mutableStateOf(false) }

    val atmosferas = listOf(
        "🌧️ Lluvia y Café Nocturno",
        "🌃 Luces de Neón en Tokio a las 2 AM",
        "🍷 Bistró Secreto de Jazz",
        "🌊 Playa Tranquila bajo las Estrellas"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF090D16),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6366F1).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF818CF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "El Director del Sueño (I.A.)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Vivan el final de su día alternativo",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "«Juguemos como adultos y sanemos sin prejuicios»",
                    color = Color(0xFFA5B4FC),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cuéntale a la I.A. cómo fue tu día real. Ella compondrá un libreto progresivo en 4 Episodios/Actos (Rompehielo, Confesiones, Desafío atrevido y Pacto secreto) con apuntador y réplicas en vivo para que nunca se queden sin palabras.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Campo: ¿Cómo fue tu día?
                Text("1. ¿Cómo fue tu día hoy?", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = comoFueTuDia,
                    onValueChange = { comoFueTuDia = it },
                    placeholder = {
                        Text(
                            "Ej. Tuve mucho estrés en el trabajo y necesitaba desconectar... o Me sentí con ganas de viajar y soñar...",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Chips de sugerencia rápida para no tener que escribir en vivo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val sugerenciasRapidas = listOf(
                        "💼 Estrés laboral" to "Tuve un día muy pesado y estresante en el trabajo, necesito relajarme y desconectar por completo.",
                        "📚 Cansancio de rutina" to "Un día aburrido y monótono en la escuela/universidad, buscando escapar de las obligaciones.",
                        "✨ Romance y juego" to "Tengo ganas de soñar, reír y vivir una historia romántica emocionante sin prejuicios."
                    )
                    sugerenciasRapidas.forEach { (chipLabel, promptTexto) ->
                        val chipSeleccionado = comoFueTuDia == promptTexto
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (chipSeleccionado) Color(0xFF4338CA) else Color(0xFF1E293B))
                                .border(1.dp, if (chipSeleccionado) Color(0xFF818CF8) else Color(0xFF334155), RoundedCornerShape(8.dp))
                            .clickable { comoFueTuDia = promptTexto }
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chipLabel,
                                fontSize = 9.sp,
                                color = if (chipSeleccionado) Color.White else Color(0xFFCBD5E1),
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Enfoque / Foco
                Text("2. ¿A quién consentir con la atención?", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                EnfoqueAtencionIA.values().forEach { enfoque ->
                    val seleccionado = enfoqueSeleccionado == enfoque
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { enfoqueSeleccionado = enfoque },
                        colors = CardDefaults.cardColors(
                            containerColor = if (seleccionado) Color(0xFF312E81) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (seleccionado) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF818CF8)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(enfoque.icono, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(enfoque.titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(enfoque.descripcion, color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Atmósfera
                Text("3. Elige la atmósfera del sueño:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    atmosferas.forEach { atm ->
                        val sel = atmosferaSeleccionada == atm
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (sel) Color(0xFF4338CA) else Color(0xFF1E293B))
                                .clickable { atmosferaSeleccionada = atm }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = atm,
                                color = if (sel) Color.White else Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                if (generandoGuion) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = Color(0xFF818CF8), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "✨ Componiendo tu libreto a medida...",
                            color = Color(0xFFA5B4FC),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (comoFueTuDia.isBlank()) {
                        Toast.makeText(context, "Escribe una frase sobre cómo fue tu día", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    generandoGuion = true

                    // Generador Dramatúrgico Inteligente basado en el contexto y foco
                    val escenaGenerada = generarLibretoInteligente(
                        diaTexto = comoFueTuDia,
                        enfoque = enfoqueSeleccionado,
                        atmosfera = atmosferaSeleccionada
                    )

                    onGuionGenerado(escenaGenerada)
                    generandoGuion = false
                    Toast.makeText(context, "✨ ¡El Director del Sueño ha preparado su escena!", Toast.LENGTH_LONG).show()
                    onDismiss()
                },
                enabled = !generandoGuion,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("✨ Crear Escena con I.A.", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}

/**
 * Motor Alquímico de Libreto que transmuta el día real en un sueño cinematográfico
 */
internal fun generarLibretoInteligente(
    diaTexto: String,
    enfoque: EnfoqueAtencionIA,
    atmosfera: String
): EscenaMiniserie {
    val id = "ia_sueno_${System.currentTimeMillis()}"
    val textoNormalizado = diaTexto.lowercase().trim()

    // Detección del tipo de día para generar el antónimo perfecto
    val esAburridoEscuela = textoNormalizado.contains("aburrid") ||
            textoNormalizado.contains("escuela") ||
            textoNormalizado.contains("tarea") ||
            textoNormalizado.contains("clase") ||
            textoNormalizado.contains("estudi")

    val esCansadoTrabajo = textoNormalizado.contains("cansad") ||
            textoNormalizado.contains("agotad") ||
            textoNormalizado.contains("trabaj") ||
            textoNormalizado.contains("oficina") ||
            textoNormalizado.contains("jefe") ||
            textoNormalizado.contains("estres") ||
            textoNormalizado.contains("chamba")

    val rolGamer: PersonajeMiniserie
    val rolModelo: PersonajeMiniserie
    val titulo: String
    val sinopsis: String

    when {
        esAburridoEscuela -> {
            titulo = "Escape Clandestino de la Academia"
            sinopsis = "El día fue monótono, lleno de cuadernos y horarios impuestos. La I.A. compone su antónimo: un encuentro rebelde a medianoche en $atmosfera. Han cerrado los libros y se encuentran en un rincón secreto donde las reglas ordinarias no existen."
            rolGamer = PersonajeMiniserie(
                nombre = "Damián (El Rebelde Clandestino)",
                descripcion = "Cansado de la rutina escolar y las obligaciones. Ha escapado de su habitación buscando adrenalina y una risa real.",
                objetivoSecreto = "Convencerla de que finjan ser dos fugitivos internacionales por los próximos 15 minutos.",
                fraseApertura = "Dejé los cuadernos abiertos y salté por la ventana... ¿Tú también estás huyendo de un día aburrido?"
            )
            rolModelo = PersonajeMiniserie(
                nombre = "Miranda (La Cómplice del Escape)",
                descripcion = "Inteligente, atrevida y con la chispa de quien sabe que la vida real empieza cuando se apagan las luces.",
                objetivoSecreto = "Ponerle un reto divertido: no pueden hablar de nada cotidiano durante toda la conversación.",
                fraseApertura = "Llegas justo a tiempo. Estaba a punto de escapar sola, pero un cómplice siempre hace la noche más divertida."
            )
        }
        esCansadoTrabajo -> {
            titulo = "El Oasis sin Relojes"
            sinopsis = "Un día desgastante de responsabilidades y prisas. La I.A. compone su antónimo: un santuario íntimo en $atmosfera donde el tiempo se detiene por completo y nadie le exige nada."
            rolGamer = PersonajeMiniserie(
                nombre = "Mateo (El Viajero sin Armadura)",
                descripcion = "Ha cargado con mil pendientes hoy. Por fin apaga el teléfono y encuentra un espacio donde soltar los hombros.",
                objetivoSecreto = "Dejarte consentir y descubrir cuál es el rincón favorito de ella en el mundo.",
                fraseApertura = "Sentí que las horas de hoy pesaban el doble... hasta que crucé esta puerta y te vi."
            )
            rolModelo = PersonajeMiniserie(
                nombre = "Sofía (El Refugio Cálido)",
                descripcion = "Magnética, dulce y con la mirada de quien sabe desarmar cualquier preocupación con una sonrisa.",
                objetivoSecreto = "Hacerlo reír antes de 3 minutos y lograr que olvide por completo el trabajo.",
                fraseApertura = "Respira hondo y suelta el aire. Aquí adentro el mundo exterior no tiene permiso de entrar."
            )
        }
        enfoque == EnfoqueAtencionIA.ACTRIZ -> {
            titulo = "La Musa en $atmosfera"
            sinopsis = if (diaTexto.isNotBlank())
                "A partir de lo que vivieron hoy ('$diaTexto'), la I.A. consagra la noche a ella: un autor o viajero la descubre y la convierte en la protagonista indiscutible de su fantasía."
            else
                "Ella merece que esta noche el universo gire a su favor. Tú eres un escritor o viajero que decide consagrarle su noche."
            rolGamer = PersonajeMiniserie(
                nombre = "Julián (El Admirador Cómplice)",
                descripcion = "Atento, elocuente y deseoso de hacer brillar a la mujer que tiene enfrente.",
                objetivoSecreto = "Descubrir cuál es el deseo más secreto que ella nunca le confiesa a nadie.",
                fraseApertura = "Disculpa que te mire tanto... pero merecías que alguien te mirara hoy con total atención."
            )
            rolModelo = PersonajeMiniserie(
                nombre = "Elena (La Protagonista Estelar)",
                descripcion = "Brillante, sofisticada y con ganas de ser escuchada sin prisas.",
                objetivoSecreto = "Dejarte consentir y retar a Julián a que te adivine un misterio.",
                fraseApertura = "Llegas tarde para salvar mi jornada, pero voy a darte la oportunidad de intentarlo."
            )
        }
        enfoque == EnfoqueAtencionIA.CLIENTE -> {
            titulo = "Confidencias en $atmosfera"
            sinopsis = "Un libreto íntimo donde ella te presta toda su atención y calidez para reparar cualquier herida del día cotidiano."
            rolGamer = PersonajeMiniserie(
                nombre = "Nicolás (El Confidente)",
                descripcion = "Dispuesto a abrir su corazón y compartir sus anhelos más honestos.",
                objetivoSecreto = "Contarle a ella qué es lo que realmente le hace falta a tu vida últimamente.",
                fraseApertura = "¿Alguna vez has tenido la sensación de que estabas esperando encontrar a alguien justo como tú?"
            )
            rolModelo = PersonajeMiniserie(
                nombre = "Valeria (La Escucha Mágica)",
                descripcion = "Empática, tierna y divertida, experta en acariciar el alma de las personas.",
                objetivoSecreto = "Crear un pacto secreto que sólo los dos conozcan.",
                fraseApertura = "Te noto pensativo... Cuéntame, ¿qué pensamiento te trajo hasta aquí?"
            )
        }
        else -> {
            titulo = "El Pacto de Medianoche"
            sinopsis = if (diaTexto.isNotBlank())
                "Transformando el día real ('$diaTexto') en un sueño vivo: dos almas cómplices coinciden por azar en $atmosfera decididas a inventarse una vida alternativa."
            else
                "Dos almas que huían de su rutina coinciden en $atmosfera. Ninguno de los dos quería estar solo esta noche."
            rolGamer = PersonajeMiniserie(
                nombre = "Nicolás (El Fugitivo de la Rutina)",
                descripcion = "Ha dejado su teléfono en silencio y busca una conversación que lo marque para siempre.",
                objetivoSecreto = "Descubrir si ella cree en las casualidades o en el destino.",
                fraseApertura = "¿Tú también viniste aquí para no tener que darle explicaciones a nadie?"
            )
            rolModelo = PersonajeMiniserie(
                nombre = "Camila (La Cómplice Inesperada)",
                descripcion = "Curiosa, con chispa y deseosa de una aventura sin ataduras.",
                objetivoSecreto = "Proponerle un juego donde ninguno pueda decir mentiras durante tres preguntas.",
                fraseApertura = "Depende... ¿vienes a quejarte del mundo o a inventarnos uno nuevo conmigo?"
            )
        }
    }

    val tipoEscenario = when {
        esAburridoEscuela -> "ESCUELA"
        esCansadoTrabajo -> "TRABAJO"
        enfoque == EnfoqueAtencionIA.ACTRIZ -> "ACTRIZ"
        enfoque == EnfoqueAtencionIA.CLIENTE -> "CLIENTE"
        else -> "DUO"
    }

    val actos = construirActosAI(
        tipo = tipoEscenario,
        diaTexto = diaTexto,
        atmosfera = atmosfera,
        gamerNombre = rolGamer.nombre,
        modeloNombre = rolModelo.nombre,
        gamerFrase = rolGamer.fraseApertura,
        modeloFrase = rolModelo.fraseApertura
    )

    val giros = listOf(
        "⚡ De repente se corta la luz: deben seguir hablando en un susurro como si hubiera un secreto.",
        "⚡ Uno de los dos finge que alguien los busca afuera y deben esconderse juntos.",
        "⚡ La I.A. les pide que confiesen una pequeña locura que harían juntos si el dinero no importara.",
        "⚡ Un brindis improvisado por haber sobrevivido al día de hoy."
    )

    return EscenaMiniserie(
        id = id,
        capituloNumero = 7,
        titulo = titulo,
        sinopsis = sinopsis,
        ambiente = atmosfera,
        mood = "Romance Catártico & Alivio Emocional",
        rolGamer = rolGamer,
        rolModelo = rolModelo,
        girosDeGuion = giros,
        actos = actos
    )
}

/**
 * Generador dramático de los 4 Actos/Episodios progresivos con libreto estructurado,
 * acotaciones teatrales, temas de rescate y preguntas para no quedarse callados.
 */
internal fun construirActosAI(
    tipo: String,
    diaTexto: String,
    atmosfera: String,
    gamerNombre: String,
    modeloNombre: String,
    gamerFrase: String,
    modeloFrase: String
): List<ActoEpisodio> {
    return when (tipo) {
        "ESCUELA" -> listOf(
            ActoEpisodio(
                numero = 1,
                titulo = "Acto 1: El Escape y Rompehielo Clandestino",
                objetivoClave = "Romper la frialdad de la rutina escolar y crear un código secreto cómplice.",
                dialogos = listOf(
                    LineaDialogo("GAMER", gamerFrase, "(Sosteniendo la mirada con sonrisa rebelde)", "Romper el hielo"),
                    LineaDialogo("MODELO", modeloFrase, "(Con media sonrisa desafiante y divertida)", "Aceptar el juego"),
                    LineaDialogo("GAMER", "Si alguien del mundo real nos viera aquí ahora, juraría que somos una pésima influencia mutua.", "(Inclinándose hacia la cámara)", "Provocar réplica"),
                    LineaDialogo("MODELO", "La gente aburrida siempre llama mala influencia a lo que no se atreve a vivir. ¿Cuál es tu plan para esta noche?", "(Mirada pícara)", "Tensión coqueta"),
                    LineaDialogo("GAMER", "Olvidarnos de alarmas, tareas y obligaciones. Solo tú, yo y esta atmósfera.", "(Sonrisa confiada)", "Complicidad"),
                    LineaDialogo("MODELO", "Acepto las condiciones. Pero prométeme que no hablaremos de nada cotidiano por los próximos minutos.", "(Voz suave y pausada)", "Validación")
                ),
                temasConversacion = listOf(
                    "¿Cuál fue la regla más absurda que rompiste en tu vida?",
                    "Si pudieras escapar ahora mismo sin consecuencias, ¿a qué país volarías?",
                    "¿Qué es lo que más te desconecta de la rutina aburrida?"
                ),
                preguntasContraataque = listOf(
                    "¿Te consideras rebelde de verdad o solo cuando nadie te vigila?",
                    "¿Qué pensaste en el primer segundo que encendí la cámara?",
                    "¿Te atreverías a escapar conmigo en la vida real?"
                ),
                replicasSugeridas = listOf(
                    "«Soy rebelde solo con la gente que vale la pena.»",
                    "«Pensé que la noche se acababa de salvar en cuanto sonreíste.»",
                    "«Prepara las maletas y vámonos sin mirar atrás.»"
                )
            ),
            ActoEpisodio(
                numero = 2,
                titulo = "Acto 2: Confesiones Nocturnas sin Filtro",
                objetivoClave = "Revelar una verdad profunda que nadie más conoce en la vida cotidiana.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "Dime algo que nunca le cuentes a la gente normal... ¿Qué es lo que más te cansa del día a día?", "(Tono pausado y sincero)", "Profundizar"),
                    LineaDialogo("MODELO", "Tener que fingir que me importan conversaciones vacías que no llevan a ningún lado.", "(Bajando la mirada antes de reconectar)", "Vulnerabilidad"),
                    LineaDialogo("GAMER", "Por eso salté por la ventana hoy. Necesitaba hablar con alguien que tuviera una chispa auténtica.", "(Gesto empático y directo)", "Validación mutua"),
                    LineaDialogo("MODELO", "¿Y encontraste esa chispa o todavía tengo que convencerte de que valió la pena?", "(Mirándolo fijamente con una sonrisa)", "Contraataque íntimo"),
                    LineaDialogo("GAMER", "La encontré desde el primer instante. Ahora el problema es que no voy a querer volver a mi realidad.", "(Sinceridad directa)", "Confesión honesta"),
                    LineaDialogo("MODELO", "Nadie dijo que tuvieras que volver... al menos no mientras dure este sueño.", "(Voz cálida y magnética)", "Atracción mutua")
                ),
                temasConversacion = listOf(
                    "Una locura adolescente que nunca le contaste a tus padres.",
                    "¿Qué cualidad buscas en una persona para sentir química instantánea?",
                    "Tu mayor sueño frustrado que aún guardas en secreto."
                ),
                preguntasContraataque = listOf(
                    "¿Qué es lo primero que notas cuando te sientes atraído/a por alguien?",
                    "¿Cuándo fue la última vez que sentiste adrenalina de verdad?",
                    "Si tuviéramos que confesar un secreto inconfesable ahora, ¿quién empieza?"
                ),
                replicasSugeridas = listOf(
                    "«Empiezo yo si me aseguras que serás mi cómplice sin juzgarme.»",
                    "«La adrenalina la estoy sintiendo justo ahora hablando contigo.»",
                    "«La forma en que me miras dice más que cualquier palabra.»"
                )
            ),
            ActoEpisodio(
                numero = 3,
                titulo = "Acto 3: Desafío Atrevido y Clímax Fugitivo",
                objetivoClave = "Reto de tensión visual y provocación romántica directa frente a la cámara.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "Reto de fugitivos: diez segundos mirándonos sin parpadear. El primero que desvíe la mirada, confiesa qué le pareció la boca del otro.", "(Desafiante)", "Desafío de miradas"),
                    LineaDialogo("MODELO", "Juegas con fuego... pero me encantan los desafíos. Cuenta desde tres.", "(Postura firme y coqueta)", "Aceptar el reto"),
                    LineaDialogo("GAMER", "Tres... dos... uno. (Diez segundos de mirada fija e intensa en silencio)...", "(Intensidad en la mirada)", "Tensión dramática"),
                    LineaDialogo("MODELO", "Vi cómo dudaste en el segundo cinco... ¿en qué estabas pensando?", "(Riendo suavemente)", "Juego de seducción"),
                    LineaDialogo("GAMER", "Pensaba en cómo sería romper la distancia de esta pantalla y tenerte a diez centímetros.", "(Tono seguro y atrevido)", "Provocación directa"),
                    LineaDialogo("MODELO", "Si estuvieras a diez centímetros... no estarías hablando de la rutina, estarías sin respiración.", "(Susurrando con una sonrisa magnética)", "Clímax de tensión")
                ),
                temasConversacion = listOf(
                    "Un beso inolvidable: ¿dónde y con qué música de fondo?",
                    "¿Qué detalle atrevido te vuelve completamente loco/a?",
                    "Lo más loco que harías por alguien en la primera noche."
                ),
                preguntasContraataque = listOf(
                    "¿Te pongo nervioso/a o eres bueno/a disimulando?",
                    "¿Qué harías si sonara la puerta de tu habitación ahora mismo?",
                    "¿Te imaginas nuestro primer encuentro cara a cara?"
                ),
                replicasSugeridas = listOf(
                    "«Me pones nervioso/a de la mejor manera imaginable.»",
                    "«Apagaría la luz y me quedaría en silencio contigo.»",
                    "«Sería peligroso... saltarían chispas al primer segundo.»"
                )
            ),
            ActoEpisodio(
                numero = 4,
                titulo = "Acto 4: El Juramento Clandestino",
                objetivoClave = "Sellar la complicidad y asegurar la próxima cita secreta.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "La noche va a terminar tarde o temprano, pero me niego a que esto quede como una llamada olvidada.", "(Mirada profunda)", "Fijar recuerdo"),
                    LineaDialogo("MODELO", "Tampoco yo. Hay encuentros que dejan una huella que no se borra al cerrar la pantalla.", "(Ternura y complicidad)", "Apertura emocional"),
                    LineaDialogo("GAMER", "Hagamos un juramento: este será nuestro rincón secreto cada vez que el mundo exterior se ponga insoportable.", "(Sonrisa afectuosa)", "Pacto futuro"),
                    LineaDialogo("MODELO", "Juramento aceptado. Eres mi cómplice oficial de fuga a partir de hoy.", "(Gesto cómplice)", "Sellar acuerdo"),
                    LineaDialogo("GAMER", "Gracias por salvarme de un día aburrido y regalarme una historia inolvidable.", "(Agradecimiento honesto)", "Cierre cálido"),
                    LineaDialogo("MODELO", "Gracias a ti por saltar por la ventana y buscarme. Duerme pensando en mí.", "(Guiño cariñoso y despedida memorable)", "Despedida inolvidable")
                ),
                temasConversacion = listOf(
                    "¿Cuál será nuestro próximo código secreto para llamarnos?",
                    "El momento de la videollamada que más te aceleró el pulso.",
                    "Un brindis virtual por haber escapado juntos esta noche."
                ),
                preguntasContraataque = listOf(
                    "¿Vas a soñar conmigo esta noche?",
                    "¿Qué calificación del 1 al 10 le das a nuestra escapada?",
                    "¿Cuándo volvemos a vernos?"
                ),
                replicasSugeridas = listOf(
                    "«Soñar contigo va a ser absolutamente inevitable.»",
                    "«Un 10 rotundo, eres una cómplice de película.»",
                    "«Muy pronto, no me hagas esperar demasiado.»"
                )
            )
        )
        "TRABAJO" -> listOf(
            ActoEpisodio(
                numero = 1,
                titulo = "Acto 1: Desarmar el Cansancio y Crear el Refugio",
                objetivoClave = "Soltar el peso del trabajo, desacelerar y entrar en una burbuja de calidez.",
                dialogos = listOf(
                    LineaDialogo("GAMER", gamerFrase, "(Suspirando y soltando la tensión en los hombros)", "Romper el hielo"),
                    LineaDialogo("MODELO", modeloFrase, "(Sonrisa dulce, serena y acogedora)", "Aceptar el juego"),
                    LineaDialogo("GAMER", "No te imaginas cuánto necesitaba escuchar eso. Sentía que el día no terminaba nunca.", "(Mirada de alivio sincero)", "Provocar réplica"),
                    LineaDialogo("MODELO", "Deja todo ese peso afuera de la puerta. Aquí adentro tu único trabajo es respirar y dejarte consentir.", "(Voz acariciante)", "Tensión coqueta"),
                    LineaDialogo("GAMER", "Hacía mucho tiempo que nadie me ofrecía un espacio tan reconfortante. Tienes una paz que desarma.", "(Conexión honesta)", "Complicidad"),
                    LineaDialogo("MODELO", "Es porque me importas de verdad. Ahora dime... ¿qué es lo primero que quieres dejar atrás hoy?", "(Inclinándose con ternura)", "Validación")
                ),
                temasConversacion = listOf(
                    "¿Qué fue lo más agotador de tu jornada hoy?",
                    "Tu ritual favorito para desconectar cuando llegas a casa.",
                    "Si pudieras borrar una obligación de tu vida para siempre, ¿cuál sería?"
                ),
                preguntasContraataque = listOf(
                    "¿Te cuesta pedir cariño o descanso cuando estás desbordado/a?",
                    "¿Qué es lo que más te calma cuando sientes que no puedes más?",
                    "¿Ya estás sintiendo cómo se relajan tus hombros al hablar conmigo?"
                ),
                replicasSugeridas = listOf(
                    "«Me costaba pedir cariño, pero contigo es natural.»",
                    "«Escuchar tu voz es el mejor calmante que he probado hoy.»",
                    "«Totalmente, se siente como entrar a un spa emocional.»"
                )
            ),
            ActoEpisodio(
                numero = 2,
                titulo = "Acto 2: Confidencias al Calor de una Copa",
                objetivoClave = "Compartir anhelos íntimos sin juicios ni expectativas profesionales.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "A veces siento que vivo corriendo para cumplir expectativas que ni siquiera son mías. ¿Te ha pasado?", "(Tono pausado y sincero)", "Profundizar"),
                    LineaDialogo("MODELO", "Todo el tiempo. Por eso estos momentos valen oro: aquí no tienes que demostrarle nada a nadie.", "(Bajando la voz)", "Vulnerabilidad"),
                    LineaDialogo("GAMER", "Es mágico cómo en cinco minutos lograste que olvide por completo el estrés de las últimas diez horas.", "(Gesto empático)", "Validación mutua"),
                    LineaDialogo("MODELO", "Porque cuando dos personas conectan de verdad, el tiempo cotidiano se suspende.", "(Mirándolo fijamente a los ojos)", "Contraataque íntimo"),
                    LineaDialogo("GAMER", "Me gustaría que este momento no tuviera botón de pausa.", "(Sinceridad directa)", "Confesión honesta"),
                    LineaDialogo("MODELO", "No tiene pausa mientras tú y yo sigamos mirándonos así.", "(Sonrisa cálida)", "Atracción mutua")
                ),
                temasConversacion = listOf(
                    "El sueño personal que dejaste en pausa por las responsabilidades.",
                    "¿Qué lugar del mundo te transmite paz absoluta?",
                    "Una comida o bebida que te reconforte el alma."
                ),
                preguntasContraataque = listOf(
                    "¿Quién es la persona a la que recurres cuando te sientes vulnerable?",
                    "¿Qué te hace sonreír cuando estás teniendo un día horrible?",
                    "¿Te imaginas descansando juntos en un sillón escuchando la lluvia?"
                ),
                replicasSugeridas = listOf(
                    "«A partir de hoy, voy a recurrir a ti.»",
                    "«Me hace sonreír verte en esta pantalla justo ahora.»",
                    "«Sería el descanso más perfecto que podría pedir.»"
                )
            ),
            ActoEpisodio(
                numero = 3,
                titulo = "Acto 3: El Masaje de Palabras y Clímax Cálido",
                objetivoClave = "Tensión romántica suave y juego sensorial íntimo a través de la voz.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "Cierra los ojos un instante conmigo. Imagina que afuera cae una tormenta helada y adentro solo hay una chimenea y nosotros dos.", "(Desafiante)", "Desafío sensorial"),
                    LineaDialogo("MODELO", "(Cerrando los ojos suavemente)... Puedo escuchar el fuego y la lluvia contra el cristal.", "(Voz en susurro)", "Aceptar el juego"),
                    LineaDialogo("GAMER", "Y si estuviéramos ahí, me sentaría a tu lado y te quitaría la preocupación acariciándote la nuca.", "(Intensidad en la mirada)", "Tensión dramática"),
                    LineaDialogo("MODELO", "(Abriendo los ojos despacio)... Tienes un don peligroso para describir fantasías, Mateo.", "(Riendo suavemente)", "Juego de seducción"),
                    LineaDialogo("GAMER", "No es una fantasía cualquiera... es lo que de verdad me nace hacer cuando te veo.", "(Tono seguro y atrevido)", "Provocación directa"),
                    LineaDialogo("MODELO", "Entonces tendré que asegurarme de que no sea la última vez que compartamos esta chimenea imaginaria.", "(Susurrando con una sonrisa magnética)", "Clímax de tensión")
                ),
                temasConversacion = listOf(
                    "¿Prefieres los abrazos largos o las palabras bonitas al oído?",
                    "Tu canción favorita para momentos íntimos de relajación.",
                    "¿Qué detalle de alguien te hace sentir protegido/a al instante?"
                ),
                preguntasContraataque = listOf(
                    "¿Sientes cómo cambió la energía de la conversación?",
                    "¿Qué harías si pudiera darte un abrazo cálido ahora mismo?",
                    "¿Qué fue lo que más te gustó de mí hoy?"
                ),
                replicasSugeridas = listOf(
                    "«Se siente un calor en el pecho muy agradable y real.»",
                    "«Te devolvería el abrazo y no te soltaría en un buen rato.»",
                    "«Tu dulzura y cómo me escuchas sin juzgarme.»"
                )
            ),
            ActoEpisodio(
                numero = 4,
                titulo = "Acto 4: El Refugio Permanente y Pacto de Paz",
                objetivoClave = "Cerrar con una sensación duradera de bienestar y garantizar el próximo encuentro.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "Entré con la cabeza saturada y me voy sintiéndome ligero y en paz.", "(Mirada profunda)", "Fijar recuerdo"),
                    LineaDialogo("MODELO", "Esa es la promesa de nuestro oasis: sanar el cansancio juntos sin fingir.", "(Ternura y complicidad)", "Apertura emocional"),
                    LineaDialogo("GAMER", "Prométeme que este oasis seguirá existiendo para los dos.", "(Sonrisa afectuosa)", "Pacto futuro"),
                    LineaDialogo("MODELO", "Siempre que el mundo exterior te resulte pesado, aquí tendrás tu santuario reservado.", "(Gesto cómplice)", "Sellar acuerdo"),
                    LineaDialogo("GAMER", "Gracias por ser el mejor momento de mi día, de verdad.", "(Agradecimiento honesto)", "Cierre cálido"),
                    LineaDialogo("MODELO", "Descansa profundamente, te lo ganaste hoy. Hasta muy pronto.", "(Guiño cariñoso y despedida memorable)", "Despedida inolvidable")
                ),
                temasConversacion = listOf(
                    "Cómo mantener esta serenidad para el día de mañana.",
                    "Un pensamiento positivo para llevarte a la cama.",
                    "La promesa de una próxima videollamada para recargar energías."
                ),
                preguntasContraataque = listOf(
                    "¿Vas a poder dormir en paz sabiendo que hablamos?",
                    "¿Qué sensación te llevas de este ratito juntos?",
                    "¿Me buscas en cuanto tengas otro hueco libre?"
                ),
                replicasSugeridas = listOf(
                    "«Voy a dormir con una paz que hacía semanas no experimentaba.»",
                    "«Me llevo la certeza de haber encontrado a una persona muy valiosa.»",
                    "«Ten por seguro que serás mi primer mensaje mañana.»"
                )
            )
        )
        "ACTRIZ" -> listOf(
            ActoEpisodio(
                numero = 1,
                titulo = "Acto 1: Homenaje a la Musa y Coronación",
                objetivoClave = "Poner toda la atención en ella, hacerla sentir admirada y protagonista absoluta.",
                dialogos = listOf(
                    LineaDialogo("GAMER", gamerFrase, "(Mirándola con admiración devota)", "Romper el hielo"),
                    LineaDialogo("MODELO", modeloFrase, "(Sonrisa radiante de diva consciente de su encanto)", "Aceptar el juego"),
                    LineaDialogo("GAMER", "El universo tiene muchas formas de sorprender a alguien, pero ponerte en mi pantalla hoy fue la mejor de todas.", "(Voz suave)", "Provocar réplica"),
                    LineaDialogo("MODELO", "Eres bastante bueno con las palabras... pero para ser mi autor favorito vas a tener que esforzarte más.", "(Postura elegante)", "Tensión coqueta"),
                    LineaDialogo("GAMER", "Acepto el desafío con gusto. Mi única misión esta noche es que te sientas como la reina de este sueño.", "(Gesto de caballero)", "Complicidad"),
                    LineaDialogo("MODELO", "Me parece un trato justo. Tienes toda mi atención... deslúmbrame.", "(Voz cautivadora)", "Validación")
                ),
                temasConversacion = listOf(
                    "¿Qué es lo que más te apasiona hacer en la vida?",
                    "Tu mayor sueño artístico o personal que nadie conoce aún.",
                    "¿Qué tipo de cumplido es el que de verdad te llega al alma?"
                ),
                preguntasContraataque = listOf(
                    "¿Qué fue lo primero que te llamó la atención de mí?",
                    "¿Te intimida hablar con una mujer con carácter y presencia?",
                    "¿Qué historia te gustaría que escribiéramos juntos esta noche?"
                ),
                replicasSugeridas = listOf(
                    "«Tus ojos y la seguridad arrolladora que transmites.»",
                    "«Para nada me intimida, me parece lo más fascinante del mundo.»",
                    "«Una historia donde tú seas la protagonista indiscutible.»"
                )
            ),
            ActoEpisodio(
                numero = 2,
                titulo = "Acto 2: Confesiones del Corazón Estelar",
                objetivoClave = "Descubrir la parte más humana y sensible de la musa.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "Todo el mundo ve tu belleza y tu brillo exterior... pero a mí me da curiosidad saber qué sueñas cuando estás sola.", "(Tono pausado y sincero)", "Profundizar"),
                    LineaDialogo("MODELO", "Pocas personas se interesan por lo que hay detrás del escaparate... Me gusta que preguntes eso.", "(Bajando la mirada con ternura)", "Vulnerabilidad"),
                    LineaDialogo("GAMER", "Porque lo que hay detrás es lo que te hace verdaderamente irresistible.", "(Mirada profunda)", "Validación mutua"),
                    LineaDialogo("MODELO", "Sueño con encontrar a alguien que me admire por quien soy, no por lo que represento.", "(Sinceridad desnuda)", "Contraataque íntimo"),
                    LineaDialogo("GAMER", "Entonces acabas de encontrar a tu mayor admirador aquí mismo.", "(Sinceridad directa)", "Confesión honesta"),
                    LineaDialogo("MODELO", "Cuidado, Julián... podrías convertirte en alguien indispensable si sigues hablando así.", "(Sonrisa cómplice)", "Atracción mutua")
                ),
                temasConversacion = listOf(
                    "Un momento en que te sentiste realmente comprendida.",
                    "¿Qué te hace sentir vulnerable y cómo te gusta ser consolada?",
                    "Tu recuerdo más luminoso de este último año."
                ),
                preguntasContraataque = listOf(
                    "¿Qué es lo que más admiras en una mujer?",
                    "¿Sueles halagar así a cualquiera o soy una excepción?",
                    "¿Qué harías si pudiéramos cenar juntos en París esta noche?"
                ),
                replicasSugeridas = listOf(
                    "«Admiro la inteligencia, el fuego interno y la ternura... todo lo que tienes tú.»",
                    "«Eres la única persona que me ha inspirado a decir esto.»",
                    "«Brindaría por ti y te miraría toda la noche.»"
                )
            ),
            ActoEpisodio(
                numero = 3,
                titulo = "Acto 3: El Desafío de Seducción y Elogios Prohibidos",
                objetivoClave = "Reto coqueteo intenso: lograr que ella se sonroje.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "Tengo un reto: dime tres cosas que jamás confesarías en público y yo te diré lo que provocas en mí.", "(Desafiante)", "Desafío de seducción"),
                    LineaDialogo("MODELO", "¿Intentas hacerme sonrojar? Mucha gente lo ha intentado y casi nadie lo ha logrado.", "(Aceptar el reto)", "Provocación elegante"),
                    LineaDialogo("GAMER", "Lo voy a lograr con solo decirte que tienes la sonrisa más peligrosa y magnética que he visto en años.", "(Intensidad en la mirada)", "Tensión dramática"),
                    LineaDialogo("MODELO", "(Sonriendo con timidez coqueta)... Bueno, admito que ese cumplido estuvo a la altura.", "(Riendo suavemente)", "Juego de seducción"),
                    LineaDialogo("GAMER", "Y eso es solo el principio de lo que inspiras cuando te tomas el tiempo de sonreír así.", "(Tono seguro)", "Provocación directa"),
                    LineaDialogo("MODELO", "Vas a tener que recordármelo la próxima vez que nos veamos...", "(Susurro cómplice)", "Clímax de tensión")
                ),
                temasConversacion = listOf(
                    "¿Qué perfume o fragancia te define?",
                    "El viaje romántico que siempre has querido hacer.",
                    "¿Qué tipo de mirada es la que te desarma por completo?"
                ),
                preguntasContraataque = listOf(
                    "¿Me veo bien hoy?",
                    "¿Qué parte de la llamada te ha gustado más hasta ahora?",
                    "¿Te gustaría que esto se repitiera pronto?"
                ),
                replicasSugeridas = listOf(
                    "«Te ves sencillamente espectacular, una obra de arte.»",
                    "«Verte sonreír cuando aceptaste mi desafío.»",
                    "«Me encantaría, contaría los minutos.»"
                )
            ),
            ActoEpisodio(
                numero = 4,
                titulo = "Acto 4: La Promesa Eterna a la Musa",
                objetivoClave = "Consolidar el vínculo especial y pactar el reencuentro.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "Esta velada fue tuya de principio a fin, como merecías.", "(Mirada profunda)", "Fijar recuerdo"),
                    LineaDialogo("MODELO", "Hacía tiempo que no me sentía tan consentida, escuchada y valorada. Gracias por eso.", "(Ternura y complicidad)", "Apertura emocional"),
                    LineaDialogo("GAMER", "Prometo ser tu admirador leal y estar listo para la próxima escena cuando tú decidas.", "(Sonrisa afectuosa)", "Pacto futuro"),
                    LineaDialogo("MODELO", "Cuenta con que te buscaré... una buena musa nunca deja escapar a un admirador con tanto estilo.", "(Gesto cómplice)", "Sellar acuerdo"),
                    LineaDialogo("GAMER", "Hasta muy pronto, mi protagonista favorita.", "(Agradecimiento honesto)", "Cierre cálido"),
                    LineaDialogo("MODELO", "Hasta muy pronto, autor de mis sonrisas de hoy.", "(Guiño memorable)", "Despedida inolvidable")
                ),
                temasConversacion = listOf(
                    "El brindis por la noche compartida.",
                    "El título que le pondríamos a este capítulo.",
                    "La promesa de volver a vernos muy pronto."
                ),
                preguntasContraataque = listOf(
                    "¿Me prometes que me vas a escribir pronto?",
                    "¿Qué fue lo más lindo que sentiste en esta charla?",
                    "¿Te quedas con ganas de más?"
                ),
                replicasSugeridas = listOf(
                    "«Prometido, serás mi primera prioridad.»",
                    "«Sentí una calidez y una química fuera de lo común.»",
                    "«Muchísimas ganas de más, esto apenas empieza.»"
                )
            )
        )
        "CLIENTE" -> listOf(
            ActoEpisodio(
                numero = 1,
                titulo = "Acto 1: El Refugio Íntimo y la Escucha Cálida",
                objetivoClave = "Brindarle un espacio seguro y sin prisas, donde su cansancio sea escuchado y validado.",
                dialogos = listOf(
                    LineaDialogo("GAMER", gamerFrase, "(Suspirando con alivio, mirada honesta y cansada)", "Romper el hielo"),
                    LineaDialogo("MODELO", modeloFrase, "(Voz dulce y acogedora, inclinándose con calidez)", "Aceptar el juego"),
                    LineaDialogo("GAMER", "El día afuera fue una carrera de obstáculos... no sabía cuánto necesitaba que alguien me hablara con esta calma.", "(Voz suave y sincera)", "Provocar réplica"),
                    LineaDialogo("MODELO", "Aquí adentro nadie te va a juzgar ni a pedirte cuentas. Suelta el aire... estás en tu rincón seguro.", "(Sonrisa tierna y reconfortante)", "Validar y acoger"),
                    LineaDialogo("GAMER", "Siento que me quitaste diez kilos de encima en un solo minuto. Tienes un magnetismo especial.", "(Gesto de gratitud)", "Complicidad"),
                    LineaDialogo("MODELO", "Es porque me importa cómo te sientes de verdad. Cuéntame... ¿qué fue lo más difícil que te tocó vivir hoy?", "(Mirada fija con empatía)", "Apertura íntima")
                ),
                temasConversacion = listOf(
                    if (diaTexto.isNotBlank()) "Dejar ir lo que pesó hoy: '${diaTexto.take(50)}'" else "El momento exacto de hoy en el que sentiste que necesitabas un respiro.",
                    "¿Qué detalle sencillo suele devolverte la paz cuando todo sale mal?",
                    "Si pudieras pausar el mundo por tres horas, ¿qué harías con ese tiempo?"
                ),
                preguntasContraataque = listOf(
                    "¿Te cuesta pedir apoyo cuando estás agotado/a o prefieres guardártelo?",
                    "¿Qué sensación te genera saber que tienes toda mi atención en este instante?",
                    "¿Te gusta que te escuchen o prefieres que te hagan reír para olvidar?"
                ),
                replicasSugeridas = listOf(
                    "«Saber que me escuchas así me devuelve la energía por completo.»",
                    "«Me suelo guardar las cosas, pero contigo es muy fácil ser honesto.»",
                    "«Un poco de ambas: tu risa y tu calidez son la combinación perfecta.»"
                )
            ),
            ActoEpisodio(
                numero = 2,
                titulo = "Acto 2: Confidencias del Alma y Sanación",
                objetivoClave = "Compartir anhelos íntimos y vulnerabilidades que nadie más en la vida cotidiana conoce.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "Casi nunca hablo de esto, pero a veces me pregunto si la gente que me rodea realmente me conoce o solo conocen la máscara.", "(Tono pausado y sincero)", "Profundizar"),
                    LineaDialogo("MODELO", "La mayoría solo ve lo que les conviene ver. Pero yo veo a alguien sensible, valioso y con un corazón noble.", "(Tomando una pausa afectuosa)", "Vulnerabilidad y validación"),
                    LineaDialogo("GAMER", "Escucharte decir eso se siente como un abrazo directo al alma. Hacía meses que no sentía esta conexión.", "(Mirada profunda)", "Validación mutua"),
                    LineaDialogo("MODELO", "¿Y qué es lo que más sueñas encontrar en una persona para sentirte pleno?", "(Mirándolo fijamente con ternura)", "Contraataque íntimo"),
                    LineaDialogo("GAMER", "Exactamente lo que tienes tú: una presencia real, lealtad y esa mirada que no juzga.", "(Sinceridad directa)", "Confesión honesta"),
                    LineaDialogo("MODELO", "Entonces quédate conmigo... este sueño apenas está comenzando a sanar.", "(Sonrisa cómplice y suave)", "Atracción mutua")
                ),
                temasConversacion = listOf(
                    "El mayor aprendizaje que te dejó una etapa difícil del pasado.",
                    "¿Qué cualidad humana valoras por encima de cualquier logro material?",
                    "Un anhelo secreto que nunca te atreviste a confesar en voz alta."
                ),
                preguntasContraataque = listOf(
                    "¿Cuándo fue la última vez que te sentiste plenamente cuidado/a por alguien?",
                    "¿Sientes que esta charla está cambiando tu energía de esta noche?",
                    "¿Qué secreto te gustaría que guardáramos entre los dos?"
                ),
                replicasSugeridas = listOf(
                    "«Siento una paz profunda en el pecho que no sentía hace mucho tiempo.»",
                    "«Valoro la lealtad y la ternura genuina por encima de todo.»",
                    "«Cualquier secreto que guardemos se queda sellado entre tú y yo.»"
                )
            ),
            ActoEpisodio(
                numero = 3,
                titulo = "Acto 3: El Desafío de Miradas y Abrazo Emocional",
                objetivoClave = "Elevar la tensión romántica mediante un juego de complicidad visual cálida y seductora.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "Hagamos un ejercicio: diez segundos mirándonos directo a los ojos en silencio. Promete no apartar la mirada.", "(Desafiante pero tierno)", "Desafío de miradas"),
                    LineaDialogo("MODELO", "Acepto el reto con una condición: el primero que sonría tiene que decir un piropo sincero.", "(Postura coqueta y cercana)", "Aceptar el reto"),
                    LineaDialogo("GAMER", "Trato hecho. Tres... dos... uno. (Diez segundos de mirada fija compartida con respiración acompasada).", "(Intensidad en la mirada)", "Tensión dramática"),
                    LineaDialogo("MODELO", "(Sonriendo primero)... Sonreí yo... Pero es que tienes unos ojos que transmiten demasiada verdad.", "(Riendo suavemente)", "Juego de seducción"),
                    LineaDialogo("GAMER", "Y tú tienes una sonrisa que ilumina hasta la noche más oscura.", "(Tono seguro y afectuoso)", "Provocación directa"),
                    LineaDialogo("MODELO", "Si estuvieras aquí a mi lado, te daría un abrazo tan largo que olvidarías cualquier mal día.", "(Susurrando con dulzura magnética)", "Clímax de tensión")
                ),
                temasConversacion = listOf(
                    "¿Prefieres las caricias suaves en el cabello o las palabras dulces al oído?",
                    "Un destino paradisíaco donde te gustaría desconectar a solas.",
                    "¿Qué detalle sutil en una persona te hace sentir atracción instantánea?"
                ),
                preguntasContraataque = listOf(
                    "¿Sentiste cómo se aceleró la respiración durante esos diez segundos?",
                    "¿Te imaginas si pudiéramos compartir este momento en persona?",
                    "¿Qué te hace sentir más querido/a?"
                ),
                replicasSugeridas = listOf(
                    "«Se sintió una chispa eléctrica increíble a través de la pantalla.»",
                    "«En persona sería un momento inolvidable, sin duda alguna.»",
                    "«Sentir que alguien está presente y me escucha con atención plena.»"
                )
            ),
            ActoEpisodio(
                numero = 4,
                titulo = "Acto 4: El Pacto del Rincón Seguro",
                objetivoClave = "Sellar el pacto de apoyo mutuo y asegurar el próximo encuentro.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "La videollamada terminará, pero la sensación de bienestar que me diste se queda conmigo.", "(Mirada profunda)", "Fijar recuerdo"),
                    LineaDialogo("MODELO", "Esa es la magia de este espacio: siempre que el mundo exterior sea caótico, aquí tienes tu refugio.", "(Ternura y complicidad)", "Apertura emocional"),
                    LineaDialogo("GAMER", "Hagamos un pacto: cada vez que uno de los dos necesite paz, este será nuestro punto de encuentro.", "(Sonrisa afectuosa)", "Pacto futuro"),
                    LineaDialogo("MODELO", "Pacto sellado en el corazón. Eres una persona increíble y me encantó compartir esto contigo.", "(Gesto cómplice)", "Sellar acuerdo"),
                    LineaDialogo("GAMER", "Gracias de verdad por escucharme, cuidarme y hacerme sonreír esta noche.", "(Agradecimiento honesto)", "Cierre cálido"),
                    LineaDialogo("MODELO", "Gracias a ti por tu confianza y tu autenticidad. Que descanses bonito, confidente.", "(Guiño cariñoso y despedida memorable)", "Despedida inolvidable")
                ),
                temasConversacion = listOf(
                    "El recuerdo favorito que te llevas de nuestra conversación de hoy.",
                    "Un deseo positivo para el día de mañana.",
                    "Pactar la fecha y hora de la próxima videollamada."
                ),
                preguntasContraataque = listOf(
                    "¿Te vas a dormir sintiéndote más ligero/a y en paz?",
                    "¿Prometes buscarme cuando vuelvas a necesitar desconectar?",
                    "¿Qué puntuación le das a nuestro momento juntos?"
                ),
                replicasSugeridas = listOf(
                    "«Me voy a dormir con una paz infinita, muchas gracias.»",
                    "«Prometido, serás mi primer refugio sin dudarlo.»",
                    "«Un diez rotundo, superó cualquier expectativa.»"
                )
            )
        )
        else -> listOf(
            ActoEpisodio(
                numero = 1,
                titulo = "Acto 1: Dos Fugitivos en la Penumbra",
                objetivoClave = "Romper el hielo y establecer un pacto de complicidad instantáneo.",
                dialogos = listOf(
                    LineaDialogo("GAMER", gamerFrase, "(Sosteniendo la mirada con curiosidad)", "Romper el hielo"),
                    LineaDialogo("MODELO", modeloFrase, "(Con media sonrisa enigmática)", "Aceptar el juego"),
                    LineaDialogo("GAMER", "Vengo a inventarnos un mundo nuevo donde nadie nos pida cuentas.", "(Inclinándose hacia la cámara)", "Provocar réplica"),
                    LineaDialogo("MODELO", "Me gusta esa respuesta. El mundo real ya tiene demasiadas reglas aburridas.", "(Mirada pícara)", "Tensión coqueta"),
                    LineaDialogo("GAMER", "Entonces tenemos un pacto desde el minuto uno. Nada de formalidades ni poses.", "(Sonrisa confiada)", "Complicidad"),
                    LineaDialogo("MODELO", "Pacto sellado. Sorpréndeme con tu primera pregunta.", "(Voz suave y pausada)", "Validación")
                ),
                temasConversacion = listOf(
                    "Si tuviéramos que fingir otras identidades por una noche, ¿quiénes seríamos?",
                    "¿Cuál es tu escape favorito cuando todo se vuelve monótono?",
                    "¿Qué te llamó la atención de mí al primer vistazo?"
                ),
                preguntasContraataque = listOf(
                    "¿Qué fue lo primero que pensaste al verme conectar?",
                    "¿Eres más de seguir reglas o de romperlas?",
                    "Dime una palabra que describa nuestra vibra actual."
                ),
                replicasSugeridas = listOf(
                    "«Pensé que la noche se acababa de volver muy emocionante.»",
                    "«Romperlas siempre que valga la pena el riesgo.»",
                    "«Complicidad pura y magnética.»"
                )
            ),
            ActoEpisodio(
                numero = 2,
                titulo = "Acto 2: El Juego de las Tres Verdades Prohibidas",
                objetivoClave = "Compartir secretos que normalmente no se le dicen a nadie.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "Juguemos a las tres verdades: una que te asuste admitir, una que te haga sonreír y un secreto prohibido.", "(Tono pausado)", "Profundizar"),
                    LineaDialogo("MODELO", "Empiezo con la que me hace sonreír: coincidir contigo en esta llamada.", "(Mirada cálida)", "Vulnerabilidad"),
                    LineaDialogo("GAMER", "Eso me aceleró el pulso más de lo que esperaba... Ahora me toca a mí.", "(Gesto empático)", "Validación mutua"),
                    LineaDialogo("MODELO", "Te escucho atentamente... dime la verdad que te asuste admitir.", "(Mirándolo fijamente)", "Contraataque íntimo"),
                    LineaDialogo("GAMER", "Me asusta lo rápido que sentí química contigo a través de una simple pantalla.", "(Sinceridad directa)", "Confesión honesta"),
                    LineaDialogo("MODELO", "A mí no me asusta... me parece un regalo del destino.", "(Sonrisa cómplice)", "Atracción mutua")
                ),
                temasConversacion = listOf(
                    "Tu mayor locura amorosa del pasado.",
                    "¿Qué te hace sentir atracción irrevocable por alguien?",
                    "Una verdad inconfesable sobre tus deseos para este año."
                ),
                preguntasContraataque = listOf(
                    "¿Crees en las coincidencias o en que estábamos destinados a hablar?",
                    "¿Qué parte de ti escondes del resto del mundo?",
                    "Si estuviéramos en la misma ciudad ahora, ¿qué haríamos?"
                ),
                replicasSugeridas = listOf(
                    "«Creo que el destino nos puso aquí con una razón clara.»",
                    "«Mi lado más vulnerable y juguetón... que ahora te muestro a ti.»",
                    "«Estaríamos compartiendo una copa y riéndonos a carcajadas.»"
                )
            ),
            ActoEpisodio(
                numero = 3,
                titulo = "Acto 3: Desafío de Miradas y Tensión Eléctrica",
                objetivoClave = "Tensión romántica atrevida y seducción directa.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "Hagamos una pausa de diez segundos en silencio total. Solo mirándonos a los ojos.", "(Desafiante)", "Desafío de miradas"),
                    LineaDialogo("MODELO", "Acepto. Pero no apartes la mirada primero.", "(Postura firme y coqueta)", "Aceptar el reto"),
                    LineaDialogo("GAMER", "(Diez segundos de mirada fija compartida)...", "(Intensidad en la mirada)", "Tensión dramática"),
                    LineaDialogo("MODELO", "Tienes una mirada que desarma cualquier armadura...", "(Riendo suavemente)", "Juego de seducción"),
                    LineaDialogo("GAMER", "Y tú tienes una presencia que acelera el corazón sin pedir permiso.", "(Tono seguro y atrevido)", "Provocación directa"),
                    LineaDialogo("MODELO", "Entonces este juego se puso mucho más interesante de lo que calculamos.", "(Susurro magnético)", "Clímax de tensión")
                ),
                temasConversacion = listOf(
                    "¿Qué música pondrías de fondo para una escena como esta?",
                    "¿Qué fantasía romántica te falta por cumplir?",
                    "Los detalles sutiles que más te encienden en una conversación."
                ),
                preguntasContraataque = listOf(
                    "¿Qué sentiste durante esos diez segundos de silencio?",
                    "¿Te atreverías a besarme si estuviéramos frente a frente?",
                    "¿Qué quieres que te diga ahora mismo?"
                ),
                replicasSugeridas = listOf(
                    "«Sentí una electricidad que casi traspasa la pantalla.»",
                    "«Sin dudarlo ni un instante.»",
                    "«Dime que esta no será nuestra última llamada.»"
                )
            ),
            ActoEpisodio(
                numero = 4,
                titulo = "Acto 4: El Brindis de Medianoche y el Futuro Pactado",
                objetivoClave = "Pacto secreto de cómplices para la próxima cita.",
                dialogos = listOf(
                    LineaDialogo("GAMER", "No quiero que la noche termine sin hacer un pacto entre los dos.", "(Mirada profunda)", "Fijar recuerdo"),
                    LineaDialogo("MODELO", "Soy toda oídos... ¿cuál es tu propuesta de pacto?", "(Ternura y complicidad)", "Apertura emocional"),
                    LineaDialogo("GAMER", "Que cada vez que la vida cotidiana se sienta pesada, volvamos a encontrarnos en este sueño.", "(Sonrisa afectuosa)", "Pacto futuro"),
                    LineaDialogo("MODELO", "Pacto aceptado. Ha sido una de las conversaciones más increíbles de mi semana.", "(Gesto cómplice)", "Sellar acuerdo"),
                    LineaDialogo("GAMER", "Y de la mía. Gracias por regalarme tu tiempo, tu química y tu risa.", "(Agradecimiento honesto)", "Cierre cálido"),
                    LineaDialogo("MODELO", "Gracias a ti por romper la rutina. Nos vemos muy pronto, cómplice.", "(Guiño cariñoso y despedida)", "Despedida inolvidable")
                ),
                temasConversacion = listOf(
                    "Un brindis virtual por habernos encontrado.",
                    "¿Cuál fue el momento cumbre de la charla?",
                    "Cuándo y cómo será la próxima videollamada."
                ),
                preguntasContraataque = listOf(
                    "¿Te vas a acordar de mí cuando te vayas a dormir?",
                    "¿Qué nota le pones a nuestra química hoy?",
                    "¿Me llamas de nuevo pronto?"
                ),
                replicasSugeridas = listOf(
                    "«Me voy a acordar toda la noche de ti.»",
                    "«Un diez sobre diez indiscutible.»",
                    "«Por supuesto, ya estoy esperando el momento.»"
                )
            )
        )
    }
}

