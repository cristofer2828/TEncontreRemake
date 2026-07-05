package com.example.teencontre.data.model

import com.google.gson.annotations.SerializedName

data class MostrarPublicaciones(

    @SerializedName("Id")
    val id: Int?,

    @SerializedName("IdUsuario")
    val idUsuario: Int?,

    // CORREGIDO: 'tipo' en minúscula tal cual tu base de datos y seguro contra nulos
    @SerializedName("tipo")
    val tipo: String?,

    // CORREGIDO: 'nombreMascota' con 'n' minúscula tal cual tu base de datos
    @SerializedName("nombreMascota")
    val nombreMascota: String?,

    @SerializedName("Especie")
    val especie: String?,

    @SerializedName("Genero")
    val genero: String?,

    @SerializedName("Raza")
    val raza: String?,

    @SerializedName("Foto")
    val foto: String?,

    @SerializedName("Lugar")
    val lugar: String?,

    @SerializedName("Descripcion")
    val descripcion: String?,

    @SerializedName("Fecha")
    val fecha: String?,

    @SerializedName("FechaRegistro")
    val fechaRegistro: String?,

    @SerializedName("vacunado")
    val vacunado: Boolean?,

    @SerializedName("esterilizado")
    val esterilizado: Boolean?,

    @SerializedName("desparasitado")
    val desparasitado: Boolean?,


    @SerializedName("tamano")
    val tamano: String?,

    @SerializedName("temperamento")
    val temperamento: String?,

    @SerializedName("NombreOrganizacion")
    val nombreOrganizacion: String?,

    @SerializedName("Telefono")
    val telefono: String?,

    @SerializedName("Correo")
    val correo: String?
)