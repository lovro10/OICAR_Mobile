package org.oicar

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import org.oicar.models.ImageDocument
import org.oicar.models.KorisnikImage
import org.oicar.models.RegisterRequest
import org.oicar.models.TempDataHolder
import org.oicar.services.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistrationDriverAndPassengerFinal : AppCompatActivity() {

    private lateinit var receivedRegisterStepTwoData : MutableMap<String, String>
    private lateinit var receivedRegisterStepTwoImages : MutableList<ImageDocument>

    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider

    private val cameraPermission = android.Manifest.permission.CAMERA
    private val requestCode = 1001

    private lateinit var registeredUserId : String
    private lateinit var createdImages : MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration_driver_and_passenger_final)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val receivedRegisterStepTwoJson = intent.getStringExtra("receivedRegisterStepOneData")
        val registerStepTwoImagesJson = TempDataHolder.jsonForHttpRequest
        val typeReceivedRegisterStepTwoJson = object : TypeToken<MutableMap<String, Any>>() {}.type
        val typeRegisterStepTwoImagesJson = object : TypeToken<MutableList<ImageDocument>>() {}.type
        receivedRegisterStepTwoData = Gson().fromJson<MutableMap<String, String>>(receivedRegisterStepTwoJson, typeReceivedRegisterStepTwoJson)
        receivedRegisterStepTwoImages = Gson().fromJson<MutableList<ImageDocument>>(registerStepTwoImagesJson, typeRegisterStepTwoImagesJson)

        createdImages = mutableListOf()

        previewView = findViewById(R.id.previewView)
        val captureButton = findViewById<Button>(R.id.captureButton)

        if (checkSelfPermission(cameraPermission) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissions(arrayOf(cameraPermission), requestCode)
        }

        captureButton.setOnClickListener {
            takePhoto()
        }

        val completeRegistrationButton : Button = findViewById<Button>(R.id.completeRegistrationButton)

        completeRegistrationButton.setOnClickListener {

            ApiClient.retrofit.registerUser(Gson().toJson(receivedRegisterStepTwoData)).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {

                        println("Success")
                        registeredUserId = response.body()!!


                    } else {
                        println("FAIL")
                        println(response)
                        println(Gson().toJson(receivedRegisterStepTwoData))
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("API", "Network error: ${t.message}")
                }
            })

            ApiClient.retrofit.uploadImage(receivedRegisterStepTwoImages[0]).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {

                        println(response.body())
                        response.body()?.let {
                            createdImages.add(it)
                        }
                        println("Success")
                        TempDataHolder.jsonForHttpRequest = null

                        if (createdImages.count() == 5) {

                            callCreateKorisnikImage()
                        }

                    } else {
                        println("FAIL")
                        println(response)
                        println(receivedRegisterStepTwoImages)
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("API", "Network error: ${t.message}")
                }
            })
            ApiClient.retrofit.uploadImage(receivedRegisterStepTwoImages[1]).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {

                        response.body()?.let {
                            createdImages.add(it)
                        }
                        println("Success")
                        TempDataHolder.jsonForHttpRequest = null

                        if (createdImages.count() == 5) {

                            callCreateKorisnikImage()
                        }

                    } else {
                        println("FAIL")
                        println(response)
                        println(receivedRegisterStepTwoImages)
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("API", "Network error: ${t.message}")
                }
            })
            ApiClient.retrofit.uploadImage(receivedRegisterStepTwoImages[2]).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {

                        response.body()?.let {
                            createdImages.add(it)
                        }
                        println("Success")
                        TempDataHolder.jsonForHttpRequest = null

                        if (createdImages.count() == 5) {

                            callCreateKorisnikImage()
                        }


                    } else {
                        println("FAIL")
                        println(response)
                        println(receivedRegisterStepTwoImages)
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("API", "Network error: ${t.message}")
                }
            })
            ApiClient.retrofit.uploadImage(receivedRegisterStepTwoImages[3]).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {

                        response.body()?.let {
                            createdImages.add(it)
                        }
                        println("Success")
                        TempDataHolder.jsonForHttpRequest = null

                        if (createdImages.count() == 5) {

                            callCreateKorisnikImage()
                        }


                    } else {
                        println("FAIL")
                        println(response)
                        println(receivedRegisterStepTwoImages)
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("API", "Network error: ${t.message}")
                }
            })
            ApiClient.retrofit.uploadImage(receivedRegisterStepTwoImages[4]).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {

                        response.body()?.let {
                            createdImages.add(it)
                        }
                        println("Success")
                        TempDataHolder.jsonForHttpRequest = null

                        if (createdImages.count() == 5) {

                            callCreateKorisnikImage()
                        }


                    } else {
                        println("FAIL")
                        println(response)
                        println(receivedRegisterStepTwoImages)
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("API", "Network error: ${t.message}")
                }
            })


        }
    }

    private fun callCreateKorisnikImage() {

        for (item in createdImages) {

            var korisnikImage = KorisnikImage(registeredUserId, item)

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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setTargetRotation(previewView.display.rotation)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    var selfieUri: Uri? = null
    var selfieBase64 : String? = null

    private fun takePhoto() {
        val imageFile = createImageFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    selfieUri = Uri.fromFile(imageFile)

                    findViewById<ImageView>(R.id.imageView).setImageURI(selfieUri)

                    selfieBase64 = selfieUri?.let { uriToBase64(this@RegistrationDriverAndPassengerFinal, it) }

                    receivedRegisterStepTwoImages.add(ImageDocument("Selfie", selfieBase64.toString(), 3))
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            }
        )
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

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("SELFIE_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

}