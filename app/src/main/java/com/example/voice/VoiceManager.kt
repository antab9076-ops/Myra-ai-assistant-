package com.example.voice

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
import android.speech.tts.Voice
import android.util.Log
import com.example.data.model.FemaleVoiceProfile
import com.example.data.model.FemaleVoiceProfiles
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class VoiceManager(private val context: Context) : RecognitionListener, TextToSpeech.OnInitListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    // State flows
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _audioRmsLevel = MutableStateFlow(0f)
    val audioRmsLevel: StateFlow<Float> = _audioRmsLevel.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    // Voice profile & settings
    private val _currentVoiceProfile = MutableStateFlow(FemaleVoiceProfiles.first())
    val currentVoiceProfile: StateFlow<FemaleVoiceProfile> = _currentVoiceProfile.asStateFlow()

    private val _availableFemaleVoices = MutableStateFlow<List<Voice>>(emptyList())
    val availableFemaleVoices: StateFlow<List<Voice>> = _availableFemaleVoices.asStateFlow()

    private val _selectedSystemVoiceName = MutableStateFlow<String?>(null)
    val selectedSystemVoiceName: StateFlow<String?> = _selectedSystemVoiceName.asStateFlow()

    private val _isContinuousMode = MutableStateFlow(true)
    val isContinuousMode: StateFlow<Boolean> = _isContinuousMode.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _pitch = MutableStateFlow(1.18f) // Default soft, sweet female pitch
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _speechRate = MutableStateFlow(0.95f) // Natural gentle cadence
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    var onSpeechResult: ((String) -> Unit)? = null
    var onSpeechPartialResult: ((String) -> Unit)? = null
    var onTtsFinished: (() -> Unit)? = null

    // Avoid looping when user cancels or pauses
    private var shouldContinueListeningOnTtsDone = false

    init {
        mainHandler.post {
            initSpeechRecognizer()
            initTts()
        }
    }

    private fun initSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@VoiceManager)
                }
            } else {
                Log.w("VoiceManager", "Speech recognition is not available on this device")
            }
        } catch (e: Exception) {
            Log.e("VoiceManager", "Failed to create SpeechRecognizer", e)
        }
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e("VoiceManager", "Failed to create TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                isTtsInitialized = true
                applySweetFemaleVoiceConfiguration(engine)
                setupUtteranceListener(engine)
            }
        } else {
            Log.e("VoiceManager", "TTS initialization failed: $status")
        }
    }

    private fun applySweetFemaleVoiceConfiguration(engine: TextToSpeech) {
        try {
            engine.setPitch(_pitch.value)
            engine.setSpeechRate(_speechRate.value)

            // Discover and extract natural female voices installed on system
            val allVoices = engine.voices
            if (allVoices != null) {
                val femaleVoices = allVoices.filter { voice ->
                    val name = voice.name.lowercase(Locale.ROOT)
                    name.contains("female") ||
                    name.contains("woman") ||
                    name.contains("sfg") ||
                    name.contains("cfl") ||
                    name.contains("f0") ||
                    name.contains("f1") ||
                    name.contains("hie") ||
                    name.contains("bnf") ||
                    (!name.contains("male") && !name.contains("m0"))
                }
                _availableFemaleVoices.value = femaleVoices

                // If user selected a custom voice, apply it; otherwise find best sweet female voice
                val chosenVoice = if (_selectedSystemVoiceName.value != null) {
                    femaleVoices.find { it.name == _selectedSystemVoiceName.value }
                } else {
                    femaleVoices.find { it.locale.language == "en" } ?: femaleVoices.firstOrNull()
                }

                if (chosenVoice != null) {
                    engine.voice = chosenVoice
                } else {
                    engine.language = Locale.US
                }
            } else {
                engine.language = Locale.US
            }
        } catch (e: Exception) {
            Log.w("VoiceManager", "Exception configuring female voice", e)
            engine.language = Locale.US
        }
    }

    private fun setupUtteranceListener(engine: TextToSpeech) {
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                mainHandler.post {
                    onTtsFinished?.invoke()

                    // Continuous hands-free two-way conversation mode
                    if (_isContinuousMode.value && shouldContinueListeningOnTtsDone) {
                        shouldContinueListeningOnTtsDone = false
                        mainHandler.postDelayed({
                            if (!_isSpeaking.value && !_isListening.value) {
                                startListening()
                            }
                        }, 400) // Brief natural pause before auto-listening
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                mainHandler.post { onTtsFinished?.invoke() }
            }
        })
    }

    fun setVoiceProfile(profile: FemaleVoiceProfile) {
        _currentVoiceProfile.value = profile
        _pitch.value = profile.pitch
        _speechRate.value = profile.speed

        tts?.let { engine ->
            engine.setPitch(profile.pitch)
            engine.setSpeechRate(profile.speed)
        }
    }

    fun selectSystemVoice(voiceName: String) {
        _selectedSystemVoiceName.value = voiceName
        tts?.let { engine ->
            val match = engine.voices?.find { it.name == voiceName }
            if (match != null) {
                engine.voice = match
            }
        }
    }

    fun setContinuousMode(enabled: Boolean) {
        _isContinuousMode.value = enabled
        if (!enabled) {
            shouldContinueListeningOnTtsDone = false
        }
    }

    fun toggleMute() {
        val newMuted = !_isMuted.value
        _isMuted.value = newMuted
        if (newMuted && _isSpeaking.value) {
            stopSpeaking()
        }
    }

    fun setVolume(vol: Float) {
        _volume.value = vol.coerceIn(0f, 1f)
    }

    fun setPitch(pitchVal: Float) {
        _pitch.value = pitchVal.coerceIn(0.5f, 2.0f)
        tts?.setPitch(_pitch.value)
    }

    fun setSpeechRate(rateVal: Float) {
        _speechRate.value = rateVal.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(_speechRate.value)
    }

    fun startListening() {
        mainHandler.post {
            // Interrupt assistant speaking if she's currently talking (Barge-in)
            interruptSpeaking()
            _speechError.value = null

            if (speechRecognizer == null) {
                initSpeechRecognizer()
            }

            val recognizer = speechRecognizer
            if (recognizer == null) {
                _speechError.value = "Speech recognition is not available on this device."
                return@post
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            try {
                recognizer.startListening(intent)
                _isListening.value = true
            } catch (e: Exception) {
                Log.e("VoiceManager", "Error starting speech recognition", e)
                _isListening.value = false
                _speechError.value = "Could not activate microphone: ${e.message}"
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            shouldContinueListeningOnTtsDone = false
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e("VoiceManager", "Error stopping listening", e)
            }
            _isListening.value = false
            _audioRmsLevel.value = 0f
        }
    }

    fun speak(text: String, isConversationalTurn: Boolean = true, onComplete: (() -> Unit)? = null) {
        if (!isTtsInitialized || tts == null) {
            Log.w("VoiceManager", "TTS not initialized, skipping speak")
            onComplete?.invoke()
            return
        }

        if (_isMuted.value) {
            Log.d("VoiceManager", "Voice is muted, skipping audio output")
            onComplete?.invoke()
            return
        }

        // Barge-in: stop any ongoing utterance and stop listening
        stopListening()
        tts?.stop()

        onTtsFinished = onComplete
        shouldContinueListeningOnTtsDone = isConversationalTurn && _isContinuousMode.value

        val utteranceId = UUID.randomUUID().toString()

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, _volume.value)
        }

        // Detect language to handle Assamese, Bengali, Hindi, or English naturally
        val detectedLocale = detectBestLocaleForText(text)
        prepareTtsForLocale(detectedLocale)

        // Make speech sound sweet, soft, realistic, and conversational with natural breath pauses
        val naturalSpeech = preprocessTextForNaturalSpeech(text)

        try {
            _isSpeaking.value = true
            tts?.speak(naturalSpeech, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error speaking text", e)
            _isSpeaking.value = false
            onComplete?.invoke()
        }
    }

    /**
     * Instantly interrupts MYRA when user speaks, taps microphone, or taps stop button.
     */
    fun interruptSpeaking() {
        shouldContinueListeningOnTtsDone = false
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error interrupting TTS", e)
        }
        _isSpeaking.value = false
    }

    fun stopSpeaking() {
        interruptSpeaking()
    }

    private fun detectBestLocaleForText(text: String): Locale {
        for (ch in text) {
            // Assamese distinct characters
            if (ch == 'ৰ' || ch == 'ৱ') {
                return Locale.forLanguageTag("as-IN")
            }
            // Bengali script block
            if (ch in '\u0980'..'\u09FF') {
                return Locale.forLanguageTag("bn-IN")
            }
            // Devanagari (Hindi) script block
            if (ch in '\u0900'..'\u097F') {
                return Locale.forLanguageTag("hi-IN")
            }
        }
        return Locale.US
    }

    private fun prepareTtsForLocale(targetLocale: Locale) {
        val engine = tts ?: return
        try {
            val res = engine.setLanguage(targetLocale)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                // If Assamese is not installed on engine, fallback to Bengali
                if (targetLocale.language == "as") {
                    engine.setLanguage(Locale.forLanguageTag("bn-IN"))
                } else {
                    engine.setLanguage(Locale.US)
                }
            }

            // Restore sweet female pitch and rate for the target language
            engine.setPitch(_pitch.value)
            engine.setSpeechRate(_speechRate.value)
        } catch (e: Exception) {
            Log.w("VoiceManager", "Locale switch error: ${targetLocale.displayName}", e)
        }
    }

    private fun preprocessTextForNaturalSpeech(raw: String): String {
        return raw
            // Remove markdown code blocks and inline code
            .replace(Regex("```[\\s\\S]*?```"), "Code snippet omitted.")
            .replace(Regex("`([^`]+)`"), "$1")
            // Remove markdown formatting
            .replace(Regex("[*#_~>]"), "")
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
            // Add gentle natural pauses after bullet points or list numbers
            .replace(Regex("^[•\\-]\\s*", RegexOption.MULTILINE), ", ")
            .replace(Regex("^\\d+\\.\\s*", RegexOption.MULTILINE), ", ")
            // Soften robotic punctuation
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
                tts?.stop()
                tts?.shutdown()
                tts = null
            } catch (e: Exception) {
                Log.e("VoiceManager", "Error destroying VoiceManager", e)
            }
        }
    }

    // RecognitionListener callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        _isListening.value = true
        _speechError.value = null
    }

    override fun onBeginningOfSpeech() {
        _isListening.value = true
    }

    override fun onRmsChanged(rmsdB: Float) {
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _audioRmsLevel.value = normalized
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _isListening.value = false
        _audioRmsLevel.value = 0f
    }

    override fun onError(error: Int) {
        _isListening.value = false
        _audioRmsLevel.value = 0f
        val errorMsg = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording issue"
            SpeechRecognizer.ERROR_CLIENT -> "Client speech recognition error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network connection required"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer is busy"
            SpeechRecognizer.ERROR_SERVER -> "Voice server unavailable"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
            else -> "Voice recognition notice ($error)"
        }
        if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            _speechError.value = errorMsg
        }
    }

    override fun onResults(results: Bundle?) {
        _isListening.value = false
        _audioRmsLevel.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim()
        if (!text.isNullOrBlank()) {
            onSpeechResult?.invoke(text)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim()
        if (!text.isNullOrBlank()) {
            onSpeechPartialResult?.invoke(text)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
