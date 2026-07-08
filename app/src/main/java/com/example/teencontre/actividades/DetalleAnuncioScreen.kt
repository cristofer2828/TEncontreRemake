package com.example.teencontre.actividades

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.teencontre.data.model.Comentario
import com.example.teencontre.data.remote.RetrofitClient
import com.example.teencontre.viewmodel.PublicacionSeleccionadaViewModel
import com.example.teencontre.sharedprefs.PreferenceManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleAnuncioScreen(
    onBack: () -> Unit,
    onVerUbicacion: (String) -> Unit
) {
    val viewModel: PublicacionSeleccionadaViewModel = viewModel()
    val publicacion = viewModel.publicacionSeleccionada.value
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    val nombreUsuarioReal = prefs.getUserName() ?: "Usuario Anónimo"
    val scope = rememberCoroutineScope()

    val tieneNombre = !publicacion?.nombreMascota.isNullOrBlank()

    val listaComentarios = remember { mutableStateListOf<Comentario>() }
    var nuevoComentarioTexto by remember { mutableStateOf("") }
    var cargandoComentarios by remember { mutableStateOf(true) }

    LaunchedEffect(publicacion) {
        if (publicacion != null) {
            try {
                cargandoComentarios = true
                val response = RetrofitClient.instance.obtenerComentarios(
                    idPublicacion = publicacion.id ?: 0,
                    tipoPublicacion = publicacion.tipo ?: "PERDIDA"
                )
                if (response.isSuccessful) {
                    val comentarios = response.body() ?: emptyList()
                    listaComentarios.clear()
                    listaComentarios.addAll(comentarios)
                } else {
                    Toast.makeText(context, "Error del servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar comentarios: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                cargandoComentarios = false
            }
        }
    }

    if (publicacion == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se encontró la publicación", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val tipoUpper = publicacion.tipo?.uppercase() ?: "PUBLICACIÓN"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Anuncio", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // --- Imagen de la Mascota ---
            publicacion.foto?.let { foto ->
                AsyncImage(
                    model = foto,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Cabecera con Nombre y Tipo ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (tieneNombre) {
                            publicacion.nombreMascota!!
                        } else {
                            if (tipoUpper == "ADOPCION") "Mascota en Adopción" else (publicacion.especie ?: "Mascota")
                        },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (!tieneNombre) {
                        Text(
                            text = "${publicacion.especie ?: "Mascota"} • ${publicacion.raza ?: "Mestizo"}",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                val (chipTextColor, chipBgColor) = when (tipoUpper) {
                    "ADOPCION" -> Pair(Color(0xFF0288D1), Color(0xFFE1F5FE))
                    "PERDIDA" -> Pair(Color(0xFF7B1FA2), Color(0xFFF3E5F5))
                    "ENCONTRADO", "ENCONTRADA" -> Pair(Color(0xFF2E7D32), Color(0xFFE8F5E9)) // Verde Correcto
                    else -> Pair(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                }

                AssistChip(
                    onClick = { },
                    label = { Text(publicacion.tipo ?: "PUBLICACIÓN", fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = chipTextColor,
                        containerColor = chipBgColor
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = chipTextColor.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Descripción ---
            Text(
                text = publicacion.descripcion ?: "Sin descripción.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // --- Ficha Técnica ---
            Text("Detalles de la mascota", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DatoItem(icon = Icons.Default.Pets, label = "Especie", value = publicacion.especie ?: "No especificada")
                    DatoItem(icon = Icons.Default.Info, label = "Género", value = publicacion.genero ?: "No especificado")
                    publicacion.raza?.let { DatoItem(icon = Icons.Default.Pets, label = "Raza", value = it) }

                    val fechaMostrar = publicacion.fecha ?: publicacion.fechaRegistro
                    fechaMostrar?.let { DatoItem(icon = Icons.Default.DateRange, label = "Fecha", value = it) }

                    val lugarTexto = if (!publicacion.lugar.isNullOrBlank()) publicacion.lugar else "No especificada"
                    DatoItem(icon = Icons.Default.LocationOn, label = "Ubicación", value = lugarTexto)
                }
            }

            // --- Datos de Contacto ---
            if (!publicacion.telefono.isNullOrBlank() || !publicacion.correo.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Contacto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        publicacion.telefono?.let { DatoItem(icon = Icons.Default.Call, label = "Teléfono", value = it) }
                        publicacion.correo?.let { DatoItem(icon = Icons.Default.Email, label = "Correo", value = it) }
                    }
                }
            }

            // ====================================================================
            // SECCIÓN ADOPCIÓN (Filtrada inteligentemente y adaptada a Booleans)
            // ====================================================================
            if (tipoUpper == "ADOPCION") {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Salud y Estado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Evaluación adaptada a datos de tipo Boolean? (true == "Sí")
                        DatoItem(icon = Icons.Default.Info, label = "Vacunado", value = if (publicacion.vacunado == true) "Sí" else "No")
                        DatoItem(icon = Icons.Default.Info, label = "Esterilizado", value = if (publicacion.esterilizado == true) "Sí" else "No")
                        DatoItem(icon = Icons.Default.Info, label = "Desparasitado", value = if (publicacion.desparasitado == true) "Sí" else "No")
                        publicacion.tamano?.let { DatoItem(icon = Icons.Default.Info, label = "Tamaño", value = it) }
                        publicacion.temperamento?.let { DatoItem(icon = Icons.Default.Info, label = "Temperamento", value = it) }
                        publicacion.nombreOrganizacion?.let { DatoItem(icon = Icons.Default.Info, label = "Organización", value = it) }
                    }
                }
            }

            // --- Botón de Mapa ---
            if (
                tipoUpper == "PERDIDO" ||
                tipoUpper == "ENCONTRADO" ||
                tipoUpper == "ENCONTRADA"
            ) {

                if (!publicacion.lugar.isNullOrBlank()) {

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onVerUbicacion(publicacion.lugar) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            "Ver ubicación en el Mapa",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- Sección de Comentarios ---
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Comentarios (${listaComentarios.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = nuevoComentarioTexto,
                    onValueChange = { nuevoComentarioTexto = it },
                    placeholder = { Text("Escribe un comentario...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    shape = RoundedCornerShape(24.dp),
                    enabled = !cargandoComentarios,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = {
                        if (nuevoComentarioTexto.isNotBlank()) {
                            val comentarioEnviado = nuevoComentarioTexto.trim()
                            nuevoComentarioTexto = ""

                            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                            sdf.timeZone = TimeZone.getTimeZone("America/Lima")
                            val horaPeru = sdf.format(Date())

                            scope.launch {
                                try {
                                    val comentarioDto = Comentario(
                                        id_publicacion = publicacion.id ?: 0,
                                        tipo_publicacion = publicacion.tipo ?: "PERDIDA",
                                        nombre_usuario = nombreUsuarioReal,
                                        mensaje = comentarioEnviado,
                                        tiempo = horaPeru
                                    )

                                    val response = RetrofitClient.instance.enviarComentario(comentarioDto)

                                    if (response.isSuccessful && response.body()?.success == true) {
                                        listaComentarios.add(0, comentarioDto)
                                        Toast.makeText(context, "Comentario enviado con éxito", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error del servidor: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error en la red: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    enabled = !cargandoComentarios
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar comentario"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (cargandoComentarios) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp))
                }
            } else if (listaComentarios.isEmpty()) {
                Text(
                    text = "No hay comentarios aún. ¡Sé el primero!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                listaComentarios.forEach { comentario ->
                    ItemComentario(comentario = comentario)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DatoItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ItemComentario(comentario: Comentario) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comentario.nombre_usuario,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = comentario.tiempo ?: "Ahora",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = comentario.mensaje,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}