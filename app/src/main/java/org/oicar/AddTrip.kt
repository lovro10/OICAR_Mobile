package org.oicar

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.ResponseBody
import org.json.JSONObject
import org.oicar.BrowseScreen
import org.oicar.models.OglasVoznja
import org.oicar.models.Vehicle
import org.oicar.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTrip : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var vehicles : MutableList<Vehicle> = mutableListOf()
    private var vehiclesData : MutableList<String> = mutableListOf()

    private lateinit var autoCompleteTextViewFrom: AutoCompleteTextView
    private lateinit var autoCompleteTextViewTo: AutoCompleteTextView
    private lateinit var cities : MutableList<String>
    private lateinit var citiesAdapter: ArrayAdapter<String>

    private lateinit var selectedVehicleString : String
    private lateinit var selectedVehicle : Vehicle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_trip)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val jwt = getJwtToken(this)

        val payload = decodeJwtPayload(jwt!!)

        ApiClient.retrofit.getAllVehiclesForCurrentDriver(payload.getString("sub").toInt()).enqueue(object : Callback<MutableList<Vehicle>> {
            override fun onResponse(call: Call<MutableList<Vehicle>>, response: Response<MutableList<Vehicle>>) {
                if (response.isSuccessful) {

                    vehicles = response.body()!!

                    for (vehicle in vehicles) {

                        vehiclesData.add("${vehicle.naziv} (${vehicle.marka} ${vehicle.model})")
                    }

                    val selectVehicleSpinner : Spinner = findViewById(R.id.selectVehicleSpinner)

                    selectVehicleSpinner.onItemSelectedListener = this@AddTrip

                    val selectVehicleSpinnerAdapter: ArrayAdapter<*> = ArrayAdapter<String>(this@AddTrip,
                        android.R.layout.simple_spinner_item, vehiclesData
                    )

                    selectVehicleSpinnerAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                    )

                    selectVehicleSpinner.adapter = selectVehicleSpinnerAdapter

                } else {
                    println("FAIL")
                    println(response)
                }
            }

            override fun onFailure(call: Call<MutableList<Vehicle>>, t: Throwable) {
                Log.e("API", "Network error: ${t.message}")
            }
        })

        var choose_date_of_departure : TextView = findViewById(R.id.choose_date_of_departure)
        var choose_time_of_departure : TextView = findViewById(R.id.choose_time_of_departure)

        choose_date_of_departure.setOnClickListener {

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            var monthFormatted : String
            var dayFormatted : String

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    if(selectedMonth + 1 < 10) {
                        monthFormatted = "0${(selectedMonth + 1).toString()}"
                    }
                    else {
                        monthFormatted = (selectedMonth + 1).toString()
                    }
                    if(selectedDay < 10) {
                        dayFormatted = "0${(selectedDay).toString()}"
                    }
                    else {
                        dayFormatted = (selectedDay).toString()
                    }
                    val selectedDate = "${dayFormatted}.${monthFormatted}.$selectedYear"
                    choose_date_of_departure.text = selectedDate
                }, year, month, day
            )

            datePickerDialog.show()
        }

        choose_time_of_departure.setOnClickListener {

            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    var selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    choose_time_of_departure.text = selectedTime
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        var choose_number_of_passengers : TextView = findViewById(R.id.choose_number_of_passengers)

        choose_number_of_passengers.setOnClickListener {

            val numberPicker = NumberPicker(this).apply {
                minValue = 1
                maxValue = 10
            }

            val dialog = AlertDialog.Builder(this)
                .setTitle("Select a Number")
                .setView(numberPicker)
                .setPositiveButton("OK") { _, _ ->
                    val selectedNumber = numberPicker.value
                    choose_number_of_passengers.text = selectedNumber.toString()
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()
        }

        autoCompleteTextViewFrom = findViewById(R.id.autoCompleteTextViewFrom)
        autoCompleteTextViewTo = findViewById(R.id.autoCompleteTextViewTo)

        ApiClient.retrofit.getAllCities().enqueue(object : Callback<MutableList<String>> {
            override fun onResponse(call: Call<MutableList<String>>, response: Response<MutableList<String>>) {
                if (response.isSuccessful) {

                    cities = response.body()!!

                    citiesAdapter = ArrayAdapter(this@AddTrip, android.R.layout.simple_list_item_1, cities)

                    autoCompleteTextViewFrom.setAdapter(citiesAdapter)
                    autoCompleteTextViewTo.setAdapter(citiesAdapter)


                } else {
                    println("FAIL")
                    println(response)
                }
            }

            override fun onFailure(call: Call<MutableList<String>>, t: Throwable) {
                Log.e("API", "Network error: ${t.message}")
            }
        })

        val btnAddTrip : Button = findViewById(R.id.btnAddTrip)

        btnAddTrip.setOnClickListener {

            val choose_date_of_departure : TextView = findViewById(R.id.choose_date_of_departure)
            val choose_time_of_departure : TextView = findViewById(R.id.choose_time_of_departure)
            val autoCompleteTextViewFrom : AutoCompleteTextView = findViewById(R.id.autoCompleteTextViewFrom)
            val autoCompleteTextViewTo : AutoCompleteTextView = findViewById(R.id.autoCompleteTextViewTo)
            val tripCostTolls : EditText = findViewById(R.id.tripCostTolls)
            val tripCostGas : EditText = findViewById(R.id.tripCostGas)
            val choose_number_of_passengers : TextView = findViewById(R.id.choose_number_of_passengers)

            val inputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val date = inputFormat.parse(choose_date_of_departure.text.toString())
            val formattedDate = outputFormat.format(date!!)

            val completeDateOfDeparture : String = "${formattedDate}T${choose_time_of_departure.text.toString()}"

            val jwt = getJwtToken(this)

            val payload = decodeJwtPayload(jwt!!)

            val trip : OglasVoznja = OglasVoznja(

                selectedVehicle.idvozilo,
                payload.getString("sub").toString(),
                completeDateOfDeparture,
                "2025-05-29T10:00",
                tripCostTolls.text.toString(),
                tripCostGas.text.toString(),
                choose_number_of_passengers.text.toString(),
                autoCompleteTextViewFrom.text.toString(),
                autoCompleteTextViewTo.text.toString(),
                "Available"
            )

            ApiClient.retrofit.createOglasVoznja(trip).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {

                        println("Success")


                    } else {
                        println("FAIL")
                        println(response)
                        println(trip)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("API", "Network error: ${t.message}")
                }
            })

            val intent = Intent(this, MyTrips::class.java)
            startActivity(intent)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int,id: Long) {

        selectedVehicleString = parent?.getItemAtPosition(position).toString()

        val vehicle : Vehicle

        for (item in vehicles) {

            if (item.naziv == selectedVehicleString.substringBefore('(').trim()) {

                selectedVehicle = item
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}


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
}