package com.example.teencontre.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.teencontre.data.local.DatabaseHelper
import com.example.teencontre.data.model.MascotasAdopcionModel
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.teencontre.data.model.BaseUser
import com.example.teencontre.data.model.Organizacion
import com.example.teencontre.data.model.Usuario
import com.example.teencontre.data.remote.RetrofitClient
import com.example.teencontre.sharedprefs.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

private val FigmaBlue = Color(0xFF2196F3)

// =================================================================
// COMPONENTE PRINCIPAL: WIZARD DE CREACIÓN (ADOPCIÓN)
// =================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardCrearAdopcion(onBackToSelector: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    val usuario = prefs.getLoggedUser()
    LaunchedEffect(Unit) {
        Log.d("LOGIN", "Usuario = $usuario")

        if (usuario is Organizacion) {
            Log.d("LOGIN", "NombreOrg = ${usuario.nombreOrg}")
            Log.d("LOGIN", "Telefono = ${usuario.telefono}")
            Log.d("LOGIN", "Correo = ${usuario.email}")
        }
    }

    val sharedPreferences = remember { context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE) }
    val dbHelper = remember { DatabaseHelper(context) }
    val coroutineScope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    // --- ESTADOS DE DATOS DE LA MASCOTA ---
    var nombreMascota by rememberSaveable { mutableStateOf("") }
    var razaMascota by rememberSaveable { mutableStateOf("") }
    var petType by rememberSaveable { mutableStateOf("Perro") }
    var gender by rememberSaveable { mutableStateOf("Hembra") }
    var edadMascota by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }

// --- ATRIBUTOS EXCLUSIVOS DE ADOPCIÓN (CON MEMORIA AL RETROCEDER) ---
    var vacunado by rememberSaveable { mutableStateOf(false) }
    var esterilizado by rememberSaveable { mutableStateOf(false) }
    var desparasitado by rememberSaveable { mutableStateOf(false) }
    var tamano by rememberSaveable { mutableStateOf("Mediano") }
    var temperamento by rememberSaveable { mutableStateOf("Juguetón") }

    val usuarioActual = usuario as? BaseUser

// Extraemos el Nombre (mapeando según la clase)
    var contactName by remember(usuarioActual) {
        mutableStateOf(
            when (usuarioActual) {
                is Usuario -> usuarioActual.nombre
                is Organizacion -> usuarioActual.nombreOrg
                else -> ""
            }
        )
    }

// 🔥 ¡AHORA SÍ! Ambos tienen teléfono asignado directamente
    var contactPhone by remember(usuarioActual) {
        mutableStateOf(
            when (usuarioActual) {
                is Usuario -> usuarioActual.telefono
                is Organizacion -> usuarioActual.telefono
                else -> ""
            }
        )
    }

// Ambos heredan el email de BaseUser
    var contactEmail by remember(usuarioActual) {
        mutableStateOf(usuarioActual?.email ?: "")
    }
    var ubicacionPublicacion by remember(usuarioActual) {

        mutableStateOf(
            when(usuarioActual) {

                is Organizacion -> {
                    if(usuarioActual.esVerificada) {
                        usuarioActual.direccion
                    } else {
                        ""
                    }
                }

                else -> ""
            }
        )

    }
    var acceptedTerms by remember { mutableStateOf(false) }

    val apiService = RetrofitClient.instance

    // =================================================================
    // VALIDACIÓN DINÁMICA DEL PASO ACTUAL
    // =================================================================
    val isCurrentStepValid = remember(
        step, nombreMascota, edadMascota, selectedPhotos, vacunado, desparasitado, esterilizado,
        description, contactName, contactPhone, contactEmail, acceptedTerms
    ) {
        when (step) {
            // 🌟 CORRECCIÓN: Quitamos 'nombreMascota.isNotBlank()' para que sea opcional.
            // Ahora el botón Siguiente se habilitará aunque el nombre esté vacío, bastando solo la edad.
            1 -> edadMascota.isNotBlank()

            2 -> selectedPhotos.isNotEmpty()
            3 -> true
            4 -> description.isNotBlank()
            5 -> contactName.isNotBlank() && contactPhone.isNotBlank() && contactEmail.isNotBlank() && acceptedTerms
            else -> true
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (step in 1..totalSteps) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 24.dp, top = 8.dp)
                    ) {
                        NavigationButtonsAdopcion(
                            step = step,
                            isButtonEnabled = isCurrentStepValid, // Pasamos la validación calculada arriba
                            onNext = {
                                if (step < totalSteps) {
                                    step++
                                } else {
                                    // Lanzamiento de la Corrutina para subir la Adopción
                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            val textType = "text/plain".toMediaTypeOrNull()
                                            val imageType = "image/jpeg".toMediaTypeOrNull()

                                            val idUsuarioReal = (usuario?.id ?: 0).toString()

                                            val descripcionCompleta = buildString {
                                                if (edadMascota.isNotBlank()) append("Edad aproximada: $edadMascota. ")
                                                if (description.isNotBlank()) append(description)
                                            }.trim()

                                            val idUsuarioPart = RequestBody.create(textType, idUsuarioReal)
                                            val nombreMascotaPart = RequestBody.create(textType, nombreMascota)
                                            val especiePart = RequestBody.create(textType, petType)
                                            val generoPart = RequestBody.create(textType, gender)
                                            val razaPart = RequestBody.create(textType, if (razaMascota.isNotBlank()) razaMascota else "Mestizo")

                                            val vacunadoPart = RequestBody.create(textType, vacunado.toString())
                                            val esterilizadoPart = RequestBody.create(textType, esterilizado.toString())
                                            val desparasitadoPart = RequestBody.create(textType, desparasitado.toString())

                                            val tamanoPart = RequestBody.create(textType, tamano)
                                            val temperamentoPart = RequestBody.create(textType, temperamento)
                                            val descPart = RequestBody.create(textType, descripcionCompleta)
                                            Log.d("SUBIR_ADOPCION", "contactName = '$contactName'")
                                            val nombreOrgPart = RequestBody.create(textType, if (contactName.isNotBlank()) contactName else "Particular")
                                            val telefonoPart = RequestBody.create(textType, contactPhone)
                                            val correoPart = RequestBody.create(textType, contactEmail)
                                            val ubicacionPart = RequestBody.create(
                                                textType,
                                                ubicacionPublicacion
                                            )
                                            var fotoPart: MultipartBody.Part? = null
                                            var fotoBytesParaSQLite: ByteArray? = null

                                            if (selectedPhotos.isNotEmpty()) {
                                                try {
                                                    context.contentResolver.openInputStream(selectedPhotos[0])?.use { inputStream ->
                                                        val bytes = inputStream.readBytes()
                                                        fotoBytesParaSQLite = bytes

                                                        val requestFile = RequestBody.create(imageType, bytes)
                                                        fotoPart = MultipartBody.Part.createFormData("foto", "mascota_adopcion.jpg", requestFile)
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("WIZARD_ADOPCION", "Error procesando bytes de imagen", e)
                                                }
                                            }

                                            val response = apiService.registrarMascotaAdopcion(
                                                idUsuarioPart,
                                                nombreMascotaPart,
                                                especiePart,
                                                generoPart,
                                                razaPart,
                                                vacunadoPart,
                                                esterilizadoPart,
                                                desparasitadoPart,
                                                tamanoPart,
                                                temperamentoPart,
                                                fotoPart,
                                                descPart,
                                                nombreOrgPart,
                                                telefonoPart,
                                                correoPart,
                                                ubicacionPart
                                            )

                                            withContext(Dispatchers.Main) {
                                                if (response.isSuccessful && response.body()?.success == true) {

                                                    // ✅ ADAPTACIÓN SQLITE: Convertimos tus bytes intactos a String en línea para tu nuevo modelo
                                                    val fotoStringParaModel = fotoBytesParaSQLite?.let { bytes ->
                                                        android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                                                    } ?: ""

                                                    val mascotaAdopcionLocal = MascotasAdopcionModel(
                                                        id = 0,
                                                        idUsuario = usuario?.id ?: 0,
                                                        nombreMascota = nombreMascota,
                                                        especie = petType,
                                                        genero = gender,
                                                        raza = if (razaMascota.isNotBlank()) razaMascota else "Mestizo",
                                                        vacunado = vacunado,
                                                        esterilizado = esterilizado,
                                                        desparasitado = desparasitado,
                                                        tamano = tamano,
                                                        temperamento = temperamento,
                                                        foto = fotoStringParaModel,
                                                        descripcion = descripcionCompleta,
                                                        nombreOrganizacion = contactName,
                                                        telefono = contactPhone,
                                                        correo = contactEmail,
                                                        ubicacion = ubicacionPublicacion
                                                    )
                                                    dbHelper.insertAdopcion(mascotaAdopcionLocal)


                                                    Toast.makeText(context, "Publicación de adopción creada en Azure con éxito", Toast.LENGTH_SHORT).show()
                                                    step = 6
                                                } else {
                                                    val errorMsg = response.body()?.error ?: response.body()?.message ?: "Código HTTP: ${response.code()}"
                                                    Toast.makeText(context, "Error del servidor: $errorMsg", Toast.LENGTH_LONG).show()
                                                }
                                            }

                                        } catch (e: Exception) {
                                            Log.e("WIZARD_ADOPCION_ERROR", "Error de red al conectar con Azure", e)
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Error de red: No se pudo subir a Azure", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                            },

                            onBack = { if (step > 1) step-- else onBackToSelector() }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- BARRA SUPERIOR CON PROGRESO EN AZUL ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { if (step > 1 && step <= totalSteps) step-- else onBackToSelector() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }

                if (step <= totalSteps) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(130.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            strokeWidth = 5.dp
                        )

                        val unCuartoDeVuelta = -90f
                        val proporcionProgreso = step.toFloat() / totalSteps.toFloat()

                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            drawArc(
                                color = FigmaBlue,
                                startAngle = unCuartoDeVuelta,
                                sweepAngle = 360f * proporcionProgreso,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 5.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (step <= totalSteps) "Dar en adopción" else "Hecho",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = if (step <= totalSteps) FigmaBlue else MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- CONTENIDO DEL FORMULARIO CON SCROLL SEGURO ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (step) {
                    1 -> PasoMascotaAdopcion(
                        nombre = nombreMascota, raza = razaMascota, type = petType, gen = gender, edad = edadMascota,
                        onNombre = { nombreMascota = it }, onRaza = { razaMascota = it }, onType = { petType = it }, onGen = { gender = it }, onEdad = { edadMascota = it }
                    )
                    2 -> PasoFotoAdopcion(selectedPhotos) { selectedPhotos = it }
                    3 -> PasoSaludYFisicoAdopcion(
                        vacunado = vacunado,
                        esterilizado = esterilizado,
                        desparasitado = desparasitado,
                        tamano = tamano,
                        temperamento = temperamento,
                        onVacunadoChanged = { vacunado = it },
                        onEsterilizadoChanged = { esterilizado = it },
                        onDesparasitadoChanged = { desparasitado = it },
                        onTamanoChanged = { tamano = it },
                        onTemperamentoChanged = { temperamento = it }
                    )

                    4 -> PasoDescripcionAdopcion(description) { description = it }
                    5 -> PasoContactoAdopcion(
                        name = contactName,
                        phone = contactPhone,
                        email = contactEmail,
                        accepted = acceptedTerms,
                        onAccepted = { acceptedTerms = it }
                    )
                    6 -> PantallaHechoAdopcion(onBackToSelector)
                }
            }
        }
    }
}

// =================================================================
// SUBPANTALLAS DEL FLUJO "ADOPCIÓN"
// =================================================================

@Composable
fun PasoMascotaAdopcion(
    nombre: String,
    raza: String,
    type: String,
    gen: String,
    edad: String,
    onNombre: (String) -> Unit,
    onRaza: (String) -> Unit,
    onType: (String) -> Unit,
    onGen: (String) -> Unit,
    onEdad: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Cuéntanos sobre la mascota",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Ayuda a los futuros adoptantes a conocer a su nuevo compañero.",

            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        Text(
            text = "Usa un nombre temporal atractivo para aumentar sus posibilidades de adopción.",

            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
                text = "Nombre de la mascota Temporal (opcional)",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = nombre,
            onValueChange = onNombre,
            placeholder = { Text("Ej: Firulais, Pelusa o 'Sin nombre'", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = FigmaBlue
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
        SelectorDobleAdopcion(label = "Mascota", opciones = "Perro" to "Gato", seleccionado = type, onSelect = onType)
        Spacer(modifier = Modifier.height(16.dp))
        SelectorDobleAdopcion(label = "Género", opciones = "Hembra" to "Macho", seleccionado = gen, onSelect = onGen)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Edad aproximada",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = edad,
            onValueChange = onEdad,
            placeholder = { Text("Ej: 3 meses, 2 años...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = FigmaBlue
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Raza",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = raza,
            onValueChange = onRaza,
            placeholder = { Text("Ej: Mestizo, Golden Retriever...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = FigmaBlue
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SelectorDobleAdopcion(
    label: String,
    opciones: Pair<String, String>,
    seleccionado: String,
    onSelect: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(opciones.first, opciones.second).forEach { op ->
                val isSel = seleccionado == op
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(
                            width = if (isSel) 2.dp else 1.dp,
                            color = if (isSel) FigmaBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelect(op) }
                        .background(if (isSel) FigmaBlue.copy(0.12f) else Color.Transparent, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = op,
                        color = if (isSel) FigmaBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PasoFotoAdopcion(
    photos: List<Uri>,
    onPhotosChanged: (List<Uri>) -> Unit
) {
    // 1. Obtenemos el contexto requerido por la función 'comprimirImagenGaleria'
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            // 2. COMPRESIÓN: Interceptamos las URIs originales y las procesamos en la caché local
            val procesadas = uris.mapNotNull { uri ->
                comprimirImagenGaleria(context, uri)
            }
            if (procesadas.isNotEmpty()) {
                onPhotosChanged(photos + procesadas)
            }
        }
    }

    // Mantenemos la columna limpia sin agregarle verticalScroll para no romper tu Wizard padre
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Fotos de la mascota",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "¡Las fotos claras e iluminadas aumentan las posibilidades de adopción!",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Foto",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Botón Añadir Foto estilizado con FigmaBlue ---
        val boxShape = RoundedCornerShape(8.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, boxShape)
                .border(1.dp, FigmaBlue.copy(alpha = 0.4f), boxShape)
                .clip(boxShape) // Mantiene el ripple dentro de las esquinas redondeadas
                .clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Añadir una foto",
                color = FigmaBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // --- RESPONSIVIDAD: Carrusel de fotos debajo del botón ---
        if (photos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            // Fijar la altura aquí es la clave para que sea responsivo dentro de scrolls padres
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp) // ← CLAVE RESPONSIVA: Evita errores de medición infinita
            ) {
                items(photos) { uri ->
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onPhotosChanged(photos.filter { it != uri }) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(20.dp)
                                .background(Color(0xCC000000), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoSaludYFisicoAdopcion(
    vacunado: Boolean,
    esterilizado: Boolean,
    desparasitado: Boolean,
    tamano: String,
    temperamento: String,
    onVacunadoChanged: (Boolean) -> Unit,
    onEsterilizadoChanged: (Boolean) -> Unit,
    onDesparasitadoChanged: (Boolean) -> Unit,
    onTamanoChanged: (String) -> Unit,
    onTemperamentoChanged: (String) -> Unit
) {
    var expandTamano by remember { mutableStateOf(false) }
    val listaTamanos = listOf("Pequeño", "Mediano", "Grande")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Estado de salud y físico",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Especifica los cuidados médicos actuales de la mascota.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- FILA: VACUNADO ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("¿Está Vacunado?", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Switch(
                checked = vacunado,
                onCheckedChange = onVacunadoChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = FigmaBlue,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        // --- FILA: ESTERILIZADO ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("¿Está Esterilizado?", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Switch(
                checked = esterilizado,
                onCheckedChange = onEsterilizadoChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = FigmaBlue,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        // --- FILA: DESPARASITADO ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("¿Está Desparasitado?", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Switch(
                checked = desparasitado,
                onCheckedChange = onDesparasitadoChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = FigmaBlue,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Tamaño", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)

        // --- MENÚ DESPLEGABLE DE TAMAÑO ---
        ExposedDropdownMenuBox(
            expanded = expandTamano,
            onExpandedChange = { expandTamano = !expandTamano }
        ) {
            OutlinedTextField(
                value = tamano,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandTamano) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = FigmaBlue
                )
            )
            ExposedDropdownMenu(
                expanded = expandTamano,
                onDismissRequest = { expandTamano = false }
            ) {
                listaTamanos.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            onTamanoChanged(item)
                            expandTamano = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Temperamento / Carácter", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(
            value = temperamento,
            onValueChange = onTemperamentoChanged,
            placeholder = { Text("Ej: Juguetón, tranquilo, miedoso...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = FigmaBlue
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PasoDescripcionAdopcion(desc: String, onDescChanged: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Personalidad e historia",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "¿Cómo se comporta con niños u otros animales? Cuéntanos su historia.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = desc,
            onValueChange = onDescChanged,
            placeholder = { Text("Ej: Es súper cariñoso, ideal para casas familiares, convive bien con gatos...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            shape = RoundedCornerShape(8.dp),
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = FigmaBlue
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoContactoAdopcion(
    name: String,
    phone: String,
    email: String,
    accepted: Boolean,
    onAccepted: (Boolean) -> Unit
) {
    var terminosAceptados by remember { mutableStateOf(false) }
    var datosPublicosAceptados by remember { mutableStateOf(accepted) }
    var mostrarVentanaTerminos by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Tus datos de contacto",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Los interesados te contactarán directamente a través de estos medios.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Los siguientes datos se mostrarán públicamente en tu publicación para que las personas interesadas en adoptar puedan comunicarse contigo.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Column {
                    Text(
                        text = "Nombre o Albergue",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FigmaBlue
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = name,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider()

                Column {
                    Text(
                        text = "Teléfono",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FigmaBlue
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = phone,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider()

                Column {
                    Text(
                        text = "Correo electrónico",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FigmaBlue
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = email,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Spacer(modifier = Modifier.height(20.dp))

        // --- CHECKBOX 1: ADOPCIÓN RESPONSABLE ---
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = datosPublicosAceptados,
                onCheckedChange = { nuevoValor ->
                    datosPublicosAceptados = nuevoValor
                    onAccepted(nuevoValor && terminosAceptados)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = FigmaBlue,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.offset(y = (-4).dp)
            )
            Text(
                text = "Confirmo que doy a la mascota de forma responsable.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp),
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- CHECKBOX 2: TÉRMINOS DE USUARIO ---
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = terminosAceptados,
                onCheckedChange = { nuevoValor ->
                    terminosAceptados = nuevoValor
                    onAccepted(datosPublicosAceptados && nuevoValor)
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = FigmaBlue,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.offset(y = (-4).dp)
            )
            Row(modifier = Modifier.padding(start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Acepto los ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "Términos de organización",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FigmaBlue,
                    modifier = Modifier.clickable { mostrarVentanaTerminos = true }
                )
                Text(".", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // --- DIÁLOGO EMERGENTE DE TÉRMINOS ---
    if (mostrarVentanaTerminos) {
        BasicAlertDialog(
            onDismissRequest = { mostrarVentanaTerminos = false },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp // Le da una ligera elevación visual elegante
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Encabezado fijo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Términos para organizaciones",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { mostrarVentanaTerminos = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Contenedor con Scroll e instrucciones optimizadas para evitar overflow
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false) // Limita el alto máximo si el texto es muy largo
                            .verticalScroll(rememberScrollState()) // Activa la barra de scroll vertical
                            .padding(end = 4.dp) // Pequeño margen para que el texto no choque visualmente con el borde al scrollear
                    ) {
                        Text(
                            text = "1. VERIFICACIÓN DE IDENTIDAD: Para registrar tu centro de adopción, es obligatorio proporcionar un Registro Único de Contribuyentes (RUC) válido y activo que acredite la existencia legal de tu organización.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Text(
                            text = "2. PLAZO DE APROBACIÓN: Nuestro equipo validará los datos institucionales en un plazo máximo de 7 días hábiles. Tu perfil permanecerá en estado de revisión hasta completar con éxito este filtro de seguridad.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Text(
                            text = "3. COMPROMISO Y ACTUALIZACIÓN: Te comprometes a velar por el bienestar físico y emocional de los animales publicados. Es tu responsabilidad marcar como 'Adoptado' cada perfil una vez que el proceso culmine para mantener la plataforma limpia.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Text(
                            text = "4. PROTECCIÓN DE DATOS: La dirección física y el RUC de la organización se almacenarán de forma segura. Estos datos serán visibles en la plataforma únicamente con fines de transparencia y confianza para los adoptantes.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Text(
                            text = "5. CUOTAS DE RECUPERACIÓN Y NO COMERCIALIZACIÓN: Queda estrictamente prohibida la venta de animales. Las cuotas de recuperación solicitadas por la organización deben ser transparentes, razonables y destinadas exclusivamente a cubrir gastos médicos, de esterilización o alimentación previamente justificados.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Text(
                            text = "6. FILTRO DE ADOPTANTES Y RESPONSABILIDAD: La organización es la única responsable de evaluar, entrevistar y seleccionar a los adoptantes finales, así como de realizar el seguimiento post-adopción. La aplicación no interviene en la decisión final ni se hace responsable por problemas derivados del acuerdo de adopción.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaHechoAdopcion(onFinished: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = FigmaBlue,
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¡Anuncio publicado!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Gracias por dar una segunda oportunidad. La comunidad interesada ya puede ver tu publicación.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onFinished,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FigmaBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Volver al Inicio", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NavigationButtonsAdopcion(
    step: Int,
    isButtonEnabled: Boolean, // Ahora recibe directamente si cumple los requisitos
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // ← SOLUCIÓN DEFINITIVA: Sube el botón por encima de la barra de Android
            .padding(bottom = 12.dp), // Espacio extra de seguridad para que no quede pegado al ras
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onNext,
            enabled = isButtonEnabled, // Controla el estado del botón aquí
            colors = ButtonDefaults.buttonColors(
                containerColor = FigmaBlue,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (step == 5) "Publicar" else "Próximo",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmitirFotoAdopcionDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Omitir fotos?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Las publicaciones con fotos reciben solicitudes de adopción casi de inmediato.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FigmaBlue),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Añadir foto", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Omitir de todas formas",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmitirDescAdopcionDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Omitir descripción?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Colocar detalles sobre su comportamiento ayuda a las familias a decidirse más rápido.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FigmaBlue),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Escribir detalles", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Omitir de todas formas",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}