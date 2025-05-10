package org.oicar

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.oicar.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class BrowseScreen : AppCompatActivity() {

    private lateinit var autoCompleteTextViewFrom: AutoCompleteTextView
    private lateinit var autoCompleteTextViewTo: AutoCompleteTextView
    private lateinit var cities : MutableList<String>
    private lateinit var citiesAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_browse_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_browse
        bottomNavigation.setOnItemSelectedListener() {
            when (it.itemId) {
                R.id.nav_browse -> {


                }
                R.id.nav_add_drive -> {

                    val intent = Intent(this, TripsScreen::class.java)
                    startActivity(intent)
                }
                R.id.nav_messages -> {

                    val intent = Intent(this, AddVehicleScreen::class.java)
                    startActivity(intent)
                }
                R.id.nav_profile -> {


                }
            }
            true
        }

        autoCompleteTextViewFrom = findViewById(R.id.autoCompleteTextViewFrom)
        autoCompleteTextViewTo = findViewById(R.id.autoCompleteTextViewTo)

        ApiClient.retrofit.getAllCities().enqueue(object : Callback<MutableList<String>> {
            override fun onResponse(call: Call<MutableList<String>>, response: Response<MutableList<String>>) {
                if (response.isSuccessful) {

                    cities = response.body()!!

                    citiesAdapter = ArrayAdapter(this@BrowseScreen, android.R.layout.simple_list_item_1, cities)

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

        var btn_swap_destination: Button = findViewById(R.id.btn_swap_destination)

        btn_swap_destination.setOnClickListener {

            var tempFrom = autoCompleteTextViewFrom.text
            var tempTo = autoCompleteTextViewTo.text

            autoCompleteTextViewFrom.text = tempTo
            autoCompleteTextViewTo.text = tempFrom
        }

        var choose_date_of_departure : TextView = findViewById(R.id.choose_date_of_departure)

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
    }
}