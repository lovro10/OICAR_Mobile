package org.oicar

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.oicar.models.Trip
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale

class TripDetails : AppCompatActivity() {

    private lateinit var trip : Trip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trip_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val receivedTripInfoJson = intent.getStringExtra("trip")
        val type = object : TypeToken<Trip>() {}.type
        trip = Gson().fromJson<Trip>(receivedTripInfoJson, type)

        val departureLocation = findViewById<TextView>(R.id.departureLocation)
        val arrivalLocation = findViewById<TextView>(R.id.arrivalLocation)
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
        departureTime.text = time
        departureDate.text = formattedDate
        tripCarName.text = "${trip.marka} ${trip.model} (${trip.registracija.substring(2, trip.registracija.length)})"
        numberOfPassengers.text = trip.brojPutnika.toString()
        driverName.text = "${trip.ime} ${trip.prezime}"
        driverTelephone.text = trip.username
        tripPrice.text = BigDecimal(trip.cijenaPoPutniku).setScale(2, RoundingMode.HALF_UP).toString()
    }
}