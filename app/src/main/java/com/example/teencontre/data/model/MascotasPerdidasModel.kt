package com.example.teencontre.data.model

import com.google.gson.annotations.SerializedName

data class MascotasPerdidasModel(
    // El @SerializedName mapea la mayúscula de Azure, pero el valor por defecto (= 0) protege tu pantalla de creación
    @SerializedName("Id") val id: Int = 0,
    @SerializedName("IdUsuario") val idUsuario: Int = 0,

    // Al dejarlos como String con = "", tu pantalla de creación no se rompe si usabas variables no nulas
    @SerializedName("NombreM") val nombreM: String = "",
    @SerializedName("Especie") val especie: String = "",
    @SerializedName("Genero") val genero: String = "",
    @SerializedName("Raza") val raza: String = "",

    // Cambiamos estrictamente a Any? solo aquí para que soporte dinámicamente el String vacío "" de Azure o el ByteArray local
    @SerializedName("Foto") val foto: Any? = null,

    @SerializedName("Fecha") val fecha: String = "",
    @SerializedName("Lugar") val lugar: String = "",
    @SerializedName("Descripcion") val descripcion: String = "",
    @SerializedName("Contacto") val contacto: String = "",
    @SerializedName("Telefono") val telefono: String = "",
    @SerializedName("Correo") val correo: String = ""
){

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MascotasPerdidasModel
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id
    }
}

data class ApiResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

data class EliminarRequest(
    val id: Int
)