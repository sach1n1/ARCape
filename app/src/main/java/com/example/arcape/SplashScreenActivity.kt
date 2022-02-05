package com.example.arcape

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onboard = Intent(
            this,
            Onboarding::class.java
        )

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(onboard)
            this.finish()
        }, 2500)
    }
}