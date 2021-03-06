package com.example.arcape

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.arcape.mqtt.MqttClientHelper
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage


class Number : AppCompatActivity() {

    private val mqttClientPub by lazy {
        MqttClientHelper(this)
    }

    private fun setMqttCallBack() {
        mqttClientPub.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                Log.w("Connected","Successfully Connected")
            }
            override fun connectionLost(throwable: Throwable) {
                Log.w("Disconnected","Connection Lost")
            }
            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                Log.w("Debug", "Message received from host '$MQTT_HOST': $mqttMessage")
            }
            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.w("Debug", "Message published to host '$MQTT_HOST'")
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        mqttClientPub.destroy()
    }

    private fun startMainAR( participants: Int, duration: Int) {
        for (i in 1..5)
        {
            mqttClientPub.publish("game/puzzle$i","Not Activated",1,true)
        }
        val startAR = Intent(
            this,
            MainActivity::class.java
        )
        //made changes in MQTT Helper to allow topic retain
        val gameOptionsString = "{\"participants\": $participants, \"duration\": $duration, \"skipTo\": \"\"}"
        mqttClientPub.publish("op/gameOptions",gameOptionsString,1,true)
        mqttClientPub.publish("op/gameControl","START",1,true)
        mqttClientPub.publish("env/powerFail","INIT", 1,true)
        startActivity(startAR)
        this.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.number)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        setMqttCallBack()
        val startBtn2 = findViewById<Button>(R.id.startBtn2)
        val startBtn3 = findViewById<Button>(R.id.startBtn3)
        val startBtn4 = findViewById<Button>(R.id.startBtn4)

        startBtn2.setOnClickListener {
            startMainAR(2,60)
        }
        startBtn3.setOnClickListener {
            startMainAR(3,50)
        }
        startBtn4.setOnClickListener {
            startMainAR(4,40)
        }
    }
}