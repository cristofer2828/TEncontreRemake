package com.example.teencontre.data.model

data class MascotasAdopcionModel(
    val id: Int = 0,
    val idUsuario: Int = 0,
    val especie: String,
    val genero: String,
    val raza: String,
    val vacunado: Boolean,
    val esterilizado: Boolean,
    val desparasitado: Boolean,
    val tamano: String,
    val temperamento: String,
    val foto: ByteArray?,
    val descripcion: String,
    val nombreOrganizacion: String,
    val telefono: String,
    val correo: String
) {
    // Sobrescribimos equals y hashCode basándonos en el ID para optimizar el renderizado en la app
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MascotasAdopcionModel
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id
    }
}