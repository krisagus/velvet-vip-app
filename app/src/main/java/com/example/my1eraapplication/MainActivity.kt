package com.example.my1eraapplication

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.my1eraapplication.ui.theme.My1eraApplicationTheme
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import androidx.compose.runtime.collectAsState
import com.example.my1eraapplication.webrtc.WebRtcCallState
import com.example.my1eraapplication.webrtc.WebRtcSessionManager
import com.example.my1eraapplication.webrtc.WebRtcSignalingClient
import com.example.my1eraapplication.webrtc.WebRtcVideoView
import com.example.my1eraapplication.model.ActoEpisodio
import com.example.my1eraapplication.model.CatalogoMiniserie
import com.example.my1eraapplication.model.EscenaMiniserie
import com.example.my1eraapplication.model.actoEpisodioFromMap
import com.example.my1eraapplication.model.toMap
import android.app.Activity
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.IntentSenderRequest
import com.example.my1eraapplication.googlepay.GooglePayHelper
import com.example.my1eraapplication.googlepay.GooglePayPaymentResult
import com.example.my1eraapplication.googlepay.PaqueteTiempoVip
import com.example.my1eraapplication.viewmodel.GooglePayViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.WalletConstants
import com.example.my1eraapplication.payment.MercadoPagoHelper
import com.example.my1eraapplication.payment.CryptoPaymentHelper
import com.example.my1eraapplication.payment.RedCripto
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.google.pay.button.PayButton

class MainActivity : ComponentActivity() {
    companion object {
        var googlePayResultCallback: ((Int, Intent?) -> Unit)? = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GooglePayHelper.LOAD_PAYMENT_DATA_REQUEST_CODE) {
            googlePayResultCallback?.invoke(resultCode, data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.setProperty("java.net.preferIPv4Stack", "true")
        System.setProperty("java.net.preferIPv6Addresses", "false")
        enableEdgeToEdge()
        setContent {
            My1eraApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaVideollamadaP2P(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * Roles del sistema: Modelo (Emisora / Ganancias 50%) vs Gamer (Cliente / $1.00 por seg)
 */
enum class RolApp {
    MODELO,
    PARTICIPANTE
}



@Composable
fun PantallaVideollamadaP2P(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("velvet_vip_prefs", android.content.Context.MODE_PRIVATE) }
    val rolGuardado = remember { prefs.getString("user_role", RolApp.PARTICIPANTE.name) }

    val appRoleFlavor = BuildConfig.APP_ROLE
    val esAppModelo = appRoleFlavor.equals("MODELO", ignoreCase = true)
    val esAppGamer = appRoleFlavor.equals("GAMER", ignoreCase = true)

    var rolSeleccionado by remember {
        mutableStateOf(
            if (esAppModelo) RolApp.MODELO
            else if (esAppGamer) RolApp.PARTICIPANTE
            else if (rolGuardado == RolApp.MODELO.name) RolApp.MODELO
            else RolApp.PARTICIPANTE
        )
    }

    var edadVerificada18 by remember {
        mutableStateOf(prefs.getBoolean("edad_verificada_18", false))
    }

    LaunchedEffect(rolSeleccionado) {
        prefs.edit().putString("user_role", rolSeleccionado.name).apply()
    }

    var enLlamada by remember { mutableStateOf(false) }
    var camaraFrontal by remember { mutableStateOf(true) }
    var mostrarPiP by remember { mutableStateOf(true) }

    // Estados de Firestore en la nube
    var segundosRestantes by remember { mutableLongStateOf(120L) }
    var pesosGanadosModelo by remember { mutableDoubleStateOf(0.0) }
    var bonoDobleSegundos by remember { mutableLongStateOf(0L) }
    var autorizarModeloGirar by remember { mutableStateOf(false) }
    var autoSpinActivo by remember { mutableStateOf(false) }
    var apuestaActual by remember { mutableIntStateOf(5) }

    var ultimoJuegoResultado by remember { mutableStateOf("¡Gira la ruleta y ganan los dos!") }
    var mostrarMinijuegoModal by remember { mutableStateOf(false) }
    var mostrarMiniserieModal by remember { mutableStateOf(false) }
    var alertaTiempoAgotado by remember { mutableStateOf(false) }
    var mostrarMercadoPagoModal by remember { mutableStateOf(false) }
    var mostrarDialogoActualizacion by remember { mutableStateOf(false) }
    var versionNubeInfo by remember { mutableStateOf("") }

    // Estados de Billetera y Director del Sueño (I.A.)
    var mostrarBilleteraSolidaria by remember { mutableStateOf(false) }
    var mostrarDirectorSuenoAI by remember { mutableStateOf(false) }

    // Estados de Miniserie Romántica (Artistas Fugaces)
    var miniserieActiva by remember { mutableStateOf(false) }
    var miniserieTitulo by remember { mutableStateOf("") }
    var miniserieSinopsis by remember { mutableStateOf("") }
    var miniserieAmbiente by remember { mutableStateOf("") }
    var miniserieMood by remember { mutableStateOf("") }
    var miniserieRolANombre by remember { mutableStateOf("") }
    var miniserieRolADesc by remember { mutableStateOf("") }
    var miniserieRolAObjetivo by remember { mutableStateOf("") }
    var miniserieRolAFrase by remember { mutableStateOf("") }
    var miniserieRolBNombre by remember { mutableStateOf("") }
    var miniserieRolBDesc by remember { mutableStateOf("") }
    var miniserieRolBObjetivo by remember { mutableStateOf("") }
    var miniserieRolBFrase by remember { mutableStateOf("") }
    var miniserieGiroActual by remember { mutableStateOf("") }
    var miniserieRolesInvertidos by remember { mutableStateOf(false) }
    var miniserieActoActual by remember { mutableStateOf(1) }
    var miniserieTotalActos by remember { mutableStateOf(4) }
    var miniserieTurnoDialogo by remember { mutableStateOf(0) }
    var miniserieActosLista by remember { mutableStateOf<List<ActoEpisodio>>(emptyList()) }
    var ultimoGiroVisto by remember { mutableStateOf("") }
    var mostrarBannerGiroDramatico by remember { mutableStateOf(false) }

    val firestore = remember { FirebaseFirestore.getInstance() }

    // Verificación amigable de actualización: pregunta antes de actualizar y nunca bloquea
    LaunchedEffect(Unit) {
        firestore.collection("configuracion_app").document("version")
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val versionCodeNube = doc.getLong("version_code") ?: 0L
                    val versionNameNube = doc.getString("version_name") ?: ""
                    val novedades = doc.getString("novedades") ?: "Nuevas mejoras disponibles"
                    val miVersionCode = 3L // Código de versión actual v1.2
                    if (versionCodeNube > miVersionCode) {
                        versionNubeInfo = "v$versionNameNube: $novedades"
                        mostrarDialogoActualizacion = true
                    }
                }
            }
    }
    var salaId by remember { mutableStateOf("sala_vip_1") }
    val signalingClient = remember { WebRtcSignalingClient(firestore, salaId) }
    val sessionManager = remember { WebRtcSessionManager(context.applicationContext, signalingClient, coroutineScope) }
    val webrtcCallState by sessionManager.callState.collectAsState()
    val iceConnectionState by sessionManager.iceConnectionState.collectAsState()
    val remoteVideoTrack by sessionManager.remoteVideoTrack.collectAsState()
    val localVideoTrack by sessionManager.localVideoTrack.collectAsState()

    var carrete1 by remember { mutableStateOf("🍒") }
    var carrete2 by remember { mutableStateOf("7️⃣") }
    var carrete3 by remember { mutableStateOf("💎") }
    var girandoCarretes by remember { mutableStateOf(false) }

    var buscandoSalaAleatoria by remember { mutableStateOf(false) }

    fun conectarAleatorio() {
        if (segundosRestantes <= 0 && rolSeleccionado == RolApp.PARTICIPANTE) {
            alertaTiempoAgotado = true
            return
        }
        buscandoSalaAleatoria = true
        firestore.collection("salas_videollamada")
            .whereEqualTo("disponible", true)
            .get()
            .addOnSuccessListener { snapshots ->
                buscandoSalaAleatoria = false
                val candidatas = snapshots.documents.filter { doc ->
                    doc.getBoolean("en_llamada") != true
                }
                val elegidaDoc = if (candidatas.isNotEmpty()) {
                    candidatas.random()
                } else {
                    null
                }
                val elegida = elegidaDoc?.id ?: "sala_vip_1"
                salaId = elegida
                signalingClient.salaId = elegida

                // Si se había seleccionado una miniserie en el lobby, sincronizarla a la sala elegida si esta no tiene una activa
                if (miniserieActiva) {
                    val salaTieneMiniserie = elegidaDoc?.getBoolean("miniserie_activa") ?: false
                    if (!salaTieneMiniserie) {
                        val updates = mapOf(
                            "miniserie_activa" to true,
                            "miniserie_titulo" to miniserieTitulo,
                            "miniserie_sinopsis" to miniserieSinopsis,
                            "miniserie_ambiente" to miniserieAmbiente,
                            "miniserie_mood" to miniserieMood,
                            "miniserie_rol_a_nombre" to miniserieRolANombre,
                            "miniserie_rol_a_desc" to miniserieRolADesc,
                            "miniserie_rol_a_objetivo" to miniserieRolAObjetivo,
                            "miniserie_rol_a_frase" to miniserieRolAFrase,
                            "miniserie_rol_b_nombre" to miniserieRolBNombre,
                            "miniserie_rol_b_desc" to miniserieRolBDesc,
                            "miniserie_rol_b_objetivo" to miniserieRolBObjetivo,
                            "miniserie_rol_b_frase" to miniserieRolBFrase,
                            "miniserie_giro_actual" to miniserieGiroActual,
                            "miniserie_roles_invertidos" to miniserieRolesInvertidos,
                            "miniserie_acto_actual" to miniserieActoActual,
                            "miniserie_total_actos" to miniserieTotalActos,
                            "miniserie_turno_dialogo" to miniserieTurnoDialogo,
                            "miniserie_actos" to miniserieActosLista.map { it.toMap() },
                            "ultimo_juego" to "🎭 Miniserie: $miniserieTitulo"
                        )
                        firestore.collection("salas_videollamada").document(elegida)
                            .set(updates, SetOptions.merge())
                    }
                }

                enLlamada = true
            }
            .addOnFailureListener {
                buscandoSalaAleatoria = false
                salaId = "sala_vip_1"
                signalingClient.salaId = "sala_vip_1"
                enLlamada = true
            }
    }

    fun saltarSiguiente() {
        coroutineScope.launch {
            enLlamada = false
            sessionManager.closeCall()
            firestore.collection("salas_videollamada").document(salaId)
                .update("en_llamada", false, "disponible", true)
            delay(500)
            conectarAleatorio()
        }
    }

    var tienePermisoCamara by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        tienePermisoCamara = permisos[Manifest.permission.CAMERA] == true
    }

    // Iniciar previsualización local acelerada por hardware cuando se conceden permisos
    LaunchedEffect(tienePermisoCamara) {
        if (tienePermisoCamara) {
            sessionManager.startLocalPreview(isFrontCamera = camaraFrontal)
        }
    }

    // Solicitar permisos al inicio
    LaunchedEffect(Unit) {
        if (!tienePermisoCamara) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    // Control del ciclo de vida WebRTC en la nube (100% Internet con Google STUN y Firestore)
    LaunchedEffect(enLlamada, rolSeleccionado, salaId) {
        if (enLlamada) {
            signalingClient.salaId = salaId
            if (rolSeleccionado == RolApp.PARTICIPANTE) {
                sessionManager.startCallAsCaller(isFrontCamera = camaraFrontal)
            } else {
                sessionManager.joinCallAsCallee(isFrontCamera = camaraFrontal)
            }
        } else {
            sessionManager.closeCall()
        }
    }

    // Sincronización en tiempo real con Cloud Firestore (Querétaro)
    DisposableEffect(salaId) {
        signalingClient.salaId = salaId
        val listenerReg: ListenerRegistration = firestore.collection("salas_videollamada")
            .document(salaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val sRestantes = snapshot.getLong("segundos_participante") ?: 120L
                val pModelo = snapshot.getDouble("pesos_modelo") ?: 0.0
                val bono = snapshot.getLong("bono_doble_segundos") ?: 0L
                val autoGirar = snapshot.getBoolean("permiso_modelo_girar") ?: false
                val uJuego = snapshot.getString("ultimo_juego") ?: ""
                val llamadaActivaCloud = snapshot.getBoolean("en_llamada") ?: false

                segundosRestantes = sRestantes
                pesosGanadosModelo = pModelo
                bonoDobleSegundos = bono
                autorizarModeloGirar = autoGirar

                val mActiva = snapshot.getBoolean("miniserie_activa") ?: false
                val mTitulo = snapshot.getString("miniserie_titulo") ?: ""
                val mSinopsis = snapshot.getString("miniserie_sinopsis") ?: ""
                val mAmbiente = snapshot.getString("miniserie_ambiente") ?: ""
                val mMood = snapshot.getString("miniserie_mood") ?: ""
                val mRolANombre = snapshot.getString("miniserie_rol_a_nombre") ?: ""
                val mRolADesc = snapshot.getString("miniserie_rol_a_desc") ?: ""
                val mRolAObj = snapshot.getString("miniserie_rol_a_objetivo") ?: ""
                val mRolAFrase = snapshot.getString("miniserie_rol_a_frase") ?: ""
                val mRolBNombre = snapshot.getString("miniserie_rol_b_nombre") ?: ""
                val mRolBDesc = snapshot.getString("miniserie_rol_b_desc") ?: ""
                val mRolBObj = snapshot.getString("miniserie_rol_b_objetivo") ?: ""
                val mRolBFrase = snapshot.getString("miniserie_rol_b_frase") ?: ""
                val mGiro = snapshot.getString("miniserie_giro_actual") ?: ""
                val mInvertidos = snapshot.getBoolean("miniserie_roles_invertidos") ?: false

                val mActoActual = (snapshot.getLong("miniserie_acto_actual") ?: 1L).toInt()
                val mTotalActos = (snapshot.getLong("miniserie_total_actos") ?: 4L).toInt()
                val mTurno = (snapshot.getLong("miniserie_turno_dialogo") ?: 0L).toInt()
                val rawActos = snapshot.get("miniserie_actos") as? List<*>
                val parsedActos = if (rawActos != null && rawActos.isNotEmpty()) {
                    rawActos.mapNotNull { item ->
                        (item as? Map<*, *>)?.let { mapItem ->
                            val castMap = mapItem.entries.mapNotNull { (k, v) ->
                                (k as? String)?.let { strKey -> strKey to v }
                            }.toMap()
                            actoEpisodioFromMap(castMap)
                        }
                    }
                } else {
                    val esc = CatalogoMiniserie.obtenerPorId(snapshot.getString("miniserie_id") ?: "")
                        ?: CatalogoMiniserie.escenas.find { it.titulo == mTitulo }
                    esc?.obtenerActosCompletos() ?: emptyList()
                }

                val actoHaCambiado = mActiva && (mActoActual != miniserieActoActual)
                if (mActiva && enLlamada && actoHaCambiado) {
                    Toast.makeText(context, "🎬 ¡El Director avanzó al Acto $mActoActual!", Toast.LENGTH_SHORT).show()
                    triggerHapticFeedback(context)
                }

                miniserieActiva = mActiva
                miniserieTitulo = mTitulo
                miniserieSinopsis = mSinopsis
                miniserieAmbiente = mAmbiente
                miniserieMood = mMood
                miniserieRolANombre = mRolANombre
                miniserieRolADesc = mRolADesc
                miniserieRolAObjetivo = mRolAObj
                miniserieRolAFrase = mRolAFrase
                miniserieRolBNombre = mRolBNombre
                miniserieRolBDesc = mRolBDesc
                miniserieRolBObjetivo = mRolBObj
                miniserieRolBFrase = mRolBFrase
                miniserieGiroActual = mGiro
                miniserieRolesInvertidos = mInvertidos
                miniserieActoActual = mActoActual
                miniserieTotalActos = mTotalActos
                miniserieTurnoDialogo = if (actoHaCambiado) 0 else mTurno
                if (parsedActos.isNotEmpty()) {
                    miniserieActosLista = parsedActos
                }

                if (mGiro.isNotBlank() && mGiro != ultimoGiroVisto) {
                    ultimoGiroVisto = mGiro
                    mostrarBannerGiroDramatico = true
                    triggerHapticFeedback(context)
                }

                if (uJuego.isNotBlank()) {
                    ultimoJuegoResultado = uJuego
                }

                if (llamadaActivaCloud && sRestantes <= 0 && enLlamada && rolSeleccionado == RolApp.PARTICIPANTE) {
                    enLlamada = false
                    alertaTiempoAgotado = true
                }

                if (rolSeleccionado == RolApp.PARTICIPANTE && enLlamada && webrtcCallState == WebRtcCallState.CONNECTED && !llamadaActivaCloud) {
                    enLlamada = false
                    Toast.makeText(context, "La modelo finalizó la transmisión", Toast.LENGTH_SHORT).show()
                }
            }

        onDispose {
            listenerReg.remove()
            sessionManager.dispose()
        }
    }

    // Publicación de la sala en la nube cuando se es modelo
    LaunchedEffect(rolSeleccionado, salaId, enLlamada, webrtcCallState) {
        if (rolSeleccionado == RolApp.MODELO) {
            val estaEnLlamadaConGamer = enLlamada && webrtcCallState == WebRtcCallState.CONNECTED
            val datos = hashMapOf(
                "modelo_nombre" to "Modelo VIP",
                "tarifa_cliente_segundo" to 1.0,
                "tarifa_base_modelo" to 0.50,
                "disponible" to (enLlamada && !estaEnLlamadaConGamer),
                "en_llamada" to estaEnLlamadaConGamer,
                "modelo_en_linea" to true,
                "modelo_transmitiendo" to enLlamada,
                "rol" to "MODELO",
                "actualizado" to FieldValue.serverTimestamp()
            )
            firestore.collection("salas_videollamada").document(salaId)
                .set(datos, SetOptions.merge())
        }
    }

    // Función ejecutora del giro del tragamonedas (Gamer o Modelo) con animación de carretes
    fun ejecutarGiro(apuesta: Int) {
        if (segundosRestantes < apuesta && rolSeleccionado == RolApp.PARTICIPANTE) {
            alertaTiempoAgotado = true
            return
        }

        girandoCarretes = true
        coroutineScope.launch {
            val simbolos = listOf("🍒", "7️⃣", "💎", "🍋", "🔔")
            repeat(4) {
                carrete1 = simbolos.random()
                carrete2 = simbolos.random()
                carrete3 = simbolos.random()
                delay(60)
            }

            val r1 = simbolos[Random.nextInt(simbolos.size)]
            val r2 = simbolos[Random.nextInt(simbolos.size)]
            val r3 = if (Random.nextFloat() < 0.45f) r1 else simbolos[Random.nextInt(simbolos.size)]

            carrete1 = r1
            carrete2 = r2
            carrete3 = r3
            girandoCarretes = false

            val gananciaSegundos: Int
            val bonoPesosModelo: Double
            val texto: String

            if (r1 == r2 && r2 == r3) {
                // TRIPLETE JACKPOT: Gamer gana x5 segundos, Modelo gana $50.00 MXN bono + 30s de tarifa doble ($1.00/s)
                gananciaSegundos = apuesta * 5
                bonoPesosModelo = 50.00
                texto = "🎉 ¡JACKPOT TRIPLETE! +$gananciaSegundos s para Gamer & +$50 MXN (Bono x2) para Modelo"
            } else if (r1 == r2 || r2 == r3 || r1 == r3) {
                // PAR GANADOR: Gamer gana x2 segundos, Modelo gana $15.00 MXN bono
                gananciaSegundos = apuesta * 2
                bonoPesosModelo = 15.00
                texto = "⭐ ¡PAR GANADOR! +$gananciaSegundos s para Gamer & +$15 MXN para Modelo"
            } else {
                gananciaSegundos = 0
                bonoPesosModelo = 0.00
                texto = "😢 Sigue intentando (-$apuesta s)"
            }

            val netoSegundos = gananciaSegundos - apuesta
            val bonoSegundosDoble = if (r1 == r2 && r2 == r3) 30L else 0L

            firestore.collection("salas_videollamada").document(salaId)
                .update(
                    "segundos_participante", FieldValue.increment(netoSegundos.toLong()),
                    "pesos_modelo", FieldValue.increment(bonoPesosModelo),
                    "bono_doble_segundos", FieldValue.increment(bonoSegundosDoble),
                    "ultimo_juego", "$r1 | $r2 | $r3 -> $texto"
                )
            triggerHapticFeedback(context)
        }
    }

    // Funciones del modo "Artistas Fugaces: Miniserie de Romance"
    fun activarEscenaMiniserie(escena: EscenaMiniserie, invertidos: Boolean = miniserieRolesInvertidos) {
        val actosCompletos = escena.obtenerActosCompletos()
        // Actualización optimista local inmediata para evitar parpadeos
        miniserieActiva = true
        miniserieTitulo = escena.titulo
        miniserieSinopsis = escena.sinopsis
        miniserieAmbiente = escena.ambiente
        miniserieMood = escena.mood
        miniserieRolANombre = escena.rolGamer.nombre
        miniserieRolADesc = escena.rolGamer.descripcion
        miniserieRolAObjetivo = escena.rolGamer.objetivoSecreto
        miniserieRolAFrase = escena.rolGamer.fraseApertura
        miniserieRolBNombre = escena.rolModelo.nombre
        miniserieRolBDesc = escena.rolModelo.descripcion
        miniserieRolBObjetivo = escena.rolModelo.objetivoSecreto
        miniserieRolBFrase = escena.rolModelo.fraseApertura
        miniserieGiroActual = ""
        miniserieRolesInvertidos = invertidos
        miniserieActoActual = 1
        miniserieTotalActos = actosCompletos.size.coerceAtLeast(1)
        miniserieTurnoDialogo = 0
        miniserieActosLista = actosCompletos

        coroutineScope.launch {
            try {
                val updates = mapOf(
                    "miniserie_activa" to true,
                    "miniserie_id" to escena.id,
                    "miniserie_titulo" to escena.titulo,
                    "miniserie_sinopsis" to escena.sinopsis,
                    "miniserie_ambiente" to escena.ambiente,
                    "miniserie_mood" to escena.mood,
                    "miniserie_rol_a_nombre" to escena.rolGamer.nombre,
                    "miniserie_rol_a_desc" to escena.rolGamer.descripcion,
                    "miniserie_rol_a_objetivo" to escena.rolGamer.objetivoSecreto,
                    "miniserie_rol_a_frase" to escena.rolGamer.fraseApertura,
                    "miniserie_rol_b_nombre" to escena.rolModelo.nombre,
                    "miniserie_rol_b_desc" to escena.rolModelo.descripcion,
                    "miniserie_rol_b_objetivo" to escena.rolModelo.objetivoSecreto,
                    "miniserie_rol_b_frase" to escena.rolModelo.fraseApertura,
                    "miniserie_giro_actual" to "",
                    "miniserie_roles_invertidos" to invertidos,
                    "miniserie_acto_actual" to 1,
                    "miniserie_total_actos" to actosCompletos.size.coerceAtLeast(1),
                    "miniserie_turno_dialogo" to 0,
                    "miniserie_actos" to actosCompletos.map { it.toMap() },
                    "ultimo_juego" to "🎭 Miniserie: ${escena.titulo}"
                )
                firestore.collection("salas_videollamada").document(salaId)
                    .set(updates, SetOptions.merge())
                Toast.makeText(context, "🎭 ¡Capítulo activado en la sala!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cambiarActoMiniserie(nuevoActo: Int) {
        val clamped = nuevoActo.coerceIn(1, miniserieTotalActos.coerceAtLeast(1))
        miniserieActoActual = clamped
        miniserieTurnoDialogo = 0
        triggerHapticFeedback(context)
        coroutineScope.launch {
            try {
                firestore.collection("salas_videollamada").document(salaId)
                    .set(
                        mapOf(
                            "miniserie_acto_actual" to clamped,
                            "miniserie_turno_dialogo" to 0,
                            "ultimo_juego" to "🎬 Acto $clamped de Miniserie"
                        ),
                        SetOptions.merge()
                    )
                Toast.makeText(context, "🎬 ¡Avanzado al Acto $clamped!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun avanzarTurnoDialogo(nuevoTurno: Int) {
        miniserieTurnoDialogo = nuevoTurno
        coroutineScope.launch {
            try {
                firestore.collection("salas_videollamada").document(salaId)
                    .set(
                        mapOf("miniserie_turno_dialogo" to nuevoTurno),
                        SetOptions.merge()
                    )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun detonarGiroMiniserie(giro: String) {
        miniserieGiroActual = giro
        ultimoGiroVisto = giro
        mostrarBannerGiroDramatico = true
        triggerHapticFeedback(context)

        coroutineScope.launch {
            try {
                val updates = mutableMapOf<String, Any>(
                    "miniserie_giro_actual" to giro,
                    "ultimo_juego" to "⚡ Giro Dramático: $giro"
                )
                // Si la miniserie no estaba activa, activamos la escena correspondiente al giro
                if (!miniserieActiva) {
                    val escenaDefecto = CatalogoMiniserie.escenas.find { it.girosDeGuion.contains(giro) }
                        ?: CatalogoMiniserie.escenas.first()
                    miniserieActiva = true
                    miniserieTitulo = escenaDefecto.titulo
                    miniserieSinopsis = escenaDefecto.sinopsis
                    miniserieAmbiente = escenaDefecto.ambiente
                    miniserieMood = escenaDefecto.mood
                    miniserieRolANombre = escenaDefecto.rolGamer.nombre
                    miniserieRolADesc = escenaDefecto.rolGamer.descripcion
                    miniserieRolAObjetivo = escenaDefecto.rolGamer.objetivoSecreto
                    miniserieRolAFrase = escenaDefecto.rolGamer.fraseApertura
                    miniserieRolBNombre = escenaDefecto.rolModelo.nombre
                    miniserieRolBDesc = escenaDefecto.rolModelo.descripcion
                    miniserieRolBObjetivo = escenaDefecto.rolModelo.objetivoSecreto
                    miniserieRolBFrase = escenaDefecto.rolModelo.fraseApertura

                    updates["miniserie_activa"] = true
                    updates["miniserie_id"] = escenaDefecto.id
                    updates["miniserie_titulo"] = escenaDefecto.titulo
                    updates["miniserie_sinopsis"] = escenaDefecto.sinopsis
                    updates["miniserie_ambiente"] = escenaDefecto.ambiente
                    updates["miniserie_mood"] = escenaDefecto.mood
                    updates["miniserie_rol_a_nombre"] = escenaDefecto.rolGamer.nombre
                    updates["miniserie_rol_a_desc"] = escenaDefecto.rolGamer.descripcion
                    updates["miniserie_rol_a_objetivo"] = escenaDefecto.rolGamer.objetivoSecreto
                    updates["miniserie_rol_a_frase"] = escenaDefecto.rolGamer.fraseApertura
                    updates["miniserie_rol_b_nombre"] = escenaDefecto.rolModelo.nombre
                    updates["miniserie_rol_b_desc"] = escenaDefecto.rolModelo.descripcion
                    updates["miniserie_rol_b_objetivo"] = escenaDefecto.rolModelo.objetivoSecreto
                    updates["miniserie_rol_b_frase"] = escenaDefecto.rolModelo.fraseApertura
                    updates["miniserie_roles_invertidos"] = miniserieRolesInvertidos
                }

                firestore.collection("salas_videollamada").document(salaId)
                    .set(updates, SetOptions.merge())
                Toast.makeText(context, "⚡ ¡Giro dramático enviado a ambos!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun desactivarModoMiniserie() {
        miniserieActiva = false
        miniserieGiroActual = ""
        mostrarBannerGiroDramatico = false
        coroutineScope.launch {
            try {
                val updates = mapOf(
                    "miniserie_activa" to false,
                    "miniserie_giro_actual" to "",
                    "ultimo_juego" to "Modo Miniserie pausado"
                )
                firestore.collection("salas_videollamada").document(salaId)
                    .set(updates, SetOptions.merge())
                Toast.makeText(context, "🎭 Miniserie desactivada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun intercambiarRolesMiniserie() {
        val nuevoInvertidos = !miniserieRolesInvertidos
        miniserieRolesInvertidos = nuevoInvertidos
        coroutineScope.launch {
            try {
                firestore.collection("salas_videollamada").document(salaId)
                    .update("miniserie_roles_invertidos", nuevoInvertidos)
                val msg = if (nuevoInvertidos) "🔄 ¡Roles intercambiados!" else "🔄 ¡Roles originales restaurados!"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun detonarGiroAleatorio() {
        val escena = CatalogoMiniserie.escenas.find { it.titulo == miniserieTitulo }
            ?: CatalogoMiniserie.escenaAleatoria()
        val giro = escena.girosDeGuion.random()
        detonarGiroMiniserie(giro)
    }

    // Auto-Spin Automático (Gira cada 8s sin tocar botones)
    LaunchedEffect(autoSpinActivo, enLlamada, segundosRestantes) {
        if (autoSpinActivo && enLlamada) {
            while (isActive && autoSpinActivo && enLlamada && segundosRestantes >= apuestaActual) {
                delay(8000)
                if (enLlamada && segundosRestantes >= apuestaActual) {
                    ejecutarGiro(apuestaActual)
                }
            }
        }
    }

    // Contador regresivo y liquidación de $0.50/s (o $1.00/s si hay Bono Doble)
    LaunchedEffect(enLlamada, rolSeleccionado) {
        if (enLlamada && rolSeleccionado == RolApp.PARTICIPANTE) {
            firestore.collection("salas_videollamada").document(salaId)
                .update("en_llamada", true)

            while (isActive && enLlamada && segundosRestantes > 0) {
                delay(1000)
                val tieneBono = bonoDobleSegundos > 0
                val pagoSegundo = if (tieneBono) 1.00 else 0.50

                firestore.collection("salas_videollamada").document(salaId)
                    .update(
                        "segundos_participante", FieldValue.increment(-1),
                        "pesos_modelo", FieldValue.increment(pagoSegundo),
                        "bono_doble_segundos", FieldValue.increment(if (tieneBono) -1 else 0)
                    )
            }

            if (segundosRestantes <= 0) {
                enLlamada = false
                alertaTiempoAgotado = true
                firestore.collection("salas_videollamada").document(salaId)
                    .update("en_llamada", false)
            }
        }
    }

    // Animación parpadeante para alerta <= 15s o Bono Doble
    val transition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF07090E)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. CAPA DE VIDEO A PANTALLA COMPLETA TOTALMENTE AMPLIFICADA (Fondo 100%)
            val mainVideoTrack = if (rolSeleccionado == RolApp.MODELO) {
                if (enLlamada) (remoteVideoTrack ?: localVideoTrack) else localVideoTrack
            } else {
                if (enLlamada) remoteVideoTrack else localVideoTrack
            }

            if (mainVideoTrack != null) {
                WebRtcVideoView(
                    videoTrack = mainVideoTrack,
                    eglContext = sessionManager.getEglBaseContext(),
                    mirror = (mainVideoTrack == localVideoTrack && camaraFrontal),
                    modifier = Modifier.fillMaxSize()
                )
            } else if (enLlamada && rolSeleccionado == RolApp.PARTICIPANTE) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF07090E)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF38BDF8), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "🌐 Conectando con Modelo VIP...",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🔒 Enlace Cifrado WebRTC de Alta Definición",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                // Fondo mientras inicializa la cámara
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF07090E)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFEC4899), modifier = Modifier.size(40.dp))
                }
            }

            // 2. PiP FLOTANTE CUANDO HAY DOS PARTICIPANTES EN TRANSMISIÓN
            if (enLlamada && mostrarPiP) {
                val pipTrack = if (rolSeleccionado == RolApp.MODELO && remoteVideoTrack == null) {
                    null // La modelo se ve a sí misma en pantalla completa mientras espera
                } else {
                    localVideoTrack
                }

                if (pipTrack != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 90.dp)
                            .size(width = 110.dp, height = 155.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                            .border(2.dp, if (rolSeleccionado == RolApp.MODELO) Color(0xFFEC4899) else Color(0xFF38BDF8), RoundedCornerShape(16.dp))
                    ) {
                        WebRtcVideoView(
                            videoTrack = pipTrack,
                            eglContext = sessionManager.getEglBaseContext(),
                            mirror = camaraFrontal,
                            overlay = true,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "Tú",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 3. OVERLAY DRAMÁTICO AL LLEGAR UN GIRO DE GUIÓN
            if (mostrarBannerGiroDramatico && miniserieGiroActual.isNotBlank() && enLlamada) {
                OverlayGiroDramatico(
                    giro = miniserieGiroActual,
                    onDismiss = { mostrarBannerGiroDramatico = false }
                )
            }

            // 4. HUD MENU EN "U" INVERTIDA (Inspirado en Comfer)
            HudMenuUInvertida(
                rol = rolSeleccionado,
                enLlamada = enLlamada,
                webrtcCallState = webrtcCallState,
                salaId = salaId,
                segundosRestantes = segundosRestantes,
                pesosGanadosModelo = pesosGanadosModelo,
                bonoDobleSegundos = bonoDobleSegundos,
                carretes = Triple(carrete1, carrete2, carrete3),
                girandoCarretes = girandoCarretes,
                apuestaActual = apuestaActual,
                puedeGirar = if (rolSeleccionado == RolApp.MODELO) autorizarModeloGirar else true,
                onGirarSlot = { ejecutarGiro(apuestaActual) },
                onOpenSlotConfig = { mostrarMinijuegoModal = true },
                onToggleCamera = {
                    sessionManager.switchCamera { isFront -> camaraFrontal = isFront }
                },
                onTogglePiP = { mostrarPiP = !mostrarPiP },
                mostrarPiP = mostrarPiP,
                onOpenMiniserie = { mostrarMiniserieModal = true },
                miniserieActiva = miniserieActiva,
                miniserieTitulo = miniserieTitulo,
                miniserieActoActual = miniserieActoActual,
                miniserieTotalActos = miniserieTotalActos,
                miniserieFraseActual = if (miniserieRolesInvertidos) {
                    if (rolSeleccionado == RolApp.MODELO) miniserieRolAFrase else miniserieRolBFrase
                } else {
                    if (rolSeleccionado == RolApp.MODELO) miniserieRolBFrase else miniserieRolAFrase
                },
                onOpenGeminiAI = { mostrarDirectorSuenoAI = true },
                onOpenWallet = { mostrarBilleteraSolidaria = true },
                onOpenMercadoPago = { mostrarMercadoPagoModal = true },
                onColgar = {
                    enLlamada = false
                    sessionManager.closeCall()
                    val updates = if (rolSeleccionado == RolApp.MODELO) {
                        mapOf("en_llamada" to false, "disponible" to false, "modelo_transmitiendo" to false)
                    } else {
                        mapOf("en_llamada" to false)
                    }
                    firestore.collection("salas_videollamada").document(salaId)
                        .update(updates)
                },
                onSaltarSiguiente = if (rolSeleccionado == RolApp.PARTICIPANTE) {
                    { saltarSiguiente() }
                } else null,
                onIniciarTransmisionLobby = {
                    enLlamada = true
                },
                onConectarAleatorioLobby = {
                    conectarAleatorio()
                },
                onEntrarSalaDirectoLobby = {
                    if (segundosRestantes <= 0) {
                        alertaTiempoAgotado = true
                    } else {
                        salaId = "sala_vip_1"
                        signalingClient.salaId = "sala_vip_1"
                        enLlamada = true
                    }
                },
                buscandoSalaAleatoria = buscandoSalaAleatoria
            )

            // MODAL COMPACTO DE MINIJUEGO TRAGAMONEDAS CON AUTO-SPIN Y GIRO COMPARTIDO
            if (mostrarMinijuegoModal) {
                ModalTragamonedasAvanzado(
                    saldoSegundos = segundosRestantes,
                    apuestaActual = apuestaActual,
                    onCambiarApuesta = { apuestaActual = it },
                    autoSpinActivo = autoSpinActivo,
                    onToggleAutoSpin = { autoSpinActivo = it },
                    autorizarModelo = autorizarModeloGirar,
                    onToggleAutorizarModelo = { nuevoValor ->
                        autorizarModeloGirar = nuevoValor
                        firestore.collection("salas_videollamada").document(salaId)
                            .update("permiso_modelo_girar", nuevoValor)
                    },
                    onGirar = { apuesta -> ejecutarGiro(apuesta) },
                    onDismiss = { mostrarMinijuegoModal = false }
                )
            }

            // MODAL DE RECARGA CON GOOGLE PAY Y MERCADO PAGO
            if (mostrarMercadoPagoModal) {
                ModalMercadoPagoRecarga(
                    saldoSegundos = segundosRestantes,
                    salaId = salaId,
                    onRecargaExitosa = { segundosAgregados, precioMxn, yaAcreditadoEnFirestore ->
                        segundosRestantes += segundosAgregados
                        alertaTiempoAgotado = false
                        if (!yaAcreditadoEnFirestore) {
                            coroutineScope.launch {
                                try {
                                    firestore.collection("salas_videollamada").document(salaId)
                                        .update(
                                            "segundos_participante", FieldValue.increment(segundosAgregados.toLong()),
                                            "ultimo_juego", "💳 ¡Recarga VIP: +${segundosAgregados}s ($$precioMxn MXN)!"
                                        )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                    onDismiss = { mostrarMercadoPagoModal = false }
                )
            }

            // MODAL INTERACTIVO DE MINISERIE ROMÁNTICA: ARTISTAS FUGACES
            if (mostrarMiniserieModal) {
                ModalMiniserieRomance(
                    miniserieActiva = miniserieActiva,
                    miniserieTitulo = miniserieTitulo,
                    miniserieSinopsis = miniserieSinopsis,
                    miniserieAmbiente = miniserieAmbiente,
                    miniserieMood = miniserieMood,
                    miniserieRolANombre = miniserieRolANombre,
                    miniserieRolADesc = miniserieRolADesc,
                    miniserieRolAObjetivo = miniserieRolAObjetivo,
                    miniserieRolAFrase = miniserieRolAFrase,
                    miniserieRolBNombre = miniserieRolBNombre,
                    miniserieRolBDesc = miniserieRolBDesc,
                    miniserieRolBObjetivo = miniserieRolBObjetivo,
                    miniserieRolBFrase = miniserieRolBFrase,
                    miniserieGiroActual = miniserieGiroActual,
                    rolUsuario = rolSeleccionado,
                    rolesInvertidos = miniserieRolesInvertidos,
                    actoActual = miniserieActoActual,
                    totalActos = miniserieTotalActos,
                    turnoDialogoActual = miniserieTurnoDialogo,
                    actos = miniserieActosLista,
                    onCambiarActo = { act -> cambiarActoMiniserie(act) },
                    onAvanzarTurnoDialogo = { turno -> avanzarTurnoDialogo(turno) },
                    onIntercambiarRoles = { intercambiarRolesMiniserie() },
                    onActivarEscena = { escena -> activarEscenaMiniserie(escena) },
                    onDetonarGiro = { giro -> detonarGiroMiniserie(giro) },
                    onDesactivar = { desactivarModoMiniserie() },
                    onAbrirDirectorAI = { mostrarDirectorSuenoAI = true },
                    onDismiss = { mostrarMiniserieModal = false }
                )
            }

            // MODAL DE BILLETERA DE GANANCIAS & RETIRO A CUENTA BANCARIA
            if (mostrarBilleteraSolidaria) {
                ModalBilleteraGanancias(
                    saldoGanadoMxn = pesosGanadosModelo,
                    onRetirarSaldo = { monto, clabe ->
                        coroutineScope.launch {
                            try {
                                firestore.collection("salas_videollamada").document(salaId).update(
                                    "pesos_modelo", FieldValue.increment(-monto),
                                    "ultimo_juego", "💳 Retiro solicitado: $${String.format("%.2f", monto)} MXN a cuenta ***${clabe.takeLast(4)}"
                                )
                                pesosGanadosModelo = (pesosGanadosModelo - monto).coerceAtLeast(0.0)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    onDismiss = { mostrarBilleteraSolidaria = false }
                )
            }

            // MODAL DE VERIFICACIÓN OBLIGATORIA +18 PARA LA APP DE MODELOS
            if (esAppModelo && !edadVerificada18) {
                ModalVerificacionEdadModelo(
                    onVerificacionExitosa = { aliasCreadora, anioNac ->
                        prefs.edit()
                            .putBoolean("edad_verificada_18", true)
                            .putString("alias_creadora", aliasCreadora)
                            .putInt("anio_nacimiento", anioNac)
                            .apply()
                        edadVerificada18 = true
                        coroutineScope.launch {
                            try {
                                firestore.collection("modelos_verificadas")
                                    .document(aliasCreadora)
                                    .set(
                                        mapOf(
                                            "alias" to aliasCreadora,
                                            "anio_nacimiento" to anioNac,
                                            "verificada_18" to true,
                                            "fecha_registro" to FieldValue.serverTimestamp()
                                        )
                                    )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                )
            }

            // MODAL DEL DIRECTOR DEL SUEÑO (I.A. GEMINI)
            if (mostrarDirectorSuenoAI) {
                ModalDirectorDelSuenoAI(
                    onGuionGenerado = { escena ->
                        activarEscenaMiniserie(escena)
                        mostrarDirectorSuenoAI = false
                        mostrarMiniserieModal = true
                    },
                    onDismiss = { mostrarDirectorSuenoAI = false }
                )
            }

            // Alerta de Tiempo Agotado
            if (alertaTiempoAgotado) {
                AlertDialog(
                    onDismissRequest = { alertaTiempoAgotado = false },
                    containerColor = Color(0xFF0F172A),
                    title = {
                        Text("⏱️ ¡Tiempo de Llamada Agotado!", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text(
                            "Tus segundos de videollamada se han consumido por completo. Recarga tu saldo al instante con Mercado Pago para continuar disfrutando de la sala VIP sin cortes.",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                alertaTiempoAgotado = false
                                mostrarMercadoPagoModal = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009EE3)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("💳 Recargar con Mercado Pago", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = {
                                alertaTiempoAgotado = false
                                firestore.collection("salas_videollamada").document(salaId)
                                    .update("segundos_participante", 120L)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Demo +120s", color = Color.Gray)
                        }
                    }
                )
            }

            // Diálogo amigable de actualización opcional (Pregunta con respeto y nunca bloquea)
            if (mostrarDialogoActualizacion) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoActualizacion = false },
                    title = {
                        Text("✨ Actualización opcional", fontWeight = FontWeight.Bold, color = Color.White)
                    },
                    text = {
                        Text(
                            text = "Hay una nueva versión de Velvet VIP disponible ($versionNubeInfo).\n\n¿Deseas descargarla ahora o prefieres continuar usando la app?",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                mostrarDialogoActualizacion = false
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://local-casino-hub-970df.web.app")
                                )
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📥 Descargar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { mostrarDialogoActualizacion = false },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Continuar usando la app", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF1E293B)
                )
            }
        }
    }
}

/**
 * Modelos y Pantalla de Recarga con Pasarela Mercado Pago
 */
data class PaqueteRecarga(
    val id: String,
    val nombre: String,
    val precioMxn: Int,
    val segundosBase: Int,
    val segundosBono: Int,
    val badge: String? = null,
    val icon: String = "💎",
    val descripcion: String
) {
    val totalSegundos: Int get() = segundosBase + segundosBono
}

enum class MetodoPago(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val icono: String,
    val etiqueta: String
) {
    MERCADO_PAGO(
        "mp_checkout",
        "Mercado Pago Checkout Oficial",
        "Paga con saldo MP, Tarjetas, SPEI u OXXO",
        "🤝",
        "Oficial México"
    ),
    CRYPTO(
        "crypto_usdt",
        "Criptomonedas USDT VIP",
        "Tron (TRC-20), Polygon y BSC (Sin bloqueos)",
        "🪙",
        "Global / +18"
    ),
    TARJETA(
        "tarjeta",
        "Tarjeta Débito / Crédito",
        "Visa, Mastercard, AMEX (Hasta 3 MSI)",
        "💳",
        "Sin comisión"
    ),
    SPEI(
        "spei",
        "Transferencia SPEI instantánea",
        "Depósito directo 24/7 vía CLABE interbancaria STP",
        "🏦",
        "Sin comisión"
    ),
    OXXO(
        "oxxo",
        "Depósito en OXXO Pay",
        "Código de barras digital para pagar en caja",
        "🏪",
        "Efectivo"
    ),
    QR(
        "qr",
        "Código QR Mercado Pago",
        "Escanea y paga directo con tu app Mercado Pago",
        "📱",
        "QR Dinámico"
    )
}

/**
 * Modal Híbrido de Recarga en Jetpack Compose:
 * - Primer Plano: Google Pay API v2 Nativo con com.google.pay.button.PayButton
 * - Escritura Atómica en Cloud Firestore (transacciones + incremento de saldo)
 * - Sección colapsable inferior con métodos locales de México (SPEI STP, OXXO Pay, QR Mercado Pago)
 */
@Composable
fun ModalMercadoPagoRecarga(
    saldoSegundos: Long,
    salaId: String = "sala_vip_1",
    onRecargaExitosa: (segundos: Int, precioMxn: Int, yaAcreditadoEnFirestore: Boolean) -> Unit,
    onDismiss: () -> Unit,
    googlePayViewModel: GooglePayViewModel = viewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Catálogo oficial de paquetes VIP en MXN
    val paquetes = remember { GooglePayHelper.PAQUETES_VIP }
    val metodosPagoLocales = remember {
        listOf(
            MetodoPago.MERCADO_PAGO,
            MetodoPago.CRYPTO,
            MetodoPago.TARJETA,
            MetodoPago.SPEI,
            MetodoPago.OXXO,
            MetodoPago.QR
        )
    }

    val paqueteSeleccionado by googlePayViewModel.paqueteSeleccionado.collectAsState()
    var metodoLocalSeleccionado by remember { mutableStateOf(metodosPagoLocales[0]) }
    var redCriptoSeleccionada by remember { mutableStateOf(RedCripto.TRC20) }
    var txHashCripto by remember { mutableStateOf("") }
    var cargandoCheckoutMp by remember { mutableStateOf(false) }
    var mostrarMetodosLocales by remember { mutableStateOf(false) }
    var pasoActual by remember { mutableStateOf("SELECCION") } // SELECCION, DETALLE_LOCAL, PROCESANDO_GPAY, PROCESANDO_LOCAL, EXITO_GPAY, EXITO_LOCAL
    var mesesSinIntereses by remember { mutableIntStateOf(1) }
    var referenciaPagoLocal by remember { mutableStateOf("MP-" + Random.nextInt(10000000, 99999999)) }

    // Google Pay Client & ViewModel reactivo
    val isGooglePayReady by googlePayViewModel.isReadyToPay.collectAsState()
    val paymentsClient = remember {
        GooglePayHelper.crearPaymentsClient(context, WalletConstants.ENVIRONMENT_TEST)
    }

    LaunchedEffect(Unit) {
        googlePayViewModel.verificarDisponibilidad(paymentsClient)
    }

    var transaccionGPayExito by remember { mutableStateOf<GooglePayPaymentResult?>(null) }
    var transaccionIdGPay by remember { mutableStateOf("") }

    // Procesamiento y acreditación atómica en Firestore tras éxito de Google Pay
    fun procesarPagoExitosoGooglePay(paymentData: PaymentData) {
        coroutineScope.launch {
            try {
                val resultado = GooglePayHelper.parsearPaymentData(paymentData)
                val uidEfectivo = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                ) ?: "gamer_vip_mexico"

                val resFirestore = GooglePayHelper.acreditarRecargaGooglePay(
                    firestore = FirebaseFirestore.getInstance(),
                    uid = uidEfectivo,
                    salaId = salaId,
                    resultado = resultado,
                    montoMxn = paqueteSeleccionado.precioMxn,
                    segundosAcreditados = paqueteSeleccionado.segundos
                )

                resFirestore.onSuccess { txnId ->
                    transaccionIdGPay = txnId
                    transaccionGPayExito = resultado
                    pasoActual = "EXITO_GPAY"
                    googlePayViewModel.notificarExito(txnId, paqueteSeleccionado, resultado)
                    onRecargaExitosa(paqueteSeleccionado.segundos, paqueteSeleccionado.precioEntero, true)
                }.onFailure { err ->
                    pasoActual = "SELECCION"
                    googlePayViewModel.notificarError(err.message ?: "Error al registrar en Firestore")
                    Toast.makeText(context, "Error al registrar en Firestore: ${err.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                pasoActual = "SELECCION"
                googlePayViewModel.notificarError(e.message ?: "Error procesando respuesta Google Pay")
                Toast.makeText(context, "Error procesando respuesta Google Pay: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Launcher con ActivityResultContracts.StartIntentSenderForResult
    val paymentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val intent = result.data
                val paymentData = intent?.let { PaymentData.getFromIntent(it) }
                if (paymentData != null) {
                    procesarPagoExitosoGooglePay(paymentData)
                } else {
                    pasoActual = "SELECCION"
                    googlePayViewModel.notificarError("No se recibieron datos de pago")
                    Toast.makeText(context, "Error: Datos de pago no encontrados", Toast.LENGTH_SHORT).show()
                }
            }
            Activity.RESULT_CANCELED -> {
                pasoActual = "SELECCION"
                googlePayViewModel.reiniciarEstado()
                Toast.makeText(context, "Operación cancelada", Toast.LENGTH_SHORT).show()
            }
            AutoResolveHelper.RESULT_ERROR -> {
                pasoActual = "SELECCION"
                val status = AutoResolveHelper.getStatusFromIntent(result.data)
                val msg = status?.statusMessage ?: "Error al procesar con Google Pay"
                googlePayViewModel.notificarError(msg)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
            else -> {
                pasoActual = "SELECCION"
                googlePayViewModel.reiniciarEstado()
                Toast.makeText(context, "Operación no completada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Callback de respaldo para AutoResolveHelper si se delega vía onActivityResult
    DisposableEffect(Unit) {
        MainActivity.googlePayResultCallback = { resultCode, intent ->
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val paymentData = intent?.let { PaymentData.getFromIntent(it) }
                    if (paymentData != null) {
                        procesarPagoExitosoGooglePay(paymentData)
                    } else {
                        pasoActual = "SELECCION"
                        googlePayViewModel.notificarError("No se recibieron datos de pago en callback")
                    }
                }
                Activity.RESULT_CANCELED -> {
                    pasoActual = "SELECCION"
                    googlePayViewModel.reiniciarEstado()
                }
                AutoResolveHelper.RESULT_ERROR -> {
                    pasoActual = "SELECCION"
                    val status = AutoResolveHelper.getStatusFromIntent(intent)
                    val msg = status?.statusMessage ?: "Error Google Pay"
                    googlePayViewModel.notificarError(msg)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
                else -> {
                    pasoActual = "SELECCION"
                    googlePayViewModel.reiniciarEstado()
                }
            }
        }
        onDispose {
            MainActivity.googlePayResultCallback = null
        }
    }

    // Lanzador oficial de PaymentDataRequest
    fun lanzarGooglePay() {
        pasoActual = "PROCESANDO_GPAY"
        googlePayViewModel.setProcesando()
        try {
            val requestJson = GooglePayHelper.obtenerPaymentDataRequestJson(
                precioMxn = paqueteSeleccionado.precioMxn,
                environment = WalletConstants.ENVIRONMENT_TEST
            )
            val request = PaymentDataRequest.fromJson(requestJson)
            val task = paymentsClient.loadPaymentData(request)
            task.addOnCompleteListener { completedTask ->
                if (completedTask.isSuccessful) {
                    completedTask.result?.let { paymentData ->
                        procesarPagoExitosoGooglePay(paymentData)
                    }
                } else {
                    val exception = completedTask.exception
                    if (exception is ResolvableApiException) {
                        try {
                            val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                            paymentLauncher.launch(intentSenderRequest)
                        } catch (e: Exception) {
                            val act = context as? Activity
                            if (act != null) {
                                AutoResolveHelper.resolveTask(task, act, GooglePayHelper.LOAD_PAYMENT_DATA_REQUEST_CODE)
                            } else {
                                pasoActual = "SELECCION"
                                googlePayViewModel.notificarError(e.message ?: "Error al lanzar Google Pay")
                                Toast.makeText(context, "Error al lanzar Google Pay: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        pasoActual = "SELECCION"
                        val errorMsg = exception?.localizedMessage ?: "No se pudo iniciar Google Pay"
                        googlePayViewModel.notificarError(errorMsg)
                        Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            pasoActual = "SELECCION"
            googlePayViewModel.notificarError(e.message ?: "Error al configurar Google Pay")
            Toast.makeText(context, "Error al configurar Google Pay: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (pasoActual == "DETALLE_LOCAL") {
                pasoActual = "SELECCION"
            } else if (pasoActual != "PROCESANDO_GPAY" && pasoActual != "PROCESANDO_LOCAL") {
                googlePayViewModel.reiniciarEstado()
                onDismiss()
            }
        },
        containerColor = Color(0xFF0B1426),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2563EB))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "GPay",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Bóveda VIP Gamer",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Google Pay & Pasarelas México",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💎 ${saldoSegundos}s",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
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
                when (pasoActual) {
                    "SELECCION" -> {
                        // 1. Selector de Paquetes VIP
                        Text(
                            text = "1. Elige tu paquete de recarga VIP:",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            paquetes.forEach { paquete ->
                                val estaSeleccionado = paqueteSeleccionado.id == paquete.id
                                val bordeColor = if (estaSeleccionado) {
                                    if (paquete.id == "velvet_vip") Color(0xFFF59E0B) else Color(0xFF38BDF8)
                                } else {
                                    Color(0xFF1E293B)
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { googlePayViewModel.seleccionarPaquete(paquete) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (estaSeleccionado) Color(0xFF1E293B) else Color(0xFF020617)
                                    ),
                                    border = BorderStroke(if (estaSeleccionado) 2.dp else 1.dp, bordeColor)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (paquete.badge != null) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        if (paquete.id == "velvet_vip") Color(0xFFF59E0B).copy(alpha = 0.25f)
                                                        else Color(0xFF10B981).copy(alpha = 0.25f)
                                                    )
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = paquete.badge,
                                                    color = if (paquete.id == "velvet_vip") Color(0xFFF59E0B) else Color(0xFF10B981),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        } else {
                                            Box(modifier = Modifier.height(14.dp))
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = paquete.icon, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = paquete.nombre.replace("Paquete ", ""),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "$${paquete.precioEntero} MXN",
                                            color = Color(0xFF38BDF8),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "+${paquete.segundos}s",
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. EN PRIMER PLANO: GOOGLE PAY NATIVO (com.google.pay.button.PayButton)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF2563EB))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "⚡", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Google Pay (Recomendado)",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isGooglePayReady) "Listo ✅" else "1-Tap ⚡",
                                            color = Color(0xFF10B981),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pago instantáneo con Visa, Mastercard o AMEX encriptado por Google Wallet.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Componente nativo oficial PayButton
                                PayButton(
                                    onClick = { lanzarGooglePay() },
                                    allowedPaymentMethods = GooglePayHelper.obtenerAllowedPaymentMethodsArrayJson(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    theme = ButtonTheme.Dark,
                                    type = ButtonType.Pay,
                                    radius = 24.dp
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "🔒 Google Pay API v2 Sandbox México • Sin comisiones adicionales",
                                    color = Color(0xFF64748B),
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3. SECCIÓN COLAPSABLE: OPCIONES LOCALES DE MÉXICO (SPEI / OXXO / MERCADO PAGO)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mostrarMetodosLocales = !mostrarMetodosLocales },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🌐", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Mercado Pago Oficial & Criptomonedas VIP",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Checkout Pro, USDT (TRC-20 / Polygon), SPEI y OXXO",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                                Text(
                                    text = if (mostrarMetodosLocales) "▲" else "▼",
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (mostrarMetodosLocales) {
                            Spacer(modifier = Modifier.height(8.dp))
                            metodosPagoLocales.forEach { metodo ->
                                val estaSeleccionado = metodoLocalSeleccionado == metodo
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clickable { metodoLocalSeleccionado = metodo },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (estaSeleccionado) Color(0xFF1E293B) else Color(0xFF020617)
                                    ),
                                    border = BorderStroke(
                                        if (estaSeleccionado) 1.5.dp else 1.dp,
                                        if (estaSeleccionado) Color(0xFF009EE3) else Color(0xFF1E293B)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (estaSeleccionado) Color(0xFF009EE3) else Color.Transparent
                                                )
                                                .border(
                                                    1.5.dp,
                                                    if (estaSeleccionado) Color.White else Color(0xFF64748B),
                                                    CircleShape
                                                )
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = metodo.icono, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = metodo.titulo,
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = metodo.subtitulo,
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF009EE3).copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = metodo.etiqueta,
                                                color = Color(0xFF38BDF8),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "DETALLE_LOCAL" -> {
                        when (metodoLocalSeleccionado) {
                            MetodoPago.MERCADO_PAGO -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🤝 Mercado Pago Checkout Oficial", color = Color(0xFF009EE3), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Paga de forma oficial con Tarjetas (Débito/Crédito), SPEI STP, OXXO Pay o Saldo MP:",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFF009EE3))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("Paquete Seleccionado:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                                    Text("${paqueteSeleccionado.nombre} (+${paqueteSeleccionado.segundos}s)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF009EE3).copy(alpha = 0.2f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Oficial México 🇲🇽", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1E293B)))
                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Total a Pagar:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                                Text("$${paqueteSeleccionado.precioEntero}.00 MXN", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Referencia de Orden:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                                Text(referenciaPagoLocal, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Button(
                                                onClick = {
                                                    cargandoCheckoutMp = true
                                                    coroutineScope.launch {
                                                        try {
                                                            val pref = MercadoPagoHelper.crearPreferenciaCheckout(paqueteSeleccionado)
                                                            referenciaPagoLocal = pref.externalReference
                                                            MercadoPagoHelper.abrirCheckoutEnNavegador(context, pref.initPointUrl)
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Error abriendo checkout: ${e.message}", Toast.LENGTH_SHORT).show()
                                                        } finally {
                                                            cargandoCheckoutMp = false
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009EE3))
                                            ) {
                                                if (cargandoCheckoutMp) {
                                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                }
                                                Text("🚀 Abrir Checkout Mercado Pago Web/App", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }

                            MetodoPago.CRYPTO -> {
                                val cotizacion = remember(paqueteSeleccionado, redCriptoSeleccionada) {
                                    CryptoPaymentHelper.calcularCotizacion(paqueteSeleccionado, redCriptoSeleccionada)
                                }
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🪙 Pago VIP en Criptomonedas (USDT)", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Sin cancelaciones ni restricciones bancarias. Acreditación global:",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Selector de red blockchain
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        RedCripto.values().forEach { red ->
                                            val seleccionada = redCriptoSeleccionada == red
                                            Button(
                                                onClick = { redCriptoSeleccionada = red },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (seleccionada) Color(0xFF10B981) else Color(0xFF1E293B)
                                                ),
                                                border = BorderStroke(1.dp, if (seleccionada) Color.White else Color(0xFF334155)),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = red.name,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (seleccionada) Color.White else Color(0xFF94A3B8)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFF10B981))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("Monto exacto a transferir:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                                    Text("${cotizacion.montoUsdtFormateado} USDT", color = Color(0xFF10B981), fontWeight = FontWeight.Black, fontSize = 16.sp)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(cotizacion.red.token, color = Color(0xFF34D399), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Equivalente: $${paqueteSeleccionado.precioEntero}.00 MXN (+${paqueteSeleccionado.segundos}s)", color = Color(0xFF94A3B8), fontSize = 10.sp)

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1E293B)))
                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text("Dirección de Billetera Receptora:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = cotizacion.walletDestino.take(10) + "..." + cotizacion.walletDestino.takeLast(8),
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Button(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(cotizacion.walletDestino))
                                                        Toast.makeText(context, "Billetera copiada", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(26.dp),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text("📋 Copiar", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            OutlinedTextField(
                                                value = txHashCripto,
                                                onValueChange = { txHashCripto = it },
                                                label = { Text("Hash de Transacción / TxID", fontSize = 10.sp) },
                                                placeholder = { Text("Ej. 4a7c8d9e...", fontSize = 10.sp) },
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFF10B981),
                                                    unfocusedBorderColor = Color(0xFF334155),
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            MetodoPago.TARJETA -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("💳 Tarjeta Débito / Crédito", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Ingresa o confirma el cargo con tarjeta bancaria autorizada:",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(115.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                        border = BorderStroke(1.dp, Color(0xFF009EE3))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Velvet Platinum Card", color = Color(0xFF009EE3), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Text("VISA / MC", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                                            }

                                            Text(
                                                "•••• •••• •••• 4242",
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                letterSpacing = 2.sp
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text("TITULAR", color = Color(0xFF64748B), fontSize = 8.sp)
                                                    Text("GAMER VIP PLATINUM", color = Color(0xFFE2E8F0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("VENCE", color = Color(0xFF64748B), fontSize = 8.sp)
                                                    Text("12/28", color = Color(0xFFE2E8F0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Plan de financiamiento:",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.align(Alignment.Start)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { mesesSinIntereses = 1 },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (mesesSinIntereses == 1) Color(0xFF1E293B) else Color(0xFF020617)
                                            ),
                                            border = BorderStroke(1.dp, if (mesesSinIntereses == 1) Color(0xFF009EE3) else Color(0xFF334155))
                                        ) {
                                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("1 pago de", color = Color(0xFF94A3B8), fontSize = 9.sp)
                                                Text("$${paqueteSeleccionado.precioEntero} MXN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Text("Al contado", color = Color(0xFF10B981), fontSize = 9.sp)
                                            }
                                        }

                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { mesesSinIntereses = 3 },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (mesesSinIntereses == 3) Color(0xFF1E293B) else Color(0xFF020617)
                                            ),
                                            border = BorderStroke(1.dp, if (mesesSinIntereses == 3) Color(0xFF009EE3) else Color(0xFF334155))
                                        ) {
                                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                val cuota = paqueteSeleccionado.precioEntero / 3
                                                Text("3 pagos de", color = Color(0xFF94A3B8), fontSize = 9.sp)
                                                Text("$$cuota MXN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Text("Hasta 3 MSI", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            MetodoPago.SPEI -> {
                                val clabe = "646180157023948512"
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🏦 Transferencia SPEI Instantánea", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Transfiere desde tu banca móvil (BBVA, Banamex, Santander, etc.) con los datos STP:",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFF009EE3))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Banco Destino:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                                Text("STP / Mercado Pago", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("CLABE Interbancaria:", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                                    Text("6461 8015 7023 9485 12", color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                }
                                                Button(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(clabe))
                                                        Toast.makeText(context, "CLABE copiada al portapapeles", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text("📋 Copiar", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Beneficiario:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                                Text("VIP Call México S.A.", color = Color.White, fontSize = 11.sp)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Monto exacto:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                                Text("$${paqueteSeleccionado.precioEntero}.00 MXN", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Concepto:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                                Text(referenciaPagoLocal, color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    }
                                }
                            }

                            MetodoPago.OXXO -> {
                                val oxxoRef = "4815162342109982"
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🏪 Depósito en Efectivo OXXO Pay", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Acude a cualquier tienda OXXO y presenta la siguiente referencia en caja:",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFF59E0B))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(34.dp)
                                                    .background(Color.White)
                                                    .padding(horizontal = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                repeat(28) { i ->
                                                    Box(
                                                        modifier = Modifier
                                                            .width(if (i % 3 == 0) 3.dp else if (i % 2 == 0) 2.dp else 1.dp)
                                                            .height(26.dp)
                                                            .background(Color.Black)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("4815 1623 4210 9982", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                Button(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(oxxoRef))
                                                        Toast.makeText(context, "Referencia OXXO copiada", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text("📋 Copiar", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Monto en caja:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                                Text("$${paqueteSeleccionado.precioEntero}.00 MXN (+ com. tienda)", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Vigencia:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                                Text("24 horas (Acreditación inmediata)", color = Color(0xFF38BDF8), fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            MetodoPago.QR -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("📱 Código QR Mercado Pago", color = Color(0xFF009EE3), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Abre tu app móvil de Mercado Pago y escanea este código para acreditar tus segundos:",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(110.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Box(modifier = Modifier.size(24.dp).border(3.dp, Color.Black).padding(3.dp)) {
                                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                                                }
                                                Box(modifier = Modifier.size(24.dp).border(3.dp, Color.Black).padding(3.dp)) {
                                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                                                }
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .align(Alignment.CenterHorizontally)
                                                    .background(Color(0xFF009EE3)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("MP", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Box(modifier = Modifier.size(24.dp).border(3.dp, Color.Black).padding(3.dp)) {
                                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                                                }
                                                Text("QR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Ref: $referenciaPagoLocal", color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        Button(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(referenciaPagoLocal))
                                                Toast.makeText(context, "Folio QR copiado", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.height(24.dp),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("📋 Copiar", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text("Monto: $${paqueteSeleccionado.precioEntero}.00 MXN", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    "PROCESANDO_GPAY" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF2563EB),
                                modifier = Modifier.size(44.dp),
                                strokeWidth = 3.5.dp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Conectando con Google Wallet...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Acreditando segundos en Cloud Firestore...",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    "PROCESANDO_LOCAL" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF009EE3),
                                modifier = Modifier.size(44.dp),
                                strokeWidth = 3.5.dp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Procesando pago con pasarela local...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Confirmando acreditación de fondos...",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }

                    "EXITO_GPAY" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎉", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "¡Recarga Google Pay Exitosa!",
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Acreditación atómica completada en Cloud Firestore",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF10B981))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Tiempo acreditado:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text(
                                            "+${paqueteSeleccionado.segundos}s (${paqueteSeleccionado.nombre})",
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Monto pagado:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text(
                                            "$${paqueteSeleccionado.precioEntero}.00 MXN",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Tarjeta vinculada:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text(
                                            "${transaccionGPayExito?.cardNetwork ?: "VISA"} •••• ${transaccionGPayExito?.cardDetails ?: "••••"}",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Folio Transacción:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text(
                                            transaccionIdGPay.take(12),
                                            color = Color.White,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "EXITO_LOCAL" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎉", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "¡Pago Aprobado con Éxito!",
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (metodoLocalSeleccionado == MetodoPago.CRYPTO) "Transacción Cripto verificada y acreditada en Cloud Firestore" else "Transacción procesada correctamente por Mercado Pago México",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF10B981))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Tiempo acreditado:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text(
                                            "+${paqueteSeleccionado.segundos}s (${paqueteSeleccionado.nombre})",
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Monto pagado:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text(
                                            if (metodoLocalSeleccionado == MetodoPago.CRYPTO) {
                                                val usdtVal = String.format(java.util.Locale.US, "%.2f", paqueteSeleccionado.precioMxn / 20.0)
                                                "$usdtVal USDT ($${paqueteSeleccionado.precioEntero} MXN)"
                                            } else {
                                                "$${paqueteSeleccionado.precioEntero}.00 MXN"
                                            },
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Método:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text(
                                            metodoLocalSeleccionado.titulo,
                                            color = Color(0xFF38BDF8),
                                            fontSize = 11.sp
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(if (metodoLocalSeleccionado == MetodoPago.CRYPTO) "Folio / TxID Cripto:" else "Folio Mercado Pago:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        Text(
                                            referenciaPagoLocal,
                                            color = Color.White,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (pasoActual) {
                "SELECCION" -> {
                    if (mostrarMetodosLocales) {
                        Button(
                            onClick = { pasoActual = "DETALLE_LOCAL" },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009EE3)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Continuar con Método Seleccionado ($${paqueteSeleccionado.precioEntero} MXN) →",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
                "DETALLE_LOCAL" -> {
                    Button(
                        onClick = {
                            pasoActual = "PROCESANDO_LOCAL"
                            coroutineScope.launch {
                                val uidEfectivo = Settings.Secure.getString(
                                    context.contentResolver,
                                    Settings.Secure.ANDROID_ID
                                ) ?: "gamer_vip_mexico"

                                when (metodoLocalSeleccionado) {
                                    MetodoPago.MERCADO_PAGO -> {
                                        MercadoPagoHelper.acreditarPagoMercadoPago(
                                            firestore = FirebaseFirestore.getInstance(),
                                            uid = uidEfectivo,
                                            salaId = salaId,
                                            paquete = paqueteSeleccionado,
                                            folioReferencia = referenciaPagoLocal
                                        )
                                    }
                                    MetodoPago.CRYPTO -> {
                                        val cotizacion = CryptoPaymentHelper.calcularCotizacion(
                                            paquete = paqueteSeleccionado,
                                            red = redCriptoSeleccionada
                                        )
                                        val hashEfectivo = txHashCripto.ifBlank { "TX-USDT-" + Random.nextInt(10000000, 99999999) }
                                        referenciaPagoLocal = hashEfectivo
                                        CryptoPaymentHelper.acreditarPagoCripto(
                                            firestore = FirebaseFirestore.getInstance(),
                                            uid = uidEfectivo,
                                            salaId = salaId,
                                            cotizacion = cotizacion,
                                            txHash = hashEfectivo
                                        )
                                    }
                                    else -> {
                                        delay(800)
                                    }
                                }
                                onRecargaExitosa(
                                    paqueteSeleccionado.segundos,
                                    paqueteSeleccionado.precioEntero,
                                    true
                                )
                                pasoActual = "EXITO_LOCAL"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (metodoLocalSeleccionado) {
                                MetodoPago.MERCADO_PAGO -> Color(0xFF009EE3)
                                MetodoPago.CRYPTO -> Color(0xFF10B981)
                                else -> Color(0xFF10B981)
                            }
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        val textoBoton = when (metodoLocalSeleccionado) {
                            MetodoPago.MERCADO_PAGO -> "✅ Ya Pagué / Acreditar Mercado Pago"
                            MetodoPago.CRYPTO -> "⚡ Confirmar y Acreditar USDT Cripto"
                            MetodoPago.TARJETA -> "🔒 Pagar $${paqueteSeleccionado.precioEntero} MXN"
                            MetodoPago.SPEI -> "✅ Ya transferí / Simular SPEI"
                            MetodoPago.OXXO -> "🏪 Simular Depósito OXXO Pay"
                            MetodoPago.QR -> "📱 Simular Pago con App MP"
                        }
                        Text(text = textoBoton, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                "EXITO_GPAY", "EXITO_LOCAL" -> {
                    Button(
                        onClick = {
                            googlePayViewModel.reiniciarEstado()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🚀 Volver a la Sala VIP", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                else -> {}
            }
        },
        dismissButton = {
            when (pasoActual) {
                "SELECCION" -> {
                    OutlinedButton(
                        onClick = {
                            googlePayViewModel.reiniciarEstado()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cerrar", color = Color(0xFF94A3B8))
                    }
                }
                "DETALLE_LOCAL" -> {
                    OutlinedButton(
                        onClick = { pasoActual = "SELECCION" },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("← Volver", color = Color(0xFF94A3B8))
                    }
                }
                else -> null
            }
        }
    )
}

/**
 * Minijuego de Tragamonedas Avanzado con:
 * - Botón pequeño siempre disponible
 * - Auto-Spin (giro automático cada 8s)
 * - Giro Compartido (autorización para que la modelo también gire)
 * - Premios compartidos (Gamer gana segundos, Modelo gana doble en efectivo)
 */
@Composable
fun ModalTragamonedasAvanzado(
    saldoSegundos: Long,
    apuestaActual: Int,
    onCambiarApuesta: (Int) -> Unit,
    autoSpinActivo: Boolean,
    onToggleAutoSpin: (Boolean) -> Unit,
    autorizarModelo: Boolean,
    onToggleAutorizarModelo: (Boolean) -> Unit,
    onGirar: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val simbolos = listOf("🍒", "7️⃣", "💎", "🍋", "🔔")
    var reel1 by remember { mutableStateOf("🍒") }
    var reel2 by remember { mutableStateOf("7️⃣") }
    var reel3 by remember { mutableStateOf("💎") }
    var estaGirando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎰 Ruleta P2P Win-Win",
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "💎 ${saldoSegundos}s",
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¡Si ganas tú, la modelo gana doble bono!",
                    color = Color(0xFF10B981),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Los 3 Carretes del Tragamonedas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF020617))
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = reel1, fontSize = 28.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = reel2, fontSize = 28.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = reel3, fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de Apuestas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Apuesta:", color = Color.Gray, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(5, 10, 20).forEach { bet ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (apuestaActual == bet) Color(0xFFF59E0B) else Color(0xFF1E293B))
                                    .clickable { onCambiarApuesta(bet) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${bet}s",
                                    color = if (apuestaActual == bet) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // OPCIÓN 1: Auto-Spin Automático
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("⚡ Auto-Spin (Cada 8s)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Gira solo sin tocar la pantalla", color = Color.Gray, fontSize = 9.sp)
                    }
                    Switch(
                        checked = autoSpinActivo,
                        onCheckedChange = onToggleAutoSpin,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFF59E0B))
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // OPCIÓN 2: Permitir a la Modelo Girar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🤝 Giro Compartido", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("Permitir que la modelo también gire", color = Color.Gray, fontSize = 9.sp)
                    }
                    Switch(
                        checked = autorizarModelo,
                        onCheckedChange = onToggleAutorizarModelo,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFEC4899))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (saldoSegundos < apuestaActual) return@Button
                    estaGirando = true
                    scope.launch {
                        repeat(10) {
                            reel1 = simbolos[Random.nextInt(simbolos.size)]
                            reel2 = simbolos[Random.nextInt(simbolos.size)]
                            reel3 = simbolos[Random.nextInt(simbolos.size)]
                            delay(60)
                        }
                        estaGirando = false
                        onGirar(apuestaActual)
                    }
                },
                enabled = !estaGirando && saldoSegundos >= apuestaActual,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
            ) {
                Text(if (estaGirando) "⏳..." else "🎲 ¡GIRAR AHORA!", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Minimizar")
            }
        }
    )
}