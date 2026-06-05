package com.example.teencontre.data.model

data class MascotasEncontradasModel(
    val id: Int = 0,
    val idUsuario: Int = 0,
    val especie: String,
    val genero: String,
    val foto: ByteArray?,
    val fecha: String,
    val lugar: String,
    val descripcion: String,
    val contacto: String,
    val telefono: String,
    val correo: String
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