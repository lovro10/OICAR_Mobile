package org.oicar

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.oicar.models.DirectionResponse
import org.oicar.models.Trip
import org.oicar.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

class TripsScreen : AppCompatActivity() {

    private lateinit var listOfTrips: List<Trip>

    private lateinit var googleMapsApiKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trips)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        googleMapsApiKey = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
            .metaData
            .getString("com.google.android.geo.API_KEY").toString()

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_browse
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


                }
            }
            true
        }

        ApiClient.retrofit.getAllTrips().enqueue(object : Callback<List<Trip>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<List<Trip>>, response: Response<List<Trip>>) {
                if (response.isSuccessful) {

                    listOfTrips = response.body()!!
                    println(response.body())

                    for (trip in listOfTrips) {

                        lifecycleScope.launch {
                            runAndWait(trip)

                            val tripCard = createTripCard(trip)
                            val containerForTripCards = findViewById<LinearLayout>(R.id.layout_trips)
                            containerForTripCards.addView(tripCard)
                        }
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

    }

    var cardViews: MutableList<CardView> = mutableListOf()

    var approximateDuration: String = "00000"

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

                val intent = Intent(this@TripsScreen, TripDetails::class.java)
                intent.putExtra("trip", Gson().toJson(trip))
                startActivity(intent)
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
            text = "00:00"
            tag = "durationText"
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
            text = BigDecimal(trip.cijenaPoPutniku).setScale(2, RoundingMode.HALF_UP).toString()
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

        driverRow.addView(driverInfo)

        rootLayout.addView(topRow)
        rootLayout.addView(driverRow)
        cardView.addView(rootLayout)

        cardViews.add(cardView)
        return cardView
    }

    suspend fun runAndWait(trip: Trip) {

        withContext(Dispatchers.IO) {
            getDirectionsDetails(trip)
        }
    }

    var ests: MutableList<String> = mutableListOf<String>()

    fun getDirectionsDetails(trip: Trip) {

        ApiClient.retrofitGoogle.getDirections(trip.polaziste, trip.odrediste, googleMapsApiKey).enqueue(object : Callback<DirectionResponse> {
            override fun onResponse(call: Call<DirectionResponse>, response: Response<DirectionResponse>) {
                if (response.isSuccessful) {

                    val directions = response.body()!!

                    approximateDuration = directions.routes.first().legs.first().duration.text

                    ests.add(approximateDuration)

                    println(approximateDuration)

                } else {
                    println("FAIL")
                    println(response)
                }
            }

            override fun onFailure(call: Call<DirectionResponse>, t: Throwable) {
                Log.e("API", "Network error: ${t.message}")
            }
        })
    }

}