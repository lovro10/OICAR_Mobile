package org.oicar

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class RegisterDriverAndPassenger : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_driver_and_passenger)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_register_driver_and_passenger_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val openDatePickerButton : Button = findViewById<Button>(R.id.openDatePickerButton)
        val selectedDateTextView : TextView = findViewById<TextView>(R.id.selectedDateTextView)
        val nextRegisterStepButton : Button = findViewById<Button>(R.id.nextRegisterStepButton)

        openDatePickerButton.setOnClickListener {

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            var monthFormatted : String

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    if(selectedMonth + 1 < 10) {
                        monthFormatted = "0${(selectedMonth + 1).toString()}"
                    }
                    else {
                        monthFormatted = (selectedMonth + 1).toString()
                    }
                    val selectedDate = "$selectedYear-${monthFormatted}-$selectedDay"
                    selectedDateTextView.text = selectedDate
                }, year, month, day
            )

            datePickerDialog.show()
        }

        nextRegisterStepButton.setOnClickListener {

            val firstName : String = findViewById<EditText?>(R.id.firstName).text.toString()
            val lastName : String = findViewById<EditText?>(R.id.lastName).text.toString()

            val dateOfBirth : String = findViewById<TextView?>(R.id.selectedDateTextView).text.toString()

            val email : String = findViewById<EditText?>(R.id.email).text.toString()
            val confirmEmail : String = findViewById<EditText?>(R.id.confirmEmail).text.toString()
            val phone : String = findViewById<EditText?>(R.id.phone).text.toString()

            val registerStepOneData = mutableMapOf(
                "ime" to firstName,
                "prezime" to lastName,
                "datumrodjenja" to dateOfBirth,
                "email" to email,
                "telefon" to phone
            )

            val registerStepOneJson = Gson().toJson(registerStepOneData)

            val intent = Intent(this, RegisterDriverStepTwo::class.java)
            intent.putExtra("registerStepOneData", registerStepOneJson)
            startActivity(intent)
        }
    }
}