package com.example.my1eraapplication

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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

/**
 * Modal ineludible de Verificación de Mayoría de Edad (+18 Gate):
 * Garantiza que únicamente creadoras y modelos adultas verificadas (mayores de 18 años)
 * puedan acceder a la transmisión en vivo, cámara y lobby de Velvet Modelos.
 */
@Composable
fun ModalVerificacionEdadModelo(
    onVerificacionExitosa: (alias: String, anioNacimiento: Int) -> Unit
) {
    val context = LocalContext.current
    var alias by remember { mutableStateOf("") }
    var anioNacimientoTexto by remember { mutableStateOf("") }
    var confirmoMayor18 by remember { mutableStateOf(false) }
    var aceptoTerminos by remember { mutableStateOf(false) }

    val anioInt = anioNacimientoTexto.toIntOrNull()
    // Año límite para ser mayor de 18 (2026 - 18 = 2008)
    val esMayorDeEdad = anioInt != null && anioInt in 1940..2008
    val formularioValido = alias.trim().isNotEmpty() && esMayorDeEdad && confirmoMayor18 && aceptoTerminos

    AlertDialog(
        onDismissRequest = {
            // Modal forzoso / ineludible: no se puede cerrar tocando fuera
        },
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                        .border(2.dp, Color(0xFFEF4444), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔞", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Acceso Exclusivo +18",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Velvet Modelos — Verificación Obligatoria",
                    color = Color(0xFFF87171),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Por normativas legales y protección de la comunidad, esta aplicación está estrictamente reservada para creadoras y modelos adultas independientes (18 años o más).",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("Nombre o Alias de Creadora") },
                    placeholder = { Text("Ej. Sofía Velvet") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEC4899),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = anioNacimientoTexto,
                    onValueChange = { if (it.length <= 4) anioNacimientoTexto = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Año de Nacimiento (≤ 2008)") },
                    placeholder = { Text("Ej. 2002") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = anioNacimientoTexto.length == 4 && !esMayorDeEdad,
                    supportingText = {
                        if (anioNacimientoTexto.length == 4 && !esMayorDeEdad) {
                            Text("Debes ser mayor de 18 años (nacida en 2008 o antes)", color = Color(0xFFEF4444), fontSize = 10.sp)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEC4899),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = confirmoMayor18,
                        onCheckedChange = { confirmoMayor18 = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEC4899))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Certifico bajo protesta de decir verdad que tengo 18 años de edad cumplidos o más.",
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = aceptoTerminos,
                        onCheckedChange = { aceptoTerminos = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFEC4899))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Acepto los Términos de Servicio y Normas de Transmisión Independiente de Velvet.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (formularioValido) {
                        onVerificacionExitosa(alias.trim(), anioInt!!)
                        Toast.makeText(context, "✅ Identidad +18 verificada. ¡Bienvenida a Velvet Modelos!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Por favor completa todos los campos requeridos para verificar tu mayoría de edad", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = formularioValido,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEC4899),
                    disabledContainerColor = Color(0xFF475569)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🔞 Confirmar y Desbloquear Sala (+18)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    (context as? Activity)?.finishAffinity()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salir de la Aplicación", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
        }
    )
}
