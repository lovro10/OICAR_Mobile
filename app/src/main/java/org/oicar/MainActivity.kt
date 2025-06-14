package org.oicar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import org.oicar.models.KorisnikDetails
import org.oicar.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun getSecurePrefs(context: Context): EncryptedSharedPreferences {

    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    return EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    ) as EncryptedSharedPreferences
}

class MainActivity : AppCompatActivity() {

    private var currentUserId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val registerButton : TextView = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {

            val intent = Intent(this, RegisterChoosable::class.java)
            startActivity(intent)
        }

        val loginButton : Button = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {

            val username : String = findViewById<EditText?>(R.id.usernameEditText).text.toString()
            val password : String = findViewById<EditText?>(R.id.passwordEditText).text.toString()

            var requestBody = mapOf(
                "username" to username,
                "password" to password
            )

            ApiClient.retrofit.loginUser(requestBody).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {

                        saveJwtToken(this@MainActivity, response.body().toString())

                        val jwt = response.body().toString()
                        val payload = decodeJwtPayload(jwt)
                        currentUserId = payload.getString("sub").toInt()

                        ApiClient.retrofit.getCurrentUserDetails(currentUserId).enqueue(object : Callback<KorisnikDetails> {
                            override fun onResponse(call: Call<KorisnikDetails>, response: Response<KorisnikDetails>) {
                                if (response.isSuccessful) {

                                    val userDetails = response.body()

                                    if (userDetails?.isconfirmed == true) {

                                        val intent = Intent(this@MainActivity, BrowseScreen::class.java)
                                        startActivity(intent)
                                    }
                                    else {

                                        val intent = Intent(this@MainActivity, RegisterPendingScreen::class.java)
                                        startActivity(intent)
                                    }

                                } else {
                                    println("FAIL")
                                    println(response)
                                }
                            }

                            override fun onFailure(call: Call<KorisnikDetails>, t: Throwable) {
                                Log.e("API", "Network error: ${t.message}")
                            }
                        })

                    } else {

                        val errorMessageIncPassUser = findViewById<TextView>(R.id.errorMessageIncPassUser)

                        errorMessageIncPassUser.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("API", "Network error: ${t.message}")
                }
            })
        }
    }

    fun saveJwtToken(context: Context, token: String) {
        val securePrefs = getSecurePrefs(context)
        securePrefs.edit().putString("jwt_token", token).apply()
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

