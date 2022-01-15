package com.example.arcape

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class Number : AppCompatActivity() {

    private fun startMainAR() {
        val startAR = Intent(
            this,
            MainActivity::class.java
        )
        startActivity(startAR)
        this.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.number)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val startBtn2 = findViewById<Button>(R.id.startBtn2)
        val startBtn3 = findViewById<Button>(R.id.startBtn3)
        val startBtn4 = findViewById<Button>(R.id.startBtn4)

        startBtn2.setOnClickListener {
            startMainAR()
        }
        startBtn3.setOnClickListener {
            startMainAR()
        }
        startBtn4.setOnClickListener {
            startMainAR()
        }
    }
}