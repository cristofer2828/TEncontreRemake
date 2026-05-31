package com.example.teencontre.data.model

// Modelos de datos locales actualizados con idUsuario para SQLite y Azure



data class MascotasEncontradasModel(
    val id: Int = 0,
    val idUsuario: Int = 0, // 👈 Agregado: Enlace para saber qué usuario la encontró
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
    val idUsuario: Int = 0, // 👈 Agregado: Enlace de la organización/usuario que da en adopción
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