package org.oicar

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.ResponseBody
import org.json.JSONObject
import org.oicar.models.KorisnikDetails
import org.oicar.services.ApiClient
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RequestForClearingDataScreen : AppCompatActivity() {

    private var currentUserId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_request_for_clearing_data_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val jwt = getJwtToken(this)
        val payload = decodeJwtPayload(jwt!!)
        currentUserId = payload.getString("sub").toInt()

        val acceptDataDeletion = findViewById<CheckBox>(R.id.acceptDataDeletion)

        val deleteData = findViewById<Button>(R.id.deleteData)

        val backDeleteData = findViewById<Button>(R.id.backDeleteData)
        backDeleteData.setOnClickListener {

            finish()
        }

        deleteData.setOnClickListener {

            if (!acceptDataDeletion.isChecked) {

                AlertDialog.Builder(this)
                    .setTitle("Data deletion instructions")
                    .setMessage("You must check the check box so we know you are sure you want do delete.")
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
            else {

                AlertDialog.Builder(this)
                    .setTitle("Data deletion instructions")
                    .setMessage("You must check the check box so we know you are sure you want do delete.")
                    .setCancelable(false)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()

                        ApiClient.retrofit.sendRequestForDataDeletion(currentUserId).enqueue(object : Callback<ResponseBody> {
                            @RequiresApi(Build.VERSION_CODES.O)
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                if (response.isSuccessful) {

                                    val headerClearData = findViewById<TextView>(R.id.headerClearData)
                                    val deletePara1 = findViewById<TextView>(R.id.deletePara1)
                                    val deletePara2 = findViewById<TextView>(R.id.deletePara2)
                                    val deletePara3 = findViewById<TextView>(R.id.deletePara3)

                                    headerClearData.text = "THANK YOU FOR EVERYTHING, IT HAS BEEN A PLEASURE!"
                                    deletePara1.text = "If you wish to come back, feel free to register again, till next time!"
                                    deleteData.visibility = View.GONE
                                    deletePara2.text = "The administrator will delete your data as soon as possible!"
                                    deletePara3.visibility = View.GONE
                                    acceptDataDeletion.visibility = View.GONE

                                    backDeleteData.text = "close application"

                                    backDeleteData.setOnClickListener {

                                        finishAffinity()
                                    }

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
                    .setNegativeButton("Close") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
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