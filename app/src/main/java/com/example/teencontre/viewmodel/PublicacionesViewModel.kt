package com.example.teencontre.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teencontre.data.model.MostrarPublicaciones
import com.example.teencontre.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class PublicacionesViewModel : ViewModel() {

    var publicaciones by mutableStateOf<List<MostrarPublicaciones>>(emptyList())
        private set

    init {
        cargarPublicaciones()
    }

    fun cargarPublicaciones() {

        viewModelScope.launch {

            try {

                Log.d(
                    "PUBLICACIONES",
                    "Cargando publicaciones..."
                )

                val response =
                    RetrofitClient
                        .instance
                        .obtenerPublicaciones()

                Log.d(
                    "PUBLICACIONES",
                    "HTTP ${response.code()}"
                )

                if (response.isSuccessful) {

                    publicaciones =
                        response.body() ?: emptyList()

                    Log.d(
                        "PUBLICACIONES",
                        "Recibidas: ${publicaciones.size}"
                    )

                    publicaciones.forEach {

                        Log.d(
                            "PUBLICACION",
                            "${it.id} | ${it.tipo} | ${it.especie}"
                        )
                    }

                } else {

                    Log.e(
                        "PUBLICACIONES",
                        "Error HTTP: ${response.code()}"
                    )
                }

            } catch (e: Exception) {

                Log.e(
                    "PUBLICACIONES",
                    "Error cargando publicaciones",
                    e
                )
            }
        }
    }
}