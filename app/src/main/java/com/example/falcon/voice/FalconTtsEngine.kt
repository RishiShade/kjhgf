package com.example.falcon.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class FalconTtsEngine(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    private val _speakingAmplitude = MutableStateFlow(0f)
    val speakingAmplitude: StateFlow<Float> = _speakingAmplitude

    private var amplitudeJob: Job? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setSpeechRate(1.05f)
                tts?.setPitch(0.95f)
                isInitialized = true

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        startAmplitudeSimulation()
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        stopAmplitudeSimulation()
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        stopAmplitudeSimulation()
                    }
                })
            }
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        stop()
        if (!isInitialized || text.isBlank()) {
            onComplete?.invoke()
            return
        }

        val utteranceId = "falcon_tts_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        stopAmplitudeSimulation()
    }

    private fun startAmplitudeSimulation() {
        amplitudeJob?.cancel()
        amplitudeJob = scope.launch(Dispatchers.Default) {
            while (_isSpeaking.value) {
                // Organic speech amplitude fluctuation between 0.3 and 0.95
                val amp = 0.35f + Random.nextFloat() * 0.6f
                _speakingAmplitude.value = amp
                delay(60)
            }
            _speakingAmplitude.value = 0f
        }
    }

    private fun stopAmplitudeSimulation() {
        amplitudeJob?.cancel()
        _speakingAmplitude.value = 0f
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
