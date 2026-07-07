package com.example.teencontre.actividades

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teencontre.R
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.teencontre.viewmodel.PublicacionSeleccionadaViewModel
import com.example.teencontre.viewmodel.PublicacionesViewModel

@Composable
fun EncuentranosScreen(
    onProfileClick: () -> Unit,
    onPublishClick: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val publicacionesViewModel: PublicacionesViewModel = viewModel()
    LaunchedEffect(Unit) {
        publicacionesViewModel.cargarPublicaciones()
    }

    val publicaciones = publicacionesViewModel.publicaciones
    var mostrarFiltros by remember { mutableStateOf(false) }

    var desaparecido by remember { mutableStateOf(false) }
    var encontrado by remember { mutableStateOf(false) }
    var adopcion by remember { mutableStateOf(false) }

    var perro by remember { mutableStateOf(false) }
    var gato by remember { mutableStateOf(false) }
    var otro by remember { mutableStateOf(false) }

    val publicacionesFiltradas = publicaciones.filter { publicacion ->
        val tipoUpper = publicacion.tipo?.uppercase() ?: ""
        val coincideEstado =
            (!desaparecido && !encontrado && !adopcion)
                    || (desaparecido && tipoUpper in listOf("PERDIDA", "PERDIDO"))
                    || (encontrado && tipoUpper in listOf("ENCONTRADO", "ENCONTRADA"))
                    || (adopcion && tipoUpper == "ADOPCION")

        val especieStr = publicacion.especie ?: ""
        val coincideTipo = (!perro && !gato && !otro)
                || (perro && especieStr.equals("Perro", true))
                || (gato && especieStr.equals("Gato", true))
                || (otro && !especieStr.equals("Perro", true) && !especieStr.equals("Gato", true))

        coincideEstado && coincideTipo
    }

    val filtrosSeleccionados = buildList {
        if (desaparecido) add("Desaparecido")
        if (encontrado) add("Encontrado")
        if (adopcion) add("Adopción")
        if (perro) add("Perro")
        if (gato) add("Gato")
        if (otro) add("Otro")
    }.joinToString(", ")

    var pantallaActiva by remember { mutableStateOf("encuentranos") }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = pantallaActiva,
                onProfileClick = onProfileClick,
                onPublishClick = onPublishClick,
                onEncuentranosClick = {
                    pantallaActiva = "encuentranos"
                    onNavigate("encuentranos")
                },
                onMapaClick = {
                    pantallaActiva = "mapa"
                    onNavigate("mapa")
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = 0.dp,
                    end = 0.dp,
                    bottom = 0.dp
                )
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Publicaciones",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_search),
                    contentDescription = null
                )
            }

            // SECCIÓN DE FILTROS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarFiltros = !mostrarFiltros }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Filtros: ")
                    Text(
                        text = filtrosSeleccionados.ifEmpty { "No seleccionado" },
                        color = Color(0xFF6C63FF),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = if (mostrarFiltros)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (mostrarFiltros) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Estado de la mascota", fontWeight = FontWeight.Bold)
                        FiltroItem("Desaparecido", desaparecido) { desaparecido = it }
                        FiltroItem("Encontrado", encontrado) { encontrado = it }
                        FiltroItem("Busca un nuevo dueño.", adopcion) { adopcion = it }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text("Tipo", fontWeight = FontWeight.Bold)
                        FiltroItem("Perro", perro) { perro = it }
                        FiltroItem("Gato", gato) { gato = it }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { mostrarFiltros = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Aplicar Filtros")
                            }
                        }
                    }
                }
            }

            val publicacionSeleccionadaViewModel: PublicacionSeleccionadaViewModel = viewModel()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 4.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
                )
            ) {
                items(publicacionesFiltradas) { publicacion ->
                    val tipoUpper = publicacion.tipo?.uppercase() ?: "PUBLICACIÓN"
                    val (colorTexto, colorFondo) = when (tipoUpper) {
                        "PERDIDA", "PERDIDO" -> Pair(
                            Color(0xFF6A1B9A),      // Morado
                            Color(0xFFEDE7F6)       // Fondo lila claro
                        )

                        "ENCONTRADO", "ENCONTRADA" -> Pair(
                            Color(0xFF2E7D32),      // Verde
                            Color(0xFFE8F5E9)       // Verde claro
                        )

                        "ADOPCION" -> Pair(
                            Color(0xFF0288D1),      // Azul
                            Color(0xFFE3F2FD)       // Azul claro
                        )

                        else -> Pair(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                publicacionSeleccionadaViewModel.seleccionar(publicacion)
                                onNavigate("detalle_anuncio")
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            AsyncImage(
                                model = publicacion.foto,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.logo_perros),
                                error = painterResource(R.drawable.logo_perros)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = colorFondo
                                ) {
                                    Text(
                                        text = publicacion.tipo ?: "PUBLICACIÓN",
                                        color = colorTexto,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                val tieneNombre = !publicacion.nombreMascota.isNullOrBlank()
                                Text(
                                    text = if (tieneNombre) {
                                        publicacion.nombreMascota!!
                                    } else {
                                        if (tipoUpper == "ADOPCION") "Mascota en Adopción" else (publicacion.especie ?: "Mascota")
                                    },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = publicacion.descripcion ?: "Sin descripción.",
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "📍 ${publicacion.lugar ?: "Sin ubicación"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                publicacion.fechaRegistro?.let {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📅 $it",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FiltroItem(
    texto: String,
    seleccionado: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!seleccionado) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = seleccionado,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = texto,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
