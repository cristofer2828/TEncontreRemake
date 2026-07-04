package com.example.teencontre.sharedprefs

import android.content.Context
import android.content.SharedPreferences
import com.example.teencontre.data.model.BaseUser
import com.example.teencontre.data.model.Usuario
import com.example.teencontre.data.model.Organizacion
import com.google.gson.Gson
import androidx.core.content.edit

class PreferenceManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "user_settings_panda"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PHONE = "phone"
        private const val KEY_EMAIL = "email"
        private const val KEY_NOTIFICATIONS = "notifications"

        // Llave modo oscuro
        private const val KEY_DARK_MODE = "dark_mode_enabled"

        // Nuevas llaves para la sesión de Azure
        private const val KEY_USER_DATA = "user_data_json"
        private const val KEY_USER_ROLE = "user_role_type"

        // Llave para la persistencia del estado de navegación de la App
        private const val KEY_LAST_ROUTE = "last_route"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // --- NOTIFICACIONES ---
    fun getNotifications(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS, true)
    fun setNotifications(enabled: Boolean) { prefs.edit { putBoolean(KEY_NOTIFICATIONS, enabled) } }

    // --- MODO OSCURO ---
    fun isDarkModeEnabled(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)
    fun setDarkModeEnabled(enabled: Boolean) { prefs.edit { putBoolean(KEY_DARK_MODE, enabled) } }

    // --- GESTIÓN DE SESIÓN DE USUARIOS (AZURE) ---

    /**
     * Recupera el usuario logueado reconstruyendo polimórficamente su clase real.
     */
    fun getLoggedUser(): BaseUser? {
        return try {
            val userJson = prefs.getString(KEY_USER_DATA, null)
            // SI EL JSON ESTÁ VACÍO O ES NULO, RETORNAMOS NULL DE INMEDIATO
            if (userJson.isNullOrEmpty() || userJson == "null") {
                return null
            }

            val userRole = prefs.getString(KEY_USER_ROLE, null)

            when (userRole) {
                "USUARIO" -> gson.fromJson(userJson, Usuario::class.java)
                "ORG" -> gson.fromJson(userJson, Organizacion::class.java)
                else -> gson.fromJson(userJson, BaseUser::class.java)
            }
        } catch (e: Exception) {
            // Si hay cualquier error de casteo o JSON roto, limpiamos para no buclear la app
            e.printStackTrace()
            null
        }
    }

    fun saveLoggedUser(user: BaseUser) {
        val json = gson.toJson(user)
        prefs.edit {
            putString(KEY_USER_DATA, json)
            putString(KEY_USER_ROLE, user.tipo)
        }
    }

    fun clearSession() {
        prefs.edit {
            remove(KEY_USER_DATA)
            remove(KEY_USER_ROLE)
            remove(KEY_LAST_ROUTE) // Limpia también la última pantalla al cerrar sesión
        }
    }

    /**
     * Extrae de forma segura el nombre del usuario o de la organización actual
     * mapeando correctamente las propiedades exactas de tus modelos.
     */
    fun getUserName(): String? {
        val user = getLoggedUser() ?: return null
        return when (user) {
            is Usuario -> user.nombre       // Usa .nombre de tu data class Usuario
            is Organizacion -> user.nombreOrg // Usa .nombreOrg de tu data class Organizacion
            else -> null
        }
    }

    // --- PERSISTENCIA DE PANTALLA / ESTADO DE NAVEGACIÓN ---

    /**
     * Almacena de forma persistente la última ruta de navegación activa.
     */
    fun saveLastRoute(route: String) {
        prefs.edit {
            putString(KEY_LAST_ROUTE, route)
        }
    }

    /**
     * Recupera la última pantalla guardada. Si no existe un valor previo,
     * se establece "perfil" por defecto como la pantalla de inicio estándar.
     */
    fun getLastRoute(): String {
        return prefs.getString(KEY_LAST_ROUTE, "perfil") ?: "perfil"
    }
}
