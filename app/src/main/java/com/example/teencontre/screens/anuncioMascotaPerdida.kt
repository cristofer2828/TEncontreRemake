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

// IMPORTACIONES DE TU CONFIGURACIÓN DE BASE DE DATOS
import com.example.teencontre.data.DatabaseHelper
import com.example.teencontre.data.MascotasPerdidasModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardCrearAnuncio(onBackToSelector: () -> Unit) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE) }
    val dbHelper = remember { DatabaseHelper(context) }

    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    // --- ESTADOS DE LOS DATOS RECOGIDOS ---
    var nombreMascota by remember { mutableStateOf("") }
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

    // Estados para controlar los Bottom Sheets de omisión
    var showOmitirFotoSheet by remember { mutableStateOf(false) }
    var showOmitirDescSheet by remember { mutableStateOf(false) }

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
            Spacer(modifier = Modifier.height(20.dp))

            // --- BARRA SUPERIOR (CÍRCULO GRANDE Y CENTRADO + FLECHA FLOTANTE ADAPTABLE) ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Flecha Atrás (Posicionada al inicio del Box y adaptada al Modo Noche)
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

                // Indicador circular de progreso aumentado a 80.dp según el diseño Figma
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
                            color = MaterialTheme.colorScheme.primary,
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

            // Título de la sección
            Text(
                text = if (step <= totalSteps) "Mascota perdida" else "Hecho",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- CONTENEDOR DINÁMICO DE PASOS ---
            Box(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                when (step) {
                    1 -> PasoMascota(
                        nombre = nombreMascota, raza = razaMascota, type = petType, gen = gender,
                        onNombre = { nombreMascota = it }, onRaza = { razaMascota = it },
                        onType = { petType = it }, onGen = { gender = it }
                    )
                    2 -> PasoFoto(selectedPhotos) { selectedPhotos = it }
                    3 -> PasoUbicacion(location, selectedDate, { location = it }, { selectedDate = it })
                    4 -> PasoDescripcion(description) { description = it }
                    5 -> PasoContacto(
                        contactName, contactPhone, contactEmail, acceptedTerms,
                        { contactName = it }, { contactPhone = it }, { contactEmail = it }, { acceptedTerms = it }
                    )
                    6 -> PantallaHecho(onBackToSelector)
                }
            }

            // --- BOTONES DE NAVEGACIÓN ---
            if (step in 1..totalSteps) {
                Spacer(modifier = Modifier.height(40.dp))
                NavigationButtons(
                    step = step,
                    accepted = acceptedTerms,
                    onNext = {
                        if (step < totalSteps) {
                            step++
                        } else {
                            // Proceso de guardado en SQLite al presionar Publicar
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

                            val mascotaReportada = MascotasPerdidasModel(
                                id = 0,
                                nombreM = nombreMascota,
                                especie = petType,
                                genero = gender,
                                raza = razaMascota,
                                foto = fotoBytes,
                                fecha = sdf.format(Date(selectedDate)),
                                lugar = location,
                                descripcion = description,
                                contacto = contactName,
                                telefono = contactPhone,
                                correo = contactEmail
                            )

                            dbHelper.insertPerdido(mascotaReportada)
                            step = 6
                        }
                    },
                    onBack = { if (step > 1) step-- else onBackToSelector() },
                    onOmit = {
                        if (step == 2) showOmitirFotoSheet = true
                        if (step == 4) showOmitirDescSheet = true
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- DESPLIEGUE DE HOJAS EMERGENTES DE OMISIÓN ---
    if (showOmitirFotoSheet) {
        OmitirFotoSheet(
            onDismiss = { showOmitirFotoSheet = false },
            onConfirm = {
                showOmitirFotoSheet = false
                step++
            }
        )
    }

    if (showOmitirDescSheet) {
        OmitirDescripcionSheet(
            onDismiss = { showOmitirDescSheet = false },
            onConfirm = {
                showOmitirDescSheet = false
                step++
            }
        )
    }
}

// =================================================================
// COMPONENTE: BOTONES DE NAVEGACIÓN
// =================================================================
@Composable
fun NavigationButtons(step: Int, accepted: Boolean, onNext: () -> Unit, onBack: () -> Unit, onOmit: () -> Unit) {
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
                containerColor = if (botonHabilitado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (botonHabilitado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.weight(1f).height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (step == 5) "Publicar" else "Siguiente", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// =================================================================
// SUBPANTALLAS DEL WIZARD (DIFERENTES PASOS)
// =================================================================

@Composable
fun PasoMascota(
    nombre: String, raza: String, type: String, gen: String,
    onNombre: (String) -> Unit, onRaza: (String) -> Unit,
    onType: (String) -> Unit, onGen: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("¿Qué mascota es?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("Por favor, indique el tipo y sexo de su mascota", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)

        Spacer(Modifier.height(24.dp))

        Text("Nombre de la mascota", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(
            value = nombre, onValueChange = onNombre,
            placeholder = { Text("Nombre", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(Modifier.height(12.dp))
        SelectorDoble("Mascota", "Perro" to "Gato", type, onType)
        Spacer(Modifier.height(12.dp))
        SelectorDoble("Género", "Hembra" to "Macho", gen, onGen)
        Spacer(Modifier.height(12.dp))

        Text("Raza", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(
            value = raza, onValueChange = onRaza,
            placeholder = { Text("Raza de la mascota", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
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
fun SelectorDoble(label: String, opciones: Pair<String, String>, seleccionado: String, onSelect: (String) -> Unit) {
    Column {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        Row(Modifier.padding(top = 8.dp), Arrangement.spacedBy(12.dp)) {
            listOf(opciones.first, opciones.second).forEach { op ->
                val isSel = seleccionado == op
                Box(
                    Modifier
                        .weight(1f).height(48.dp)
                        .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable { onSelect(op) }
                        .background(if (isSel) MaterialTheme.colorScheme.primary.copy(0.12f) else Color.Transparent, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(op, color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun PasoFoto(photos: List<Uri>, onPhotosChanged: (List<Uri>) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Fotos de la mascota", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("Añada imágenes claras para facilitar la identificación.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth().height(160.dp)
                .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .clickable { /* Aquí integras tu FilePicker o selector de fotos habitual */ },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (photos.isEmpty()) "Presiona para añadir fotos" else "¡${photos.size} Foto(s) Seleccionada(s)!",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun PasoUbicacion(lugar: String, fecha: Long, onLugar: (String) -> Unit, onFecha: (Long) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("¿Dónde y cuándo se perdió?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("Ayuda a delimitar la zona de búsqueda", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))

        Text("Última ubicación conocida", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(
            value = lugar, onValueChange = onLugar,
            placeholder = { Text("Ej. Distrito, parque, avenidas de referencia", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(Modifier.height(16.dp))

        Text("Fecha de extravío", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        Box(
            modifier = Modifier
                .fillMaxWidth().padding(vertical = 4.dp).height(56.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .clickable { /* MostrarDatePicker */ }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val formattedDate = SimpleDateFormat("dd/MM/yyyy", LocalLocale.current.platformLocale).format(Date(fecha))
            Text(formattedDate, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
fun PasoDescripcion(descripcion: String, onDescripcion: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Descripción adicional", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("Describe características particulares (collares, cicatrices, temperamento)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = descripcion, onValueChange = onDescripcion,
            placeholder = { Text("Ej: Lleva un collar rojo. Es asustadiza pero no agresiva...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
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
fun PasoContacto(
    nombre: String, telefono: String, correo: String, aceptado: Boolean,
    onNombre: (String) -> Unit, onTelefono: (String) -> Unit, onCorreo: (String) -> Unit, onAceptado: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Información de contacto", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        Text("¿Cómo te contactarán si encuentran a tu mascota?", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))

        Text("Nombre de contacto", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(value = nombre, onValueChange = onNombre, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp), singleLine = true)

        Spacer(Modifier.height(12.dp))

        Text("Teléfono / Celular", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(value = telefono, onValueChange = onTelefono, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp), singleLine = true)

        Spacer(Modifier.height(12.dp))

        Text("Correo electrónico", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
        OutlinedTextField(value = correo, onValueChange = onCorreo, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp), singleLine = true)

        Spacer(Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(checked = aceptado, onCheckedChange = onAceptado)
            Text(
                text = "Acepto que mis datos de contacto sean públicos para la resolución del caso.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun PantallaHecho(onFinished: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Éxito",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "¡Anuncio publicado correctamente!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Esperamos que tu mascota regrese pronto a casa. La comunidad ya está alerta.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onFinished,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Volver al Inicio", fontWeight = FontWeight.Bold)
        }
    }
}

// =================================================================
// COMPONENTES: HOJAS EMERGENTES (BOTTOM SHEETS)
// =================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmitirFotoSheet(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("¿Omitir fotos?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Los anuncios con fotos tienen un 80% más de probabilidad de éxito.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Añadir foto ahora")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onConfirm, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Omitir de todas formas", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmitirDescripcionSheet(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("¿Omitir descripción?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Detalles como el color del collar o marcas ayudan a diferenciar a tu mascota.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Escribir detalles")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onConfirm, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Omitir de todas formas", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}