package com.example.teencontre.data.model

import com.google.gson.annotations.SerializedName

data class MascotasEncontradasModel(
    @SerializedName("Id") val id: Int = 0,
    @SerializedName("IdUsuario") val idUsuario: Int = 0,
    @SerializedName("Especie") val especie: String = "",
    @SerializedName("Genero") val genero: String = "",

    // Cambiado a Any? para soportar el String vacío "" de Azure o el ByteArray local de SQLite
    @SerializedName("Foto")
    val foto: Any? = null,

    @SerializedName("Fecha") val fecha: String = "",
    @SerializedName("Lugar") val lugar: String = "",
    @SerializedName("Descripcion") val descripcion: String = "",
    @SerializedName("Contacto") val contacto: String = "",
    @SerializedName("Telefono") val telefono: String = "",
    @SerializedName("Correo") val correo: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MascotasEncontradasModel
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id
    }
}