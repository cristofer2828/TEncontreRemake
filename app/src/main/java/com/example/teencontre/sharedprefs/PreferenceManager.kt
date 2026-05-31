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

        // Nuevas llaves para la sesión de Azure
        private const val KEY_USER_DATA = "user_data_json"
        private const val KEY_USER_ROLE = "user_role_type"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson() // Instancia de Gson para serializar/deserializer objetos complejos

    // --- INFORMACIÓN DE CONTACTO ---
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun setUserName(name: String) { prefs.edit { putString(KEY_USER_NAME, name) } }

    fun getPhone(): String = prefs.getString(KEY_PHONE, "") ?: ""
    fun setPhone(phone: String) { prefs.edit { putString(KEY_PHONE, phone) } }

    fun getEmail(): String = prefs.getString(KEY_EMAIL, "") ?: ""
    fun setEmail(email: String) { prefs.edit { putString(KEY_EMAIL, email) } }

    // --- NOTIFICACIONES ---
    fun getNotifications(): Boolean = prefs.getBoolean(KEY_NOTIFICATIONS, true)
    fun setNotifications(enabled: Boolean) { prefs.edit { putBoolean(KEY_NOTIFICATIONS, enabled) } }

    // --- GESTIÓN DE SESIÓN DE USUARIOS (AZURE) ---



    /**
     * Recupera el usuario logueado reconstruyendo polimórficamente su clase real.
     */
    fun getLoggedUser(): BaseUser? {
        val userJson = prefs.getString(KEY_USER_DATA, null) ?: return null
        val userRole = prefs.getString(KEY_USER_ROLE, null)

        return try {
            when (userRole) {
                "USUARIO" -> gson.fromJson(userJson, Usuario::class.java)
                "ORG" -> gson.fromJson(userJson, Organizacion::class.java)
                else -> gson.fromJson(userJson, BaseUser::class.java)
            }
        } catch (e: Exception) {
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

    fun logout() {

        prefs.edit {

            remove(KEY_USER_DATA)
            remove(KEY_USER_ROLE)

        }
    }
}

