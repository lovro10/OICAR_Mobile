package org.oicar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import okhttp3.ResponseBody
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

                        println("Success")

                        val intent = Intent(this@MainActivity, BrowseScreen::class.java)
                        startActivity(intent)


                    } else {
                        println("FAIL")
                        println(response)
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
}

