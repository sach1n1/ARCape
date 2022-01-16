package com.example.arcape

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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


class MainActivity : AppCompatActivity(), FragmentOnAttachListener, OnSessionConfigurationListener {

    private val mqttClient by lazy {
        MqttClientHelper(this)
    }

    private val futures: MutableList<CompletableFuture<Void>> = ArrayList()
    private var arFragment: ArFragment? = null
    private var robotDetected = false
    private var padLockDetected = false
    private var database: AugmentedImageDatabase? = null

    var robotSub = 0
    var padLockSub = 0

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

        supportFragmentManager.addFragmentOnAttachListener(this)
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.arFragment, ArFragment::class.java, null)
                    .commit()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
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
                if(topic == "test/robot")
                {
                    when("$mqttMessage")
                    {
                        "Not Activated" -> robotSub = 0
                        "Activated" -> robotSub = 1
                        "Solved" -> robotSub = 2
                    }
                }
                if(topic == "test/padLock")
                {
                    when("$mqttMessage")
                    {
                        "Not Activated" -> padLockSub = 0
                        "Activated" -> padLockSub = 1
                        "Solved" -> padLockSub = 2
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
        val inputStream = assets.open("myimages.imgdb")
        database= AugmentedImageDatabase.deserialize(session,inputStream)

        //If you want to generate database on the go
        /*database = AugmentedImageDatabase(session)
        val padlock = BitmapFactory.decodeResource(resources, R.drawable.padlock)
        val robotImage = BitmapFactory.decodeResource(resources, R.drawable.robot)
        database!!.addImage("robot", robotImage)
        database!!.addImage("padlock", padlock)*/

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
                    "Unable to load rabbit model",
                    Toast.LENGTH_LONG
                )
                    .show()
                null
            })
    }

    private fun onAugmentedImageTrackingUpdate(augmentedImage: AugmentedImage) {
        //If robot image already detected, we can set it to false so the tracking takes place again.
        if (robotDetected && padLockDetected) {
            //robotDetected = false
            //padLockDetected = false
            return
        }
        if (augmentedImage.trackingState == TrackingState.TRACKING ) {
            val anchorNodeRobot = AnchorNode(augmentedImage.createAnchor(augmentedImage.centerPose))
            val anchorNodePadLock = AnchorNode(augmentedImage.createAnchor(augmentedImage.centerPose))
            if (!robotDetected && augmentedImage.name == "robot") {
                robotDetected = true
                val topic = "test/robot"
                mqttClient.subscribe(topic)
                when(robotSub)
                {
                    0 -> placeObject(anchorNodeRobot, "models/nactive.glb")
                    1 -> placeObject(anchorNodeRobot, "models/hint.glb")
                    2 -> placeObject(anchorNodeRobot, "models/solved.glb")
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    arFragment!!.arSceneView.scene.removeChild(anchorNodeRobot)
                    robotDetected = false
                }, 1000)
            }
            if (!padLockDetected && augmentedImage.name == "padlock") {
                padLockDetected = true
                val topic = "test/padLock"
                mqttClient.subscribe(topic)
                when(padLockSub)
                {
                    0 -> placeObject(anchorNodePadLock, "models/nactive.glb")
                    1 -> placeObject(anchorNodePadLock, "models/hint.glb")
                    2 -> placeObject(anchorNodePadLock, "models/solved.glb")
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    arFragment!!.arSceneView.scene.removeChild(anchorNodePadLock)
                    padLockDetected = false
                }, 1000)
            }
        }
        /*if (robotDetected) {
            arFragment!!.instructionsController.setEnabled(
                InstructionsController.TYPE_AUGMENTED_IMAGE_SCAN, false
            )
        }*/
    }
}
