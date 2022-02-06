package com.example.arcape

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess

class ExitSplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.exit_activity)
        super.onCreate(savedInstanceState)
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 500, 200, 500, 200)
        vibrator.vibrate(pattern,4)
        Handler(Looper.getMainLooper()).postDelayed({
            this.finish()
            exitProcess(0)
        },2500)
    }

}