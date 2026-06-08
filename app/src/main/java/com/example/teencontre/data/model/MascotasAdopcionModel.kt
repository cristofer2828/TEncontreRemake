package com.example.teencontre.data.model

import com.google.gson.annotations.SerializedName

data class MascotasAdopcionModel(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("idUsuario") val idUsuario: Int = 0,
    @SerializedName("especie") val especie: String = "",
    @SerializedName("genero") val genero: String = "",
    @SerializedName("raza") val raza: String = "",

    // Regresamos a Boolean normal para que tu Cursor de SQLite no falle nunca
    @SerializedName("vacunado") val vacunado: Boolean = false,
    @SerializedName("esterilizado") val esterilizado: Boolean = false,
    @SerializedName("desparasitado") val desparasitado: Boolean = false,

    @SerializedName("tamano") val tamano: String = "",
    @SerializedName("temperamento") val temperamento: String = "",
    @SerializedName("Foto")
    val foto: Any? = null,
    @SerializedName("descripcion") val descripcion: String = "",
    @SerializedName("nombreOrganizacion") val nombreOrganizacion: String = "",
    @SerializedName("telefono") val telefono: String = "",
    @SerializedName("correo") val correo: String = ""
) {
    // Campos alternativos que se llenan AUTOMÁTICAMENTE si Azure manda "1" o "0" en el JSON
    @SerializedName("vacunado_string") private val vacunadoStr: String? = null
    @SerializedName("esterilizado_string") private val esterilizadoStr: String? = null
    @SerializedName("desparasitado_string") private val desparasitadoStr: String? = null

    // Funciones de ayuda por si necesitas evaluar compatibilidad extraña de Azure
    fun esVacunado(): Boolean = vacunado || vacunadoStr == "1" || vacunadoStr == "true"
    fun esEsterilizado(): Boolean = esterilizado || esterilizadoStr == "1" || esterilizadoStr == "true"
    fun esDesparasitado(): Boolean = desparasitado || desparasitadoStr == "1" || desparasitadoStr == "true"

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