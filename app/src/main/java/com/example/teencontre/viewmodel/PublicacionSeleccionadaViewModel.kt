package com.example.teencontre.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.teencontre.data.model.MostrarPublicaciones

class PublicacionSeleccionadaViewModel : ViewModel() {

    var publicacionSeleccionada =
        mutableStateOf<MostrarPublicaciones?>(null)
        private set

    fun seleccionar(publicacion: MostrarPublicaciones) {
        publicacionSeleccionada.value = publicacion
    }
}