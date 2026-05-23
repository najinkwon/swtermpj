package com.example.swtermproject.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(
                this,
                MainActivity::class.java
            ).apply {
                putExtra("launched_from", "SplashActivity")
                putExtra("launch_time", System.currentTimeMillis())
            }

            startActivity(intent)

            finish()
        }, 1800)
    }
}
