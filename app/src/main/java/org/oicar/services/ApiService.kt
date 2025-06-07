package org.oicar.services

import okhttp3.ResponseBody
import org.oicar.models.DirectionResponse
import org.oicar.models.ImageDocument
import org.oicar.models.JoinRide
import org.oicar.models.KorisnikImage
import org.oicar.models.OglasVoznja
import org.oicar.models.Trip
import org.oicar.models.Vehicle
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Response

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

    @POST("api/KorisnikVoznja/JoinRide")
    fun joinTrip(@Body request: JoinRide): Call<ResponseBody>

    @GET("api/KorisnikVoznja/UserJoinedRide")
    fun checkIfUserAlreadyAppliedForTrip(
        @Query("userId") userid: Int,
        @Query("oglasVoznjaId") oglasVoznjaId: Int): Call<Boolean>

    @DELETE("api/KorisnikVoznja/DeleteKorisnikVoznja")
    fun deleteUserTripApplication(
        @Query("userId") userid: Int,
        @Query("oglasVoznjaId") oglasVoznjaId: Int): Call<ResponseBody>
}

interface GoogleApiService {
    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String
    ): Response<DirectionResponse>
}