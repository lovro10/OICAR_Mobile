package org.oicar.services

import okhttp3.ResponseBody
import org.oicar.models.DirectionResponse
import org.oicar.models.ImageDocument
import org.oicar.models.KorisnikImage
import org.oicar.models.OglasVoznja
import org.oicar.models.RegisterRequest
import org.oicar.models.Trip
import org.oicar.models.Vehicle
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/Korisnik/Login")
    fun loginUser(@Body request: Map<String, String>): Call<String>

    @POST("api/Korisnik/RegistracijaVozac")
    fun registerUser(@Body request: String): Call<String>

    @POST("api/Image")
    fun uploadImage(@Body request: ImageDocument): Call<String>

    @GET("api/CitySearch")
    fun getAllCities(): Call<MutableList<String>>

    @GET("api/OglasVoznja/GetAll")
    fun getAllTrips(): Call<List<Trip>>

    @POST("api/KorisnikImage/CreateKorisnikImage")
    fun createKorisnikImage(@Body request: KorisnikImage): Call<ResponseBody>

    @POST("api/Vozilo/KrerirajVozilo")
    fun createVehicle(@Body request: Vehicle): Call<ResponseBody>

    @GET("api/Vozilo/GetVehicleByUser")
    fun getAllVehiclesForCurrentDriver(@Query("userId") userId: Int): Call<MutableList<Vehicle>>

    @POST("api/OglasVoznja/KreirajOglasVoznje")
    fun createOglasVoznja(@Body request: OglasVoznja): Call<ResponseBody>

    @GET("api/OglasVoznja/GetAllByUser")
    fun getAllTripsForCurrentUser(@Query("userid") userid: Int): Call<List<Trip>>
}

interface GoogleApiService {
    @GET("directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String
    ): Call<DirectionResponse>
}