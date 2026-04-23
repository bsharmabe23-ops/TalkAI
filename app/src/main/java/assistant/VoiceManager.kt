package com.example.talkai.assistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class VoiceManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    var isSpeaking = false
    private var isListening = false

    var onSpeechResult: ((String) -> Unit)? = null
    var onListeningStarted: (() -> Unit)? = null
    var onListeningEnded: (() -> Unit)? = null
    var onSpeakingDone: (() -> Unit)? = null

    init {
        setupTTS()
        setupSpeechRecognizer()
    }

    private fun setupTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(0.9f)
                tts?.setPitch(1.0f)
            }
        }
    }

    private fun setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        speechRecognizer = SpeechRecognizer
            .createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    onListeningStarted?.invoke()
                }
                override fun onResults(results: Bundle?) {
                    isListening = false
                    onListeningEnded?.invoke()
                    val matches = results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )
                    if (!matches.isNullOrEmpty()) {
                        onSpeechResult?.invoke(matches[0])
                    }
                }
                override fun onError(error: Int) {
                    isListening = false
                    onListeningEnded?.invoke()
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            }
        )
    }

    fun startListening() {
        if (isSpeaking) {
            tts?.stop()
            isSpeaking = false
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        // Stop anything currently playing
        tts?.stop()
        isSpeaking = true

        tts?.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isSpeaking = true
                }
                override fun onDone(utteranceId: String?) {
                    isSpeaking = false
                    Handler(Looper.getMainLooper()).post {
                        onDone?.invoke()
                        onSpeakingDone?.invoke()
                    }
                }
                override fun onError(utteranceId: String?) {
                    isSpeaking = false
                    Handler(Looper.getMainLooper()).post {
                        onDone?.invoke()
                    }
                }
            }
        )

        val utteranceId = "tts_${System.currentTimeMillis()}"
        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId
        )
    }

    fun stopSpeaking() {
        tts?.stop()
        isSpeaking = false
    }

    fun isCurrentlyListening() = isListening

    fun destroy() {
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
    }
}