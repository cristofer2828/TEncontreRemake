package com.example.teencontre.screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.platform.LocalLocale

// IMPORTACIONES DE TU PAQUETE DE DATOS
import com.example.teencontre.data.DatabaseHelper
import com.example.teencontre.data.MascotasEncontradasModel

// COLOR VERDE SÉPTIMO PASO (CONSERVA TU IDENTIDAD VISUAL)
private val FigmaGreen = Color(0xFF4CAF50)

// =================================================================
// COMPONENTE PRINCIPAL: WIZARD DE CREACIÓN (MASCOTA ENCONTRADA)
// =================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardEncontreAnuncio(onBackToSelector: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE) }
    val dbHelper = remember { DatabaseHelper(context) }

    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    // --- ESTADOS DE CONTROL DE INTERFAZ ---
    var showPhotoSheet by remember { mutableStateOf(false) }
    var showDescSheet by remember { mutableStateOf(false) }

    // --- ESTADOS DE DATOS DE LA MASCOTA ---
    var razaMascota by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Perro") }
    var gender by remember { mutableStateOf("Hembra") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }

    var contactName by remember { mutableStateOf(sharedPreferences.getString("userName", "") ?: "") }
    var contactPhone by remember { mutableStateOf(sharedPreferences.getString("userPhone", "") ?: "") }
    var contactEmail by remember { mutableStateOf(sharedPreferences.getString("userEmail", "") ?: "") }
    var acceptedTerms by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd/MM/yyyy", LocalLocale.current.platformLocale)

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
            // Aumentamos el espacio inicial para que el círculo quede un poco más abajo
            Spacer(modifier = Modifier.height(32.dp))

            // --- BARRA SUPERIOR (CÍRCULO MÁS GRANDE, CENTRADO Y FLECHA ADAPTABLE) ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Flecha Atrás: cambia automáticamente a blanco en modo noche
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

                // Círculo indicador de 80.dp centrado perfectamente
                if (step <= totalSteps) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(80.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            strokeWidth = 4.dp
                        )
                        CircularProgressIndicator(
                            progress = { step.toFloat() / totalSteps.toFloat() },
                            modifier = Modifier.fillMaxSize(),
                            color = FigmaGreen, // Color verde temático del flujo
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "$step/$totalSteps",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Título adaptable según modo noche/día
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
                    3 -> PasoUbicacionEncontrada(location, selectedDate, { location = it }, { selectedDate = it })
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
                NavigationButtonsEncontrada(
                    step = step,
                    accepted = acceptedTerms,
                    onNext = {
                        if (step < totalSteps) {
                            step++
                        } else {
                            var fotoBytes: ByteArray? = null
                            if (selectedPhotos.isNotEmpty()) {
                                try {
                                    val inputStream = context.contentResolver.openInputStream(selectedPhotos[0])
                                    fotoBytes = inputStream?.readBytes()
                                    inputStream?.close()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            val detallesIntroducidos = StringBuilder()
                            if (razaMascota.isNotBlank()) detallesIntroducidos.append("Raza/Rasgos: $razaMascota. ")
                            detallesIntroducidos.append(description)

                            val mascotaHallada = MascotasEncontradasModel(
                                id = 0,
                                especie = petType,
                                genero = gender,
                                foto = fotoBytes,
                                fecha = sdf.format(Date(selectedDate)),
                                lugar = location,
                                descripcion = detallesIntroducidos.toString().trim(),
                                contacto = contactName,
                                telefono = contactPhone,
                                correo = contactEmail
                            )

                            dbHelper.insertEncontrada(mascotaHallada)
                            step = 6
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

    if (showPhotoSheet) {
        OmitirFotoDialog(onDismiss = { showPhotoSheet = false }, onConfirm = { showPhotoSheet = false; step++ })
    }
    if (showDescSheet) {
        OmitirDescDialog(onDismiss = { showDescSheet = false }, onConfirm = { showDescSheet = false; step++ })
    }
}

// =================================================================
// SUBPANTALLAS DEL FLUJO "ENCONTRADA" (CON COLORES DEL TEMA)
// =================================================================

@Composable
fun PasoMascotaEncontrada(
    raza: String, type: String, gen: String,
    onRaza: (String) -> Unit, onType: (String) -> Unit, onGen: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("¿Qué mascota encontraste?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("Por favor, indique el tipo y sexo aproximado de la mascota", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

        Spacer(Modifier.height(24.dp))

        SelectorDobleEncontrada("Mascota", "Perro" to "Gato", type, onType)
        Spacer(Modifier.height(16.dp))
        SelectorDobleEncontrada("Género", "Hembra" to "Macho", gen, onGen)
        Spacer(Modifier.height(16.dp))

        Text("Raza o rasgos parecidos", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(
            value = raza, onValueChange = onRaza,
            placeholder = { Text("Ej: Cruzado, Pitbull, Siamés...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@Composable
fun SelectorDobleEncontrada(label: String, opciones: Pair<String, String>, seleccionado: String, onSelect: (String) -> Unit) {
    Column {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        Row(Modifier.padding(top = 8.dp), Arrangement.spacedBy(12.dp)) {
            listOf(opciones.first, opciones.second).forEach { op ->
                val isSel = seleccionado == op
                Box(
                    Modifier
                        .weight(1f).height(48.dp)
                        .border(1.dp, if (isSel) FigmaGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable { onSelect(op) }
                        .background(if (isSel) FigmaGreen.copy(0.12f) else Color.Transparent, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(op, color = if (isSel) FigmaGreen else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun PasoFotoEncontrada(photos: List<Uri>, onPhotosChanged: (List<Uri>) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Fotos de la mascota encontrada", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("Sube fotos del estado actual de la mascota para ayudar a su dueño.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth().height(160.dp)
                .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .clickable { /* Vincular galería */ },
            contentAlignment = Alignment.Center
        ) {
            Text(if(photos.isEmpty()) "Presiona para tomar o subir foto" else "¡${photos.size} Imagen añadida!", color = FigmaGreen, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoUbicacionEncontrada(loc: String, date: Long, onLoc: (String) -> Unit, onDate: (Long) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val state = rememberDatePickerState(initialSelectedDateMillis = date)
    val sdf = SimpleDateFormat("dd/MM/yyyy", LocalLocale.current.platformLocale)

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onDate(it) }
                    showPicker = false
                }) { Text("OK", color = FigmaGreen) }
            }
        ) { DatePicker(state = state) }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("¿Dónde lo encontraste?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("Indica la fecha exacta y zona donde se le vio o rescató.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth().padding(vertical = 4.dp).height(56.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .clickable { showPicker = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = sdf.format(Date(date)), modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onBackground)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Lugar de avistamiento", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(
            value = loc, onValueChange = onLoc,
            placeholder = { Text("Ej: Parque del Periodista, cuadra 4...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@Composable
fun PasoDescripcionEncontrada(desc: String, onDescChanged: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Detalles adicionales", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("¿Tiene collar? ¿Está herido? ¿Es manso o temeroso?", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = desc, onValueChange = onDescChanged,
            placeholder = { Text("Describe el estado de la mascota...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            shape = RoundedCornerShape(8.dp),
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

@Composable
fun PasoContactoEncontrada(
    name: String, phone: String, email: String, accepted: Boolean,
    onName: (String) -> Unit, onPhone: (String) -> Unit, onEmail: (String) -> Unit, onAccepted: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Tus datos para que el dueño te contacte", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = onName, label = { Text("Tu Nombre") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = phone, onValueChange = onPhone, label = { Text("Tu Teléfono") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = email, onValueChange = onEmail, label = { Text("Tu Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp))

        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = accepted,
                onCheckedChange = onAccepted,
                colors = CheckboxDefaults.colors(checkedColor = FigmaGreen)
            )
            Text("Acepto los términos de ayuda y protección animal.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
fun PantallaHechoEncontrada(onFinished: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
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
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FigmaGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Volver al Inicio", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// =================================================================
// COMPONENTE: BOTONES DE NAVEGACIÓN (ADAPTABLES)
// =================================================================
@Composable
fun NavigationButtonsEncontrada(step: Int, accepted: Boolean, onNext: () -> Unit, onBack: () -> Unit, onOmit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (step == 2 || step == 4) {
            TextButton(onClick = onOmit, modifier = Modifier.height(48.dp)) {
                Text("Omitir", color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
            }
        } else {
            Spacer(modifier = Modifier.width(4.dp))
        }

        val botonHabilitado = step != 5 || accepted
        Button(
            onClick = onNext,
            enabled = botonHabilitado,
            colors = ButtonDefaults.buttonColors(
                containerColor = if(botonHabilitado) FigmaGreen else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if(botonHabilitado) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (step == 5) "Publicar" else "Siguiente", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// =================================================================
// DIÁLOGOS EMERGENTES
// =================================================================
@Composable
fun OmitirFotoDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Omitir fotos?") },
        text = { Text("Subir una foto aumenta radicalmente la velocidad de reconocimiento por parte de su dueño original.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Omitir", color = FigmaGreen) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun OmitirDescDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Omitir detalles?") },
        text = { Text("Mencionar si lleva collar o algún color en específico ayuda un montón.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Omitir", color = FigmaGreen) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}