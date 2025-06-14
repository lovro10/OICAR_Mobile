package org.oicar

import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.oicar.models.ImageDocument
import org.oicar.models.KorisnikImage
import org.oicar.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class UserRegistrationIntegrationTest {

    @Test
    fun testUserRegistration() {

        val latch = CountDownLatch(6)

        val registerRequest = mapOf(
            "username" to "user",
            "password" to "pass",
            "email" to "userpass@test.com",
            "ime" to "User",
            "prezime" to "Pass",
            "telefon" to "0956541237",
            "datumrodjenja" to "2003-06-11"
        )

        var registeredUserId: String? = null
        val uploadedImageIds = mutableListOf<String>()

        ApiClient.retrofit.registerUser(Gson().toJson(registerRequest))
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {

                        registeredUserId = response.body().toString()

                    } else {
                        fail("Registration failed: ${response.errorBody()?.string()}")
                    }
                    latch.countDown()
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    fail("Registration failed: ${t.message}")
                    latch.countDown()
                }
            })

        repeat(5) { i ->
            val image = ImageDocument(
                name = "Image ${i}",
                base64Content = "iVBORw0KGgoAAAANSUhEUgAAAQAAAAEACAIAAADTED8xAAADMElEQVR4nOzVwQnAIBQFQYXff81RUkQCOyDj1YOPnbXWPmeTRef+/3O/OyBjzh3CD95BfqICMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMK0CMO0TAAD//2Anhf4QtqobAAAAAElFTkSuQmCC",
                imageTypeId = 3
            )

            ApiClient.retrofit.uploadImage(image).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        response.body()?.let { imageId ->
                            uploadedImageIds.add(imageId)
                        }
                    } else {
                        fail("Image upload failed: ${response.errorBody()?.string()}")
                    }
                    latch.countDown()
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    fail("Image upload failed: ${t.message}")
                    latch.countDown()
                }
            })
        }

        val completed = latch.await(60, TimeUnit.SECONDS)
        assertTrue("Too much time for execution of the API calls", completed)

        assertNotNull(registeredUserId)
        assertEquals(5, uploadedImageIds.size)

        uploadedImageIds.forEach { imageId ->
            val korisnikImage = KorisnikImage(
                registeredUserId!!,
                imageId
            )

            runBlocking {

                val response = ApiClient.retrofit.createKorisnikImage(korisnikImage).execute()
                assertTrue("Failed", response.isSuccessful)
            }
        }

        ActivityScenario.launch(RegisterPendingScreen::class.java)

        onView(withId(R.id.headerRegistrationPending))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("THANK YOU FOR YOUR REGISTRATION!"))))
    }
}