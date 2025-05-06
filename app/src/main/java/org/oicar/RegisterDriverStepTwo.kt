package org.oicar

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import org.oicar.models.ImageDocument
import org.oicar.models.RegisterRequest
import org.oicar.models.TempDataHolder
import org.oicar.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterDriverStepTwo : AppCompatActivity() {

    private lateinit var receivedRegisterStepOneData : MutableMap<String, String>

    private lateinit var frontImageIdView : ImageView
    private lateinit var backImageIdView : ImageView
    private lateinit var frontImageDlView : ImageView
    private lateinit var backImageDlView : ImageView

    private val GALLERY_REQUEST_CODE = 101

    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001

    private var capturingFront = true

    private var isPhotoId = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_driver_step_two)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val receivedRegisterStepOneJson = intent.getStringExtra("registerStepOneData")
        val type = object : TypeToken<MutableMap<String, Any>>() {}.type
        receivedRegisterStepOneData = Gson().fromJson<MutableMap<String, String>>(receivedRegisterStepOneJson, type)

        frontImageIdView = findViewById(R.id.photoIdFront)
        backImageIdView = findViewById(R.id.photoIdBack)

        frontImageDlView = findViewById(R.id.photoDlFront)
        backImageDlView = findViewById(R.id.photoDlBack)

        findViewById<Button>(R.id.uploadPhotoIdButton).setOnClickListener {
            isPhotoId = true
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(Intent.createChooser(intent, "Select ID Photos"), GALLERY_REQUEST_CODE)
        }

        findViewById<Button>(R.id.uploadPhotoDlButton).setOnClickListener {
            isPhotoId = false
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(Intent.createChooser(intent, "Select ID Photos"), GALLERY_REQUEST_CODE)
        }

        findViewById<Button>(R.id.takePhotoIdButton).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                openCamera()
            } else {
                println("Sorry you're version android is not support, Min Android 6.0 (Marsmallow)")
            }
        }

        val regButton : Button = findViewById<Button>(R.id.backButton)

        regButton.setOnClickListener {

            val username : String = findViewById<EditText?>(R.id.username).text.toString()
            val password : String = findViewById<EditText?>(R.id.password).text.toString()
            val confirmPassword : String = findViewById<EditText?>(R.id.confirmPassword).text.toString()

            if (password == confirmPassword) {

                receivedRegisterStepOneData.put("username", username)
                receivedRegisterStepOneData.put("password", password)

                val registerRequest = RegisterRequest(
                    username = "${receivedRegisterStepOneData["username"]}",
                    password = "${receivedRegisterStepOneData["password"]}",
                    ime = "${receivedRegisterStepOneData["ime"]}",
                    prezime = "${receivedRegisterStepOneData["prezime"]}",
                    email = "${receivedRegisterStepOneData["email"]}",
                    telefon = "${receivedRegisterStepOneData["telefon"]}",
                    datumrodjenja = "${receivedRegisterStepOneData["datumrodjenja"]}"

                )

                ApiClient.retrofit.registerUser(Gson().toJson(receivedRegisterStepOneData)).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {

                            println("Success")


                        } else {
                            println("FAIL")
                            println(response)
                            println(Gson().toJson(receivedRegisterStepOneData))
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("API", "Network error: ${t.message}")
                    }
                })
            }
            else {

                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_LONG).show()
            }
        }

        val nextStep : Button = findViewById<Button>(R.id.submitButton)



        nextStep.setOnClickListener {

            val username : String = findViewById<EditText?>(R.id.username).text.toString()
            val password : String = findViewById<EditText?>(R.id.password).text.toString()
            val confirmPassword : String = findViewById<EditText?>(R.id.confirmPassword).text.toString()

            if(password == confirmPassword) {

                receivedRegisterStepOneData.put("username", username)
                receivedRegisterStepOneData.put("password", password)



                val registerStepTwoImages = mutableListOf(
                    ImageDocument("FrontID", frontImageIdBase64.toString(), 1),
                    ImageDocument("BackID", backImageIdBase64.toString(), 1),
                    ImageDocument("FrontDL", frontImageDlBase64.toString(), 2),
                    ImageDocument("BackDL", backImageDlBase64.toString(), 2)
                )

                val registerStepTwoImagesJson = Gson().toJson(registerStepTwoImages)

                val registerStepTwoJson = Gson().toJson(receivedRegisterStepOneData)

                TempDataHolder.jsonForRegistrationData = registerStepTwoImagesJson

                val intent = Intent(this, RegistrationDriverAndPassengerFinal::class.java)
                intent.putExtra("receivedRegisterStepOneData", registerStepTwoJson)
//            intent.putExtra("registerStepTwoImagesJson", registerStepTwoImagesJson)
                startActivity(intent)
            }
            else {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_LONG).show()
            }

//            var imageFront = mapOf(
//                "name" to "FrontID",
//                "base64Content" to frontImageIdBase64.toString()
//            )
//
//            var imageBack = mapOf(
//                "name" to "BackID",
//                "base64Content" to frontImageIdBase64.toString()
//            )
//
//            ApiClient.retrofit.uploadImage(imageFront).enqueue(object : Callback<ResponseBody> {
//                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                    if (response.isSuccessful) {
//
//
//
//                        println("Success")
//
//
//                    } else {
//                        println("FAIL")
//                        println(response)
//                    }
//                }
//
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    Log.e("API", "Network error: ${t.message}")
//                }
//            })
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //called when user presses ALLOW or DENY from Permission Request Popup
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    openCamera()
                } else{
                    println("Permission denied")
                }
            }
        }
    }

    var frontImageIdUri: Uri? = null
    var backImageIdUri: Uri? = null

    var frontImageIdBase64 : String? = null
    var backImageIdBase64 : String? = null

    var frontImageDlBase64 : String? = null
    var backImageDlBase64 : String? = null

    var frontImageDlUri: Uri? = null
    var backImageDlUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    val clipData = data?.clipData
                    if (clipData != null && clipData.itemCount >= 2) {

                        if(isPhotoId) {
                            frontImageIdUri = clipData.getItemAt(0).uri
                            backImageIdUri = clipData.getItemAt(1).uri

                            frontImageIdView.setImageURI(frontImageIdUri)
                            backImageIdView.setImageURI(backImageIdUri)

                            frontImageIdBase64 = frontImageIdUri?.let { uriToBase64(this, it) }
                            backImageIdBase64 = backImageIdUri?.let { uriToBase64(this, it) }
                        }
                        else {
                            frontImageDlUri = clipData.getItemAt(0).uri
                            backImageDlUri = clipData.getItemAt(1).uri

                            frontImageDlView.setImageURI(frontImageDlUri)
                            backImageDlView.setImageURI(backImageDlUri)

                            frontImageDlBase64 = frontImageDlUri?.let { uriToBase64(this, it) }
                            backImageDlBase64 = backImageDlUri?.let { uriToBase64(this, it) }
                        }

                    } else {
                        Toast.makeText(this, "Please select 2 images", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
}
