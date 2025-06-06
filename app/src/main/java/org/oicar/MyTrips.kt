package org.oicar

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.internal.TextScale
import org.json.JSONObject
import org.oicar.models.Trip
import org.oicar.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime

class MyTrips : AppCompatActivity() {

    private lateinit var listOfTrips: List<Trip>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_trips)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_add_drive
        bottomNavigation.setOnItemSelectedListener() {
            when (it.itemId) {
                R.id.nav_browse -> {

                    val intent = Intent(this, BrowseScreen::class.java)
                    startActivity(intent)
                }
                R.id.nav_add_drive -> {


                }
                R.id.nav_messages -> {


                }
                R.id.nav_profile -> {


                }
            }
            true
        }

        val jwt = getJwtToken(this)

        val payload = decodeJwtPayload(jwt!!)

        ApiClient.retrofit.getAllTripsForCurrentUser(payload.getString("sub").toInt()).enqueue(object : Callback<List<Trip>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<Trip>>, response: Response<List<Trip>>) {
                if (response.isSuccessful) {

                    listOfTrips = response.body()!!
                    println(response.body())

                    for (trip in listOfTrips) {

                        val tripCard = createTripCard(trip)
                        val containerForTripCards = findViewById<LinearLayout>(R.id.layout_my_trips)
                        containerForTripCards.addView(tripCard)
                    }


                } else {
                    println("FAIL")
                    println(response)
                }
            }

            override fun onFailure(call: Call<List<Trip>>, t: Throwable) {
                Log.e("API", "Network error: ${t.message}")
            }
        })

        var addTripButton : TextView = findViewById(R.id.addTripButton)

        addTripButton.setOnClickListener {

            val intent = Intent(this, AddTrip::class.java)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createTripCard(trip: Trip): CardView {
        val context = this

        val cardView = CardView(context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }
            radius = 50f
            cardElevation = 8f
            setContentPadding(16, 16, 16, 16)
            isClickable = true
            isFocusable = true

            setOnClickListener {
                println("Clicked!")
                println(trip)
            }
        }

        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val leftLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val timeColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                marginEnd = -20
            }
            gravity = Gravity.CENTER
        }

        timeColumn.addView(TextView(context).apply {

            val unformattedDateTime = trip.datumIVrijemePolaska
            val extractedDateTime = LocalDateTime.parse(unformattedDateTime)
            val formattedDateTime = extractedDateTime.toLocalTime()

            text = formattedDateTime.toString()
            setTypeface(null, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0,
                1f
            ).apply {
                topMargin = 10
            }
        })

        timeColumn.addView(TextView(context).apply {
            text = "~04:00"
            setTypeface(null, Typeface.ITALIC)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0,
                1f
            )
        })

        timeColumn.addView(TextView(context).apply {
            text = "04:00"
            setTypeface(null, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0,
                1f
            )
        })

        val imageColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                marginEnd = -20
            }
        }

        imageColumn.addView(ImageView(context).apply {
            setImageResource(R.drawable.icon_travel_line)
            rotation = 90f
        })

        val locationColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ).apply {
                marginEnd = -20
            }
        }

        locationColumn.addView(TextView(context).apply {
            text = trip.polaziste
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0,
                1f
            ).apply {
                topMargin = 2
            }
        })

        locationColumn.addView(TextView(context).apply {
            text = trip.odrediste
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.BOTTOM
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0,
                1f
            ).apply {
                bottomMargin = 4
            }
        })

        val priceText = TextView(context).apply {
            text = trip.cijenaPoPutniku.toString()
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_euro, 0)
            compoundDrawablePadding = (10 * Resources.getSystem().displayMetrics.scaledDensity).toInt()
            compoundDrawableTintList = ContextCompat.getColorStateList(context, R.color.black)
        }

        leftLayout.addView(timeColumn)
        leftLayout.addView(imageColumn)
        leftLayout.addView(locationColumn)
        topRow.addView(leftLayout)
        topRow.addView(priceText)

        // Driver info row
        val driverRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 0)
        }

//        val profileImage = ImageView(context).apply {
//            layoutParams = LinearLayout.LayoutParams(40.dpToPx(), 40.dpToPx()).apply {
//                marginEnd = 8.dpToPx()
//            }
//            scaleType = ImageView.ScaleType.CENTER_CROP
//            setImageResource(R.drawable.profile_placeholder) // Your placeholder
//        }

        val driverInfo = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        driverInfo.addView(TextView(context).apply {
            text = "${trip.ime} ${trip.prezime} (${trip.username})"
            setTypeface(null, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        driverInfo.addView(TextView(context).apply {
            text = "${trip.brojPutnika}"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            gravity = Gravity.CENTER_VERTICAL
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_number_of_passengers, 0, 0, 0)
            compoundDrawablePadding = (10 * Resources.getSystem().displayMetrics.scaledDensity).toInt()
            compoundDrawableTintList = ContextCompat.getColorStateList(context, R.color.black)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        })

//        driverRow.addView(profileImage)
        driverRow.addView(driverInfo)

        rootLayout.addView(topRow)
        rootLayout.addView(driverRow)
        cardView.addView(rootLayout)

        return cardView
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
}