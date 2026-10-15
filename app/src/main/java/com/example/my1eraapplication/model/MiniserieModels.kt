package com.example.my1eraapplication.model

/**
 * Modelos de datos para el modo "Artistas Fugaces: Miniserie de Romance".
 * Gamificación interactiva donde Gamer y Modelo interpretan personajes de una historia
 * romántica improvisada, eliminando la barrera puramente transaccional.
 */
data class LineaDialogo(
    val personaje: String, // "GAMER", "MODELO", o el nombre del personaje
    val texto: String,
    val acotacion: String = "",
    val intencion: String = ""
)

data class ActoEpisodio(
    val numero: Int,
    val titulo: String,
    val objetivoClave: String,
    val dialogos: List<LineaDialogo> = emptyList(),
    val temasConversacion: List<String> = emptyList(),
    val preguntasContraataque: List<String> = emptyList(),
    val replicasSugeridas: List<String> = emptyList()
)

fun ActoEpisodio.toMap(): Map<String, Any> {
    return mapOf(
        "numero" to numero,
        "titulo" to titulo,
        "objetivoClave" to objetivoClave,
        "dialogos" to dialogos.map { d ->
            mapOf(
                "personaje" to d.personaje,
                "texto" to d.texto,
                "acotacion" to d.acotacion,
                "intencion" to d.intencion
            )
        },
        "temasConversacion" to temasConversacion,
        "preguntasContraataque" to preguntasContraataque,
        "replicasSugeridas" to replicasSugeridas
    )
}

fun actoEpisodioFromMap(map: Map<String, Any?>): ActoEpisodio {
    val numero = (map["numero"] as? Number)?.toInt() ?: 1
    val titulo = map["titulo"] as? String ?: "Acto $numero"
    val objetivoClave = map["objetivoClave"] as? String ?: ""
    val rawDialogos = (map["dialogos"] as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
    val dialogos = rawDialogos.map { dMap ->
        LineaDialogo(
            personaje = dMap["personaje"] as? String ?: "GAMER",
            texto = dMap["texto"] as? String ?: "",
            acotacion = dMap["acotacion"] as? String ?: "",
            intencion = dMap["intencion"] as? String ?: ""
        )
    }
    val temas = (map["temasConversacion"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    val preguntas = (map["preguntasContraataque"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
    val replicas = (map["replicasSugeridas"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

    return ActoEpisodio(
        numero = numero,
        titulo = titulo,
        objetivoClave = objetivoClave,
        dialogos = dialogos,
        temasConversacion = temas,
        preguntasContraataque = preguntas,
        replicasSugeridas = replicas
    )
}

data class PersonajeMiniserie(
    val nombre: String,
    val descripcion: String,
    val objetivoSecreto: String,
    val fraseApertura: String
)

data class EscenaMiniserie(
    val id: String,
    val capituloNumero: Int,
    val titulo: String,
    val sinopsis: String,
    val ambiente: String,
    val mood: String,
    val rolGamer: PersonajeMiniserie,
    val rolModelo: PersonajeMiniserie,
    val girosDeGuion: List<String>,
    val actos: List<ActoEpisodio> = emptyList()
) {
    fun obtenerActosCompletos(): List<ActoEpisodio> {
        if (actos.isNotEmpty()) return actos
        return generarActosGenericos(this)
    }
}

object CatalogoMiniserie {
    val escenas = listOf(
        EscenaMiniserie(
            id = "paris_libreria",
            capituloNumero = 1,
            titulo = "Encuentro en la Librería de París",
            sinopsis = "Tarde lluviosa en el Barrio Latino de París. Dos extraños alcanzan exactamente el mismo libro de poesía antigua en un rincón bohemio.",
            ambiente = "Lluvia contra los cristales, aroma a café tostado, páginas amarillentas y luces de filamento.",
            mood = "☕ Café Bohemio & Lluvia",
            rolGamer = PersonajeMiniserie(
                nombre = "Julián (Escritor en crisis)",
                descripcion = "Un novelista melancólico y reservado que busca desesperadamente la chispa de su próxima obra maestra.",
                objetivoSecreto = "Descubrir qué misterio esconde ella sin revelar que eres escritor profesional.",
                fraseApertura = "¿Ibas a llevarte la última copia de este poema, o el destino nos obligó a coincidir?"
            ),
            rolModelo = PersonajeMiniserie(
                nombre = "Elena (Curadora de arte enigmática)",
                descripcion = "Una experta en arte refinada y perspicaz que huye de una vida burguesa predecible.",
                objetivoSecreto = "Convencerlo de invitarte un café antes de que cese la lluvia, fingiendo total indiferencia.",
                fraseApertura = "La buena poesía sólo le pertenece a quien sabe leer entre líneas... ¿tú sabes leerlas?"
            ),
            girosDeGuion = listOf(
                "⚡ ¡Un trueno fuerte apaga las luces de la librería por unos segundos!",
                "⚡ ¡Cae un sobre cerrado con una nota manuscrita de entre las páginas del libro!",
                "⚡ ¡El dueño de la librería anuncia que cerrará en 3 minutos y deben salir juntos bajo la lluvia!",
                "⚡ ¡Uno de los dos descubre que el libro tiene una dedicatoria idéntica a su historia personal!"
            )
        ),
        EscenaMiniserie(
            id = "tren_medianoche",
            capituloNumero = 2,
            titulo = "Coincidencia en el Último Tren a Medianoche",
            sinopsis = "El compartimiento de primera clase en el expreso nocturno Venecia-Viena. La niebla alpina oculta el exterior y son los únicos dos pasajeros despiertos.",
            ambiente = "Traqueteo hipnótico de los rieles, lámparas de bronce, sombras y copas de champán.",
            mood = "🚂 Expreso Nocturno & Sombras",
            rolGamer = PersonajeMiniserie(
                nombre = "Matteo (Arquitecto con equipaje confidencial)",
                descripcion = "Un hombre elegante y calculador que viaja con un maletín que no suelta jamás.",
                objetivoSecreto = "Hacerla reír con sinceridad en menos de 2 minutos sin revelar qué guardas en tu maletín.",
                fraseApertura = "Dicen que quien toma este tren a medianoche siempre está escapando de algo... ¿de qué escapas tú?"
            ),
            rolModelo = PersonajeMiniserie(
                nombre = "Sofía (Heredera viajando de incógnito)",
                descripcion = "Una mujer magnética y audaz que viaja con pasaporte bajo seudónimo buscando libertad.",
                objetivoSecreto = "Descubrir si él es un agente encubierto o simplemente el hombre más intrigante del tren.",
                fraseApertura = "Si te confesara de qué escapo, tendría que pedirte que bajes en la próxima estación... y la noche apenas comienza."
            ),
            girosDeGuion = listOf(
                "⚡ ¡El revisor del tren golpea la puerta y deben fingir ser una pareja de recién casados!",
                "⚡ ¡El tren frena bruscamente sobre un viaducto alpino envuelto en niebla!",
                "⚡ ¡El camarero deja una botella de vino espumoso de cortesía diciendo 'De parte de su admirador'!",
                "⚡ ¡La temperatura del vagón desciende y deciden compartir la misma manta de viaje!"
            )
        ),
        EscenaMiniserie(
            id = "baile_gala",
            capituloNumero = 3,
            titulo = "Secretos en el Baile de Gala",
            sinopsis = "Un suntuoso baile de máscaras en un palacio de Mónaco con vista al mar. Sus miradas se cruzan a través de antifaces de terciopelo veneciano.",
            ambiente = "Orquesta de cuerdas de fondo, brisa del Mediterráneo, copas de cristal y misterio nocturno.",
            mood = "🎭 Máscaras & Alta Sociedad",
            rolGamer = PersonajeMiniserie(
                nombre = "Leonardo (Diplomático audaz)",
                descripcion = "Un estratega con sonrisa cautivadora que desafía el estricto protocolo de la velada.",
                objetivoSecreto = "Adivinar el verdadero color de sus ojos detrás del antifaz y lograr una confesión sincera.",
                fraseApertura = "Las máscaras ocultan el rostro, pero revelan lo que el corazón realmente busca en secreto..."
            ),
            rolModelo = PersonajeMiniserie(
                nombre = "Camila (Pintora rebelde infiltrada)",
                descripcion = "Una artista apasionada y desafiante que se coló en la fiesta para sabotear una aburrida subasta benéfica.",
                objetivoSecreto = "Hacerle una pregunta atrevida que lo desconcierte y quiebre su postura de diplomático.",
                fraseApertura = "Es un juego peligroso intentar leer a alguien que ni siquiera sabe si volverá a verte cuando amanezca."
            ),
            girosDeGuion = listOf(
                "⚡ ¡La orquesta arranca con un tango apasionado y todos en la terraza deben elegir pareja!",
                "⚡ ¡Suena la campana de la medianoche: la regla del baile exige una confesión sin mentiras!",
                "⚡ ¡Un grupo de reporteros se acerca con flashes y deben esconderse tras los cortinajes!",
                "⚡ ¡Uno de los dos pierde su antifaz y el otro debe cubrirlo con sus manos!"
            )
        ),
        EscenaMiniserie(
            id = "boda_lejana",
            capituloNumero = 4,
            titulo = "Ex-parejas en una Boda Lejana",
            sinopsis = "Fueron el amor de sus vidas hace cinco años y juraron no volver a verse. Coinciden frente a la barra de cócteles en una villa de la Toscana.",
            ambiente = "Luces doradas entre olivares centenarios, risas lejanas, copas de Chianti y nostalgia pura.",
            mood = "🍷 Toscana & Recuerdos No Resueltos",
            rolGamer = PersonajeMiniserie(
                nombre = "Gabriel (El que se marchó al extranjero)",
                descripcion = "Lograste tus metas profesionales en el extranjero, pero ninguna ciudad borró su recuerdo.",
                objetivoSecreto = "Averiguar si ella todavía usa aquel dije o si aún recuerda la promesa que hicieron.",
                fraseApertura = "Tenía el presentimiento de que este día llegaría... sigues usando esa misma mirada que desarma."
            ),
            rolModelo = PersonajeMiniserie(
                nombre = "Valeria (La que se quedó y triunfó)",
                descripcion = "Una mujer brillante, dueña de su vida e independiente, que prometió no ser vulnerable otra vez.",
                objetivoSecreto = "Demostrarle que estás en tu mejor momento y hacerlo suspirar sin darle la victoria.",
                fraseApertura = "Gabriel... juraba que estarías en Tokio o Nueva York huyendo de cualquier compromiso."
            ),
            girosDeGuion = listOf(
                "⚡ ¡Los recién casados piden a los dos viejos amigos brindar al micrófono por el amor duradero!",
                "⚡ ¡El DJ pone exactamente la canción acústica que bailaron en su primera cita!",
                "⚡ ¡Una copa de vino se derrama y deben ir a la fuente del jardín a limpiarse!",
                "⚡ ¡Uno de los dos confiesa lo que nunca se atrevió a decir en la despedida de hace 5 años!"
            )
        ),
        EscenaMiniserie(
            id = "ascensor_tokio",
            capituloNumero = 5,
            titulo = "Atrapados en el Ascensor de Tokio",
            sinopsis = "Un rascacielos vanguardista en Shibuya. Son las 11:30 PM, el ascensor panorámico se bloquea entre los pisos 45 y 46 y las luces se apagan.",
            ambiente = "Luces de neón de la metrópoli brillando abajo, silencio eléctrico y el latido de dos corazones.",
            mood = "🌃 Rascacielos & Neón Nocturno",
            rolGamer = PersonajeMiniserie(
                nombre = "Kenji (Empresario nocturno)",
                descripcion = "Un hombre que vive atrapado en agendas y números, hasta que el destino lo obliga a detenerse.",
                objetivoSecreto = "Lograr que ella olvide la claustrofobia y se sienta completamente cómoda y entretenida.",
                fraseApertura = "Respira hondo... si vamos a quedarnos atrapados aquí, al menos la vista de Tokio no tiene precio."
            ),
            rolModelo = PersonajeMiniserie(
                nombre = "Aiko (Diseñadora de modas cosmopolita)",
                descripcion = "Creativa, perspicaz y con un sentido del humor que desafía cualquier situación incómoda.",
                objetivoSecreto = "Proponer un juego de 'Tres verdades y un secreto' para poner a prueba su sinceridad.",
                fraseApertura = "Solo hay dos opciones para sobrevivir a esto: ponernos histéricos o fingir que esto es una cita improvisada."
            ),
            girosDeGuion = listOf(
                "⚡ ¡Suena la alarma del ascensor y la luz de emergencia cambia a un tono rojo tenue!",
                "⚡ ¡El intercomunicador avisa que los técnicos tardarán al menos 20 minutos más en llegar!",
                "⚡ ¡Sube la temperatura de la cabina y deciden aflojarse las corbatas y chaquetas!",
                "⚡ ¡El reflejo en el cristal los hace verse cara a cara sin necesidad de mirarse directo!"
            )
        ),
        EscenaMiniserie(
            id = "subasta_clandestina",
            capituloNumero = 6,
            titulo = "La Subasta de Arte Clandestina",
            sinopsis = "Un club secreto en el sótano de una mansión victoriana en Londres. Ambos pujan con insistencia por una misteriosa pintura: 'Amor Fugaz'.",
            ambiente = "Paneles de roble antiguo, humo de pipa aromática, miradas cómplices y susurros de coleccionistas.",
            mood = "🍸 Londres Noir & Tensión Cómplice",
            rolGamer = PersonajeMiniserie(
                nombre = "Alexander (Coleccionista de enigmas)",
                descripcion = "Un estratega seguro de sí mismo que disfruta competir con alguien tan inteligente como él.",
                objetivoSecreto = "Convencerla de pactar una tregua y compartir la posesión de la obra... o una copa a solas.",
                fraseApertura = "Estás subiendo la puja demasiado rápido... ¿tanto deseas el cuadro o solo te gusta retarme?"
            ),
            rolModelo = PersonajeMiniserie(
                nombre = "Diana (Tasadora encubierta)",
                descripcion = "Una experta audaz con contactos internacionales y una sonrisa que oculta cartas bajo la manga.",
                objetivoSecreto = "Averiguar si él descubrió el mensaje oculto detrás del marco de la pintura.",
                fraseApertura = "El arte sólo se rinde ante quien está dispuesto a arriesgarlo todo... ¿estás listo para seguir el paso?"
            ),
            girosDeGuion = listOf(
                "⚡ ¡El subastador declara empate y los invita a un salón privado a resolver la disputa!",
                "⚡ ¡Se escucha una sirena policial afuera y deben actuar como una pareja que disimula su escape!",
                "⚡ ¡Uno de los dos descubre que el marco del cuadro tiene una llave diminuta oculta!",
                "⚡ ¡Se sirven dos martinis secos con una nota: 'Uno de ustedes dos ya sabe cómo termina esto'!"
            )
        )
    )

    fun obtenerPorId(id: String): EscenaMiniserie? = escenas.find { it.id == id }
    fun escenaAleatoria(): EscenaMiniserie = escenas.random()
}

/**
 * Generador automático de libreto completo en 4 Episodios/Actos progresivos
 * para garantizar que la conversación fluya sin silencios incómodos.
 */
fun generarActosGenericos(escena: EscenaMiniserie): List<ActoEpisodio> {
    val ambienteResumido = escena.ambiente.take(50)
    return listOf(
        ActoEpisodio(
            numero = 1,
            titulo = "Acto 1: El Encuentro y Rompehielo",
            objetivoClave = "Romper la frialdad inicial, cruzar miradas y establecer la complicidad en $ambienteResumido.",
            dialogos = listOf(
                LineaDialogo("GAMER", escena.rolGamer.fraseApertura, "(Sosteniendo la mirada con curiosidad y sonrisa leve)", "Romper el hielo"),
                LineaDialogo("MODELO", escena.rolModelo.fraseApertura, "(Con media sonrisa enigmática, acomodándose el cabello)", "Aceptar el juego"),
                LineaDialogo("GAMER", "No esperaba encontrar a alguien con esa presencia hoy... ¿Siempre analizas a las personas con tanta atención?", "(Inclinándose suavemente hacia la cámara)", "Provocar réplica"),
                LineaDialogo("MODELO", "Solo a quienes parecen tener un misterio interesante guardado. Y presiento que tú tienes más de uno.", "(Mirada pícara)", "Tensión coqueta"),
                LineaDialogo("GAMER", "Tal vez. Pero prometí no revelar mis secretos tan rápido, a menos que prometas quedarte conmigo en este rincón.", "(Sonrisa confiada)", "Complicidad"),
                LineaDialogo("MODELO", "El tiempo dirá si te ganas ese derecho... por ahora, tienes mi atención completa. ¿Cuál es tu siguiente jugada?", "(Voz suave y pausada)", "Validación")
            ),
            temasConversacion = listOf(
                "¿Qué fue lo más inesperado o loco que te pasó hoy antes de conectarte?",
                "Si pudieras escapar ahora mismo a cualquier lugar del mundo sin consecuencias, ¿a dónde irías?",
                "¿Crees en las conexiones instantáneas a primera vista o necesitas tiempo para confiar?"
            ),
            preguntasContraataque = listOf(
                "¿Qué fue lo primero que pensaste al verme conectar la cámara?",
                "¿Eres de los que se arriesgan al primer impulso o calculas cada paso?",
                "Dime una sola palabra que describa la vibra que transmito hoy."
            ),
            replicasSugeridas = listOf(
                "«Depende de con quién hable... pero contigo me arriesgo.»",
                "«Transmites un magnetismo que da demasiada curiosidad descubrir.»",
                "«Pensé que la noche acababa de ponerse diez veces más interesante.»"
            )
        ),
        ActoEpisodio(
            numero = 2,
            titulo = "Acto 2: Preguntas Íntimas y Confesiones",
            objetivoClave = "Descubrir quién es la persona detrás de las apariencias y confesar un anhelo honesto.",
            dialogos = listOf(
                LineaDialogo("GAMER", "Dime la verdad... cuando estás sola y se apagan los reflectores, ¿qué es lo que realmente te hace falta?", "(Tono pausado y sincero)", "Profundizar"),
                LineaDialogo("MODELO", "Paz. Y alguien con quien no tenga que fingir que tengo todo bajo control todo el tiempo.", "(Bajando la mirada un instante antes de reconectar)", "Vulnerabilidad"),
                LineaDialogo("GAMER", "Entiendo esa sensación más de lo que imaginas. Es agotador tener que ponerse una armadura todos los días.", "(Gesto empático)", "Validación mutua"),
                LineaDialogo("MODELO", "¿Y tú? ¿Qué es lo que nunca le cuentas a nadie porque temes que no lo entiendan o te juzguen?", "(Mirándolo fijamente)", "Contraataque íntimo"),
                LineaDialogo("GAMER", "Que a veces solo deseo desconectar del mundo y encontrar a alguien que me mire justo como me estás mirando ahora.", "(Sinceridad directa)", "Confesión honesta"),
                LineaDialogo("MODELO", "Cuidado con lo que deseas... podrías acostumbrarte a que te mire así.", "(Sonrisa cómplice y cálida)", "Atracción mutua")
            ),
            temasConversacion = listOf(
                "Una mentira piadosa que dijiste recientemente para salir de una situación incómoda.",
                "Tu mayor sueño frustrado que aún te gustaría cumplir en secreto.",
                "¿Qué cualidad sutil es indispensable para que alguien te robe el aliento?"
            ),
            preguntasContraataque = listOf(
                "¿Qué experiencia del pasado cambió por completo tu forma de querer?",
                "¿Cuándo fue la última vez que sentiste mariposas reales en el estómago?",
                "Si tuviéramos que confesar un secreto prohibido ahora mismo, ¿empezarías tú o yo?"
            ),
            replicasSugeridas = listOf(
                "«Empiezo yo si me aseguras que serás mi cómplice sin juzgarme.»",
                "«Las mariposas vuelven cuando menos te lo esperas... por ejemplo, ahora.»",
                "«Valoro la honestidad pura por encima de cualquier fachada perfecta.»"
            )
        ),
        ActoEpisodio(
            numero = 3,
            titulo = "Acto 3: Desafío Atrevido y Clímax Romántico",
            objetivoClave = "Elevar la tensión romántica al máximo mediante un reto directo de miradas y seducción.",
            dialogos = listOf(
                LineaDialogo("GAMER", "Hagamos un pacto: diez segundos en silencio total mirándonos directo a los ojos. Quien desvíe la mirada primero, cumple un castigo.", "(Desafiante)", "Desafío de miradas"),
                LineaDialogo("MODELO", "¿Crees que vas a intimidarme tan fácil? Acepto el reto. Cuenta desde tres.", "(Postura firme y coqueta)", "Aceptar el reto"),
                LineaDialogo("GAMER", "Tres... dos... uno. (Diez segundos de mirada fija).", "(Intensidad en la mirada)", "Tensión dramática"),
                LineaDialogo("MODELO", "Casi pierdes en el segundo cinco... vi cómo dudaste por un instante.", "(Riendo suavemente)", "Juego de seducción"),
                LineaDialogo("GAMER", "No dudé. Me distraje pensando en qué harías si estuviéramos en la misma habitación ahora mismo.", "(Tono seguro y atrevido)", "Provocación directa"),
                LineaDialogo("MODELO", "Si estuviéramos en la misma habitación... no estarías haciéndome preguntas, estaríamos mucho más cerca.", "(Susurrando con una sonrisa magnética)", "Clímax de tensión")
            ),
            temasConversacion = listOf(
                "Un viaje espontáneo de fin de semana: ¿dónde nos escaparíamos sin avisarle a nadie?",
                "¿Qué detalle sutil en una persona te vuelve completamente loco/a?",
                "¿Prefieres los besos lentos bajo la lluvia o las risas cómplices a medianoche?"
            ),
            preguntasContraataque = listOf(
                "¿Qué parte de mi personalidad te ha sorprendido más desde que empezamos a hablar?",
                "¿Te atreverías a besarme en una primera cita si sintieras esta misma química?",
                "Dime algo que quieras que haga o diga en este preciso instante."
            ),
            replicasSugeridas = listOf(
                "«Tu seguridad... es lo más magnético y atractivo que he visto hoy.»",
                "«No dudaría ni un segundo si la química es tan evidente como esta.»",
                "«Acércate un poco más a la cámara para verte los ojos con más claridad.»"
            )
        ),
        ActoEpisodio(
            numero = 4,
            titulo = "Acto 4: El Pacto Secreto y Cierre",
            objetivoClave = "Sellar la complicidad con una promesa inolvidable y asegurar el próximo encuentro.",
            dialogos = listOf(
                LineaDialogo("GAMER", "La llamada terminará tarde o temprano, pero no quiero que esta vibra se quede solo atrapada en una pantalla.", "(Mirada profunda)", "Fijar recuerdo"),
                LineaDialogo("MODELO", "Coincido... hacía tiempo que una conversación no se sentía tan real, electrizante y fuera de la rutina.", "(Ternura y complicidad)", "Apertura emocional"),
                LineaDialogo("GAMER", "Prométeme una cosa: que la próxima vez que nos veamos, retomaremos la historia exactamente donde la dejamos hoy.", "(Sonrisa afectuosa)", "Pacto futuro"),
                LineaDialogo("MODELO", "Trato hecho. Pero con una condición: la próxima vez eliges tú el vino y yo el destino.", "(Gesto cómplice)", "Sellar acuerdo"),
                LineaDialogo("GAMER", "Acepto todas tus condiciones. Gracias por hacer que esta noche valiera la pena.", "(Agradecimiento honesto)", "Cierre cálido"),
                LineaDialogo("MODELO", "Gracias a ti por jugar como adultos y regalarme una sonrisa que no esperaba hoy. Hasta muy pronto.", "(Guiño cariñoso y despedida memorable)", "Despedida inolvidable")
            ),
            temasConversacion = listOf(
                "¿Cuál será nuestro primer plan cuando volvamos a llamarnos?",
                "El momento favorito de la conversación que más te hizo reír o sentir cosquillas.",
                "Un brindis virtual por haber coincidido hoy en este sueño."
            ),
            preguntasContraataque = listOf(
                "¿Te vas a quedar pensando en mí cuando colguemos la videollamada?",
                "¿Qué calificación del 1 al 10 le das a nuestra química?",
                "¿Cuándo volvemos a vernos?"
            ),
            replicasSugeridas = listOf(
                "«Un 10 rotundo, superaste cualquier expectativa que tenía.»",
                "«Definitivamente me voy a quedar pensando en esta charla toda la noche.»",
                "«Tan pronto como tu agenda y la mía coincidan... que sea pronto.»"
            )
        )
    )
}

