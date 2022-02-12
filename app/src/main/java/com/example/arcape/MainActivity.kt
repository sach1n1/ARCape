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
import android.os.*
import android.view.MotionEvent
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Sceneform
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.example.arcape.mqtt.MqttClientHelper
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment.OnSessionConfigurationListener
import com.google.ar.sceneform.ux.TransformableNode
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer


class MainActivity : AppCompatActivity(), FragmentOnAttachListener, OnSessionConfigurationListener {

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

    var puzzle1Sub = 0
    var puzzle2Sub = 0
    var puzzle3Sub = 0
    var puzzle4Sub = 0
    var puzzle5Sub = 0

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
            mqttClient.subscribe("game/puzzle1")
            mqttClient.subscribe("game/puzzle2")
            mqttClient.subscribe("game/puzzle3")
            mqttClient.subscribe("game/puzzle4")
            mqttClient.subscribe("game/puzzle5")
            mqttClient.subscribe("env/powerFail")
            mqttClient.subscribe("op/gameControl")
        }, 2000)

        supportFragmentManager.addFragmentOnAttachListener(this)
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.arFragment, ArFragment::class.java, null)
                    .commit()
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when(event.action)
        {
            MotionEvent.ACTION_UP -> {
                Toast.makeText(applicationContext, "Tap registered", Toast.LENGTH_SHORT).show()
                if (!shake) shake = true
            }
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    private fun launchExit()
    {
        val startExit = Intent(
            this,
            ExitSplashScreenActivity::class.java
        )
        startActivity(startExit)
        this.finish()
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
                if (topic == "env/powerFail" && "$mqttMessage" == "INIT" )
                {
                    mqttClient.publish("env/powerFail","{\"method\": \"status\", \"state\": \"solved\"}",1,true)
                }

                if (topic == "op/gameControl" && "$mqttMessage" == "SOLVED" )
                {
                    launchExit()
                }

                if(topic == "game/puzzle1")
                {
                    when("$mqttMessage")
                    {
                        "Not Activated" -> puzzle1Sub = 0
                        "Hint1" -> {
                            puzzle1Sub = 1
                            vibrator.vibrate(500)
                            shake = false
                        }
                        "Hint2" -> {
                            puzzle1Sub = 2
                            vibrator.vibrate(500)
                            shake = false
                        }
                        "Hint3" -> {
                            puzzle1Sub = 3
                            vibrator.vibrate(500)
                            shake = false
                        }
                        "Solved" -> {
                            puzzle1Sub = 4
                            mqttClient.publish("game/puzzle1","Done",1,true)
                            reloadActivity()
                        }
                        "Done" -> puzzle1Sub = 5
                    }
                }
                if(topic == "game/puzzle2")
                {
                    when("$mqttMessage")
                    {
                        "Not Activated" -> puzzle2Sub = 0
                        "Activated" -> {
                            puzzle2Sub = 1
                            vibrator.vibrate(500)
                            shake = false
                        }
                        "Solved" -> {
                            puzzle2Sub = 2
                            mqttClient.publish("game/puzzle2","Done",1,true)
                            reloadActivity()
                        }
                        "Done" -> puzzle2Sub = 3
                    }
                }
                if(topic == "game/puzzle3")
                {
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
                            mqttClient.publish("game/puzzle3","Done",1,true)
                            reloadActivity()
                        }
                        "Done" -> puzzle3Sub = 8
                    }
                }
                if(topic == "game/puzzle4")
                {
                    when("$mqttMessage")
                    {
                        "Not Activated" -> puzzle4Sub = 0
                        "Activated" -> {
                            puzzle4Sub = 1
                            vibrator.vibrate(500)
                            shake = false
                        }
                        "Solved" -> {
                            puzzle4Sub = 2
                            mqttClient.publish("game/puzzle4","Done",1,true)
                            reloadActivity()
                        }
                        "Done" -> puzzle4Sub = 3
                    }
                }
                if(topic == "game/puzzle5")
                {
                    when("$mqttMessage")
                    {
                        "Not Activated" -> puzzle5Sub = 0
                        "Activated" -> {
                            puzzle5Sub = 1
                            vibrator.vibrate(500)
                            shake=false
                        }
                        "Solved" -> {
                            puzzle5Sub = 2
                            mqttClient.publish("game/puzzle5","Done",1,true)
                            reloadActivity()
                        }
                        "Done" -> puzzle5Sub = 3
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

    private fun reloadActivity() {
        this.finish()
        startActivity(intent)
        //overridePendingTransition(0, 0)
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
                var delay=1000
                when (puzzle1Sub) {
                    0 -> placeObject(anchorNodePuzzle1, "models/nactive.glb")
                    1 -> {
                        when(shake) {
                            false -> placeObject(anchorNodePuzzle1, "models/tap.glb")
                            true ->  placeObject(anchorNodePuzzle1, "models/puzzle1/hint1.glb")
                        }
                    }
                    2 -> {
                        when(shake){
                            false -> placeObject(anchorNodePuzzle1, "models/tap.glb")
                            true -> placeObject(anchorNodePuzzle1, "models/puzzle1/hint2.glb")
                        }
                    }
                    3 -> {
                        when(shake){
                            false -> placeObject(anchorNodePuzzle1, "models/tap.glb")
                            true -> placeObject(anchorNodePuzzle1, "models/puzzle1/hint3.glb")
                        }
                    }
                    5 -> {
                        placeObject(anchorNodePuzzle1, "models/solved.glb")
                        puzzle1Sub=6
                        delay=5000
                    }
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    anchorNodePuzzle1.anchor?.detach()
                    anchorNodePuzzle1.parent = null
                    anchorNodePuzzle1.anchor = null
                    anchorNodePuzzle1.renderable = null
                    arFragment!!.arSceneView.scene.removeChild(anchorNodePuzzle2)
                    puzzle1Detected = false
                    if (puzzle1Sub==6){
                        puzzle1Detected = true
                    }
                }, delay.toLong())
            }


            if (!puzzle2Detected && augmentedImage.name == "puzzle2") {
                var delay=1000
                puzzle2Detected = true
                when(puzzle2Sub)
                {
                    0 -> placeObject(anchorNodePuzzle2, "models/nactive.glb")
                    1 -> {
                        when(shake) {
                            false -> placeObject(anchorNodePuzzle2, "models/tap.glb")
                            true -> placeObject(anchorNodePuzzle2, "models/puzzle2/hint.glb")
                        }
                    }
                    3 -> {
                        placeObject(anchorNodePuzzle2, "models/solved.glb")
                        puzzle2Sub=4
                        delay=5000
                    }
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    anchorNodePuzzle2.anchor?.detach()
                    anchorNodePuzzle2.parent = null
                    anchorNodePuzzle2.anchor = null
                    anchorNodePuzzle2.renderable = null
                    arFragment!!.arSceneView.scene.removeChild(anchorNodePuzzle2)
                    puzzle2Detected = false
                    if (puzzle2Sub==4){
                        puzzle2Detected = true
                    }
                },delay.toLong())
            }


            if (!puzzle3Detected && augmentedImage.name == "puzzle3") {
                var delay=1000
                puzzle3Detected = true
                when(puzzle3Sub)
                {
                    0 -> placeObject(anchorNodePuzzle3, "models/nactive.glb")
                    1 -> {
                        when(shake){
                            false -> placeObject(anchorNodePuzzle3, "models/tap.glb")
                            true -> placeObject(anchorNodePuzzle3, "models/puzzle3/antenna.glb")
                        }
                    }
                    2 -> {
                        when(shake){
                            false -> placeObject(anchorNodePuzzle3, "models/tap.glb")
                            true -> placeObject(anchorNodePuzzle3, "models/puzzle3/map1.glb")
                        }
                    }
                    3 -> {
                        when(shake){
                            false -> placeObject(anchorNodePuzzle3, "models/tap.glb")
                            true -> placeObject(anchorNodePuzzle3, "models/puzzle3/map2.glb")
                        }
                    }
                    4 -> {
                        when(shake){
                            false -> placeObject(anchorNodePuzzle3, "models/tap.glb")
                            true -> placeObject(anchorNodePuzzle3, "models/puzzle3/touch1.glb")
                        }
                    }
                    5 -> {
                        when(shake){
                            false -> placeObject(anchorNodePuzzle3, "models/tap.glb")
                            true -> placeObject(anchorNodePuzzle3, "models/puzzle3/touch2.glb")
                        }
                    }
                    6 -> {
                        when(shake){
                            false -> placeObject(anchorNodePuzzle3, "models/tap.glb")
                            true -> placeObject(anchorNodePuzzle3, "models/puzzle3/touch3.glb")
                        }
                    }
                    8 -> {
                        placeObject(anchorNodePuzzle3, "models/solved.glb")
                        puzzle3Sub=9
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
                    if (puzzle3Sub==9){
                        puzzle3Detected = true
                    }
                }, delay.toLong())
            }


            if (!puzzle4Detected && augmentedImage.name == "puzzle4") {
                var delay=1000
                puzzle4Detected = true
                when(puzzle4Sub)
                {
                    0 -> placeObject(anchorNodePuzzle4, "models/nactive.glb")
                    1 -> {
                        when(shake)
                        {
                            false -> placeObject(anchorNodePuzzle4, "models/tap.glb")
                            true -> placeObject(anchorNodePuzzle4, "models/puzzle4/hint.glb")
                        }
                    }
                    3 -> {
                        placeObject(anchorNodePuzzle4, "models/solved.glb")
                        puzzle4Sub=4
                        delay=5000
                    }
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    arFragment!!.arSceneView.scene.removeChild(anchorNodePuzzle4)
                    anchorNodePuzzle4.anchor?.detach()
                    puzzle4Detected = false
                    anchorNodePuzzle4.parent = null
                    anchorNodePuzzle4.anchor = null
                    anchorNodePuzzle4.renderable = null
                    if (puzzle4Sub==4){
                        puzzle4Detected = true
                    }
                },delay.toLong())
            }


            if (!puzzle5Detected && augmentedImage.name == "puzzle5") {
                var delay=1000
                puzzle5Detected = true
                when(puzzle5Sub)
                {
                    0 -> placeObject(anchorNodePuzzle5, "models/nactive.glb")
                    1 -> {
                        when(shake)
                        {
                            false -> placeObject(anchorNodePuzzle5, "models/tap.glb")
                            true -> placeObject(anchorNodePuzzle5, "models/puzzle5/hint.glb")
                        }
                    }
                    3 -> {
                        placeObject(anchorNodePuzzle5, "models/solved.glb")
                        puzzle5Sub=4
                        delay=5000
                    }
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    arFragment!!.arSceneView.scene.removeChild(anchorNodePuzzle5)
                    anchorNodePuzzle5.anchor?.detach()
                    puzzle5Detected = false
                    anchorNodePuzzle5.parent = null
                    anchorNodePuzzle5.anchor = null
                    anchorNodePuzzle5.renderable = null
                    if (puzzle5Sub==4){
                        puzzle5Detected = true
                    }
                },delay.toLong())
            }
        }
    }
}
