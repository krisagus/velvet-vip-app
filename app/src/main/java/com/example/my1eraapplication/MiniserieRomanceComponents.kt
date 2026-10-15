package com.example.my1eraapplication

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my1eraapplication.model.ActoEpisodio
import com.example.my1eraapplication.model.CatalogoMiniserie
import com.example.my1eraapplication.model.EscenaMiniserie
import com.example.my1eraapplication.model.LineaDialogo

/**
 * Modal interactivo de "Artistas Fugaces: Miniserie de Romance".
 * Ambos participantes asumen personajes de una historia romántica improvisada,
 * sincronizada en tiempo real mediante Cloud Firestore.
 */
@Composable
fun ModalMiniserieRomance(
    miniserieActiva: Boolean,
    miniserieTitulo: String,
    miniserieSinopsis: String,
    miniserieAmbiente: String,
    miniserieMood: String,
    miniserieRolANombre: String,
    miniserieRolADesc: String,
    miniserieRolAObjetivo: String,
    miniserieRolAFrase: String,
    miniserieRolBNombre: String,
    miniserieRolBDesc: String,
    miniserieRolBObjetivo: String,
    miniserieRolBFrase: String,
    miniserieGiroActual: String,
    rolUsuario: RolApp,
    rolesInvertidos: Boolean = false,
    actoActual: Int = 1,
    totalActos: Int = 4,
    turnoDialogoActual: Int = 0,
    actos: List<ActoEpisodio> = emptyList(),
    onCambiarActo: ((Int) -> Unit)? = null,
    onAvanzarTurnoDialogo: ((Int) -> Unit)? = null,
    onIntercambiarRoles: () -> Unit = {},
    onActivarEscena: (EscenaMiniserie) -> Unit,
    onDetonarGiro: (String) -> Unit,
    onDesactivar: () -> Unit,
    onAbrirDirectorAI: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var pestanaActual by remember {
        mutableStateOf(if (miniserieActiva) "GUIÓN" else "CATALOGO")
    }

    val soyRolA = if (rolesInvertidos) {
        rolUsuario == RolApp.MODELO
    } else {
        rolUsuario == RolApp.PARTICIPANTE
    }
    val miNombrePersonaje = if (soyRolA) miniserieRolANombre else miniserieRolBNombre
    val miDescPersonaje = if (soyRolA) miniserieRolADesc else miniserieRolBDesc
    val miObjetivo = if (soyRolA) miniserieRolAObjetivo else miniserieRolBObjetivo
    val miFrase = if (soyRolA) miniserieRolAFrase else miniserieRolBFrase

    val parejaNombrePersonaje = if (soyRolA) miniserieRolBNombre else miniserieRolANombre
    val parejaDescPersonaje = if (soyRolA) miniserieRolBDesc else miniserieRolADesc

    // Buscar escena activa o tomar la primera como referencia para giros
    val escenaActivaObj = CatalogoMiniserie.escenas.find { it.titulo == miniserieTitulo }
        ?: CatalogoMiniserie.escenas.first()

    val listaActos = if (actos.isNotEmpty()) {
        actos
    } else {
        escenaActivaObj.obtenerActosCompletos()
    }
    var actoSeleccionadoModal by remember { mutableStateOf(actoActual) }
    androidx.compose.runtime.LaunchedEffect(actoActual) {
        actoSeleccionadoModal = actoActual
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFBE185D))
                                .border(1.5.dp, Color(0xFFFDE047), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎭", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Artistas Fugaces",
                                color = Color(0xFFFDE047),
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "Miniserie Romántica en Vivo",
                                color = Color(0xFFFDA4AF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (miniserieActiva) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🎬 EN ESCENA",
                                color = Color(0xFF34D399),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selector de pestañas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tabs = listOf(
                        "GUIÓN" to "🎭 Guión",
                        "GIROS" to "⚡ Giros",
                        "CATALOGO" to "📚 Capítulos",
                        "MANIFIESTO" to "✨ Filosofía"
                    )
                    tabs.forEach { (id, label) ->
                        val seleccionada = pestanaActual == id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (seleccionada) Color(0xFFBE185D) else Color.Transparent
                                )
                                .clickable { pestanaActual = id }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (seleccionada) Color.White else Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                when (pestanaActual) {
                    "GUIÓN" -> {
                        if (!miniserieActiva) {
                            // Estado inicial sin escena
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                                border = BorderStroke(1.dp, Color(0xFF818CF8)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("✨", fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Ninguna escena activa aún",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFFA5B4FC)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Rompan la rutina de una llamada convencional. Conviértanse en dos artistas fugaces con roles, objetivos secretos y una historia compartida.",
                                        fontSize = 12.sp,
                                        color = Color(0xFFE2E8F0),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = {
                                            val randomEscena = CatalogoMiniserie.escenaAleatoria()
                                            onActivarEscena(randomEscena)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "🎲 ¡Lanzar Capítulo al Azar!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = { pestanaActual = "CATALOGO" },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("📚 Explorar los 6 Capítulos", fontSize = 12.sp, color = Color(0xFF38BDF8))
                                    }
                                    if (onAbrirDirectorAI != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                onDismiss()
                                                onAbrirDirectorAI.invoke()
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("✨ El Director del Sueño (I.A. Gemini)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        } else {
                            // Escena activa
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF31102B)),
                                border = BorderStroke(1.dp, Color(0xFFE11D48)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = miniserieTitulo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFFFDE047),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF831843))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = miniserieMood,
                                                fontSize = 9.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = miniserieSinopsis,
                                        fontSize = 12.sp,
                                        color = Color(0xFFFCE7F3)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "🕯️ Atmósfera: $miniserieAmbiente",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFBCFE8),
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }

                            // Alerta de giro de guión si existe
                            if (miniserieGiroActual.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C4A6E)),
                                    border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("⚡", fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "GIRO DE GUIÓN EN VIVO:",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 10.sp,
                                                color = Color(0xFF7DD3FC)
                                            )
                                            Text(
                                                text = miniserieGiroActual,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // TU PERSONAJE
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF0284C7))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "TU PAPEL",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = miNombrePersonaje,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF334155))
                                                .clickable(onClick = onIntercambiarRoles)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (rolesInvertidos) "🔄 Restaurar" else "🔄 Invertir",
                                                color = Color(0xFFFDE047),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = miDescPersonaje,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Objetivo Secreto
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF431407)),
                                        border = BorderStroke(1.dp, Color(0xFFF97316)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("🤫", fontSize = 14.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "TU OBJETIVO SECRETO:",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFFDBA74)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = miObjetivo,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFFFEDD5)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "⚠️ Manténlo en secreto. Cúmplelo de forma sutil durante la conversación.",
                                                fontSize = 9.sp,
                                                color = Color(0xFFFB923C)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Frase de apertura
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("💬", fontSize = 14.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "FRASE PARA ROMPER EL HIELO:",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF6EE7B7)
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF047857))
                                                        .clickable {
                                                            clipboardManager.setText(AnnotatedString(miFrase))
                                                            Toast.makeText(context, "Frase copiada al portapapeles", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("📋 Copiar", fontSize = 9.sp, color = Color.White)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "\"$miFrase\"",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFFD1FAE5),
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // PERSONAJE DE TU PAREJA
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF64748B)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF475569))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "COMPAÑERO/A",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = parejaNombrePersonaje,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFFE2E8F0)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = parejaDescPersonaje,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            // LIBRETO PROGRESIVO EN 4 ACTOS Y APUNTADOR
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                                border = BorderStroke(1.2.dp, Color(0xFF818CF8)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🎬", fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "Libreto por Episodios / Actos",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFFFDE047)
                                                )
                                                Text(
                                                    text = "Turno por turno • Sincronizado en vivo",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFA5B4FC)
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF4338CA))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Acto $actoActual/$totalActos en vivo",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Selector de Actos
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF0F172A))
                                            .padding(3.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listaActos.forEach { a ->
                                            val seleccionado = actoSeleccionadoModal == a.numero
                                            val esActoEnVivo = a.numero == actoActual
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (seleccionado) Color(0xFF6366F1)
                                                        else if (esActoEnVivo) Color(0xFF312E81)
                                                        else Color.Transparent
                                                    )
                                                    .clickable { actoSeleccionadoModal = a.numero }
                                                    .padding(vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = "Acto ${a.numero}",
                                                        color = if (seleccionado) Color.White else Color(0xFF94A3B8),
                                                        fontSize = 10.sp,
                                                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                    if (esActoEnVivo) {
                                                        Text("● En vivo", fontSize = 7.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    val actoActualObj = listaActos.find { it.numero == actoSeleccionadoModal } ?: listaActos.firstOrNull()

                                    if (actoActualObj != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = actoActualObj.titulo,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "🎯 ${actoActualObj.objetivoClave}",
                                            fontSize = 11.sp,
                                            color = Color(0xFFC7D2FE)
                                        )

                                        if (actoSeleccionadoModal != actoActual && onCambiarActo != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { onCambiarActo(actoSeleccionadoModal) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE185D)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("🚀 Sincronizar Acto $actoSeleccionadoModal a la Videollamada", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "📜 Diálogos sugeridos turno por turno:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFFFDE047)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        actoActualObj.dialogos.forEachIndexed { idx, d ->
                                            val esGamer = d.personaje.equals("GAMER", ignoreCase = true)
                                            val esMiTurno = (esGamer && soyRolA) || (!esGamer && !soyRolA)
                                            val esTurnoActivoEnLlamada = (idx == turnoDialogoActual) && (actoSeleccionadoModal == actoActual)

                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (esTurnoActivoEnLlamada) Color(0xFF4C1D95)
                                                    else if (esMiTurno) Color(0xFF1E293B)
                                                    else Color(0xFF0F172A)
                                                ),
                                                border = BorderStroke(
                                                    if (esTurnoActivoEnLlamada) 1.5.dp else 1.dp,
                                                    if (esTurnoActivoEnLlamada) Color(0xFFF43F5E)
                                                    else if (esMiTurno) Color(0xFF6366F1)
                                                    else Color(0xFF334155)
                                                ),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(if (esMiTurno) Color(0xFFBE185D) else Color(0xFF0284C7))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = if (esMiTurno) "👑 TÚ (${miNombrePersonaje.take(15)})" else "👤 PAREJA (${parejaNombrePersonaje.take(15)})",
                                                                    color = Color.White,
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                            if (d.intencion.isNotBlank()) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(
                                                                    text = "[${d.intencion}]",
                                                                    fontSize = 9.sp,
                                                                    color = Color(0xFFA5B4FC),
                                                                    fontWeight = FontWeight.Medium
                                                                )
                                                            }
                                                        }

                                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            if (onAvanzarTurnoDialogo != null && actoSeleccionadoModal == actoActual) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(4.dp))
                                                                        .background(if (esTurnoActivoEnLlamada) Color(0xFF10B981) else Color(0xFF334155))
                                                                        .clickable { onAvanzarTurnoDialogo(idx) }
                                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                                ) {
                                                                    Text(
                                                                        text = if (esTurnoActivoEnLlamada) "En turno" else "Marcar turno",
                                                                        fontSize = 8.sp,
                                                                        color = Color.White,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                            }
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(Color(0xFF475569))
                                                                    .clickable {
                                                                        clipboardManager.setText(AnnotatedString(d.texto))
                                                                        Toast.makeText(context, "Línea copiada", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                                                ) {
                                                                    Text("Copiar", fontSize = 8.sp, color = Color.White)
                                                                }
                                                        }
                                                    }

                                                    if (d.acotacion.isNotBlank()) {
                                                        Spacer(modifier = Modifier.height(3.dp))
                                                        Text(
                                                            text = d.acotacion,
                                                            fontSize = 10.sp,
                                                            color = Color(0xFFFDE68A),
                                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "\"${d.texto}\"",
                                                        fontSize = 11.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }

                                        // BANQUILLO ANTI-SILENCIO: TEMAS DE CONVERSACIÓN Y CONTRAATAQUES
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = "💡 Banquillo Anti-Silencio del Director",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF38BDF8)
                                                )
                                                Text(
                                                    text = "Si se desvían del libreto o hay pausa, usa estos temas y contraataques:",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF94A3B8)
                                                )

                                                if (actoActualObj.temasConversacion.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("Temas de Emergencia:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE047))
                                                    actoActualObj.temasConversacion.forEach { tema ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 2.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(Color(0xFF1E293B))
                                                                .clickable {
                                                                    clipboardManager.setText(AnnotatedString(tema))
                                                                    Toast.makeText(context, "Tema copiado para conversar", Toast.LENGTH_SHORT).show()
                                                                }
                                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("• $tema", fontSize = 10.sp, color = Color(0xFFE2E8F0), modifier = Modifier.weight(1f))
                                                            Text("📋", fontSize = 10.sp)
                                                        }
                                                    }
                                                }

                                                if (actoActualObj.preguntasContraataque.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("⚡ Preguntas de Contraataque Íntimo:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFB7185))
                                                    actoActualObj.preguntasContraataque.forEach { preg ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 2.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(Color(0xFF31102B))
                                                                .clickable {
                                                                    clipboardManager.setText(AnnotatedString(preg))
                                                                    Toast.makeText(context, "Pregunta copiada", Toast.LENGTH_SHORT).show()
                                                                }
                                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("• $preg", fontSize = 10.sp, color = Color(0xFFFCE7F3), modifier = Modifier.weight(1f))
                                                            Text("💬", fontSize = 10.sp)
                                                        }
                                                    }
                                                }

                                                if (actoActualObj.replicasSugeridas.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("💬 Réplicas Rápidas Sugeridas:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                                                    actoActualObj.replicasSugeridas.forEach { rep ->
                                                        Text(
                                                            text = rep,
                                                            fontSize = 10.sp,
                                                            color = Color(0xFFD1FAE5),
                                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    clipboardManager.setText(AnnotatedString(rep))
                                                                    Toast.makeText(context, "Réplica copiada", Toast.LENGTH_SHORT).show()
                                                                }
                                                                .padding(vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Botones de acción rápida
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { pestanaActual = "GIROS" },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("⚡ Giro de Guión", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        val randomEscena = CatalogoMiniserie.escenaAleatoria()
                                        onActivarEscena(randomEscena)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE185D)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("🎲 Cambiar Escena", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (onAbrirDirectorAI != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onAbrirDirectorAI.invoke()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("✨ Componer Nueva Escena con Director I.A.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = {
                                    onDesactivar()
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("⏹️ Salir del Modo Miniserie", fontSize = 11.sp)
                            }
                        }
                    }

                    "GIROS" -> {
                        Text(
                            text = "⚡ Giros de Guión Dramáticos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Presiona cualquier giro para detonarlo en tiempo real. Aparecerá en la pantalla de ambos participantes con vibración e intriga dramática.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val giroRandom = escenaActivaObj.girosDeGuion.random()
                                onDetonarGiro(giroRandom)
                                pestanaActual = "GUIÓN"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE185D)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Text("🎲 ¡Detonar Giro Imprevisto al Azar!", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        val girosDisponibles = escenaActivaObj.girosDeGuion
                        girosDisponibles.forEach { giro ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF0284C7)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = giro,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            onDetonarGiro(giro)
                                            pestanaActual = "GUIÓN"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("⚡ Lanzar este Giro", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    "CATALOGO" -> {
                        Text(
                            text = "📚 Catálogo de Capítulos Románticos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFFDE047)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Selecciona una escena para sincronizarla en la sala VIP. Ambos recibirán sus personajes y objetivos al instante.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (onAbrirDirectorAI != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .clickable {
                                        onDismiss()
                                        onAbrirDirectorAI.invoke()
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                                border = BorderStroke(1.2.dp, Color(0xFF818CF8)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF6366F1).copy(alpha = 0.25f))
                                            .border(1.dp, Color(0xFFA5B4FC), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✨", fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "El Director del Sueño (I.A.)",
                                                color = Color(0xFFF3E8FF),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF6366F1).copy(alpha = 0.3f))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "NUEVO",
                                                    color = Color(0xFFC7D2FE),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                        Text(
                                            text = "La I.A. pregunta cómo fue tu día y compone un libreto romántico personalizado.",
                                            color = Color(0xFFC7D2FE),
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("🪄", fontSize = 16.sp)
                                }
                            }
                        }

                        CatalogoMiniserie.escenas.forEach { escena ->
                            val estaActiva = miniserieActiva && miniserieTitulo == escena.titulo
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (estaActiva) Color(0xFF31102B) else Color(0xFF1E293B)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (estaActiva) Color(0xFFE11D48) else Color(0xFF475569)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Capítulo ${escena.capituloNumero}",
                                            color = Color(0xFFFDE047),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF334155))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = escena.mood,
                                                fontSize = 9.sp,
                                                color = Color(0xFFE2E8F0)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = escena.titulo,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = escena.sinopsis,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "🎮 ${escena.rolGamer.nombre}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF38BDF8)
                                        )
                                        Text(
                                            text = "💃 ${escena.rolModelo.nombre}",
                                            fontSize = 10.sp,
                                            color = Color(0xFFF472B6)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            onActivarEscena(escena)
                                            pestanaActual = "GUIÓN"
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (estaActiva) Color(0xFF10B981) else Color(0xFFBE185D)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (estaActiva) "✅ Escena Actual Activa" else "🎬 Interpretar esta Escena",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "MANIFIESTO" -> {
                        Text(
                            text = "✨ Manifiesto de los Artistas Fugaces",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFFDE047)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Por qué este modo cambia las reglas del juego psicológico y emocional:",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val pilares = listOf(
                            Triple(
                                "🕊️ Cero Presión Comercial",
                                "Hay mujeres extraordinarias que jamás entrarían a una videollamada puramente por dinero ni por transacciones frías. Este espacio dignifica la interacción convirtiéndola en un juego teatral de complicidad mutua.",
                                Color(0xFFBE185D)
                            ),
                            Triple(
                                "🎭 El Escudo Seguro del Roleplay",
                                "Asumir un personaje romántico da libertad absoluta. Puedes ser atrevida/o, misteriosa/o o seductor/a sin la vulnerabilidad ni la timidez de estar exponiendo tu vida privada directa.",
                                Color(0xFF0284C7)
                            ),
                            Triple(
                                "🔥 Conexión Emocional & Curiosidad",
                                "La curiosidad de '¿cuál será su objetivo secreto?' o '¿cómo reaccionará al próximo giro de guión?' genera química genuina. El tiempo vuela y la experiencia se vuelve inolvidable.",
                                Color(0xFFF59E0B)
                            )
                        )

                        pilares.forEach { (titulo, desc, colorBorde) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, colorBorde),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = titulo,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = desc,
                                        fontSize = 11.sp,
                                        color = Color(0xFFCBD5E1),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
            ) {
                Text("Cerrar", color = Color.White)
            }
        }
    )
}

/**
 * Banner y Apuntador / Teleprompter del Director en Vivo durante la videollamada.
 * Ofrece modo compacto no invasivo y modo teleprompter expandido con diálogos turno a turno,
 * navegación sincrónica de actos (1 a 4) y banquillo anti-silencio de emergencia.
 */
@Composable
fun BannerMiniserieEnLlamada(
    titulo: String,
    miPersonaje: String,
    otroPersonaje: String,
    giroActual: String,
    miObjetivo: String = "",
    miFrase: String = "",
    esMiRolA: Boolean = true,
    actoActual: Int = 1,
    totalActos: Int = 4,
    turnoDialogoActual: Int = 0,
    actos: List<ActoEpisodio> = emptyList(),
    onCambiarActo: ((Int) -> Unit)? = null,
    onAvanzarTurnoDialogo: ((Int) -> Unit)? = null,
    onDetonarGiroAleatorio: (() -> Unit)? = null,
    onCambiarEscena: (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var expandido by remember { mutableStateOf(false) }
    var mostrarBanquilloSilencio by remember { mutableStateOf(false) }

    val actoActivo = actos.find { it.numero == actoActual } ?: actos.firstOrNull()
    val dialogosActivos = actoActivo?.dialogos ?: emptyList()
    val safeTurno = if (dialogosActivos.isNotEmpty()) turnoDialogoActual.coerceIn(0, dialogosActivos.size - 1) else 0
    val dialogoActual = dialogosActivos.getOrNull(safeTurno)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E112A).copy(alpha = 0.96f)),
        border = BorderStroke(1.2.dp, Color(0xFFE11D48)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            // CABECERA SUPERIOR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎬", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = titulo,
                        color = Color(0xFFFDE047),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF6366F1).copy(alpha = 0.3f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Acto $actoActual/$totalActos",
                            color = Color(0xFFA5B4FC),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Rescate Anti-Silencio
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (mostrarBanquilloSilencio) Color(0xFF0284C7) else Color(0xFF0C4A6E))
                            .clickable { mostrarBanquilloSilencio = !mostrarBanquilloSilencio }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "💡 Rescate",
                            color = Color(0xFF7DD3FC),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botón Expandir / Contraer Teleprompter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (expandido) Color(0xFF4338CA) else Color(0xFF1E293B))
                            .clickable { expandido = !expandido }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (expandido) "▲ Ocultar" else "📜 Apuntador",
                            color = Color(0xFFE0E7FF),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botón Ver Guión Completo Modal
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFBE185D))
                            .clickable(onClick = onClick)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Modal 🎭",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tú: $miPersonaje  •  Con: $otroPersonaje",
                    color = Color(0xFFFCE7F3),
                    fontSize = 10.sp
                )
                if (actoActivo != null) {
                    Text(
                        text = actoActivo.titulo.take(30),
                        color = Color(0xFFFDA4AF),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // APUNTADOR EN VIVO (BARRA DE TURNO ACTUAL - VISIBLE EN MODO COMPACTO)
            if (dialogoActual != null) {
                val esGamer = dialogoActual.personaje.equals("GAMER", ignoreCase = true)
                val esMiTurno = (esGamer && esMiRolA) || (!esGamer && !esMiRolA)
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (esMiTurno) Color(0xFF4A0E2E).copy(alpha = 0.9f)
                        else Color(0xFF0F172A).copy(alpha = 0.9f)
                    ),
                    border = BorderStroke(1.dp, if (esMiTurno) Color(0xFFEC4899) else Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (esMiTurno) "👑 TU TURNO AHORA ($miPersonaje):" else "👤 HABLA $otroPersonaje:",
                                    color = if (esMiTurno) Color(0xFFF472B6) else Color(0xFF7DD3FC),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1
                                )
                                if (dialogoActual.acotacion.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = dialogoActual.acotacion,
                                        color = Color(0xFFFDE68A),
                                        fontSize = 8.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (onAvanzarTurnoDialogo != null && dialogosActivos.isNotEmpty()) {
                                    val esUltimoTurnoDelActo = safeTurno >= dialogosActivos.size - 1

                                    if (safeTurno > 0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF1E293B))
                                                .clickable { onAvanzarTurnoDialogo(safeTurno - 1) }
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text("◀", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (esUltimoTurnoDelActo && actoActual < totalActos && onCambiarActo != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFBE185D))
                                                .clickable { onCambiarActo(actoActual + 1) }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("🚀 Acto ${actoActual + 1} ▶", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (esMiTurno) Color(0xFFBE185D) else Color(0xFF334155))
                                                .clickable {
                                                    val nextTurn = if (esUltimoTurnoDelActo) 0 else safeTurno + 1
                                                    onAvanzarTurnoDialogo(nextTurn)
                                                }
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (esUltimoTurnoDelActo) "↺ Repetir" else "▶ Sig. Turno",
                                                fontSize = 8.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF047857))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(dialogoActual.texto))
                                            Toast.makeText(context, "Línea copiada", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                    Text("Copiar", fontSize = 8.sp, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "\"${dialogoActual.texto}\"",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = if (expandido) Int.MAX_VALUE else 2
                        )
                    }
                }
            }

            // POPUP DE RESCATE ANTI-SILENCIO RÁPIDO
            if (mostrarBanquilloSilencio && actoActivo != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C4A6E)),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text(
                            text = "⚡ PREGUNTAS DE RESCATE (Toca para copiar):",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFBAE6FD)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val preguntasMuestra = actoActivo.preguntasContraataque.take(2)
                        preguntasMuestra.forEach { preg ->
                            Text(
                                text = "• \"$preg\"",
                                fontSize = 10.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(preg))
                                        Toast.makeText(context, "Pregunta copiada al portapapeles", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // ALERTA DE GIRO DRAMÁTICO
            if (giroActual.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0C4A6E).copy(alpha = 0.85f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = giroActual,
                        color = Color(0xFF7DD3FC),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // MODO TELEPROMPTER EXPANDIDO (HUD COMPLETO)
            if (expandido) {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color(0xFFE11D48).copy(alpha = 0.5f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(6.dp))

                // NAVEGADOR SINCRONIZADO DE ACTOS (1 a 4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (actoActual > 1 && onCambiarActo != null) {
                                onCambiarActo(actoActual - 1)
                            }
                        },
                        enabled = actoActual > 1 && onCambiarActo != null,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("◀ Acto Ant.", fontSize = 9.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🎬 Acto $actoActual de $totalActos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFFFDE047)
                        )
                        if (actoActivo != null) {
                            Text(
                                text = actoActivo.titulo,
                                fontSize = 9.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (actoActual < totalActos && onCambiarActo != null) {
                                onCambiarActo(actoActual + 1)
                            }
                        },
                        enabled = actoActual < totalActos && onCambiarActo != null,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE185D)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("Sig. Acto ▶", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // DIÁLOGOS COMPLETOS DEL ACTO CON SELECCIÓN DE TURNO
                if (dialogosActivos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📜 Diálogos de este Acto (Toca una línea para marcar tu turno):",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA5B4FC)
                    )
                    Spacer(modifier = Modifier.height(3.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        dialogosActivos.forEachIndexed { idx, d ->
                            val esTurno = idx == safeTurno
                            val esGamerLinea = d.personaje.equals("GAMER", true)
                            val esMiLinea = (esGamerLinea && esMiRolA) || (!esGamerLinea && !esMiRolA)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (esTurno) Color(0xFF4C1D95) else Color(0xFF1E293B))
                                    .border(
                                        1.dp,
                                        if (esTurno) Color(0xFFF43F5E) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onAvanzarTurnoDialogo?.invoke(idx) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (esMiLinea) "👑 Tú ($miPersonaje):" else "👤 Pareja ($otroPersonaje):",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (esTurno) Color(0xFFFDE047) else if (esMiLinea) Color(0xFFF472B6) else Color(0xFF7DD3FC)
                                        )
                                        if (esTurno) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (esMiLinea) "[¡Te toca hablar!]" else "[Escuchando...]",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (esMiLinea) Color(0xFF34D399) else Color(0xFF38BDF8)
                                            )
                                        }
                                        if (d.acotacion.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = d.acotacion,
                                                fontSize = 8.sp,
                                                color = Color(0xFFFDE68A),
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            )
                                        }
                                    }
                                    Text(
                                        text = "\"${d.texto}\"",
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF334155))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(d.texto))
                                            Toast.makeText(context, "Línea copiada", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Copiar", fontSize = 7.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // BANQUILLO ANTI-SILENCIO EXPANDIDO
                if (actoActivo != null) {
                    if (actoActivo.temasConversacion.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "💡 Temas sugeridos para este acto:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFDE047)
                        )
                        actoActivo.temasConversacion.take(2).forEach { t ->
                            Text(
                                text = "• $t",
                                fontSize = 9.sp,
                                color = Color(0xFFE2E8F0),
                                modifier = Modifier
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(t))
                                        Toast.makeText(context, "Tema copiado", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 1.dp)
                            )
                        }
                    }

                    if (actoActivo.preguntasContraataque.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚡ Contraataques para romper el silencio:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFB7185)
                        )
                        actoActivo.preguntasContraataque.take(2).forEach { p ->
                            Text(
                                text = "• \"$p\"",
                                fontSize = 9.sp,
                                color = Color(0xFFFFEDD5),
                                modifier = Modifier
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(p))
                                        Toast.makeText(context, "Pregunta copiada", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 1.dp)
                            )
                        }
                    }
                }

                if (miObjetivo.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤫", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "OBJETIVO SECRETO: $miObjetivo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color(0xFFFDBA74)
                        )
                    }
                }

                // Acciones rápidas en llamada
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (onDetonarGiroAleatorio != null) {
                        Button(
                            onClick = onDetonarGiroAleatorio,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                        ) {
                            Text("⚡ Giro al Azar", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (onCambiarEscena != null) {
                        Button(
                            onClick = onCambiarEscena,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBE185D)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                        ) {
                            Text("🎲 Otra Escena", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente directo de Teleprompter / Apuntador para la videollamada.
 */
@Composable
fun TeleprompterDirectorEnLlamada(
    titulo: String,
    miPersonaje: String,
    otroPersonaje: String,
    giroActual: String,
    miObjetivo: String = "",
    miFrase: String = "",
    esMiRolA: Boolean = true,
    actoActual: Int = 1,
    totalActos: Int = 4,
    turnoDialogoActual: Int = 0,
    actos: List<ActoEpisodio> = emptyList(),
    onCambiarActo: ((Int) -> Unit)? = null,
    onAvanzarTurnoDialogo: ((Int) -> Unit)? = null,
    onDetonarGiroAleatorio: (() -> Unit)? = null,
    onCambiarEscena: (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BannerMiniserieEnLlamada(
        titulo = titulo,
        miPersonaje = miPersonaje,
        otroPersonaje = otroPersonaje,
        giroActual = giroActual,
        miObjetivo = miObjetivo,
        miFrase = miFrase,
        esMiRolA = esMiRolA,
        actoActual = actoActual,
        totalActos = totalActos,
        turnoDialogoActual = turnoDialogoActual,
        actos = actos,
        onCambiarActo = onCambiarActo,
        onAvanzarTurnoDialogo = onAvanzarTurnoDialogo,
        onDetonarGiroAleatorio = onDetonarGiroAleatorio,
        onCambiarEscena = onCambiarEscena,
        onClick = onClick,
        modifier = modifier
    )
}

/**
 * Overlay de alerta dramática de Giro de Guión sobre el video en llamada.
 */
@Composable
fun OverlayGiroDramatico(
    giro: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.runtime.LaunchedEffect(giro) {
        kotlinx.coroutines.delay(7000)
        onDismiss()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C4A6E).copy(alpha = 0.95f)),
        border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚡", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "¡GIRO DE GUIÓN EN VIVO!",
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    color = Color(0xFF7DD3FC)
                )
                Text(
                    text = giro,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF0369A1))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("OK", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Disparador de respuesta háptica (vibración dramática doble) para momentos clave.
 */
fun triggerHapticFeedback(context: android.content.Context) {
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            val vibrator = vibratorManager?.defaultVibrator
            vibrator?.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 250), -1))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(android.os.VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 250), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 150, 100, 250), -1)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Botón / tarjeta interactiva para el Lobby previo a entrar a la llamada.
 */
@Composable
fun CardMiniserieLobby(
    miniserieActiva: Boolean,
    miniserieTitulo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (miniserieActiva) Color(0xFF31102B) else Color(0xFF1E1B4B)
        ),
        border = BorderStroke(
            1.2.dp,
            if (miniserieActiva) Color(0xFFE11D48) else Color(0xFF818CF8)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (miniserieActiva) Color(0xFFBE185D) else Color(0xFF4F46E5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎭", fontSize = 17.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Artistas Fugaces",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (miniserieActiva) Color(0xFFFDE047) else Color(0xFFA5B4FC)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "ROMANCE",
                                color = Color(0xFFF59E0B),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (miniserieActiva)
                            "🎬 Escena lista: $miniserieTitulo"
                        else
                            "Improvisen personajes en una historia romántica única",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (miniserieActiva) Color(0xFFBE185D) else Color(0xFF4F46E5))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (miniserieActiva) "Ver Rol" else "Explorar",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Botón / tarjeta interactiva en el Lobby para convocar a "El Director del Sueño (I.A. Gemini)".
 */
@Composable
fun CardDirectorSuenoLobby(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.2.dp, Color(0xFF6366F1)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4F46E5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", fontSize = 17.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "El Director del Sueño",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA5B4FC)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF6366F1).copy(alpha = 0.3f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "I.A. GEMINI",
                                color = Color(0xFFC7D2FE),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "¿Cómo fue tu día? Compone un libreto para sanar y soñar despiertos",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF4F46E5))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Dirigir",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

