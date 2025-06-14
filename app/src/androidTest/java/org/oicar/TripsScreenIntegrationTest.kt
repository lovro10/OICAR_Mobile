package org.oicar

import android.widget.LinearLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.oicar.models.Trip
import org.oicar.services.ApiClient


@RunWith(AndroidJUnit4::class)
class TripsScreenIntegrationTest {

    @Test
    fun testTripsScreenSuccessfullyLoadsWithTripsFromApi() {

        lateinit var trips: List<Trip>

        runBlocking {

            val tripsResponse = ApiClient.retrofit.getAllTrips().execute()


            require(tripsResponse.isSuccessful) { "Failed: ${tripsResponse.errorBody()?.string()}" }
            trips = tripsResponse.body()!!
            require(!trips.isNullOrEmpty()) { "Failed" }

        }


        val scenario = ActivityScenario.launch(TripsScreen::class.java)
        Thread.sleep(5000)

        scenario.onActivity { activity ->
            val layout = activity.findViewById<LinearLayout>(R.id.layout_trips)
            require(layout.childCount >= trips.size) { "Fail" }
        }

        onView(withId(R.id.layout_trips)).check(matches(isDisplayed()))
    }

    @Test
    fun testTripsScreenDisplaysCorrectTripPolaziste() {

        lateinit var trips: List<Trip>
        lateinit var expectedTitles: List<String>

        runBlocking {

            val tripsResponse = ApiClient.retrofit.getAllTrips().execute()

            require(tripsResponse.isSuccessful) { "Failed" }
            val trips = tripsResponse.body()
            require(!trips.isNullOrEmpty()) { "Failed" }

            expectedTitles = trips.take(trips.size).mapNotNull { it.polaziste }
        }

        ActivityScenario.launch(TripsScreen::class.java)
        Thread.sleep(3000)

        if(!expectedTitles.isNullOrEmpty()) {

            var counter = 0

            for (title in expectedTitles) {

                onView(withTagValue(`is`("polaziste${counter++}")))
                    .check(matches(isDisplayed()))
                    .check(matches(withText(containsString(title))))
            }
        }
    }

    @Test
    fun testTripScreenHandlesEmptyTrips() {

        lateinit var originalTrips: List<Trip>

        runBlocking {

            val response = ApiClient.retrofit.getAllTrips().execute()

            originalTrips = response.body()!!

        }

        if (originalTrips.isNullOrEmpty()) {

            val scenario = ActivityScenario.launch(TripsScreen::class.java)
            Thread.sleep(3000)

            scenario.onActivity { activity ->

                val layout = activity.findViewById<LinearLayout>(R.id.layout_trips)

                require(layout.childCount == 0 || layout.childCount == 1)
            }
        } else {

            println("API not empty")
        }
    }
}
