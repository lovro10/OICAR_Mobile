package org.oicar

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.ButtCap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import org.oicar.AddTrip
import org.oicar.TripsScreen
import org.oicar.models.DirectionResponse
import org.oicar.models.JoinRide
import org.oicar.models.Trip
import org.oicar.models.Vehicle
import org.oicar.services.ApiClient
import org.oicar.services.ApiClient.retrofit
import org.oicar.services.ApiService
import org.oicar.services.GoogleApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Locale

class TripDetails : AppCompatActivity() {

//    val retrofitGoogle = Retrofit.Builder()
//        .baseUrl("https://maps.googleapis.com/maps/api/")
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//
//    val apiService = retrofitGoogle.create((GoogleApiService::class.java))

    private var currentUserId: Int = 0
    private lateinit var currentUserRole: String
    private lateinit var trip : Trip

    private var isAlreadyApplied: Boolean = false
    private lateinit var btnApplyForTrip: Button

    private lateinit var googleMapsApiKey: String
    private lateinit var approximateDuration: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trip_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        googleMapsApiKey = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
            .metaData
            .getString("com.google.android.geo.API_KEY").toString()

        btnApplyForTrip = findViewById<Button>(R.id.btnApplyForTrip)

        val receivedTripInfoJson = intent.getStringExtra("trip")
        val type = object : TypeToken<Trip>() {}.type
        trip = Gson().fromJson<Trip>(receivedTripInfoJson, type)



        val jwt = getJwtToken(this)
        val payload = decodeJwtPayload(jwt!!)
        currentUserId = payload.getString("sub").toInt()
        currentUserRole = payload.getString("role").toString()

        ApiClient.retrofit.checkIfUserAlreadyAppliedForTrip(currentUserId, trip.idOglasVoznja).enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {

                    isAlreadyApplied = response.body()!!

                    if (isAlreadyApplied == true && currentUserRole == "PASSENGER") {

                        btnApplyForTrip.text = "cancel application"
                        btnApplyForTrip.setTextColor(Color.RED)
                    }
                    else if (currentUserRole == "DRIVER") {

                        btnApplyForTrip.text = "start trip"
                        btnApplyForTrip.setTextColor(Color.parseColor("#008000"))
                    }

                } else {
                    println("FAIL")
                    println(response)
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Log.e("API", "Network error: ${t.message}")
            }
        })

        val departureLocation = findViewById<TextView>(R.id.departureLocation)
        val arrivalLocation = findViewById<TextView>(R.id.arrivalLocation)
        val tripDuration = findViewById<TextView>(R.id.tripDuration)
        val departureTime = findViewById<TextView>(R.id.departureTime)
        val departureDate = findViewById<TextView>(R.id.DepartureDate)
        val arrivalTime = findViewById<TextView>(R.id.arrivalTime)
        val tripCarName = findViewById<TextView>(R.id.tripCarName)
        val numberOfPassengers = findViewById<TextView>(R.id.numberOfPassengers)
        val driverName = findViewById<TextView>(R.id.driverName)
        val driverTelephone = findViewById<TextView>(R.id.driverTelephone)
        val tripPrice = findViewById<TextView>(R.id.tripPrice)

        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        val tempDate = trip.datumIVrijemePolaska.split('T')[0]

        val date = inputFormat.parse(tempDate.toString())
        val formattedDate = outputFormat.format(date!!)

        val time = trip.datumIVrijemePolaska.split("T")[1].substring(0, 5)

        departureLocation.text = trip.polaziste
        arrivalLocation.text = trip.odrediste

        lifecycleScope.launch {

            val directionData = getDirectionsData()

            if(directionData?.routes?.size != 0) {

                tripDuration.text = directionData?.routes?.first()?.legs?.first()?.duration?.text.toString()
                arrivalTime.text = addTimeStrings(time, convertToHourMinuteFormat(tripDuration.text.toString()))
            }
            else {
                tripDuration.text = "N/A"
                arrivalTime.text = "N/A"
            }
        }

        departureTime.text = time
        departureDate.text = formattedDate

        tripCarName.text = "${trip.marka} ${trip.model} (${trip.registracija.substring(2, trip.registracija.length)})"
        numberOfPassengers.text = trip.brojPutnika.toString()
        driverName.text = "${trip.ime} ${trip.prezime}"
        driverTelephone.text = trip.username
        tripPrice.text = BigDecimal(trip.cijenaPoPutniku).setScale(2, RoundingMode.HALF_UP).toString()

        val joinRideData = JoinRide(
            currentUserId.toString(),
            trip.idOglasVoznja.toString()
        )

        btnApplyForTrip.setOnClickListener {

            if(isAlreadyApplied == false && currentUserRole != "DRIVER") {

                ApiClient.retrofit.joinTrip(joinRideData).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {

                            println("SUCCESS")
                            println(response)

                            btnApplyForTrip.text = "cancel application"
                            btnApplyForTrip.setTextColor(Color.RED)

                            isAlreadyApplied = true

                        } else {
                            println("FAIL")
                            println(response)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("API", "Network error: ${t.message}")
                    }
                })
            }
            else if (isAlreadyApplied == false && currentUserRole != "DRIVER") {

                ApiClient.retrofit.deleteUserTripApplication(joinRideData.korisnikId.toInt(), joinRideData.oglasVoznjaId.toInt()).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {

                            println("SUCCESS")
                            println(response)

                            btnApplyForTrip.text = "apply"
                            btnApplyForTrip.setTextColor(Color.parseColor("#008000"))

                            isAlreadyApplied = false

                        } else {
                            println("FAIL")
                            println(response)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("API", "Network error: ${t.message}")
                    }
                })
            }
            else {

                val intent = Intent(this@TripDetails, TripTracking::class.java)
                intent.putExtra("trip", Gson().toJson(trip))
                startActivity(intent)
            }
        }
    }

    suspend fun getDirectionsData(): DirectionResponse? {
        return try {
            val response = ApiClient.retrofitGoogle.getDirections(
                trip.polaziste,
                trip.odrediste,
                googleMapsApiKey
            )

            if (response.isSuccessful) {

                response.body()!!
            } else {
                Log.e("API", "Response failed: ${response.errorBody()?.string()}")
                null
            }

        } catch (e: Exception) {
            Log.e("API", "Network exception: ${e.message}")
            null
        }
    }

    fun getJwtToken(context: Context): String? {
        val securePrefs = getSecurePrefs(context)
        return securePrefs.getString("jwt_token", null)
    }

    fun decodeJwtPayload(jwt: String): JSONObject {
        val parts = jwt.split(".")
        if (parts.size != 3) throw IllegalArgumentException("Invalid JWT format")

        val payloadEncoded = parts[1]
        val decodedBytes = Base64.decode(payloadEncoded, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val decodedPayload = String(decodedBytes, Charsets.UTF_8)

        return JSONObject(decodedPayload)
    }

    fun convertToHourMinuteFormat(input: String): String {
        val hourRegex = Regex("(\\d+)\\s*hour")
        val minuteRegex = Regex("(\\d+)\\s*min")

        val hours = hourRegex.find(input)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val minutes = minuteRegex.find(input)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        return String.format("%02d:%02d", hours, minutes)
    }

    fun addTimeStrings(time1: String, time2: String): String {
        fun toMinutes(time: String): Int {
            val parts = time.split(":")
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            return hours * 60 + minutes
        }

        val totalMinutes = toMinutes(time1) + toMinutes(time2)
        val resultHours = totalMinutes / 60
        val resultMinutes = totalMinutes % 60

        return String.format("%02d:%02d", resultHours, resultMinutes)
    }
}