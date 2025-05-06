package org.oicar

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
import okhttp3.ResponseBody
import org.oicar.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

            ApiClient.retrofit.loginUser(requestBody).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {



                        println("Success")


                    } else {
                        println("FAIL")
                        println(response)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("API", "Network error: ${t.message}")
                }
            })
        }
    }
}