package org.oicar

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.containsString
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.oicar.models.KorisnikDetails
import org.oicar.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class UserLoginIntegrationTest {

    @Test
    fun testLoginAndFetchProfile() {
        val latch = CountDownLatch(2)

        val username = "TestUsername"
        val password = "TestPassword"

        val requestBody = mapOf(
            "username" to username,
            "password" to password
        )

        var jwtToken: String? = null
        var userId: Int? = null
        var userDetails: KorisnikDetails? = null

        ApiClient.retrofit.loginUser(requestBody).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {

                    jwtToken = response.body()

                    assertNotNull(jwtToken)

                    userId = extractUserIdFromJwt(jwtToken!!)

                } else {

                    fail("Failed: ${response.errorBody()}")
                }

                latch.countDown()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                fail("Failed: ${t.message}")
                latch.countDown()
            }
        })

        latch.await(5, TimeUnit.SECONDS)

        assertNotNull(userId)

        runBlocking {

            val profileResponse = ApiClient.retrofit.getCurrentUserDetails(userId!!).execute()

            if (profileResponse.isSuccessful) {

                userDetails = profileResponse.body()

            } else {
                fail("Failed: ${profileResponse.errorBody()?.string()}")
            }
            latch.countDown()
        }

        val completed = latch.await(60, TimeUnit.SECONDS)
        assertTrue("Too much time for execution of the API calls", completed)

        assertNotNull(userDetails)
        assertEquals(username, userDetails?.username)
    }

    private fun extractUserIdFromJwt(jwt: String): Int {

        val parts = jwt.split(".")

        if (parts.size != 3) {

            throw IllegalArgumentException("Invalid JWT format")
        }
        val payloadEncoded = parts[1]
        val decodedBytes = Base64.decode(payloadEncoded, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val decodedPayload = String(decodedBytes, Charsets.UTF_8)

        val json = JSONObject(decodedPayload)

        return json.getString("sub").toInt()
    }

    @Test
    fun testLoginFailureWithUi() {

        ActivityScenario.launch(MainActivity::class.java)

        val username = "userTest"
        val password = "passTest"

        onView(withId(R.id.usernameEditText)).perform(typeText(username), closeSoftKeyboard())
        onView(withId(R.id.passwordEditText)).perform(typeText(password), closeSoftKeyboard())

        onView(withId(R.id.loginButton)).perform(click())

        Thread.sleep(2000)

        onView(withId(R.id.errorMessageIncPassUser))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("Incorrect username or password!"))))
    }
}
