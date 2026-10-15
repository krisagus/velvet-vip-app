package com.example.my1eraapplication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my1eraapplication.webrtc.WebRtcCallState
import kotlin.math.roundToInt

/**
 * HUD Moderno en "U Invertida" (Inspirado en la ergonomía de Comfer)
 * Distribuye los controles y los 3 carretes del tragamonedas en un arco perimetral
 * (lateral izquierdo, arco superior y lateral derecho), dejando el 100% del centro
 * despejado para una visión totalmente amplificada de la cámara en vivo.
 */
@Composable
fun HudMenuUInvertida(
    rol: RolApp,
    enLlamada: Boolean,
    webrtcCallState: WebRtcCallState,
    salaId: String,
    segundosRestantes: Long,
    pesosGanadosModelo: Double,
    bonoDobleSegundos: Long,
    carretes: Triple<String, String, String>,
    girandoCarretes: Boolean,
    apuestaActual: Int,
    puedeGirar: Boolean,
    onGirarSlot: () -> Unit,
    onOpenSlotConfig: () -> Unit,
    onToggleCamera: () -> Unit,
    onTogglePiP: () -> Unit,
    mostrarPiP: Boolean,
    onOpenMiniserie: () -> Unit,
    miniserieActiva: Boolean,
    miniserieTitulo: String = "",
    miniserieActoActual: Int = 1,
    miniserieTotalActos: Int = 4,
    miniserieFraseActual: String = "",
    onOpenGeminiAI: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenMercadoPago: () -> Unit,
    onColgar: () -> Unit,
    onSaltarSiguiente: (() -> Unit)? = null,
    onIniciarTransmisionLobby: (() -> Unit)? = null,
    onConectarAleatorioLobby: (() -> Unit)? = null,
    onEntrarSalaDirectoLobby: (() -> Unit)? = null,
    buscandoSalaAleatoria: Boolean = false,
    modifier: Modifier = Modifier
) {
    val esModelo = rol == RolApp.MODELO

    // Animación infinita para el pulso de transmisión en vivo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Desplazamiento rotacional táctil de los laterales (efecto rueda Comfer)
    var offsetRotacionIzquierda by remember { mutableFloatStateOf(0f) }
    var offsetRotacionDerecha by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 24.dp)
    ) {
        // =========================================================================
        // 1. ARCO SUPERIOR DE LA "U" INVERTIDA (Status, Carretes Tragamonedas y Saldo)
        // =========================================================================
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fila superior: Sala / Conexión + Saldo / Timer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador de Sala y Estado WebRTC
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.82f))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (webrtcCallState == WebRtcCallState.CONNECTED) Color(0xFF10B981)
                                    else if (enLlamada) Color(0xFFF59E0B)
                                    else Color(0xFF94A3B8)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (esModelo) "☁️ $salaId · Creadora" else "☁️ $salaId · VIP",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Indicador de Saldo o Segundos
                if (esModelo) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF065F46).copy(alpha = 0.85f))
                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(20.dp))
                            .clickable { onOpenWallet() }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "💰 $${String.format("%.2f", pesosGanadosModelo)} MXN",
                            color = Color(0xFF34D399),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0369A1).copy(alpha = 0.85f))
                            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                            .clickable { onOpenMercadoPago() }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "⏱️ ${segundosRestantes}s 💎",
                            color = Color(0xFF7DD3FC),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // CÁPSULA CENTRAL: LOS 3 CARRETES DEL TRAGAMONEDAS EN EL ÁPICE DEL ARCO
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B1E).copy(alpha = 0.90f)),
                border = BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFEC4899), Color(0xFFF59E0B), Color(0xFF38BDF8))
                    )
                ),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(26.dp))
                    .clickable(enabled = puedeGirar && !girandoCarretes) { onGirarSlot() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Carrete 1
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E1738))
                            .border(1.dp, Color(0xFFF472B6).copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = carretes.first,
                            fontSize = 20.sp,
                            modifier = if (girandoCarretes) Modifier.scale(pulseScale) else Modifier
                        )
                    }

                    // Carrete 2
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E1738))
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = carretes.second,
                            fontSize = 20.sp,
                            modifier = if (girandoCarretes) Modifier.scale(pulseScale) else Modifier
                        )
                    }

                    // Carrete 3
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E1738))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = carretes.third,
                            fontSize = 20.sp,
                            modifier = if (girandoCarretes) Modifier.scale(pulseScale) else Modifier
                        )
                    }

                    // Botón de Disparo o Configuración del Slot
                    Button(
                        onClick = { onGirarSlot() },
                        enabled = puedeGirar && !girandoCarretes,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEC4899),
                            disabledContainerColor = Color(0xFF475569)
                        ),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = if (girandoCarretes) "⚡" else "🎰 x${apuestaActual}s",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Bono Doble / Indicador Activo
            if (bonoDobleSegundos > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFDC2626).copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🔥 ¡TARIFA DOBLE ACTIVA: $1.00/s (${bonoDobleSegundos}s)! 🔥",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // =========================================================================
        // 2. BRAZO IZQUIERDO DE LA "U" INVERTIDA (Rueda de Controles de Cámara y Dramaturgia)
        // =========================================================================
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset(0, offsetRotacionIzquierda.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetRotacionIzquierda = (offsetRotacionIzquierda + dragAmount.y).coerceIn(-120f, 120f)
                    }
                },
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Girar Cámara (Frontal / Trasera)
            BotonCircularComfer(
                icono = "🔄",
                titulo = "Cámara",
                colorGlow = Color(0xFF38BDF8),
                onClick = onToggleCamera
            )

            // Alternar PiP
            BotonCircularComfer(
                icono = if (mostrarPiP) "👁️" else "🙈",
                titulo = "PiP",
                colorGlow = Color(0xFFA855F7),
                onClick = onTogglePiP
            )

            // Guión / Miniserie Romántica
            BotonCircularComfer(
                icono = "🎭",
                titulo = "Guión",
                colorGlow = if (miniserieActiva) Color(0xFFFACC15) else Color(0xFFF472B6),
                activo = miniserieActiva,
                onClick = onOpenMiniserie
            )

            // Director I.A. Gemini
            BotonCircularComfer(
                icono = "✨",
                titulo = "I.A.",
                colorGlow = Color(0xFF6366F1),
                onClick = onOpenGeminiAI
            )
        }

        // =========================================================================
        // 3. BRAZO DERECHO DE LA "U" INVERTIDA (Finanzas, Ajustes y Salida de Emergencia)
        // =========================================================================
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset { IntOffset(0, offsetRotacionDerecha.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetRotacionDerecha = (offsetRotacionDerecha + dragAmount.y).coerceIn(-120f, 120f)
                    }
                },
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Billetera / Recarga
            if (esModelo) {
                BotonCircularComfer(
                    icono = "💼",
                    titulo = "Retiro",
                    colorGlow = Color(0xFF10B981),
                    onClick = onOpenWallet
                )
            } else {
                BotonCircularComfer(
                    icono = "💎",
                    titulo = "Recargar",
                    colorGlow = Color(0xFF009EE3),
                    onClick = onOpenMercadoPago
                )
            }

            // Configuración de Apuesta / Auto-Spin
            BotonCircularComfer(
                icono = "🎰",
                titulo = "Apuesta",
                colorGlow = Color(0xFFF59E0B),
                onClick = onOpenSlotConfig
            )

            // Saltar siguiente (Solo Gamer)
            if (!esModelo && enLlamada && onSaltarSiguiente != null) {
                BotonCircularComfer(
                    icono = "⏭️",
                    titulo = "Siguiente",
                    colorGlow = Color(0xFFEC4899),
                    onClick = onSaltarSiguiente
                )
            }

            // Colgar / Desconectar (Siempre visible y accesible)
            if (enLlamada) {
                BotonCircularComfer(
                    icono = "🔴",
                    titulo = "Colgar",
                    colorGlow = Color(0xFFEF4444),
                    esPeligro = true,
                    onClick = onColgar
                )
            }
        }

        // =========================================================================
        // 4. BASE Y CENTRO: 100% CÁMARA DESPEJADA CON APUNTADOR COMPACTO O BOTONES LOBBY
        // =========================================================================
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Si hay miniserie activa durante la llamada: Apuntador ultra-compacto tipo teleprompter
            if (enLlamada && miniserieActiva) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B1E).copy(alpha = 0.88f)),
                    border = BorderStroke(1.dp, Color(0xFFBE185D).copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clickable { onOpenMiniserie() }
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎬 $miniserieTitulo",
                                color = Color(0xFFFDE047),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Acto $miniserieActoActual/$miniserieTotalActos 🎭",
                                color = Color(0xFFA5B4FC),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (miniserieFraseActual.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "💬 \"$miniserieFraseActual\"",
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // CONTROLES DE LOBBY (Cuando no está en llamada activa)
            if (!enLlamada) {
                if (esModelo) {
                    // Botón gigante para la Creadora: Iniciar Transmisión VIP (En Vivo)
                    Button(
                        onClick = { onIniciarTransmisionLobby?.invoke() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(52.dp)
                            .shadow(12.dp, RoundedCornerShape(22.dp))
                    ) {
                        Text(
                            text = "💃 Iniciar Transmisión VIP (En Vivo)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    // Botones del Gamer en el Lobby
                    Row(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onConectarAleatorioLobby?.invoke() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(20.dp),
                            enabled = !buscandoSalaAleatoria,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(
                                text = if (buscandoSalaAleatoria) "🎲 Buscando..." else "🎲 Ruleta VIP",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = { onEntrarSalaDirectoLobby?.invoke() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .height(50.dp)
                        ) {
                            Text(
                                text = "📞 Sala 1",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Botón Circular flotante con estética moderna tipo lanzador Comfer.
 */
@Composable
fun BotonCircularComfer(
    icono: String,
    titulo: String,
    colorGlow: Color,
    activo: Boolean = false,
    esPeligro: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (esPeligro) Color(0xFFDC2626) else Color(0xFF0F172A).copy(alpha = 0.88f)
    val borderColor = if (activo) Color(0xFFFACC15) else colorGlow.copy(alpha = 0.75f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(1.8.dp, borderColor, CircleShape)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icono,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = titulo,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
