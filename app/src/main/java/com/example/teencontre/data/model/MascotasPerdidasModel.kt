package com.example.teencontre.data.model
data class MascotasPerdidasModel(
    val id: Int = 0,
    val idUsuario: Int = 0, // Para identificar de quién es la publicación
    val nombreM: String,
    val especie: String,
    val genero: String,
    val raza: String,
    val foto: ByteArray?, // Se guarda como BLOB / Array de bytes para la foto
    val fecha: String,
    val lugar: String,
    val descripcion: String,
    val contacto: String,
    val telefono: String,
    val correo: String
) {
    // Estos métodos ayudan si necesitas comparar datos rápidamente
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