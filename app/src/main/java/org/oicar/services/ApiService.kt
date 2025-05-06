package org.oicar.services

import okhttp3.ResponseBody
import org.oicar.models.ImageDocument
import org.oicar.models.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/Korisnik/Login")
    fun loginUser(@Body request: Map<String, String>): Call<ResponseBody>

    @POST("api/Korisnik/RegistracijaVozac")
    fun registerUser(@Body request: String): Call<ResponseBody>

    @POST("api/Image")
    fun uploadImage(@Body request: ImageDocument): Call<ResponseBody>
}