package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.BuildConfig
import com.example.MainActivity
import com.example.R
import com.example.actions.DeviceActionResult
import com.example.actions.MyraDeviceActions
import com.example.data.model.AssistantState
import com.example.data.model.Persona
import com.example.data.repository.MyraRepository
import com.example.voice.VoiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyraVoiceForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var voiceManager: VoiceManager
    private lateinit var repository: MyraRepository

    companion object {
        const val CHANNEL_ID = "myra_voice_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START = "com.example.service.action.START"
        const val ACTION_STOP = "com.example.service.action.STOP"
        const val ACTION_TOGGLE_MIC = "com.example.service.action.TOGGLE_MIC"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _serviceStatusMessage = MutableStateFlow("Background voice assistant active")
        val serviceStatusMessage: StateFlow<String> = _serviceStatusMessage.asStateFlow()

        private val _isWakeWordEnabled = MutableStateFlow(true)
        val isWakeWordEnabled: StateFlow<Boolean> = _isWakeWordEnabled.asStateFlow()

        fun setWakeWordEnabled(enabled: Boolean) {
            _isWakeWordEnabled.value = enabled
        }
    }

    override fun onCreate() {
        super.onCreate()
        voiceManager = VoiceManager(this)
        repository = MyraRepository(this)

        acquireWakeLock()
        createNotificationChannel()
        setupVoiceCallbacks()
        _isServiceRunning.value = true
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MyraAI::BackgroundVoiceWakeLock"
            ).apply {
                acquire(10 * 60 * 1000L) // 10 minutes timeout safeguard
            }
        } catch (e: Exception) {
            Log.e("MyraVoiceService", "Error acquiring wake lock", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "MYRA AI Voice Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground microphone service for hands-free background AI assistance"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MyraVoiceForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val micIntent = Intent(this, MyraVoiceForegroundService::class.java).apply {
            action = ACTION_TOGGLE_MIC
        }
        val micPendingIntent = PendingIntent.getService(
            this,
            2,
            micIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MYRA AI — Voice Assistant Active")
            .setContentText(statusText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .addAction(R.mipmap.ic_launcher, "Mic", micPendingIntent)
            .addAction(R.mipmap.ic_launcher, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(statusText: String) {
        _serviceStatusMessage.value = statusText
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_MIC -> {
                if (voiceManager.isListening.value) {
                    voiceManager.stopListening()
                    updateNotification("Microphone muted. Tap to resume.")
                } else {
                    voiceManager.startListening()
                    updateNotification("Listening for your command...")
                }
            }
            else -> {
                val notification = buildNotification("Listening in background • Hey MYRA ready")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceCompat.startForeground(
                        this,
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }

                voiceManager.startListening()
            }
        }
        return START_STICKY
    }

    private fun setupVoiceCallbacks() {
        voiceManager.onSpeechResult = { recognizedText ->
            handleSpeechInput(recognizedText)
        }

        serviceScope.launch {
            voiceManager.isListening.collect { listening ->
                if (listening) {
                    updateNotification("Listening... (Say command or 'Hey MYRA')")
                } else if (voiceManager.isSpeaking.value) {
                    updateNotification("MYRA is speaking aloud...")
                } else {
                    updateNotification("MYRA background standby • Ready")
                }
            }
        }
    }

    private fun handleSpeechInput(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        vibrateFeedback()

        // 1. Wake word filtering if enabled
        val isWakeWord = checkWakeWord(trimmed)
        val cleanCommand = if (isWakeWord) {
            stripWakeWord(trimmed)
        } else trimmed

        updateNotification("Processing: \"$cleanCommand\"")

        // If user just said "Hey MYRA" alone
        if (isWakeWord && cleanCommand.isBlank()) {
            voiceManager.speak("Yes sweetheart, I'm listening. How can I help you?", isConversationalTurn = true)
            return
        }

        // 2. Check for device command (Open app, set alarm, camera, etc.)
        val deviceResult = MyraDeviceActions.parseAndExecuteCommand(this, cleanCommand)
        if (deviceResult != null) {
            when (deviceResult) {
                is DeviceActionResult.Executed -> {
                    val isStopCommand = cleanCommand.lowercase().trim() in setOf("stop listening", "stop", "go to sleep", "mute", "quiet")
                    if (isStopCommand) {
                        voiceManager.stopListening()
                    }
                    voiceManager.speak(deviceResult.message, isConversationalTurn = !isStopCommand)
                    updateNotification("Done: ${deviceResult.message}")
                }
                is DeviceActionResult.NeedsConfirmation -> {
                    voiceManager.speak("${deviceResult.actionPrompt}. Please confirm.", isConversationalTurn = true)
                }
                is DeviceActionResult.Error -> {
                    voiceManager.speak(deviceResult.error, isConversationalTurn = false)
                }
            }
            return
        }

        // 3. Process with Gemini AI
        serviceScope.launch {
            val apiKey = repository.getEffectiveApiKey(BuildConfig.GEMINI_API_KEY)
            val result = repository.generateAssistantResponse(
                prompt = cleanCommand,
                history = emptyList(),
                persona = Persona.HELPFUL,
                apiKey = apiKey
            )

            result.fold(
                onSuccess = { reply ->
                    voiceManager.speak(reply, isConversationalTurn = true)
                    updateNotification("Answered: ${reply.take(40)}...")
                },
                onFailure = { error ->
                    val fallback = "I heard you: \"$cleanCommand\". I'm here in background mode to assist you."
                    voiceManager.speak(fallback, isConversationalTurn = false)
                }
            )
        }
    }

    private fun checkWakeWord(text: String): Boolean {
        if (!_isWakeWordEnabled.value) return false
        val lower = text.lowercase()
        return lower.startsWith("hey myra") ||
               lower.startsWith("ok myra") ||
               lower.startsWith("hello myra") ||
               lower.startsWith("myra") ||
               lower.contains("মাইরা") ||
               lower.contains("मायरा")
    }

    private fun stripWakeWord(text: String): String {
        return text
            .replace(Regex("^(hey|ok|hello)\\s+myra[\\s,:]*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^myra[\\s,:]*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^(হাই|হ্যালো|হে)\\s+মাইরা[\\s,:]*"), "")
            .replace(Regex("^(नमस्ते|हे)\\s+मायरा[\\s,:]*"), "")
            .trim()
    }

    private fun vibrateFeedback() {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        } catch (e: Exception) {
            Log.w("MyraVoiceService", "Vibration failed", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false
        voiceManager.destroy()
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.e("MyraVoiceService", "Error releasing wake lock", e)
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
