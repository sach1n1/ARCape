package com.example.arcape

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Sceneform
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.samples.augmentedimages.mqtt.MqttClientHelper
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment.OnSessionConfigurationListener
import com.google.ar.sceneform.ux.TransformableNode
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer


class MainActivity3 : AppCompatActivity(), FragmentOnAttachListener, OnSessionConfigurationListener {

    private val mqttClient by lazy {
        MqttClientHelper(this)
    }

    private val futures: MutableList<CompletableFuture<Void>> = ArrayList()
    private var arFragment: ArFragment? = null
    private var puzzle1Detected = false
    private var puzzle2Detected = false
    private var puzzle3Detected = false
    private var puzzle4Detected = false
    private var puzzle5Detected = false
    private var database: AugmentedImageDatabase? = null


    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f


    var puzzle3Sub = 0


    var shake = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        setMqttCallBack()


        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { _: View?, insets: WindowInsetsCompat ->
            (toolbar.layoutParams as MarginLayoutParams).topMargin = insets
                .getInsets(WindowInsetsCompat.Type.systemBars()).top
            WindowInsetsCompat.CONSUMED
        }



        Handler(Looper.getMainLooper()).postDelayed({
            mqttClient.subscribe("game/puzzle3")
        }, 2000)



        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        Objects.requireNonNull(sensorManager)?.registerListener(sensorListener, sensorManager!!
            .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        supportFragmentManager.addFragmentOnAttachListener(this)
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.arFragment, ArFragment::class.java, null)
                    .commit()
            }
        }
    }

    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration
            currentAcceleration = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta
            if (acceleration > 12) {
                //Toast.makeText(applicationContext, "Shake event detected", Toast.LENGTH_SHORT).show()
                if (!shake) shake=true
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private fun setMqttCallBack() {
        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                Log.w("Connected","Successfully Connected")
            }
            override fun connectionLost(throwable: Throwable) {
                Log.w("Disconnected","Connection Lost")
            }
            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                Log.w("Debug", "Message received from host '$MQTT_HOST': $mqttMessage")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                when("$mqttMessage")
                {
                    "Not Activated" -> puzzle3Sub = 0
                    "antenna_activate_1" -> {
                        puzzle3Sub = 1
                        vibrator.vibrate(500)
                        shake = false
                    }
                    "map_activate_1" -> {
                        puzzle3Sub = 2
                        vibrator.vibrate(500)
                        shake = false
                    }
                    "map_activate_2" -> {
                        puzzle3Sub = 3
                        vibrator.vibrate(500)
                        shake = false
                    }
                    "touch_activate_1" -> {
                        puzzle3Sub = 4
                        vibrator.vibrate(500)
                        shake = false
                    }
                    "touch_activate_2" -> {
                        puzzle3Sub = 5
                        vibrator.vibrate(500)
                        shake = false
                    }
                    "touch_activate_3" -> {
                        puzzle3Sub = 6
                        vibrator.vibrate(500)
                        shake = false
                    }
                    "Solved" -> {
                        puzzle3Sub = 7
                        val pattern = longArrayOf(0, 200, 300, 200, 300)
                        vibrator.vibrate(pattern,-1)
                    }
                }
            }
            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.w("Debug", "Message published to host '$MQTT_HOST'")
            }
        })
    }

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment.id == R.id.arFragment) {
            arFragment = fragment as ArFragment
            arFragment!!.setOnSessionConfigurationListener(this)
        }
    }

    override fun onSessionConfiguration(session: Session, config: Config) {
        // Disable plane detection
        config.planeFindingMode = Config.PlaneFindingMode.DISABLED

        //Using preloaded Image Database arcoreimg tool
        val inputStream = assets.open("myimages5.imgdb")
        database= AugmentedImageDatabase.deserialize(session,inputStream)

        config.augmentedImageDatabase = database

        // Check for image detection
        arFragment!!.setOnAugmentedImageUpdateListener { augmentedImage: AugmentedImage ->
            onAugmentedImageTrackingUpdate(
                augmentedImage
            )
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mqttClient.destroy()
        futures.forEach(Consumer { future: CompletableFuture<Void> ->
            if (!future.isDone) future.cancel(
                true
            )
        })
    }

    private fun placeObject(anchorNode: AnchorNode, string: String){
        anchorNode.worldScale = Vector3(0.1f, 0.1f, 0.1f)
        arFragment!!.arSceneView.scene.addChild(anchorNode)
        futures.add(ModelRenderable.builder()
            .setSource(this, Uri.parse(string))
            .setIsFilamentGltf(true)
            .build()
            .thenAccept { textModel: ModelRenderable? ->
                val modelNode = TransformableNode(
                    arFragment!!.transformationSystem
                )
                modelNode.renderable = textModel
                anchorNode.addChild(modelNode)
            }
            .exceptionally {
                Toast.makeText(
                    this,
                    "Unable to load model",
                    Toast.LENGTH_LONG
                )
                    .show()
                null
            })
    }

    private fun onAugmentedImageTrackingUpdate(augmentedImage: AugmentedImage) {
        if (puzzle1Detected && puzzle2Detected && puzzle3Detected && puzzle4Detected && puzzle5Detected) {
            return
        }
        if (augmentedImage.trackingState == TrackingState.TRACKING ) {
            val anchorNodePuzzle1 = AnchorNode(augmentedImage.createAnchor(augmentedImage.centerPose))
            val anchorNodePuzzle2 = AnchorNode(augmentedImage.createAnchor(augmentedImage.centerPose))
            val anchorNodePuzzle3 = AnchorNode(augmentedImage.createAnchor(augmentedImage.centerPose))
            val anchorNodePuzzle4 = AnchorNode(augmentedImage.createAnchor(augmentedImage.centerPose))
            val anchorNodePuzzle5 = AnchorNode(augmentedImage.createAnchor(augmentedImage.centerPose))

            if (!puzzle1Detected && augmentedImage.name == "puzzle1") {
                puzzle1Detected = true
                placeObject(anchorNodePuzzle1, "models/solved.glb")
                Handler(Looper.getMainLooper()).postDelayed({
                    anchorNodePuzzle1.anchor?.detach()
                    anchorNodePuzzle1.parent = null
                    anchorNodePuzzle1.anchor = null
                    anchorNodePuzzle1.renderable = null
                    arFragment!!.arSceneView.scene.removeChild(anchorNodePuzzle1)
                },5000)
            }

            if (!puzzle2Detected && augmentedImage.name == "puzzle2") {
                puzzle2Detected = true
                placeObject(anchorNodePuzzle2, "models/solved.glb")
                Handler(Looper.getMainLooper()).postDelayed({
                    anchorNodePuzzle2.anchor?.detach()
                    anchorNodePuzzle2.parent = null
                    anchorNodePuzzle2.anchor = null
                    anchorNodePuzzle2.renderable = null
                    arFragment!!.arSceneView.scene.removeChild(anchorNodePuzzle2)
                },5000)
            }



            if (!puzzle3Detected && augmentedImage.name == "puzzle3") {
                var delay=1000
                puzzle3Detected = true
                when(puzzle3Sub)
                {
                    0 -> placeObject(anchorNodePuzzle3, "models/nactive.glb")
                    1 -> {
                        when(shake){
                            false -> placeObject(anchorNodePuzzle3, "models/shake.glb")
                            true -> placeObject(anchorNodePuzzle3, "models/puzzle3/antenna.glb")
                        }
                    }
                    2 -> {
                        when(shake){
                            false -> placeObject(anchorNodePuzzle3, "models/shake.glb")
                            true -> placeObject(anchorNodePuzzle3, "models/puzzle3/map1.glb")
                        }
                    }
                    3 -> {
                        when(shake){
                            false -> placeObject(anchorNodePuzzle3, "models/shake.glb")
                            true -> placeObject(anchorNodePuzzle3, "models/puzzle3/map2.glb")
                        }
                    }
                    4 -> {
                        when(shake){
                            false -> placeObject(anchorNodePuzzle3, "models/shake.glb")
                            true -> placeObject(anchorNodePuzzle3, "models/puzzle3/touch1.glb")
                        }
                    }
                    5 -> {
                        when(shake){
                            false -> placeObject(anchorNodePuzzle3, "models/shake.glb")
                            true -> placeObject(anchorNodePuzzle3, "models/puzzle3/touch2.glb")
                        }
                    }
                    6 -> {
                        when(shake){
                            false -> placeObject(anchorNodePuzzle3, "models/shake.glb")
                            true -> placeObject(anchorNodePuzzle3, "models/puzzle3/touch3.glb")
                        }
                    }
                    7 -> {
                        placeObject(anchorNodePuzzle3, "models/solved.glb")
                        puzzle3Sub=8
                        delay=5000
                    }
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    arFragment!!.arSceneView.scene.removeChild(anchorNodePuzzle3)
                    puzzle3Detected = false
                    anchorNodePuzzle3.anchor?.detach()
                    anchorNodePuzzle3.parent = null
                    anchorNodePuzzle3.anchor = null
                    anchorNodePuzzle3.renderable = null
                    if (puzzle3Sub==8){
                        puzzle3Detected = true
                        anchorNodePuzzle3.anchor?.detach()
                        anchorNodePuzzle3.parent = null
                        anchorNodePuzzle3.anchor = null
                        anchorNodePuzzle3.renderable = null
                        anchorNodePuzzle3.removeChild(anchorNodePuzzle3)
                        val puzzle2 = Intent(
                            this,
                            MainActivity2::class.java
                        )
                        startActivity(puzzle2)
                        this.finish()
                    }
                }, delay.toLong())
            }

            if (!puzzle4Detected && augmentedImage.name == "puzzle4") {
                puzzle4Detected = true
                placeObject(anchorNodePuzzle4, "models/nactive.glb")
                Handler(Looper.getMainLooper()).postDelayed({
                    anchorNodePuzzle4.anchor?.detach()
                    anchorNodePuzzle4.parent = null
                    anchorNodePuzzle4.anchor = null
                    anchorNodePuzzle4.renderable = null
                    arFragment!!.arSceneView.scene.removeChild(anchorNodePuzzle4)
                },5000)
            }


            if (!puzzle5Detected && augmentedImage.name == "puzzle5") {
                puzzle5Detected = true
                placeObject(anchorNodePuzzle5, "models/nactive.glb")
                Handler(Looper.getMainLooper()).postDelayed({
                    anchorNodePuzzle5.anchor?.detach()
                    anchorNodePuzzle5.parent = null
                    anchorNodePuzzle5.anchor = null
                    anchorNodePuzzle5.renderable = null
                    arFragment!!.arSceneView.scene.removeChild(anchorNodePuzzle5)
                },5000)
            }
        }
    }
}
