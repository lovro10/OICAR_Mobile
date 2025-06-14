package org.oicar

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.libraries.navigation.Waypoint
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.datatransport.BuildConfig
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.navigation.NavigationApi
import com.google.android.libraries.navigation.NavigationApi.NavigatorListener
import com.google.android.libraries.navigation.NavigationView
import com.google.android.libraries.navigation.Navigator
import com.google.android.libraries.navigation.Navigator.RouteStatus
import com.google.android.libraries.navigation.SimulationOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.oicar.models.DirectionResponse
import org.oicar.models.Trip
import org.oicar.services.ApiClient

class TripTracking : AppCompatActivity() {

    private var SPLASH_SCREEN_DELAY_MILLIS = 2000L
    private lateinit var navView: NavigationView
    private var mNavigator: Navigator? = null

    private var arrivalListener: Navigator.ArrivalListener? = null
    private var routeChangedListener: Navigator.RouteChangedListener? = null

    var navigatorScope: org.oicar.InitializedNavScope? = null
    var pendingNavActions = mutableListOf<org.oicar.InitializedNavRunnable>()

    private lateinit var googleMapsApiKey: String

    private lateinit var currentUserRole: String

    private lateinit var directionData: DirectionResponse

    private lateinit var trip : Trip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_trip_tracking)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        googleMapsApiKey = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
            .metaData
            .getString("com.google.android.geo.API_KEY").toString()

        val jwt = getJwtToken(this)
        val payload = decodeJwtPayload(jwt!!)
        currentUserRole = payload.getString("role").toString()

        val receivedTripInfoJson = intent.getStringExtra("trip")
        val type = object : TypeToken<Trip>() {}.type
        trip = Gson().fromJson<Trip>(receivedTripInfoJson, type)

        val btnEndTrip = findViewById<Button>(R.id.btnEndTrip)

        btnEndTrip.setOnClickListener {

            finish()
        }

        lifecycleScope.launch {

            directionData = getDirectionsData()!!
        }

        navView = findViewById(R.id.navigation_view)
        navView.onCreate(savedInstanceState)

        // Ensure the screen stays on during nav.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val permissions =
            if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                arrayOf(permission.ACCESS_FINE_LOCATION, permission.POST_NOTIFICATIONS)
            } else {
                arrayOf(permission.ACCESS_FINE_LOCATION)
            }

        if (permissions.any { !checkPermissionGranted(it) }) {

            if (permissions.any { shouldShowRequestPermissionRationale(it) }) {
                // Display a dialogue explaining the required permissions.
            }

            val permissionsLauncher =
                registerForActivityResult(
                    RequestMultiplePermissions(),
                    { permissionResults ->
                        if (permissionResults.getOrDefault(
                                permission.ACCESS_FINE_LOCATION,
                                false
                            )
                        ) {
                            onLocationPermissionGranted()
                        } else {
                            finish()
                        }
                    },
                )

            permissionsLauncher.launch(permissions)
        } else {
            android.os.Handler(Looper.getMainLooper())
                .postDelayed({ onLocationPermissionGranted() }, SPLASH_SCREEN_DELAY_MILLIS)
        }
    }

    private fun checkPermissionGranted(permissionToCheck: String): Boolean =
        ContextCompat.checkSelfPermission(this, permissionToCheck) == PackageManager.PERMISSION_GRANTED

    private fun onLocationPermissionGranted() {
        initializeNavigationApi()
    }

    /** Starts the Navigation API, capturing a reference when ready. */
    @SuppressLint("MissingPermission")
    private fun initializeNavigationApi() {
        NavigationApi.getNavigator(
            this,
            object : NavigatorListener {
                override fun onNavigatorReady(navigator: Navigator) {

                    navigator.setTaskRemovedBehavior(Navigator.TaskRemovedBehavior.QUIT_SERVICE)
                    // store a reference to the Navigator object
                    mNavigator = navigator

                    if (BuildConfig.DEBUG) {
                        mNavigator?.simulator?.setUserLocation(TripTracking.startLocation)
                    }

                    // code to start guidance will go here
                    registerNavigationListeners()

                    navView.getMapAsync {
                            googleMap  ->
                        googleMap.followMyLocation(GoogleMap.CameraPerspective.TILTED)
                    }

                    navigateToPlace(TripTracking.TRAFALGAR_SQUARE)
                }

                override fun onError(@NavigationApi.ErrorCode errorCode: Int) {
                    when (errorCode) {
                        NavigationApi.ErrorCode.NOT_AUTHORIZED -> {
                            // Note: If this message is displayed, you may need to check that
                            // your API_KEY is specified correctly in AndroidManifest.xml
                            // and is been enabled to access the Navigation API
                        }
                        NavigationApi.ErrorCode.TERMS_NOT_ACCEPTED -> {

                        }
                        else -> println("Error loading Navigation API: $errorCode")
                    }
                }
            },
        )
    }

    private fun registerNavigationListeners() {
        withNavigatorAsync {
            arrivalListener =
                Navigator.ArrivalListener { // Show an onscreen message
                    mNavigator?.clearDestinations()
                }
            mNavigator?.addArrivalListener(arrivalListener)

            routeChangedListener =
                Navigator.RouteChangedListener { // Show an onscreen message when the route changes
                }
            mNavigator?.addRouteChangedListener(routeChangedListener)
        }
    }

    private fun withNavigatorAsync(block: InitializedNavRunnable) {
        val navigatorScope = navigatorScope
        if (navigatorScope != null) {
            navigatorScope.block()
        } else {
            pendingNavActions.add(block)
        }
    }

    private fun navigateToPlace(placeId: String) {

        val waypoint: Waypoint? =
            // Set a destination by using a Place ID (the recommended method)
            try {
                Waypoint.builder().setLatLng(directionData.routes.first().legs.first().end_location.lat.toDouble(), directionData.routes.first().legs.first().end_location.lng.toDouble()).build()
            } catch (e: Waypoint.UnsupportedPlaceIdException) {
                return
            }

        val pendingRoute = mNavigator?.setDestination(waypoint)

        val departureLocation = LatLng(directionData.routes.first().legs.first().start_location.lat.toDouble(), directionData.routes.first().legs.first().start_location.lng.toDouble())

        mNavigator?.simulator?.setUserLocation(departureLocation)

        // Set an action to perform when a route is determined to the destination
        pendingRoute?.setOnResultListener { code ->
            when (code) {
                RouteStatus.OK -> {
                    // Hide the toolbar to maximize the navigation UI
                    supportActionBar?.hide()

                    mNavigator?.simulator?.simulateLocationsAlongExistingRoute(
                        SimulationOptions().speedMultiplier(5f)
                    )

                    mNavigator?.startGuidance()
                }

                RouteStatus.ROUTE_CANCELED -> println("Route guidance canceled.")
                RouteStatus.NO_ROUTE_FOUND,
                RouteStatus.NETWORK_ERROR ->

                    println("Error starting guidance: $code")

                else -> println("Error starting guidance: $code")
            }
        }
    }

    companion object{
        const val TRAFALGAR_SQUARE ="ChIJq9PmsTzUZUcRyyAKdLVs1F0"
        val startLocation = LatLng(45.780501762663704, 15.961754912499313)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        navView.onSaveInstanceState(savedInstanceState)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        navView.onTrimMemory(level)
    }

    override fun onStart() {
        super.onStart()
        navView.onStart()
    }

    override fun onResume() {
        super.onResume()
        navView.onResume()
    }

    override fun onPause() {
        navView.onPause()
        super.onPause()
    }

    override fun onConfigurationChanged(configuration: Configuration) {
        super.onConfigurationChanged(configuration)
        navView.onConfigurationChanged(configuration)
    }

    override fun onStop() {
        navView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        navView.onDestroy()
        withNavigatorAsync {

            if (arrivalListener != null) {
                navigator.removeArrivalListener(arrivalListener)
            }
            if (routeChangedListener != null) {
                navigator.removeRouteChangedListener(routeChangedListener)
            }

            navigator.simulator?.unsetUserLocation()
            navigator.cleanup()
        }
        super.onDestroy()
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
}

open class InitializedNavScope(val navigator: Navigator)

typealias InitializedNavRunnable = InitializedNavScope.() -> Unit