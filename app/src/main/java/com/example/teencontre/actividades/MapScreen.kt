package com.example.teencontre.actividades // Asegúrate de que este sea tu package real

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

data class Ubicacion(
    val ubicacion: LatLng,
    val titulo: String,
    val descripcion: String
)

@Composable
fun MapScreen(
    onNavigate: (String) -> Unit,
    onProfileClick: () -> Unit,
    onPublishClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = onProfileClick,
                onPublishClick = onPublishClick,
                onEncuentranosClick = { onNavigate("encuentranos") },
                onMapaClick = {}
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val ubicacionDefault = Ubicacion(
                LatLng(-11.9592875, -77.0052892), // Coordenadas de Lima
                "Ubicación",
                "Mascotas cerca de ti"
            )

            MyMap(ubicacionDefault) { }

            Button(
                onClick = { onNavigate("selector") },
                modifier = Modifier
                    .padding(top = 45.dp, start = 16.dp)
                    .align(Alignment.TopStart),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Volver", color = Color.White)
            }
        }
    }
}

@Composable
fun MyMap(ubicacion: Ubicacion, onReady: (GoogleMap) -> Unit) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Usamos el observador de ciclo de vida
    lifecycle.addObserver(rememberMapLifeCycle(map = mapView))

    AndroidView(
        factory = {
            mapView.apply {
                getMapAsync { googleMap ->
                    val zoomLevel = 15f
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion.ubicacion, zoomLevel))
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(ubicacion.ubicacion)
                            .title(ubicacion.titulo)
                            .snippet(ubicacion.descripcion)
                    )
                    onReady(googleMap)
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun rememberMapLifeCycle(map: MapView): LifecycleObserver {
    return remember {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> map.onCreate(Bundle())
                Lifecycle.Event.ON_START -> map.onStart()
                Lifecycle.Event.ON_RESUME -> map.onResume()
                Lifecycle.Event.ON_PAUSE -> map.onPause()
                Lifecycle.Event.ON_STOP -> map.onStop()
                Lifecycle.Event.ON_DESTROY -> map.onDestroy()
                else -> {}
            }
        }
    }
}