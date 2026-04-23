package com.example.talkai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.talkai.assistant.CommandHandler
import com.example.talkai.assistant.GroqManager
import com.example.talkai.assistant.VoiceManager
import com.example.talkai.camera.ObjectDetectionManager
import com.example.talkai.location.LocationManager
import com.example.talkai.ui.HomeScreen

class MainActivity : ComponentActivity() {

    private lateinit var voiceManager: VoiceManager
    private lateinit var groqManager: GroqManager
    private lateinit var commandHandler: CommandHandler
    private lateinit var locationManager: LocationManager
    private lateinit var objectDetectionManager: ObjectDetectionManager
    private lateinit var previewView: PreviewView

    private var currentEmotion = "neutral"
    private var isNavigationMode = false

    private val isListeningState = mutableStateOf(false)
    private val isSpeakingState = mutableStateOf(false)
    private val isNavigatingState = mutableStateOf(false)
    private val emotionState = mutableStateOf("neutral 😐")
    private val aiReplyState = mutableStateOf("")
    private val statusState = mutableStateOf("Ready to help")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        voiceManager = VoiceManager(this)
        groqManager = GroqManager()
        commandHandler = CommandHandler()
        locationManager = LocationManager(this)
        previewView = PreviewView(this)
        objectDetectionManager = ObjectDetectionManager(this, this)

        requestAllPermissions()
        setupVoiceCallbacks()
        setupObjectDetection()
        objectDetectionManager.startFrontCamera(previewView)

        setContent {
            val isListening by isListeningState
            val isSpeaking by isSpeakingState
            val isNavigating by isNavigatingState
            val emotion by emotionState
            val aiReply by aiReplyState
            val status by statusState

            HomeScreen(
                isListening = isListening,
                isSpeaking = isSpeaking,
                isNavigating = isNavigating,
                emotion = emotion,
                aiReply = aiReply,
                statusText = status,
                previewView = previewView,
                onTalkClicked = { handleTalkButton() },
                onNavigationToggle = { toggleNavigation() }
            )
        }

        Handler(Looper.getMainLooper()).postDelayed({
            voiceManager.speak(
                "Hello! I am Talk AI. " +
                        "I can guide you, read books for you, " +
                        "and find nearby places. How can I help?"
            )
        }, 2000)
    }

    private fun setupVoiceCallbacks() {
        voiceManager.onListeningStarted = {
            isListeningState.value = true
            statusState.value = "Listening... speak now"
            vibrate(50)
        }

        voiceManager.onListeningEnded = {
            isListeningState.value = false
            statusState.value = "Processing..."
        }

        voiceManager.onSpeechResult = { spokenText ->
            statusState.value = "You said: $spokenText"
            aiReplyState.value = "You said: $spokenText"
            handleUserInput(spokenText)
        }

        voiceManager.onSpeakingDone = {
            isSpeakingState.value = false
            statusState.value = "Ready to help"
        }
    }

    private fun setupObjectDetection() {
        objectDetectionManager.onEmotionDetected = { emotion ->
            currentEmotion = emotion
            emotionState.value = emotion
        }

        objectDetectionManager.onObjectDetected = { description ->
            Handler(Looper.getMainLooper()).post {
                aiReplyState.value = description
                statusState.value = "Describing..."
                isSpeakingState.value = true
            }
            Handler(Looper.getMainLooper()).post {
                voiceManager.speak(description) {
                    Handler(Looper.getMainLooper()).post {
                        isSpeakingState.value = false
                        statusState.value = "Ready to help"
                    }
                }
            }
        }
    }

    private fun handleTalkButton() {
        vibrate(30)
        if (voiceManager.isSpeaking) {
            voiceManager.stopSpeaking()
            isSpeakingState.value = false
            statusState.value = "Tap to talk"
            return
        }
        if (isListeningState.value) return
        voiceManager.startListening()
    }

    private fun handleUserInput(input: String) {
        val command = commandHandler.detectCommand(input)

        when (command) {

            CommandHandler.CommandType.READ_TEXT -> {
                voiceManager.speak(
                    "Let me read that for you. " +
                            "Hold the book steady in front of camera."
                ) {
                    Handler(Looper.getMainLooper()).post {
                        objectDetectionManager.setReadingMode(true)
                        objectDetectionManager.startCamera(previewView)
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        objectDetectionManager.setReadingMode(false)
                        if (!isNavigationMode) {
                            objectDetectionManager.startFrontCamera(
                                previewView
                            )
                        }
                    }, 20000)
                }
            }

            CommandHandler.CommandType.NEARBY_HOSPITAL ->
                findNearbyPlace("hospital")

            CommandHandler.CommandType.NEARBY_PHARMACY ->
                findNearbyPlace("pharmacy")

            CommandHandler.CommandType.NEARBY_SHOP ->
                findNearbyPlace("shop")

            CommandHandler.CommandType.NEARBY_RESTAURANT ->
                findNearbyPlace("restaurant")

            CommandHandler.CommandType.NEARBY_BANK ->
                findNearbyPlace("bank")

            CommandHandler.CommandType.NEARBY_POLICE ->
                findNearbyPlace("police")

            CommandHandler.CommandType.NEARBY_SCHOOL ->
                findNearbyPlace("school")

            CommandHandler.CommandType.WHAT_IS_AROUND -> {
                voiceManager.speak(
                    "Let me look around. Hold phone steady."
                ) {
                    Handler(Looper.getMainLooper()).post {
                        objectDetectionManager.setNavigationMode(false)
                        objectDetectionManager.startCamera(previewView)
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (!isNavigationMode) {
                            objectDetectionManager
                                .startFrontCamera(previewView)
                        }
                    }, 15000)
                }
            }

            CommandHandler.CommandType.START_NAVIGATION -> {
                isNavigationMode = true
                isNavigatingState.value = true
                Handler(Looper.getMainLooper()).post {
                    objectDetectionManager.setNavigationMode(true)
                    objectDetectionManager.startCamera(previewView)
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    voiceManager.speak(
                        "Navigation started. " +
                                "I will tell you if path is clear or blocked."
                    )
                }, 500)
            }

            CommandHandler.CommandType.STOP_NAVIGATION -> {
                isNavigationMode = false
                isNavigatingState.value = false
                objectDetectionManager.setNavigationMode(false)
                Handler(Looper.getMainLooper()).post {
                    objectDetectionManager.startFrontCamera(previewView)
                }
                voiceManager.speak("Navigation stopped.")
            }

            CommandHandler.CommandType.HOW_AM_I_LOOKING -> {
                voiceManager.speak(
                    "You look $currentEmotion right now!"
                )
            }

            CommandHandler.CommandType.GENERAL_CHAT -> {
                sendToAI(input)
            }
        }
    }

    private fun findNearbyPlace(placeType: String) {
        voiceManager.speak("Searching for $placeType nearby.")
        statusState.value = "Searching..."

        locationManager.getCurrentLocation(
            onSuccess = { lat, lng ->
                locationManager.getNearbyPlaces(
                    lat, lng, placeType
                ) { result ->
                    Handler(Looper.getMainLooper()).post {
                        aiReplyState.value = result
                        voiceManager.speak(result)
                    }
                }
            },
            onError = {
                Handler(Looper.getMainLooper()).post {
                    val msg =
                        "Cannot get location. Please enable GPS."
                    aiReplyState.value = msg
                    voiceManager.speak(msg)
                }
            }
        )
    }

    private fun sendToAI(input: String) {
        statusState.value = "Thinking..."
        isSpeakingState.value = true

        groqManager.chat(
            userMessage = input,
            emotion = currentEmotion,
            onResponse = { reply ->
                Handler(Looper.getMainLooper()).post {
                    aiReplyState.value = reply
                    voiceManager.speak(reply) {
                        Handler(Looper.getMainLooper()).post {
                            isSpeakingState.value = false
                            statusState.value = "Ready to help"
                        }
                    }
                }
            },
            onError = {
                Handler(Looper.getMainLooper()).post {
                    val msg = "Connection error. Please try again."
                    aiReplyState.value = msg
                    voiceManager.speak(msg)
                    isSpeakingState.value = false
                }
            }
        )
    }

    private fun toggleNavigation() {
        if (isNavigationMode) {
            handleUserInput("stop navigation")
        } else {
            handleUserInput("start navigation")
        }
    }

    private fun vibrate(ms: Long) {
        try {
            val vibrator = getSystemService(VIBRATOR_SERVICE)
                    as Vibrator
            @Suppress("DEPRECATION")
            vibrator.vibrate(ms)
        } catch (e: Exception) { }
    }

    private fun requestAllPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) !=
                    PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, needed.toTypedArray(), 101
            )
        }
    }

    override fun onDestroy() {
        voiceManager.destroy()
        super.onDestroy()
    }
}