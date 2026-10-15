# TASK PLAN: Implementación y Revisión de Pantalla de Mercado Pago y Artistas Fugaces

## 1. Pasarela de Pago & Saldo Gamer
- [x] Inspección de la arquitectura actual en `MainActivity.kt` (Cloud Firestore `salas_videollamada/sala_vip_1`, estados de Gamer y Modelo).
- [x] Creación de modelos de datos `PaqueteRecarga` y `MetodoPago` en Jetpack Compose.
- [x] Implementación de paquetes de minutos oficiales:
  - [x] Paquete Básico: $100 MXN = 100s
  - [x] Paquete Popular: $300 MXN = 300s + 30s bono (330s total)
  - [x] Paquete VIP: $500 MXN = 500s + 100s bono (600s total)
- [x] Implementación de métodos de pago soportados:
  - [x] Mercado Pago Checkout (instantáneo)
  - [x] Tarjeta Débito / Crédito
  - [x] Transferencia SPEI instantánea (CLABE STP)
  - [x] Depósito en efectivo OXXO (OXXO Pay)
  - [x] Código QR Mercado Pago
- [x] Integración de botón "💎 Recargar" en la barra superior del Gamer.
- [x] Integración de botón "💳 Recargar Segundos con Mercado Pago" en el panel de control del Gamer.
- [x] Integración de botón flotante "💎" accesible durante la videollamada.
- [x] Apertura directa de Mercado Pago al agotarse el tiempo de llamada (`alertaTiempoAgotado`).
- [x] Actualización atómica en Cloud Firestore (`segundos_participante` y `ultimo_juego`).

## 2. Red y Conectividad WebRTC Cloud
- [x] Eliminación total de red local (servidor HTTP puerto 8080 y campos de IP) y migración 100% nativa a WebRTC por Internet con STUN de Google y Firestore.
- [x] Unificación de captura y renderizado en WebRTC con OpenGL ES sin conflictos de CameraX.
- [x] Deduplicación de señalización SDP y encolado de candidatos ICE para handshake robusto en 4G/5G.

## 3. Modo Interactivo: "Artistas Fugaces (Miniserie de Romance)"
- [x] Análisis conceptual y psicológico: gamificación teatral que elimina la fricción transaccional/comercial, atrayendo mujeres y modelos que buscan complicidad, misterio y conexión real sin sentirse en un intercambio comercial frío.
- [x] Modelos de datos `PersonajeMiniserie`, `EscenaMiniserie` y catálogo en `MiniserieModels.kt`:
  - [x] Capítulo 1: Encuentro en la Librería de París (Café Bohemio & Lluvia)
  - [x] Capítulo 2: Coincidencia en el Último Tren a Medianoche (Expreso Nocturno Venecia-Viena)
  - [x] Capítulo 3: Secretos en el Baile de Gala (Palacio de Mónaco & Antifaces)
  - [x] Capítulo 4: Ex-parejas en una Boda Lejana (La Toscana & Recuerdos No Resueltos)
  - [x] Capítulo 5: Atrapados en el Ascensor de Tokio (Shibuya, Rascacielos & Neón)
  - [x] Capítulo 6: La Subasta de Arte Clandestina (Club Privado en Londres & Cuadro Enigmático)
- [x] Asignación de roles diferenciados:
  - [x] Gamer: Rol A con rasgos, objetivos secretos y frases rompehielo para iniciar la escena.
  - [x] Modelo: Rol B con rasgos, objetivos secretos y respuestas ingeniosas.
- [x] Sincronización en tiempo real vía Cloud Firestore en la sala activa (`miniserie_activa`, roles, sinopsis, giro dramático y último juego).
- [x] Componentes de interfaz de usuario en `MiniserieRomanceComponents.kt` y `MainActivity.kt`:
  - [x] Tarjeta interactiva en Lobby previo a entrar a la llamada (`CardMiniserieLobby`).
  - [x] Banner flotante cinematográfico durante la videollamada (`BannerMiniserieEnLlamada`).
  - [x] Botón flotante "🎭" visible en pantalla tanto para Gamer como para Modelo.
  - [x] Botón "🎭 Guión / Rol" en la barra de controles inferiores durante la llamada.
  - [x] Modal interactivo completo `ModalMiniserieRomance` con 4 pestañas: Guión Activo, Giros de Guión, Catálogo de 6 Capítulos y Manifiesto Conceptual.
  - [x] Detonador en tiempo real de "Giro de Guión Dramático" sincronizado para ambos dispositivos.
- [x] Compilación exitosa con `./gradlew assembleDebug` (0 errores).
- [x] Despliegue de los 3 APKs (`app-debug.apk`, `app-velvet-vip.apk`, `app-casino-vip.apk`) en `/home/asus/casino-web-download/public/`.
- [x] Instalación y verificación exitosa vía ADB en el dispositivo de prueba físico (`192.168.0.13:5555`).

## 4. Revisión Crítica, Corrección de Fallos y Experiencia de Usuario Avanzada
- [x] **Pérdida de capítulo preseleccionado en Lobby**: Corregido en `conectarAleatorio` para propagar automáticamente la miniserie activa configurada en el Lobby a la sala aleatoria asignada.
- [x] **Eliminación de parpadeos y lag**: Implementadas actualizaciones de estado optimistas locales inmediatas en `activarEscenaMiniserie`, `detonarGiroMiniserie` y `desactivarModoMiniserie`.
- [x] **Giro sin escena activa**: Si el usuario detona un giro de guión sin capítulo activo, el sistema ahora auto-activa la escena correspondiente junto con el giro dramático.
- [x] **Intercambio dinámico de roles**: Integrado soporte de inversión de roles (`miniserie_roles_invertidos`) tanto en Firestore como en Compose para permitir a Gamer y Modelo alternar personajes en cualquier momento.
- [x] **HUD expandible en videollamada**: `BannerMiniserieEnLlamada` ahora incluye vista previa expandible de "Objetivo Secreto" y "Frase Rompehielo" con copia rápida, más botones de acción inmediata ("⚡ Giro al Azar" y "🎲 Otra Escena") sin bloquear el video con un modal de pantalla completa.
- [x] **Overlay dramático y respuesta háptica**: Añadido permiso `VIBRATE` en `AndroidManifest.xml`, disparador háptico `triggerHapticFeedback` y componente `OverlayGiroDramatico` que notifica visual y sensorialmente los giros a ambos participantes.
- [x] **Suite de pruebas unitarias**: Creado [`MiniserieModelsTest.kt`](file:///home/asus/AndroidStudioProjects/My1eraApplication/app/src/test/java/com/example/my1eraapplication/model/MiniserieModelsTest.kt), pasando al 100% (3 pruebas exitosas con 0 fallos).
- [x] **Compilación y redespliegue de APKs**: Generado nuevo APK de 96MB y sincronizado en `/home/asus/casino-web-download/public/` e instalado con éxito en el terminal de prueba.

## 5. Rediseño HUD en 'U' Invertida (estilo Comfer) y Cámara Amplificada en Vivo
- [x] **Visión de Cámara Amplificada 100% Pantalla Completa**:
  - [x] Renderizado de video a pantalla completa en el fondo (`WebRtcVideoView` con OpenGL ES), eliminando la caja pequeña y marcos negros.
  - [x] Continuidad de previsualización local: la Modelo se ve inmediatamente a sí misma al pulsar "Iniciar Transmisión" (sin pantallas negras).
  - [x] PiP flotante inteligente y dinámico cuando hay dos participantes conectados.
- [x] **Menú Flotante Moderno en 'U' Invertida (Inspirado en Comfer)**:
  - [x] Arquitectura de arco perimetral (lateral izquierdo, arco superior y lateral derecho).
  - [x] Integración en el arco superior de los 3 carretes del tragamonedas (`[🍒|7️⃣|💎]`) con botón de giro / auto-spin táctil.
  - [x] Iconos de acción distribuidos a lo largo del arco:
    - Lateral izquierdo: 🔄 Girar cámara, 🙈 PiP, 🎭 Miniserie Guión, ✨ Director I.A.
    - Lateral derecho: 💰 Ganancias / Saldo en vivo, 💼 Billetera / Retiro, 💎 Recargar (Gamer), 🔴 Finalizar / Colgar.
  - [x] Centro de la pantalla 100% despejado y libre para disfrutar de la transmisión.
- [x] **Corrección del Emparejamiento y Ciclo de Vida WebRTC**:
  - [x] Publicación correcta en Firestore: la Modelo en transmisión se marca `disponible: true` y `en_llamada: false` mientras espera cliente.
  - [x] Al conectar un Gamer, la sala pasa a `en_llamada: true` y `disponible: false`.
  - [x] Limpieza preventiva de señalización anterior (SDP y candidatos ICE) para evitar reanudaciones con datos obsoletos.
  - [x] Corrección de la condición de desconexión en cascada (race condition).
- [x] **Verificación y Despliegue en Dispositivos Físicos**:
  - [x] Ejecución de pruebas unitarias (`./gradlew test`).
  - [x] Compilación de ambos APKs (`app-velvet-vip.apk` y `app-velvet-modelos.apk`).
  - [x] Instalación y prueba visual en el Samsung Galaxy A06 (`SM-A065M`).
  - [x] Notificación y guía al usuario para conectar el teléfono Motorola (Gamer).
- [x] Conexión y detección por ADB del teléfono Motorola (Gamer).
- [x] Instalación y verificación de `app-velvet-vip.apk` en el dispositivo Motorola (interfaz moderna en 'U' invertida confirmada por captura de pantalla).
- [x] Conexión a Internet en Motorola (detectada en Wi-Fi 192.168.100.2).
- [x] Prueba de videollamada P2P en vivo de extremo a extremo (Modelo Samsung <-> Gamer Motorola):
  - [x] Señalización WebRTC sobre Cloud Firestore Querétaro (`sala_vip_1`).
  - [x] Streaming de video hardware VP8 decodificado y renderizado a 20.0 fps (0 frames caídos) con OpenGL ES.
  - [x] HUD perimetral en "U" invertida activo y responsive en ambos dispositivos.
  - [x] Sincronización en vivo de minijuego tragamonedas, bonos dobles y saldo ($1983.25 MXN acumulados por la modelo).

## 6. Protocolo de Ejecución en 3 Fases: Git, Releases v1.5.0, Firebase y Prueba en Vivo
- [x] **FASE 1: Blindaje e Inicialización Git + Release v1.5.0 en GitHub**
  - [x] Auditoría pre-Git y configuración de `.gitignore` estricto (Android/Kotlin).
  - [x] Detección de archivos `.json` (`app/google-services.json`) y verificación de privacidad en GitHub (`krisagus/velvet-vip-app` es público).
  - [x] Inicialización `git init`, configuración de rama `main` y vinculación con `krisagus/velvet-vip-app`.
  - [x] Presentación de `git status` y confirmación de inclusión de `google-services.json`.
  - [x] Commit base v1.5.0 y push a GitHub (`main`) exitoso (`1a7d017`).
  - [x] Creación de tag `v1.5.0` y publicación de Release en GitHub con APKs adjuntos (`app-velvet-vip.apk` y `app-velvet-modelos.apk`).
- [x] **FASE 2: Sincronización de Producción en Firebase Hosting (v1.5.0)**
  - [x] Actualización de enlaces en `index.html` hacia el Release de GitHub v1.5.0 para resolución de descargas globales.
  - [x] Despliegue con `firebase deploy --only hosting` exitoso en `local-casino-hub-970df`.
  - [x] Verificación HTTP en vivo (`curl -I https://local-casino-hub-970df.web.app` -> `HTTP/2 200 OK`).
- [x] **FASE 3: Preparación y Ejecución de Prueba P2P en Vivo**
  - [x] Verificación de estado de dispositivos con `adb devices -l` (Samsung Galaxy A06 y Motorola Moto G20 conectados).
  - [x] Preparación de script ejecutable de monitoreo [`monitor_p2p_test.sh`](file:///home/asus/AndroidStudioProjects/My1eraApplication/monitor_p2p_test.sh) con filtros WebRTC y AndroidRuntime.
  - [x] Reconexión exitosa de Motorola Moto G20 en ADB (`N5OEB50228`) y en red Wi-Fi local (`192.168.100.2`).
  - [x] Ejecución de videollamada P2P en vivo (Samsung A06 ↔ Moto G20) con captura de video bidireccional, OpenGL ES y sincronización de Firestore.



