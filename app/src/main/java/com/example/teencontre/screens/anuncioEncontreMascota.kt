package com.example.teencontre.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Close
import coil.compose.rememberAsyncImagePainter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
// IMPORTACIONES DE TU PAQUETE DE DATOS
import com.example.teencontre.data.local.DatabaseHelper
import com.example.teencontre.data.remote.RetrofitClient
import com.example.teencontre.sharedprefs.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody


private val FigmaGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardEncontreAnuncio(onBackToSelector: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember {
        PreferenceManager(context)
    }

    val usuario = prefs.getLoggedUser()

    val sharedPreferences = remember { context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE) }
    val dbHelper = remember { DatabaseHelper(context) }
    val coroutineScope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    // --- ESTADOS DE CONTROL DE INTERFAZ ---
    var showPhotoSheet by remember { mutableStateOf(false) }
    var showDescSheet by remember { mutableStateOf(false) }
    var showLocationConfirmSheet by remember { mutableStateOf(false) }
    var showMapPickerEncontrado by remember { mutableStateOf(false) }

    // --- ESTADOS DE DATOS DE LA MASCOTA ---
    var razaMascota by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Perro") }
    var gender by remember { mutableStateOf("Hembra") }
    var location by remember { mutableStateOf("") }
    var ubicacionLatLng by remember { mutableStateOf<LatLng?>(null) }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var contactName by remember { mutableStateOf(sharedPreferences.getString("userName", "") ?: "") }
    var contactPhone by remember { mutableStateOf(sharedPreferences.getString("userPhone", "") ?: "") }
    var contactEmail by remember { mutableStateOf(sharedPreferences.getString("userEmail", "") ?: "") }
    var acceptedTerms by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("yyyy-MM-dd", LocalLocale.current.platformLocale)
    val apiService = RetrofitClient.instance

    // --- INTERCEPCIÓN DE PANTALLA COMPLETA PARA SELECCIONAR MAPA ---
    if (showMapPickerEncontrado) {
        SeleccionarUbicacionScreen(
            onConfirmar = { latLng ->
                ubicacionLatLng = latLng
                location = "Obteniendo ubicación..."

                coroutineScope.launch {
                    val direccion = obtenerDireccionDesdeCoordenadas(context, latLng)
                    location = direccion
                    showMapPickerEncontrado = false
                }
            },
            onBack = {
                showMapPickerEncontrado = false
            }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // --- BARRA SUPERIOR INDICADORA ---
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
                        modifier = Modifier.size(30.dp)
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
                            strokeWidth = 6.dp
                        )

                        val unCuartoDeVuelta = -90f
                        val proporcionProgreso = step.toFloat() / totalSteps.toFloat()

                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            drawArc(
                                color = FigmaGreen,
                                startAngle = unCuartoDeVuelta,
                                sweepAngle = 360f * proporcionProgreso,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 6.dp.toPx(),
                                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = if (step <= totalSteps) "Mascota encontrada" else "Hecho",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = if (step <= totalSteps) FigmaGreen else MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- CONTENEDOR DINÁMICO DE PASOS ---
            Box(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                when (step) {
                    1 -> PasoMascotaEncontrada(
                        raza = razaMascota, type = petType, gen = gender,
                        onRaza = { razaMascota = it }, onType = { petType = it }, onGen = { gender = it }
                    )
                    2 -> PasoFotoEncontrada(selectedPhotos) { selectedPhotos = it }
                    3 -> PasoUbicacionEncontrada(
                        lugar = location,
                        fecha = selectedDate,
                        selectedLatLng = ubicacionLatLng,
                        mostrarModal = showLocationConfirmSheet,
                        onLugar = { location = it },
                        onFecha = { selectedDate = it },
                        onOpenMap = { showMapPickerEncontrado = true },
                        onDireccionConfirmada = {
                            showLocationConfirmSheet = false
                            step++
                        },
                        onDismissModal = { showLocationConfirmSheet = false }
                    )
                    4 -> PasoDescripcionEncontrada(description) { description = it }
                    5 -> PasoContactoEncontrada(
                        contactName, contactPhone, contactEmail, acceptedTerms,
                        { contactName = it }, { contactPhone = it }, { contactEmail = it }, { acceptedTerms = it }
                    )
                    6 -> PantallaHechoEncontrada(onBackToSelector)
                }
            }

            // --- BOTONES DE NAVEGACIÓN ---
            if (step in 1..totalSteps) {
                Spacer(modifier = Modifier.height(40.dp))

                // === Añadido: Validación estricta por cada paso ===
                val isCurrentStepValid = when (step) {
                    1 -> true // Encontrados no tiene un campo de texto obligatorio en paso 1 (raza es opcional)
                    2 -> selectedPhotos.isNotEmpty() // 'Siguiente' requiere foto. Si no, debe usar 'Omitir'
                    3 -> ubicacionLatLng != null && location.isNotBlank() && location != "Obteniendo ubicación..." // Requiere mapa y dirección texto
                    4 -> description.isNotBlank()    // 'Siguiente' requiere texto. Si no, debe usar 'Omitir'
                    5 -> contactName.isNotBlank() && contactPhone.isNotBlank() && contactEmail.isNotBlank() // Contacto obligatorio
                    else -> false
                }

                NavigationButtonsEncontrada(
                    step = step,
                    accepted = acceptedTerms,
                    isNextEnabled = isCurrentStepValid,
                    onNext = {
                        if (step == 3) {
                            showLocationConfirmSheet = true
                        } else if (step < totalSteps) {
                            step++
                        } else {
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val textType = "text/plain".toMediaTypeOrNull()
                                    val imageType = "image/jpeg".toMediaTypeOrNull()

                                    // Extraemos el ID exacto del usuario (mismo método que en Perdidos)
                                    val idUsuarioReal = (usuario?.id ?: 0).toString()

                                    // 1. Preparación de RequestBody estructurados
                                    val idUsuarioPart = RequestBody.create(textType, idUsuarioReal)
                                    val especiePart = RequestBody.create(textType, petType)
                                    val generoPart = RequestBody.create(textType, gender)
                                    val fechaPart = RequestBody.create(textType, sdf.format(Date(selectedDate)))
                                    val lugarPart = RequestBody.create(textType, location)

                                    // Unificar rasgos descriptivos
                                    val detallesIntroducidos = StringBuilder()
                                    if (razaMascota.isNotBlank()) detallesIntroducidos.append("Raza/Rasgos: $razaMascota. ")
                                    detallesIntroducidos.append(description)
                                    val descPart = RequestBody.create(textType, detallesIntroducidos.toString().trim())

                                    val contactoPart = RequestBody.create(textType, contactName)
                                    val telefonoPart = RequestBody.create(textType, contactPhone)
                                    val correoPart = RequestBody.create(textType, contactEmail)

                                    // 2. Procesar imagen binaria real para Multipart
                                    var fotoPart: MultipartBody.Part? = null
                                    if (selectedPhotos.isNotEmpty()) {
                                        try {
                                            context.contentResolver.openInputStream(selectedPhotos[0])?.use { inputStream ->
                                                val bytes = inputStream.readBytes()
                                                val requestFile = RequestBody.create(imageType, bytes)
                                                fotoPart = MultipartBody.Part.createFormData("foto", "mascota_encontrada.jpg", requestFile)
                                            }
                                        } catch (e: Exception) {
                                            Log.e("WIZARD_UPLOAD", "Error leyendo bytes de imagen", e)
                                        }
                                    }

                                    // 3. Ejecutar llamada asíncrona Multipart
                                    val response = apiService.registrarMascotaEncontrada(
                                        idUsuarioPart, especiePart, generoPart, fotoPart,
                                        fechaPart, lugarPart, descPart, contactoPart, telefonoPart, correoPart
                                    )

                                    withContext(Dispatchers.Main) {
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            Toast.makeText(context, "Publicación creada con éxito en Azure", Toast.LENGTH_SHORT).show()
                                            step = 6
                                        } else {
                                            val errorMsg = response.body()?.error ?: response.body()?.message ?: "Código HTTP: ${response.code()}"
                                            Toast.makeText(context, "Error del servidor: $errorMsg", Toast.LENGTH_LONG).show()
                                        }
                                    }

                                } catch (e: Exception) {
                                    Log.e("WIZARD_UPLOAD_ERROR", "Error de red al conectar con Azure", e)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Error de red: No se pudo subir a Azure", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    },
                    onBack = { if (step > 1) step-- else onBackToSelector() },
                    onOmit = {
                        if (step == 2) showPhotoSheet = true
                        if (step == 4) showDescSheet = true
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- BOTTOM SHEETS DE CONTROL ---
    if (showPhotoSheet) {
        OmitirFotoDialog(onDismiss = { showPhotoSheet = false }, onConfirm = { showPhotoSheet = false; step++ })
    }
    if (showDescSheet) {
        OmitirDescDialog(onDismiss = { showDescSheet = false }, onConfirm = { showDescSheet = false; step++ })
    }
}

@Composable
fun PasoMascotaEncontrada(
    raza: String,
    type: String,
    gen: String,
    onRaza: (String) -> Unit,
    onType: (String) -> Unit,
    onGen: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "¿Qué mascota encontraste?",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Por favor, indique el tipo y sexo aproximado de la mascota",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(24.dp))

        SelectorDobleEncontrada("Mascota", "Perro" to "Gato", type, onType)
        Spacer(Modifier.height(16.dp))
        SelectorDobleEncontrada("Género", "Hembra" to "Macho", gen, onGen)
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Raza o rasgos parecidos",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = raza,
            onValueChange = onRaza,
            placeholder = {
                Text(
                    text = "Ej: Cruzado, Pitbull, Siamés...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@Composable
fun SelectorDobleEncontrada(
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
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            listOf(opciones.first, opciones.second).forEach { op ->
                val isSel = seleccionado == op
                val shape = RoundedCornerShape(10.dp)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .border(
                            width = 1.dp,
                            color = if (isSel) FigmaGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = shape
                        )
                        // Corregido el orden: Primero asignamos la forma física del contenedor
                        .clip(shape)
                        // El clickable ahora se adapta perfectamente al borde redondeado
                        .clickable(
                            role = androidx.compose.ui.semantics.Role.RadioButton
                        ) { onSelect(op) }
                        // Aplicamos el color de fondo después de registrar el click
                        .background(if (isSel) FigmaGreen.copy(alpha = 0.12f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = op,
                        color = if (isSel) FigmaGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PasoFotoEncontrada(photos: List<Uri>, onPhotosChanged: (List<Uri>) -> Unit) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onPhotosChanged(photos + uris)
        }
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Fotografía de la mascota",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Sube imágenes claras para que el dueño original pueda reconocerla rápidamente.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Foto",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))

        val boxShape = RoundedCornerShape(8.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, boxShape)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), boxShape)
                .clip(boxShape) // ← Corta el efecto de click para que no se salga de las esquinas
                .clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Añadir una foto",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
        }

        if (photos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(photos) { uri ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Preview Mascota",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onPhotosChanged(photos.filter { it != uri }) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(Color(0xCC000000), androidx.compose.foundation.shape.CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
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
fun PasoUbicacionEncontrada(
    lugar: String,
    fecha: Long,
    selectedLatLng: LatLng?,
    mostrarModal: Boolean,
    onLugar: (String) -> Unit,
    onFecha: (Long) -> Unit,
    onOpenMap: () -> Unit,
    onDireccionConfirmada: () -> Unit,
    onDismissModal: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "¿Dónde y cuándo lo encontraste?",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Indica la fecha exacta y la zona donde se le vio o rescató.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Fecha de avistamiento",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))

        val dateShape = RoundedCornerShape(8.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, dateShape)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), dateShape)
                .clip(dateShape) // Asegura que el efecto ripple no se salga de las esquinas
                .clickable { showDatePicker = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedDate = dateFormatter.format(Date(fecha))
                Text(
                    text = formattedDate,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Desplegar fecha",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Lugar de avistamiento",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onOpenMap,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = FigmaGreen
            )
        ) {
            Text("Seleccionar ubicación en el mapa", fontWeight = FontWeight.SemiBold)
        }

        if (selectedLatLng != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ubicación seleccionada correctamente",
                color = FigmaGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = lugar,
            onValueChange = onLugar,
            placeholder = {
                Text(
                    "Ej. Distrito, calle, avenidas de referencia,\nparque, lote o veterinaria",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp, max = 150.dp), // Se expande de forma elástica si escriben mucho
            shape = RoundedCornerShape(8.dp),
            singleLine = false,
            maxLines = 5,
            textStyle = TextStyle(fontSize = 16.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                focusedBorderColor = FigmaGreen,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }

    // --- DIÁLOGO PICKER DE FECHA ---
    if (showDatePicker) {
        // Usamos rememberDatePickerState pasándole una implementación de SelectableDates
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fecha,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    // Solo permite fechas menores o iguales a la fecha/hora actual
                    return utcTimeMillis <= System.currentTimeMillis()
                }

                override fun isSelectableYear(year: Int): Boolean {
                    // Opcional: puedes limitar los años si lo deseas
                    return year <= java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onFecha(it) }
                    showDatePicker = false
                }) {
                    Text("Aceptar", fontWeight = FontWeight.Bold, color = FigmaGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- MODAL BOTTOM SHEET DE CONFIRMACIÓN DE DIRECCIÓN ---
    if (mostrarModal) {
        ModalBottomSheet(
            onDismissRequest = onDismissModal,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Es la dirección correcta?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Una ubicación precisa ayuda a que el dueño original reconozca la zona y pueda ir por ella de inmediato.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDireccionConfirmada,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FigmaGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Sí, correcto", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismissModal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("No, quiero cambiar", fontWeight = FontWeight.Medium, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun PasoDescripcionEncontrada(desc: String, onDescChanged: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Detalles adicionales",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "¿Tiene collar? ¿Está herido? ¿Es manso o temeroso?",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = desc,
            onValueChange = onDescChanged,
            placeholder = {
                Text(
                    text = "Describe el estado de la mascota...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(8.dp),
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoContactoEncontrada(
    name: String,
    phone: String,
    email: String,
    accepted: Boolean,
    onName: (String) -> Unit,
    onPhone: (String) -> Unit,
    onEmail: (String) -> Unit,
    onAccepted: (Boolean) -> Unit
) {
    // Para simplificar la validación en el Wizard de 2 Checkboxes de manera sincronizada y evitar bloqueos,
    // usamos dos estados locales pero vinculados directamente con el callback de salida.
    var terminosAceptados by remember { mutableStateOf(accepted) }
    var datosPublicosAceptados by remember { mutableStateOf(accepted) }
    var mostrarModalTerminos by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Información de contacto",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "¿Cómo te contactará el dueño legítimo para recuperar a la mascota?",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(24.dp))

        Text(
            text = "Nombre de contacto",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = name,
            onValueChange = onName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Teléfono / Celular",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = phone,
            onValueChange = onPhone,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), // Filtro de teclado numérico
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Correo electrónico",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = email,
            onValueChange = onEmail,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // Filtro de teclado de Email
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(Modifier.height(20.dp))

        // --- CHECKBOX 1: DATOS PÚBLICOS ---
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = datosPublicosAceptados,
                onCheckedChange = { nuevoValor ->
                    datosPublicosAceptados = nuevoValor
                    onAccepted(nuevoValor && terminosAceptados)
                },
                colors = CheckboxDefaults.colors(checkedColor = FigmaGreen)
            )
            Text(
                text = "Acepto que mis datos se publiquen con fines de ayuda animal.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        // --- CHECKBOX 2: TÉRMINOS DE USUARIO ---
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = terminosAceptados,
                onCheckedChange = { nuevoValor ->
                    terminosAceptados = nuevoValor
                    onAccepted(datosPublicosAceptados && nuevoValor)
                },
                colors = CheckboxDefaults.colors(checkedColor = FigmaGreen)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = "Acepto los ",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Términos de Usuario",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FigmaGreen,
                    modifier = Modifier.clickable { mostrarModalTerminos = true }
                )
                Text(
                    text = ".",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // --- VENTANA EMERGENTE CENTRADA (DIALOG CLÁSICO) ---
    if (mostrarModalTerminos) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { mostrarModalTerminos = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Términos de Usuario",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { mostrarModalTerminos = false },
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

                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "1. USO RESPONSABLE: Esta plataforma es exclusivamente para facilitar la adopción y el reencuentro de mascotas.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = "2. DATOS PERSONALES: Al registrarte, aceptas que tus datos de contacto sean visibles para otros usuarios cuando reportes o busques una mascota.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = "3. PROHIBICIONES: Está estrictamente prohibido lucrar o vender animales a través de esta aplicación.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = "4. COMUNIDAD: Nos reservamos el derecho de eliminar cuentas que realicen reportes falsos.",
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
fun PantallaHechoEncontrada(onFinished: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Éxito",
            tint = FigmaGreen,
            modifier = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "¡Anuncio de avistamiento publicado!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Gracias por reportarlo. El dueño o la comunidad que busca a este pequeño podrá ver tu información para coordinar el reencuentro de inmediato.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onFinished,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FigmaGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Volver al Inicio", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// =================================================================
// COMPONENTE: BOTONES DE NAVEGACIÓN (CORREGIDOS ESTILO FIGMA)
// =================================================================
@Composable
fun NavigationButtonsEncontrada(
    step: Int,
    accepted: Boolean,
    isNextEnabled: Boolean, // <--- Añadido para controlar el flujo por pasos
    onNext: () -> Unit,
    onBack: () -> Unit,
    onOmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Combina que el paso sea válido y que se acepten los términos en el paso 5
        val botonHabilitado = isNextEnabled && (step != 5 || accepted)

        Button(
            onClick = onNext,
            enabled = botonHabilitado,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (botonHabilitado) FigmaGreen else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (botonHabilitado) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (step == 5) "Publicar" else "Siguiente", // Cambiado "Próximo" por "Siguiente"
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (step == 2 || step == 4) {
            val omitShape = RoundedCornerShape(8.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(omitShape) // ← Recorta el ripple para que respete los bordes redondeados
                    .clickable { onOmit() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Omitir",
                    color = FigmaGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// =================================================================
// DIÁLOGOS EMERGENTES TIPO BOTTOM SHEET (DESGLOZAN DESDE ABAJO)
// =================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmitirFotoDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp),
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
                text = "Subir una foto aumenta radicalmente la velocidad de reconocimiento por parte de su dueño original.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FigmaGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Añadir foto", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Omitir de todas formas",
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmitirDescDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Omitir detalles?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Mencionar si lleva collar o algún color en específico ayuda un montón a identificarlo.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FigmaGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Escribir detalles", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Omitir de todas formas",
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmarUbicacionDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Es correcta la dirección?",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Es importante para que el dueño sepa dónde buscar.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FigmaGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sí, cierto", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "No, quiero cambiar",
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }
    }
}