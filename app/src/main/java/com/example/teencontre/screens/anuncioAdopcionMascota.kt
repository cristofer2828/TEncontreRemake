package com.example.teencontre.screens // <-- Tu carpeta screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import java.util.Locale

// ✅ IMPORTACIONES DE TU CAPA DE DATOS
import com.example.teencontre.data.DatabaseHelper
import com.example.teencontre.data.MascotasAdopcionModel

// --- CONSTANTES DE DISEÑO REUTILIZABLES ---
private val FigmaBlue = Color(0xFF2196F3)
private val BorderGray = Color(0xFFCCCCCC)

// =================================================================
// COMPONENTE PRINCIPAL: WIZARD DE CREACIÓN (ADOPCIÓN)
// =================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardCrearAdopcion(onBackToSelector: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE) }

    // ✅ CONEXIÓN DE BASE DE DATOS
    val dbHelper = remember { DatabaseHelper(context) }

    var step by remember { mutableIntStateOf(1) }

    // --- ESTADOS DE CONTROL DE INTERFAZ ---
    var showPhotoSheet by remember { mutableStateOf(false) }
    var showDescSheet by remember { mutableStateOf(false) }

    // --- ESTADOS DE DATOS DE LA MASCOTA ---
    var nombreMascota by remember { mutableStateOf("") }
    var razaMascota by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Perro") }
    var gender by remember { mutableStateOf("Hembra") }
    var edadMascota by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // --- NUEVOS REQUISITOS / ATRIBUTOS EXCLUSIVOS DE LA TABLA ADOPCIÓN ---
    var vacunado by remember { mutableStateOf(false) }
    var esterilizado by remember { mutableStateOf(false) }
    var desparasitado by remember { mutableStateOf(false) }
    var tamano by remember { mutableStateOf("Mediano") }
    var temperamento by remember { mutableStateOf("Juguetón") }

    // --- ESTADOS DE CONTACTO ---
    var contactName by remember { mutableStateOf(sharedPreferences.getString("userName", "") ?: "") }
    var contactPhone by remember { mutableStateOf(sharedPreferences.getString("userPhone", "") ?: "") }
    var contactEmail by remember { mutableStateOf(sharedPreferences.getString("userEmail", "") ?: "") }
    var acceptedTerms by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step > 1 && step < 6) step-- else onBackToSelector()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Espacio superior para acomodar el círculo de forma perfecta abajo de la TopAppBar
            Spacer(modifier = Modifier.height(30.dp))

            // --- INDICADOR CIRCULAR GRANDE Y POSICIONADO MÁS ABAJO ---
            if (step < 6) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .border(width = 4.dp, color = FigmaBlue, shape = CircleShape)
                        .background(color = FigmaBlue.copy(alpha = 0.12f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PASO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaBlue,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$step/5",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = FigmaBlue
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(color = FigmaBlue.copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", fontSize = 42.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = if (step < 6) "Dar en adopción" else "¡Hecho!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )


            // --- CONTENEDOR DINÁMICO DE PASOS ---
            Box(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                when (step) {
                    1 -> PasoMascotaAdopcion(
                        nombre = nombreMascota,
                        raza = razaMascota,
                        type = petType,
                        gen = gender,
                        edad = edadMascota,
                        onNombre = { nombreMascota = it },
                        onRaza = { razaMascota = it },
                        onType = { petType = it },
                        onGen = { gender = it },
                        onEdad = { edadMascota = it }
                    )
                    2 -> PasoFotoAdopcion(
                        photos = selectedPhotos,
                        onPhotosChanged = { selectedPhotos = it }
                    )
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
                    4 -> PasoDescripcionAdopcion(
                        desc = description,
                        onDescChanged = { description = it }
                    )
                    5 -> PasoContactoAdopcion(
                        name = contactName,
                        phone = contactPhone,
                        email = contactEmail,
                        accepted = acceptedTerms,
                        onName = { contactName = it },
                        onPhone = { contactPhone = it },
                        onEmail = { contactEmail = it },
                        onAccepted = { acceptedTerms = it }
                    )
                    6 -> PantallaHechoAdopcion(onBackToSelector)
                }
            }

            // --- BOTONES DE NAVEGACIÓN ---
            if (step in 1..5) {
                Spacer(modifier = Modifier.height(32.dp))
                NavigationButtonsAdopcion(
                    step = step,
                    accepted = acceptedTerms,
                    onNext = {
                        if (step < 5) {
                            step++
                        } else {
                            // ------ LÓGICA DE GUARDADO EN SQLITE ------
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

                            val descripcionCompleta = buildString {
                                if (edadMascota.isNotBlank()) append("Edad aproximada: $edadMascota. ")
                                if (description.isNotBlank()) append(description)
                            }

                            val mascotaAdopcion = MascotasAdopcionModel(
                                id = 0,
                                especie = petType,
                                genero = gender,
                                raza = if (razaMascota.isNotBlank()) razaMascota else "Mestizo",
                                vacunado = vacunado,
                                esterilizado = esterilizado,
                                desparasitado = desparasitado,
                                tamano = tamano,
                                temperamento = temperamento,
                                foto = fotoBytes,
                                descripcion = descripcionCompleta,
                                nombreOrganizacion = if (contactName.isNotBlank()) contactName else "Particular",
                                telefono = contactPhone,
                                correo = contactEmail
                            )

                            val resultado = dbHelper.insertAdopcion(mascotaAdopcion)

                            if (resultado != -1L) {
                                android.util.Log.d("SQLITE_ADOPCION", "¡Anuncio de adopción insertado! ID: $resultado")
                            } else {
                                android.util.Log.e("SQLITE_ERROR", "Error al insertar en TABLE_ADOPCION.")
                            }

                            sharedPreferences.edit().apply {
                                putString("last_ad_name", contactName)
                                putString("last_ad_phone", contactPhone)
                                putString("last_ad_email", contactEmail)
                                putString("last_ad_status", "ADOPCION_OK")
                                apply()
                            }
                            step = 6
                        }
                    },
                    onBack = {
                        if (step > 1) step-- else onBackToSelector()
                    },
                    onOmit = {
                        if (step == 2) showPhotoSheet = true
                        if (step == 4) showDescSheet = true
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showPhotoSheet) {
        OmitirFotoAdopcionDialog(onDismiss = { showPhotoSheet = false }, onConfirm = { showPhotoSheet = false; step++ })
    }
    if (showDescSheet) {
        OmitirDescAdopcionDialog(onDismiss = { showDescSheet = false }, onConfirm = { showDescSheet = false; step++ })
    }
}

// =================================================================
// SUBPANTALLAS DEL FLUJO "ADOPCIÓN"
// =================================================================

@Composable
fun PasoMascotaAdopcion(
    nombre: String, raza: String, type: String, gen: String, edad: String,
    onNombre: (String) -> Unit, onRaza: (String) -> Unit,
    onType: (String) -> Unit, onGen: (String) -> Unit, onEdad: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Cuéntanos sobre la mascota", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("Ayuda a los futuros adoptantes a conocer a su nuevo compañero.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

        Spacer(Modifier.height(24.dp))

        Text("Nombre de la mascota (Opcional)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(
            value = nombre, onValueChange = onNombre,
            placeholder = { Text("Ej: Firulais, Pelusa o asigna 'Sin nombre'") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp), singleLine = true
        )

        Spacer(Modifier.height(16.dp))
        SelectorDobleAdopcion("Mascota", "Perro" to "Gato", type, onType)
        Spacer(Modifier.height(16.dp))
        SelectorDobleAdopcion("Género", "Hembra" to "Macho", gen, onGen)
        Spacer(Modifier.height(16.dp))

        Text("Edad aproximada", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(
            value = edad, onValueChange = onEdad,
            placeholder = { Text("Ej: 3 meses, 2 años...") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp), singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Text("Raza", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(
            value = raza, onValueChange = onRaza,
            placeholder = { Text("Ej: Mestizo, Golden Retriever...") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp), singleLine = true
        )
    }
}

@Composable
fun SelectorDobleAdopcion(label: String, opciones: Pair<String, String>, seleccionado: String, onSelect: (String) -> Unit) {
    Column {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Row(Modifier.padding(top = 8.dp), Arrangement.spacedBy(12.dp)) {
            listOf(opciones.first, opciones.second).forEach { op ->
                val isSel = seleccionado == op
                Box(
                    Modifier
                        .weight(1f).height(48.dp)
                        .border(
                            width = 1.dp,
                            color = if (isSel) FigmaBlue else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelect(op) }
                        .background(if (isSel) FigmaBlue.copy(0.12f) else Color.Transparent, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = op,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSel) FigmaBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PasoFotoAdopcion(photos: List<Uri>, onPhotosChanged: (List<Uri>) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Fotos de la mascota", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("¡Las fotos claras e iluminadas aumentan un 80% las posibilidades de adopción!", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth().height(160.dp)
                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .clickable { /* Vincular galería del teléfono */ },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if(photos.isEmpty()) "Presiona para subir fotos de la mascota" else "¡${photos.size} Imagen añadida!",
                color = FigmaBlue,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasoSaludYFisicoAdopcion(
    vacunado: Boolean, esterilizado: Boolean, desparasitado: Boolean,
    tamano: String, temperamento: String,
    onVacunadoChanged: (Boolean) -> Unit, onEsterilizadoChanged: (Boolean) -> Unit, onDesparasitadoChanged: (Boolean) -> Unit,
    onTamanoChanged: (String) -> Unit, onTemperamentoChanged: (String) -> Unit
) {
    var expandTamano by remember { mutableStateOf(false) }
    val listaTamanos = listOf("Pequeño", "Mediano", "Grande")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Estado de salud y físico", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("Especifica los cuidados médicos actuales de la mascota.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("¿Está Vacunado?", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Switch(checked = vacunado, onCheckedChange = onVacunadoChanged, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FigmaBlue))
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("¿Está Esterilizado?", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Switch(checked = esterilizado, onCheckedChange = onEsterilizadoChanged, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FigmaBlue))
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("¿Está Desparasitado?", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Switch(checked = desparasitado, onCheckedChange = onDesparasitadoChanged, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FigmaBlue))
        }

        Spacer(Modifier.height(16.dp))

        Text("Tamaño", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        ExposedDropdownMenuBox(
            expanded = expandTamano,
            onExpandedChange = { expandTamano = !expandTamano }
        ) {
            OutlinedTextField(
                value = tamano, onValueChange = {}, readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandTamano) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(expanded = expandTamano, onDismissRequest = { expandTamano = false }) {
                listaTamanos.forEach { item ->
                    DropdownMenuItem(text = { Text(item) }, onClick = { onTamanoChanged(item); expandTamano = false })
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Temperamento / Carácter", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        OutlinedTextField(
            value = temperamento, onValueChange = onTemperamentoChanged,
            placeholder = { Text("Ej: Juguetón, tranquilo, miedoso...") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp), singleLine = true
        )
    }
}

@Composable
fun PasoDescripcionAdopcion(desc: String, onDescChanged: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Personalidad e historia", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("¿Cómo se comporta con niños u otros animales? Cuéntanos su historia.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = desc, onValueChange = onDescChanged,
            placeholder = { Text("Ej: Es súper cariñoso, ideal para casas familiares, convive bien con gatos...") },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun PasoContactoAdopcion(
    name: String, phone: String, email: String, accepted: Boolean,
    onName: (String) -> Unit, onPhone: (String) -> Unit, onEmail: (String) -> Unit, onAccepted: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Tus datos de contacto", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Text("Los interesados te contactarán directamente a través de estos medios.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = onName, label = { Text("Tu Nombre o Albergue") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = phone, onValueChange = onPhone, label = { Text("Tu Teléfono") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = email, onValueChange = onEmail, label = { Text("Tu Email") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = accepted,
                onCheckedChange = onAccepted,
                colors = CheckboxDefaults.colors(checkedColor = FigmaBlue)
            )
            Text("Confirmo que doy a la mascota de forma responsable.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PantallaHechoAdopcion(onFinished: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Text(
            text = "¡Mascota publicada para adopción con éxito!\n\nGracias por darle la oportunidad de encontrar un hogar lleno de amor. Te notificaremos si alguien se interesa.",
            textAlign = TextAlign.Center, lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = onFinished,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FigmaBlue)
        ) {
            Text("Volver al Inicio", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NavigationButtonsAdopcion(step: Int, accepted: Boolean, onNext: () -> Unit, onBack: () -> Unit, onOmit: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (step > 1) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(48.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Atrás", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (step == 2 || step == 4) {
            TextButton(onClick = onOmit, modifier = Modifier.height(48.dp)) {
                Text("Omitir", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        val botonHabilitado = step != 5 || accepted
        Button(
            onClick = onNext,
            enabled = botonHabilitado,
            colors = ButtonDefaults.buttonColors(
                containerColor = if(botonHabilitado) FigmaBlue else MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.weight(1f).height(48.dp)
        ) {
            Text(
                text = if (step == 5) "Publicar" else "Siguiente",
                color = if(botonHabilitado) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OmitirFotoAdopcionDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Omitir fotos?") },
        text = { Text("Las publicaciones con fotos reciben solicitudes de adopción casi de inmediato. ¿Deseas omitirlo?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Omitir", color = FigmaBlue) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun OmitirDescAdopcionDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Omitir descripción?") },
        text = { Text("Colocar detalles sobre su comportamiento ayuda a las familias a decidirse más rápido.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Omitir", color = FigmaBlue) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}