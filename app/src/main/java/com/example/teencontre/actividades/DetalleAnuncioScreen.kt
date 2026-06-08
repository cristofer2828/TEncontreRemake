package com.example.teencontre.actividades

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.example.teencontre.R
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.teencontre.viewmodel.PublicacionSeleccionadaViewModel

data class Comentario(
    val nombre: String,
    val mensaje: String,
    val tiempo: String
)

@Composable
fun DetalleAnuncioScreen(

    onBack: () -> Unit,

    onVerUbicacion: (String) -> Unit

) {

    val viewModel: PublicacionSeleccionadaViewModel =
        viewModel()

    val publicacion =
        viewModel.publicacionSeleccionada.value

    if (publicacion == null) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text("No se encontró la publicación")
        }

        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp)
    ) {

        Button(
            onClick = onBack
        ) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(12.dp))

        publicacion.foto?.let { foto ->

            AsyncImage(
                model = foto,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = publicacion.nombreMascota
                ?: publicacion.especie,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = publicacion.tipo,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = publicacion.descripcion
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Especie: ${publicacion.especie}")
        Text("Género: ${publicacion.genero}")

        publicacion.raza?.let {
            Text("Raza: $it")
        }

        publicacion.fecha?.let {
            Text("Fecha: $it")
        }

        Spacer(modifier = Modifier.height(12.dp))

        publicacion.telefono?.let {
            Text("Teléfono: $it")
        }

        publicacion.correo?.let {
            Text("Correo: $it")
        }

        if (publicacion.tipo == "ADOPCION") {

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Estado de salud",
                fontWeight = FontWeight.Bold
            )

            Text(
                "Vacunado: ${
                    if (publicacion.vacunado == true)
                        "Sí"
                    else
                        "No"
                }"
            )

            Text(
                "Esterilizado: ${
                    if (publicacion.esterilizado == true)
                        "Sí"
                    else
                        "No"
                }"
            )

            Text(
                "Desparasitado: ${
                    if (publicacion.desparasitado == true)
                        "Sí"
                    else
                        "No"
                }"
            )

            publicacion.tamano?.let {
                Text("Tamaño: $it")
            }

            publicacion.temperamento?.let {
                Text("Temperamento: $it")
            }

            publicacion.nombreOrganizacion?.let {
                Text("Organización: $it")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!publicacion.lugar.isNullOrBlank()) {

            Button(
                onClick = {

                    onVerUbicacion(
                        publicacion.lugar
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("📍 Ver ubicación")
            }
        }
    }
}