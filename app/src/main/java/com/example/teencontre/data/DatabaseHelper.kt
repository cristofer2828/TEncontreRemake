package com.example.teencontre.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "teencontre_app.db"
        private const val DATABASE_VERSION = 1

        // ---- TABLA PERDIDOS ----
        const val TABLE_PERDIDOS = "addPerdido"
        const val PERDIDO_ID = "id"
        const val PERDIDO_NOMBRE = "nombreM"
        const val PERDIDO_ESPECIE = "especie"
        const val PERDIDO_GENERO = "genero"
        const val PERDIDO_RAZA = "raza"
        const val PERDIDO_FOTO = "foto"
        const val PERDIDO_FECHA = "fecha"
        const val PERDIDO_LUGAR = "lugar"
        const val PERDIDO_DESCRIPCION = "descripcion"
        const val PERDIDO_CONTACTO = "contacto"
        const val PERDIDO_TELEFONO = "telefono"
        const val PERDIDO_CORREO = "correo"

        // ---- TABLA ENCONTRADOS ----
        const val TABLE_ENCONTRADOS = "addEncontrada"
        const val ENCONTRADO_ID = "id"
        const val ENCONTRADO_ESPECIE = "especie"
        const val ENCONTRADO_GENERO = "genero"
        const val ENCONTRADO_FOTO = "foto"
        const val ENCONTRADO_FECHA = "fecha"
        const val ENCONTRADO_LUGAR = "lugar"
        const val ENCONTRADO_DESCRIPCION = "descripcion"
        const val ENCONTRADO_CONTACTO = "contacto"
        const val ENCONTRADO_TELEFONO = "telefono"
        const val ENCONTRADO_CORREO = "correo"

        // ---- TABLA ADOPCIONES ----
        const val TABLE_ADOPCION = "addAdopcion"
        const val ADOPCION_ID = "id"
        const val ADOPCION_ESPECIE = "especie"
        const val ADOPCION_GENERO = "genero"
        const val ADOPCION_RAZA = "raza"
        const val ADOPCION_VACUNADO = "vacunado"
        const val ADOPCION_ESTERILIZADO = "esterilizado"
        const val ADOPCION_DESPARASITADO = "desparasitado"
        const val ADOPCION_TAMANO = "tamano"
        const val ADOPCION_TEMPERAMENTO = "temperamento"
        const val ADOPCION_FOTO = "foto"
        const val ADOPCION_DESCRIPCION = "descripcion"
        const val ADOPCION_ORGANIZACION = "nombreOrganizacion"
        const val ADOPCION_TELEFONO = "telefono"
        const val ADOPCION_CORREO = "correo"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // SQL para Crear Tabla Perdidos
        val createPerdidos = """
            CREATE TABLE $TABLE_PERDIDOS (
                $PERDIDO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $PERDIDO_NOMBRE TEXT, $PERDIDO_ESPECIE TEXT, $PERDIDO_GENERO TEXT, $PERDIDO_RAZA TEXT,
                $PERDIDO_FOTO BLOB, $PERDIDO_FECHA TEXT, $PERDIDO_LUGAR TEXT, $PERDIDO_DESCRIPCION TEXT,
                $PERDIDO_CONTACTO TEXT, $PERDIDO_TELEFONO TEXT, $PERDIDO_CORREO TEXT
            )
        """.trimIndent()

        // SQL para Crear Tabla Encontrados
        val createEncontrados = """
            CREATE TABLE $TABLE_ENCONTRADOS (
                $ENCONTRADO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $ENCONTRADO_ESPECIE TEXT, $ENCONTRADO_GENERO TEXT, $ENCONTRADO_FOTO BLOB,
                $ENCONTRADO_FECHA TEXT, $ENCONTRADO_LUGAR TEXT, $ENCONTRADO_DESCRIPCION TEXT,
                $ENCONTRADO_CONTACTO TEXT, $ENCONTRADO_TELEFONO TEXT, $ENCONTRADO_CORREO TEXT
            )
        """.trimIndent()

        // SQL para Crear Tabla Adopciones (Booleanos se guardan como INTEGER 0 o 1)
        val createAdopcion = """
            CREATE TABLE $TABLE_ADOPCION (
                $ADOPCION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $ADOPCION_ESPECIE TEXT, $ADOPCION_GENERO TEXT, $ADOPCION_RAZA TEXT,
                $ADOPCION_VACUNADO INTEGER, $ADOPCION_ESTERILIZADO INTEGER, $ADOPCION_DESPARASITADO INTEGER,
                $ADOPCION_TAMANO TEXT, $ADOPCION_TEMPERAMENTO TEXT, $ADOPCION_FOTO BLOB,
                $ADOPCION_DESCRIPCION TEXT, $ADOPCION_ORGANIZACION TEXT, $ADOPCION_TELEFONO TEXT, $ADOPCION_CORREO TEXT
            )
        """.trimIndent()

        db?.execSQL(createPerdidos)
        db?.execSQL(createEncontrados)
        db?.execSQL(createAdopcion)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PERDIDOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ENCONTRADOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ADOPCION")
        onCreate(db)
    }

    // ============================================================================
    // METODOS PARA MASCOTAS PERDIDAS
    // ============================================================================

    fun insertPerdido(p: MascotasPerdidasModel): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(PERDIDO_NOMBRE, p.nombreM)
            put(PERDIDO_ESPECIE, p.especie)
            put(PERDIDO_GENERO, p.genero)
            put(PERDIDO_RAZA, p.raza)
            put(PERDIDO_FOTO, p.foto)
            put(PERDIDO_FECHA, p.fecha)
            put(PERDIDO_LUGAR, p.lugar)
            put(PERDIDO_DESCRIPCION, p.descripcion)
            put(PERDIDO_CONTACTO, p.contacto)
            put(PERDIDO_TELEFONO, p.telefono)
            put(PERDIDO_CORREO, p.correo)
        }
        return db.insert(TABLE_PERDIDOS, null, values)
    }

    // FUNCION CLAVE: EDITAR/ACTUALIZAR REPORTE PERDIDO
    fun updatePerdido(p: MascotasPerdidasModel): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(PERDIDO_NOMBRE, p.nombreM)
            put(PERDIDO_ESPECIE, p.especie)
            put(PERDIDO_GENERO, p.genero)
            put(PERDIDO_RAZA, p.raza)
            if (p.foto != null) put(PERDIDO_FOTO, p.foto) // Solo actualiza foto si no es nula
            put(PERDIDO_FECHA, p.fecha)
            put(PERDIDO_LUGAR, p.lugar)
            put(PERDIDO_DESCRIPCION, p.descripcion)
            put(PERDIDO_CONTACTO, p.contacto)
            put(PERDIDO_TELEFONO, p.telefono)
            put(PERDIDO_CORREO, p.correo)
        }
        return db.update(TABLE_PERDIDOS, values, "$PERDIDO_ID = ?", arrayOf(p.id.toString()))
    }

    // ============================================================================
    // METODOS PARA MASCOTAS ENCONTRADAS
    // ============================================================================

    fun insertEncontrada(e: MascotasEncontradasModel): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(ENCONTRADO_ESPECIE, e.especie)
            put(ENCONTRADO_GENERO, e.genero)
            put(ENCONTRADO_FOTO, e.foto)
            put(ENCONTRADO_FECHA, e.fecha)
            put(ENCONTRADO_LUGAR, e.lugar)
            put(ENCONTRADO_DESCRIPCION, e.descripcion)
            put(ENCONTRADO_CONTACTO, e.contacto)
            put(ENCONTRADO_TELEFONO, e.telefono)
            put(ENCONTRADO_CORREO, e.correo)
        }
        return db.insert(TABLE_ENCONTRADOS, null, values)
    }

    // FUNCION CLAVE: EDITAR/ACTUALIZAR REPORTE ENCONTRADO
    fun updateEncontrada(e: MascotasEncontradasModel): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(ENCONTRADO_ESPECIE, e.especie)
            put(ENCONTRADO_GENERO, e.genero)
            if (e.foto != null) put(ENCONTRADO_FOTO, e.foto)
            put(ENCONTRADO_FECHA, e.fecha)
            put(ENCONTRADO_LUGAR, e.lugar)
            put(ENCONTRADO_DESCRIPCION, e.descripcion)
            put(ENCONTRADO_CONTACTO, e.contacto)
            put(ENCONTRADO_TELEFONO, e.telefono)
            put(ENCONTRADO_CORREO, e.correo)
        }
        return db.update(TABLE_ENCONTRADOS, values, "$ENCONTRADO_ID = ?", arrayOf(e.id.toString()))
    }

    // ============================================================================
    // METODOS PARA CASOS DE ADOPCIÓN
    // ============================================================================

    fun insertAdopcion(a: MascotasAdopcionModel): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(ADOPCION_ESPECIE, a.especie)
            put(ADOPCION_GENERO, a.genero)
            put(ADOPCION_RAZA, a.raza)
            put(ADOPCION_VACUNADO, if (a.vacunado) 1 else 0)
            put(ADOPCION_ESTERILIZADO, if (a.esterilizado) 1 else 0)
            put(ADOPCION_DESPARASITADO, if (a.desparasitado) 1 else 0)
            put(ADOPCION_TAMANO, a.tamano)
            put(ADOPCION_TEMPERAMENTO, a.temperamento)
            put(ADOPCION_FOTO, a.foto)
            put(ADOPCION_DESCRIPCION, a.descripcion)
            put(ADOPCION_ORGANIZACION, a.nombreOrganizacion)
            put(ADOPCION_TELEFONO, a.telefono)
            put(ADOPCION_CORREO, a.correo)
        }
        return db.insert(TABLE_ADOPCION, null, values)
    }

    // FUNCION CLAVE: EDITAR/ACTUALIZAR REPORTE DE ADOPCIÓN
    fun updateAdopcion(a: MascotasAdopcionModel): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(ADOPCION_ESPECIE, a.especie)
            put(ADOPCION_GENERO, a.genero)
            put(ADOPCION_RAZA, a.raza)
            put(ADOPCION_VACUNADO, if (a.vacunado) 1 else 0)
            put(ADOPCION_ESTERILIZADO, if (a.esterilizado) 1 else 0)
            put(ADOPCION_DESPARASITADO, if (a.desparasitado) 1 else 0)
            put(ADOPCION_TAMANO, a.tamano)
            put(ADOPCION_TEMPERAMENTO, a.temperamento)
            if (a.foto != null) put(ADOPCION_FOTO, a.foto)
            put(ADOPCION_DESCRIPCION, a.descripcion)
            put(ADOPCION_ORGANIZACION, a.nombreOrganizacion)
            put(ADOPCION_TELEFONO, a.telefono)
            put(ADOPCION_CORREO, a.correo)
        }
        return db.update(TABLE_ADOPCION, values, "$ADOPCION_ID = ?", arrayOf(a.id.toString()))
    }
}