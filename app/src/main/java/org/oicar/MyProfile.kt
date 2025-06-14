package org.oicar

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject
import org.oicar.MainActivity
import org.oicar.models.KorisnikDetails
import org.oicar.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MyProfile : AppCompatActivity() {

    private var currentUserId: Int = 0
    private lateinit var userDetails: KorisnikDetails

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_my_profile)
        bottomNavigation.selectedItemId = R.id.nav_profile
        bottomNavigation.setOnItemSelectedListener() {
            when (it.itemId) {
                R.id.nav_browse -> {

                    val intent = Intent(this, BrowseScreen::class.java)
                    startActivity(intent)
                }
                R.id.nav_add_drive -> {

                    val intent = Intent(this, MyTrips::class.java)
                    startActivity(intent)
                }
                R.id.nav_messages -> {

                    val intent = Intent(this, AddVehicleScreen::class.java)
                    startActivity(intent)
                }
                R.id.nav_profile -> {

                    val intent = Intent(this, MyProfile::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        val jwt = getJwtToken(this)
        val payload = decodeJwtPayload(jwt!!)
        currentUserId = payload.getString("sub").toInt()

        val myTripsTab = findViewById<LinearLayout>(R.id.myTripsTab)
        val rightToForgetTab = findViewById<LinearLayout>(R.id.rightToForgetTab)

        myTripsTab.setOnClickListener {

            val intent = Intent(this, MyTrips::class.java)
            startActivity(intent)
        }

        rightToForgetTab.setOnClickListener {

            val intent = Intent(this, RequestForClearingDataScreen::class.java)
            startActivity(intent)
        }



        ApiClient.retrofit.getCurrentUserDetails(currentUserId).enqueue(object : Callback<KorisnikDetails> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<KorisnikDetails>, response: Response<KorisnikDetails>) {
                if (response.isSuccessful) {

                    val userDetails = response.body()

                    val driverNameAndUsername = findViewById<TextView>(R.id.driverNameAndUsername)
                    val driverDateOfBirth = findViewById<TextView>(R.id.driverDateOfBirth)
                    val driverTelephone = findViewById<TextView>(R.id.driverTelephone)
                    val driverEmail = findViewById<TextView>(R.id.driverEmail)

                    driverNameAndUsername.text = "${userDetails?.ime} ${userDetails?.prezime} (${userDetails?.username})"

                    val inputDate = userDetails?.datumRodjenja
                    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

                    val date = LocalDate.parse(inputDate, inputFormatter)
                    val formattedDate = date.format(outputFormatter)

                    driverDateOfBirth.text = formattedDate

                    driverTelephone.text = userDetails?.telefon
                    driverEmail.text = userDetails?.email

                } else {
                    println("FAIL")
                    println(response)
                }
            }

            override fun onFailure(call: Call<KorisnikDetails>, t: Throwable) {
                Log.e("API", "Network error: ${t.message}")
            }
        })
    }

    fun decodeJwtPayload(jwt: String): JSONObject {
        val parts = jwt.split(".")
        if (parts.size != 3) throw IllegalArgumentException("Invalid JWT format")

        val payloadEncoded = parts[1]
        val decodedBytes = Base64.decode(payloadEncoded, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val decodedPayload = String(decodedBytes, Charsets.UTF_8)

        return JSONObject(decodedPayload)
    }

    fun getJwtToken(context: Context): String? {
        val securePrefs = getSecurePrefs(context)
        return securePrefs.getString("jwt_token", null)
    }
}