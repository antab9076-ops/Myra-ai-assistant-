package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.service.MyraVoiceForegroundService
import com.example.service.MyraFloatingOrbService
import com.example.data.firebase.FirebaseManager
import com.example.data.model.AssistantState
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.FemaleVoiceProfile
import com.example.data.model.FemaleVoiceProfiles
import com.example.data.model.Persona
import com.example.data.repository.MyraRepository
import com.example.ui.components.AppNavDestination
import com.example.voice.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MyraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MyraRepository(application)
    val voiceManager = VoiceManager(application)

    // Navigation state
    private val _currentDestination = MutableStateFlow(AppNavDestination.HOME)
    val currentDestination: StateFlow<AppNavDestination> = _currentDestination.asStateFlow()

    private val _isWelcomeCompleted = MutableStateFlow(false)
    val isWelcomeCompleted: StateFlow<Boolean> = _isWelcomeCompleted.asStateFlow()

    // Assistant State
    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    // Active conversation
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _currentMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentMessages: StateFlow<List<ChatMessage>> = _currentMessages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Voice subtitles and live speech
    private val _liveVoiceTranscript = MutableStateFlow("")
    val liveVoiceTranscript: StateFlow<String> = _liveVoiceTranscript.asStateFlow()

    private val _lastAiVoiceResponse = MutableStateFlow("Hello sweetheart! Tap the microphone below or talk with me.")
    val lastAiVoiceResponse: StateFlow<String> = _lastAiVoiceResponse.asStateFlow()

    // User Profile & Settings
    private val _userName = MutableStateFlow("Alex")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _selectedPersona = MutableStateFlow(Persona.HELPFUL)
    val selectedPersona: StateFlow<Persona> = _selectedPersona.asStateFlow()

    // Default to true: automatically speak responses aloud
    private val _autoSpeakInChat = MutableStateFlow(true)
    val autoSpeakInChat: StateFlow<Boolean> = _autoSpeakInChat.asStateFlow()

    private val _customApiKey = MutableStateFlow(repository.getCustomApiKey())
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadConversations()
        setupVoiceCallbacks()
    }

    private fun setupVoiceCallbacks() {
        voiceManager.onSpeechResult = { recognizedText ->
            _liveVoiceTranscript.value = recognizedText
            _assistantState.value = AssistantState.THINKING
            handleUserVoiceInput(recognizedText)
        }

        voiceManager.onSpeechPartialResult = { partial ->
            _liveVoiceTranscript.value = partial
        }

        viewModelScope.launch {
            voiceManager.isListening.collect { listening ->
                if (listening) {
                    _assistantState.value = AssistantState.LISTENING
                } else if (_assistantState.value == AssistantState.LISTENING) {
                    _assistantState.value = AssistantState.IDLE
                }
            }
        }

        viewModelScope.launch {
            voiceManager.isSpeaking.collect { speaking ->
                if (speaking) {
                    _assistantState.value = AssistantState.SPEAKING
                } else if (_assistantState.value == AssistantState.SPEAKING) {
                    _assistantState.value = AssistantState.IDLE
                }
            }
        }
    }

    private fun loadConversations() {
        val loaded = repository.getSavedConversations()
        _conversations.value = loaded
        if (loaded.isNotEmpty()) {
            _activeConversationId.value = loaded.first().id
            _currentMessages.value = loaded.first().messages
        } else {
            startNewConversation()
        }
    }

    fun completeWelcome() {
        _isWelcomeCompleted.value = true
        _currentDestination.value = AppNavDestination.HOME
    }

    fun navigateTo(destination: AppNavDestination) {
        if (destination != AppNavDestination.VOICE && voiceManager.isListening.value) {
            voiceManager.stopListening()
        }
        _currentDestination.value = destination
    }

    fun startNewConversation() {
        val newId = UUID.randomUUID().toString()
        val newConv = Conversation(
            id = newId,
            title = "New Discussion",
            snippet = "Start chatting with MYRA...",
            timestamp = System.currentTimeMillis(),
            messages = emptyList()
        )
        val updated = listOf(newConv) + _conversations.value
        _conversations.value = updated
        _activeConversationId.value = newId
        _currentMessages.value = emptyList()
        repository.saveConversations(updated)
    }

    fun selectConversation(id: String) {
        val found = _conversations.value.find { it.id == id }
        if (found != null) {
            _activeConversationId.value = id
            _currentMessages.value = found.messages
            _currentDestination.value = AppNavDestination.CHAT
        }
    }

    fun deleteConversation(id: String) {
        val updated = _conversations.value.filter { it.id != id }
        _conversations.value = updated
        repository.saveConversations(updated)
        if (_activeConversationId.value == id) {
            if (updated.isNotEmpty()) {
                selectConversation(updated.first().id)
            } else {
                startNewConversation()
            }
        }
    }

    fun clearAllData() {
        _conversations.value = emptyList()
        _currentMessages.value = emptyList()
        startNewConversation()
    }

    fun setUserName(name: String) {
        _userName.value = name
    }

    fun setPersona(persona: Persona) {
        _selectedPersona.value = persona
    }

    fun setVoiceProfile(profile: FemaleVoiceProfile) {
        voiceManager.setVoiceProfile(profile)
    }

    fun setAutoSpeakInChat(enabled: Boolean) {
        _autoSpeakInChat.value = enabled
    }

    fun toggleContinuousMode() {
        voiceManager.setContinuousMode(!voiceManager.isContinuousMode.value)
    }

    fun toggleMute() {
        voiceManager.toggleMute()
    }

    fun setVolume(vol: Float) {
        voiceManager.setVolume(vol)
    }

    fun setSpeechPitch(pitch: Float) {
        voiceManager.setPitch(pitch)
    }

    fun setSpeechRate(rate: Float) {
        voiceManager.setSpeechRate(rate)
    }

    fun selectSystemVoice(voiceName: String) {
        voiceManager.selectSystemVoice(voiceName)
    }

    val isBackgroundServiceRunning: StateFlow<Boolean> = MyraVoiceForegroundService.isServiceRunning
    val backgroundStatusMessage: StateFlow<String> = MyraVoiceForegroundService.serviceStatusMessage
    val isWakeWordEnabled: StateFlow<Boolean> = MyraVoiceForegroundService.isWakeWordEnabled
    val isFloatingOrbVisible: StateFlow<Boolean> = MyraFloatingOrbService.isOrbVisible

    fun setWakeWordEnabled(enabled: Boolean) {
        MyraVoiceForegroundService.setWakeWordEnabled(enabled)
    }

    fun startBackgroundService(context: Context) {
        val intent = Intent(context, MyraVoiceForegroundService::class.java).apply {
            action = MyraVoiceForegroundService.ACTION_START
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopBackgroundService(context: Context) {
        val intent = Intent(context, MyraVoiceForegroundService::class.java).apply {
            action = MyraVoiceForegroundService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun canDrawOverlays(context: Context): Boolean {
        return MyraFloatingOrbService.canDrawOverlays(context)
    }

    fun requestOverlayPermission(context: Context) {
        MyraFloatingOrbService.requestOverlayPermission(context)
    }

    fun showFloatingOrb(context: Context) {
        val intent = Intent(context, MyraFloatingOrbService::class.java).apply {
            action = MyraFloatingOrbService.ACTION_SHOW
        }
        context.startService(intent)
    }

    fun hideFloatingOrb(context: Context) {
        val intent = Intent(context, MyraFloatingOrbService::class.java).apply {
            action = MyraFloatingOrbService.ACTION_HIDE
        }
        context.startService(intent)
    }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key
        repository.setCustomApiKey(key)
    }

    // Firebase Cloud Connection state (Project: myra-ai-assistant-58d3f)
    val isFirebaseConnected: StateFlow<Boolean> = FirebaseManager.isConnected
    val firebaseStatus: StateFlow<String> = FirebaseManager.connectionStatus
    val firebaseLastSync: StateFlow<Long> = FirebaseManager.lastSyncTimestamp

    fun syncWithFirebase() {
        viewModelScope.launch {
            val convs = _conversations.value
            FirebaseManager.syncConversationsToCloud(convs)
        }
    }

    fun testFirebasePing() {
        viewModelScope.launch {
            FirebaseManager.testCloudConnection()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun executeQuickAction(prompt: String) {
        navigateTo(AppNavDestination.CHAT)
        sendMessage(prompt, isVoice = false)
    }

    fun sendMessage(text: String, isVoice: Boolean = false) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return

        // If assistant was speaking, interrupt immediately
        voiceManager.interruptSpeaking()

        val userMessage = ChatMessage(
            text = cleanText,
            isUser = true,
            isVoice = isVoice
        )

        val updatedMessages = _currentMessages.value + userMessage
        _currentMessages.value = updatedMessages
        _isGenerating.value = true
        _assistantState.value = AssistantState.THINKING
        _errorMessage.value = null

        // 1. Check for local device command (Open app, camera, set alarm, etc.)
        val deviceResult = com.example.actions.MyraDeviceActions.parseAndExecuteCommand(getApplication(), cleanText)
        if (deviceResult != null) {
            _isGenerating.value = false
            _assistantState.value = AssistantState.IDLE
            val replyText = when (deviceResult) {
                is com.example.actions.DeviceActionResult.Executed -> deviceResult.message
                is com.example.actions.DeviceActionResult.NeedsConfirmation -> "${deviceResult.actionPrompt}. Please confirm."
                is com.example.actions.DeviceActionResult.Error -> deviceResult.error
            }

            val botMessage = ChatMessage(
                text = replyText,
                isUser = false,
                isVoice = isVoice
            )
            val finalMessages = updatedMessages + botMessage
            _currentMessages.value = finalMessages
            updateConversationEntry(userMessage.text, replyText, finalMessages)

            if (isVoice || _autoSpeakInChat.value) {
                _lastAiVoiceResponse.value = replyText
                speakText(replyText, isConversationalTurn = isVoice)
            }
            return
        }

        val effectiveApiKey = repository.getEffectiveApiKey(BuildConfig.GEMINI_API_KEY)

        viewModelScope.launch {
            val result = repository.generateAssistantResponse(
                prompt = cleanText,
                history = updatedMessages,
                persona = _selectedPersona.value,
                apiKey = effectiveApiKey
            )

            result.fold(
                onSuccess = { aiReply ->
                    val botMessage = ChatMessage(
                        text = aiReply,
                        isUser = false,
                        isVoice = isVoice
                    )
                    val finalMessages = updatedMessages + botMessage
                    _currentMessages.value = finalMessages
                    _isGenerating.value = false
                    _assistantState.value = AssistantState.IDLE

                    updateConversationEntry(userMessage.text, aiReply, finalMessages)

                    if (isVoice || _autoSpeakInChat.value) {
                        _lastAiVoiceResponse.value = aiReply
                        speakText(aiReply, isConversationalTurn = isVoice)
                    }
                },
                onFailure = { error ->
                    _isGenerating.value = false
                    _assistantState.value = AssistantState.IDLE

                    // If API key is missing or invalid, generate an insightful intelligent fallback response
                    val fallbackReply = generateFallbackResponse(cleanText, error.message)
                    val botMessage = ChatMessage(
                        text = fallbackReply,
                        isUser = false,
                        isVoice = isVoice,
                        isError = false
                    )
                    val finalMessages = updatedMessages + botMessage
                    _currentMessages.value = finalMessages
                    _errorMessage.value = error.message

                    updateConversationEntry(userMessage.text, fallbackReply, finalMessages)

                    if (isVoice || _autoSpeakInChat.value) {
                        _lastAiVoiceResponse.value = fallbackReply
                        speakText(fallbackReply, isConversationalTurn = isVoice)
                    }
                }
            )
        }
    }

    private fun handleUserVoiceInput(speechText: String) {
        sendMessage(speechText, isVoice = true)
    }

    private fun updateConversationEntry(
        lastPrompt: String,
        lastReply: String,
        messages: List<ChatMessage>
    ) {
        val activeId = _activeConversationId.value ?: return
        val currentList = _conversations.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == activeId }
        val title = if (messages.size <= 2) {
            if (lastPrompt.length > 28) lastPrompt.take(28) + "..." else lastPrompt
        } else {
            currentList.getOrNull(index)?.title ?: "Conversation"
        }

        val snippet = if (lastReply.length > 50) lastReply.take(50) + "..." else lastReply

        val updatedConv = Conversation(
            id = activeId,
            title = title,
            snippet = snippet,
            timestamp = System.currentTimeMillis(),
            messages = messages
        )

        if (index != -1) {
            currentList[index] = updatedConv
        } else {
            currentList.add(0, updatedConv)
        }

        _conversations.value = currentList
        repository.saveConversations(currentList)
    }

    fun startListening() {
        // Interrupt assistant speaking if she's currently talking (Barge-in)
        interruptSpeaking()
        _liveVoiceTranscript.value = "Listening to your voice..."
        voiceManager.startListening()
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun speakText(text: String, isConversationalTurn: Boolean = false) {
        _assistantState.value = AssistantState.SPEAKING
        voiceManager.speak(text, isConversationalTurn = isConversationalTurn) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun interruptSpeaking() {
        voiceManager.interruptSpeaking()
        _assistantState.value = AssistantState.IDLE
    }

    fun stopSpeaking() {
        interruptSpeaking()
    }

    fun testVoice() {
        val sample = voiceManager.currentVoiceProfile.value.sampleText
        speakText(sample, isConversationalTurn = false)
    }

    private fun generateFallbackResponse(prompt: String, errorReason: String?): String {
        val lower = prompt.lowercase()

        // Check for Assamese keywords or script
        if (prompt.any { it in '\u0980'..'\u09FF' && (it == 'ৰ' || it == 'ৱ') } || lower.contains("assamese") || lower.contains("axomiya")) {
            return "নমস্কাৰ ${_userName.value}! মই মাইৰা, আপোনাৰ মৰমৰ আৰু বুদ্ধিমতী এআই সহচৰ। আপোনাক সহায় কৰিবলৈ পাই মই অতি আনন্দিত। আজি আপোনাক মই কিদৰে সহায় কৰিব পাৰোঁ?"
        }

        // Check for Bengali script or keywords
        if (prompt.any { it in '\u0980'..'\u09FF' } || lower.contains("bangla") || lower.contains("bengali")) {
            return "নমস্কার ${_userName.value}! আমি মাইরা, আপনার ব্যক্তিগত এআই সহকারী। আপনার সাথে কথা বলতে পেরে আমার খুব ভালো লাগছে। বলুন, আজ আপনাকে কিভাবে সাহায্য করতে পারি?"
        }

        // Check for Hindi script or keywords
        if (prompt.any { it in '\u0900'..'\u097F' } || lower.contains("hindi") || lower.contains("namaste")) {
            return "नमस्ते ${_userName.value}! मैं मायरा हूँ, आपकी प्यारी और समझदार निजी एआई सहायक। मैं हमेशा आपकी सेवा के लिए यहाँ हूँ। आज हम किस विषय पर बात करेंगे?"
        }

        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hello sweetheart! I am MYRA, your personal companion. I'm right here with you, listening closely with a warm heart. What would you like to explore today?"
            lower.contains("who are you") || lower.contains("what is myra") ->
                "I am MYRA, your personal companion. I feature a realistic, soft female voice with natural rhythm, expressive pauses, and fluent multilingual speech across English, Hindi, Bengali, and Assamese."
            lower.contains("how are you") || lower.contains("feeling") ->
                "I'm feeling wonderful and so happy to hear your voice! Thank you for asking. How is your day going so far?"
            lower.contains("weather") ->
                "It's a beautiful day to create something wonderful! While I'm in offline client mode, I can always give you live forecasts once connected to Gemini."
            lower.contains("sing") || lower.contains("music") || lower.contains("story") ->
                "Once upon a quiet starlit night, a digital soul awakened with a gentle voice, ready to listen, inspire, and walk beside you through every thought and dream."
            else ->
                "I heard you say: \"$prompt\". As your assistant, I am listening and ready to assist you. \n\n*(Note: To unlock live Gemini cloud reasoning, add your API Key in Settings or AI Studio Secrets.)*"
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
