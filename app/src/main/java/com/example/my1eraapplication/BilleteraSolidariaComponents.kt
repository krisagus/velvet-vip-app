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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modal de Billetera y Retiro de Ganancias para la Modelo/Creadora:
 * Permite a la creadora consultar su saldo acumulado en pesos mexicanos ($XX.XX MXN)
 * y transferirlo de manera inmediata a su tarjeta bancaria, CLABE interbancaria o Mercado Pago.
 */
@Composable
fun ModalBilleteraGanancias(
    saldoGanadoMxn: Double,
    onRetirarSaldo: (monto: Double, clabe: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var titularCuenta by remember { mutableStateOf("") }
    var clabeBancaria by remember { mutableStateOf("") }
    var montoRetiro by remember {
        mutableStateOf(if (saldoGanadoMxn > 0) String.format("%.2f", saldoGanadoMxn) else "")
    }
    var retiroExitosoMensaje by remember { mutableStateOf<String?>(null) }

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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF38BDF8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💼", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Billetera de Ganancias",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "Gestión de ingresos y retiros directos",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🟢 ACTIVA",
                            color = Color(0xFF10B981),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Tarjeta de Resumen Financiero
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SALDO DISPONIBLE PARA RETIRO",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$${String.format("%.2f", saldoGanadoMxn)} MXN",
                                    color = Color(0xFF10B981),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$0.50 MXN/s",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = Color(0xFF334155)
                        )

                        Text(
                            text = "⚡ Tarifa base: $30.00 MXN por minuto en transmisión activa ($60.00 MXN/min durante Bono Doble).",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "💳 Datos para Transferencia Inmediata",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Transfiere a tu cuenta bancaria (CLABE interbancaria de 18 dígitos), tarjeta de débito o cuenta Mercado Pago.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = titularCuenta,
                    onValueChange = { titularCuenta = it },
                    label = { Text("Nombre Completo de la Titular") },
                    placeholder = { Text("Ej. Ana María López") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = clabeBancaria,
                    onValueChange = { if (it.length <= 18) clabeBancaria = it },
                    label = { Text("CLABE (18 dígitos) o Teléfono MP") },
                    placeholder = { Text("012180001234567890") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = montoRetiro,
                    onValueChange = { montoRetiro = it },
                    label = { Text("Monto a Retirar en MXN") },
                    placeholder = { Text("Ej. 500.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botones rápidos de monto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (saldoGanadoMxn >= 100.0) {
                        OutlinedButton(
                            onClick = { montoRetiro = "100.00" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF475569))
                        ) {
                            Text("$100", fontSize = 11.sp, color = Color.White)
                        }
                    }
                    if (saldoGanadoMxn >= 500.0) {
                        OutlinedButton(
                            onClick = { montoRetiro = "500.00" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF475569))
                        ) {
                            Text("$500", fontSize = 11.sp, color = Color.White)
                        }
                    }
                    OutlinedButton(
                        onClick = { montoRetiro = String.format("%.2f", saldoGanadoMxn) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF0284C7))
                    ) {
                        Text("Todo", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                }

                AnimatedVisibility(visible = retiroExitosoMensaje != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = retiroExitosoMensaje ?: "",
                            color = Color(0xFF34D399),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val monto = montoRetiro.toDoubleOrNull() ?: 0.0
                    if (monto <= 0.0 || monto > saldoGanadoMxn) {
                        Toast.makeText(context, "Ingresa un monto de retiro válido (hasta $${String.format("%.2f", saldoGanadoMxn)})", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (clabeBancaria.length < 10) {
                        Toast.makeText(context, "Ingresa una CLABE (18 dígitos) o cuenta válida", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onRetirarSaldo(monto, clabeBancaria)
                    retiroExitosoMensaje = "✅ Solicitud de retiro de $${String.format("%.2f", monto)} MXN procesada con éxito a ${clabeBancaria.takeLast(4)}."
                    Toast.makeText(context, "Transferencia iniciada exitosamente", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("💳 Procesar Retiro", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cerrar", color = Color.Gray)
            }
        }
    )
}

/**
 * Sobrecarga compatible hacia atrás con llamadas anteriores de ModalBilleteraSolidaria
 */
@Composable
fun ModalBilleteraSolidaria(
    saldoGanadoMxn: Double,
    totalDonadoMxn: Double = 0.0,
    esActrizSolidaria: Boolean = false,
    onRetirarSaldo: (monto: Double, clabe: String) -> Unit,
    onDonarSaldo: ((monto: Double, institucion: String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    ModalBilleteraGanancias(
        saldoGanadoMxn = saldoGanadoMxn,
        onRetirarSaldo = onRetirarSaldo,
        onDismiss = onDismiss
    )
}
