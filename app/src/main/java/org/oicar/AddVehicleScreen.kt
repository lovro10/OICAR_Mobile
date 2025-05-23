package org.oicar

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import org.oicar.models.ImageDocument
import org.oicar.models.KorisnikImage
import org.oicar.models.TempDataHolder
import org.oicar.models.Vehicle
import org.oicar.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddVehicleScreen : AppCompatActivity() {

    private lateinit var frontImagePdView : ImageView
    private lateinit var backImagePdView : ImageView

    private lateinit var btnAddVehicle : Button

    private val GALLERY_REQUEST_CODE = 101

    private lateinit var createdImages : MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_vehicle)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        frontImagePdView = findViewById(R.id.frontImagePdView)
        backImagePdView = findViewById(R.id.backImagePdView)

        btnAddVehicle = findViewById(R.id.btnAddVehicle)

        createdImages = mutableListOf()

        findViewById<Button>(R.id.uploadPhotoPdButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(Intent.createChooser(intent, "Select ID Photos"), GALLERY_REQUEST_CODE)
        }

        btnAddVehicle.setOnClickListener {

            val registrationCardImages = mutableListOf(
                ImageDocument("FrontPd", frontImagePdBase64.toString(), 4),
                ImageDocument("BackPd", backImagePdBase64.toString(), 4)
            )

//            val registrationCardImagesJson = Gson().toJson(registrationCardImages)
//
//            TempDataHolder.jsonForHttpRequest = registrationCardImagesJson

            val vehicleNameEditText : String = findViewById<EditText?>(R.id.vehicleNameEditText).text.toString()
            val vehicleTypeEditText : String = findViewById<EditText?>(R.id.vehicleTypeEditText).text.toString()
            val vehicleModelEditText : String = findViewById<EditText?>(R.id.vehicleModelEditText).text.toString()
            val vehicleRegCountryCodeEditText : String = findViewById<EditText?>(R.id.vehicleRegCountryCodeEditText).text.toString()
            val vehicleRegNumberEditText : String = findViewById<EditText?>(R.id.vehicleRegNumberEditText).text.toString()

            val vehicleFullRegistration = "${vehicleRegCountryCodeEditText}${vehicleRegNumberEditText}"

            val jwt = getJwtToken(this)

            val payload = decodeJwtPayload(jwt!!)

            var vehicleData = Vehicle(

                1,
                vehicleNameEditText,
                vehicleTypeEditText,
                vehicleModelEditText,
                vehicleFullRegistration,
                payload.getString("sub").toString()
            )

            ApiClient.retrofit.createVehicle(vehicleData).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {

                        println("Success")


                    } else {
                        println("FAIL")
                        println(response)
                        println(vehicleData)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("API", "Network error: ${t.message}")
                }
            })

            ApiClient.retrofit.uploadImage(registrationCardImages[0]).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {

                        println(response.body())
                        response.body()?.let {
                            createdImages.add(it)
                        }
                        println("Success")
                        TempDataHolder.jsonForHttpRequest = null

                        if (createdImages.count() == 2) {

                            callConnectVehicleAndDriver()
                        }

                    } else {
                        println("FAIL")
                        println(response)
                        println(registrationCardImages)
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("API", "Network error: ${t.message}")
                }
            })

            ApiClient.retrofit.uploadImage(registrationCardImages[1]).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {

                        println(response.body())
                        response.body()?.let {
                            createdImages.add(it)
                        }
                        println("Success")
                        TempDataHolder.jsonForHttpRequest = null

                        if (createdImages.count() == 2) {

                            callConnectVehicleAndDriver()
                        }

                    } else {
                        println("FAIL")
                        println(response)
                        println(registrationCardImages)
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("API", "Network error: ${t.message}")
                }
            })
        }
    }

    var frontImagePdUri: Uri? = null
    var backImagePdUri: Uri? = null

    var frontImagePdBase64 : String? = null
    var backImagePdBase64 : String? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    val clipData = data?.clipData
                    if (clipData != null && clipData.itemCount >= 2) {

                        frontImagePdUri = clipData.getItemAt(0).uri
                        backImagePdUri = clipData.getItemAt(1).uri

                        frontImagePdView.setImageURI(frontImagePdUri)
                        backImagePdView.setImageURI(backImagePdUri)

                        frontImagePdBase64 = frontImagePdUri?.let { uriToBase64(this, it) }
                        backImagePdBase64 = backImagePdUri?.let { uriToBase64(this, it) }

                    } else {
                        Toast.makeText(this, "Please select 2 images", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun callConnectVehicleAndDriver() {

        for (item in createdImages) {

            var korisnikImage = KorisnikImage("55", item)

            ApiClient.retrofit.createKorisnikImage(korisnikImage).enqueue(object : Callback<ResponseBody> {
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

    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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