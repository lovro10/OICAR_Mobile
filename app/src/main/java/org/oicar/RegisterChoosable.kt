package org.oicar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterChoosable : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_register_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val driverRegisterButton : Button = findViewById(R.id.driverRegisterButton)
        val passengerRegisterButton : Button = findViewById(R.id.passengerRegisterButton)

        driverRegisterButton.setOnClickListener {

            val intent = Intent(this, RegisterDriverAndPassenger::class.java)
            startActivity(intent)
        }

        passengerRegisterButton.setOnClickListener {

            val intent = Intent(this, RegisterDriverAndPassenger::class.java)
            startActivity(intent)
        }
    }
}