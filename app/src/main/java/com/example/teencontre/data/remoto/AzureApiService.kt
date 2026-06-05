package com.example.teencontre.data.remote

import com.example.teencontre.data.model.ApiResponse
import com.example.teencontre.data.model.MascotasAdopcionModel
import com.example.teencontre.data.model.MascotasEncontradasModel
import com.example.teencontre.data.model.MascotasPerdidasModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import com.example.teencontre.data.model.LoginRequest
import com.example.teencontre.data.model.LoginResponse
import com.example.teencontre.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.Path

import com.example.teencontre.data.model.RegisterResponse
import com.example.teencontre.data.model.UpdateUserRequest
import com.example.teencontre.data.model.UpdateUserResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PUT
import retrofit2.http.Query

interface AzureApiService {
    @POST("api/Usuarios/update_profile.php")
    suspend fun updateUser(
        @Body request: UpdateUserRequest
        ): Response<UpdateUserResponse>

    @POST("api/Usuarios/register.php")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("api/Usuarios/login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @Multipart
    @POST("api/Publicaciones/guardar_perdido.php")
    suspend fun subirPerdido(
        @Part("idUsuario") idUsuario: RequestBody,
        @Part("nombreM") nombreM: RequestBody,
        @Part("especie") especie: RequestBody,
        @Part("genero") genero: RequestBody,
        @Part("raza") raza: RequestBody,
        @Part foto: MultipartBody.Part?,
        @Part("fecha") fecha: RequestBody,
        @Part("lugar") lugar: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("contacto") contacto: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("correo") correo: RequestBody
    ): Response<ApiResponse>


    @GET("api/Publicaciones/obtener_perdidos_usuario.php")
    suspend fun getPerdidosUsuario(
        @Query("idUsuario") id: Int
    ): List<MascotasPerdidasModel>

    @PUT("api/Publicaciones/editar_perdido.php")
    suspend fun editarPerdido(
        @Body mascota: MascotasPerdidasModel
    ): Response<ApiResponse>

    @FormUrlEncoded
    @POST("api/Publicaciones/eliminar_perdido.php")
    suspend fun eliminarPerdido(
        @Field("id")
        id:Int
    ): ApiResponse

    @GET("api/Publicaciones/obtener_perdidos.php")
    suspend fun obtenerPerdidos(): List<MascotasPerdidasModel>

    @POST("api/Publicaciones/eliminar_encontrado.php")
    suspend fun eliminarEncontrados(
        @Query("id") id: Int
    ): ApiResponse

    @FormUrlEncoded
    @POST("api/Publicaciones/eliminar_adopcion.php")
    suspend fun eliminarAdopcion(
        @Field("id")
        id:Int
    ): ApiResponse

    @Multipart
    @POST("api/Publicaciones/insertar_encontrado.php")
    suspend fun registrarMascotaEncontrada(
        @Part("idUsuario") idUsuario: RequestBody,
        @Part("especie") especie: RequestBody,
        @Part("genero") genero: RequestBody,
        @Part foto: MultipartBody.Part?,
        @Part("fecha") fecha: RequestBody,
        @Part("lugar") lugar: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("contacto") contacto: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("correo") correo: RequestBody
    ): Response<ApiResponse>

    @POST("api/Publicaciones/editar_encontrado.php")
    suspend fun editarMascotaEncontrada(
        @Body mascota: MascotasEncontradasModel
    ): ApiResponse

    @GET("api/Publicaciones/obtener_encontrados_usuario.php")
    suspend fun getEncontradosUsuario(
        @Query("idUsuario") idUsuario: Int
    ): List<MascotasEncontradasModel>

    @GET("api/Publicaciones/obtener_encontrados.php")
    suspend fun getEncontradosGlobal(): List<MascotasEncontradasModel>



    @Multipart
    @POST("api/Publicaciones/insertar_adopcion.php")
    suspend fun registrarMascotaAdopcion(
        @Part("idUsuario") idUsuario: RequestBody,
        @Part("especie") especie: RequestBody,
        @Part("genero") genero: RequestBody,
        @Part("raza") raza: RequestBody,
        @Part("vacunado") vacunado: RequestBody,
        @Part("esterilizado") esterilizado: RequestBody,
        @Part("desparasitado") desparasitado: RequestBody,
        @Part("tamano") tamano: RequestBody,
        @Part("temperamento") temperamento: RequestBody,
        @Part foto: MultipartBody.Part?,
        @Part("descripcion") descripcion: RequestBody,
        @Part("nombreOrganizacion") nombreOrganizacion: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("correo") correo: RequestBody
    ): Response<ApiResponse>

    @GET("api/Publicaciones/obtener_adopciones.php")
    suspend fun obtenerMascotasAdopcion(): Response<AdopcionesResponse>

    data class AdopcionesResponse(
        val success: Boolean,
        val data: List<MascotasAdopcionModel>,
        val error: String? = null
    )








    @GET("api/adopciones/usuario/{id}")
    suspend fun getAdopcionesUsuario(
        @Path("id") id: Int
    ): List<MascotasAdopcionModel>











    // --- ADOPCIONES ---
    @GET("api/adopciones")
    suspend fun getTodasLasAdopciones(): List<MascotasAdopcionModel>

    @Multipart
    @POST("api/adopciones")
    suspend fun subirAdopcion(
        @Part("idUsuario") idUsuario: RequestBody,
        @Part("especie") especie: RequestBody,
        @Part("genero") genero: RequestBody,
        @Part("raza") raza: RequestBody,
        @Part("vacunado") vacunado: RequestBody,
        @Part("esterilizado") esterilizado: RequestBody,
        @Part("desparasitado") desparasitado: RequestBody,
        @Part("tamano") tamano: RequestBody,
        @Part("temperamento") temperamento: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("nombreOrganizacion") nombreOrganizacion: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("correo") correo: RequestBody,
        @Part foto: MultipartBody.Part?
    ): Response<Void>


}