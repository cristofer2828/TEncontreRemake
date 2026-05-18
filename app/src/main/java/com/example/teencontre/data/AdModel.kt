package com.example.teencontre.data

// Modelos de datos con los campos exactos solicitados

data class MascotasPerdidasModel(
    val id: Int = 0,
    val nombreM: String,
    val especie: String,
    val genero: String,
    val raza: String,
    val foto: ByteArray?, // BLOB se maneja como ByteArray en Kotlin
    val fecha: String,    // DATETIME se guarda como String formateado
    val lugar: String,
    val descripcion: String,
    val contacto: String,
    val telefono: String,
    val correo: String
)

data class MascotasEncontradasModel(
    val id: Int = 0,
    val especie: String,
    val genero: String,
    val foto: ByteArray?,
    val fecha: String,
    val lugar: String,
    val descripcion: String,
    val contacto: String,
    val telefono: String,
    val correo: String
)

data class MascotasAdopcionModel(
    val id: Int = 0,
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
)