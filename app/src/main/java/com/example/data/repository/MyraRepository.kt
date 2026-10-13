package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiClient
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.Persona
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import com.example.data.firebase.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MyraRepository(
    private val context: Context,
    private val apiService: GeminiApiService = GeminiClient.service
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        // Initialize Firebase with the connected project: myra-ai-assistant-58d3f
        FirebaseManager.initialize(context)
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("myra_ai_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val conversationListType = Types.newParameterizedType(
        List::class.java,
        Conversation::class.java
    )
    private val conversationAdapter = moshi.adapter<List<Conversation>>(conversationListType)

    fun getCustomApiKey(): String {
        return prefs.getString("custom_gemini_api_key", "") ?: ""
    }

    fun setCustomApiKey(key: String) {
        prefs.edit().putString("custom_gemini_api_key", key.trim()).apply()
    }

    fun getEffectiveApiKey(buildConfigKey: String): String {
        val custom = getCustomApiKey()
        if (custom.isNotBlank()) return custom
        if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
            return buildConfigKey
        }
        return ""
    }

    fun isApiKeyConfigured(buildConfigKey: String): Boolean {
        return getEffectiveApiKey(buildConfigKey).isNotBlank()
    }

    fun getSavedConversations(): List<Conversation> {
        val json = prefs.getString("saved_conversations", null) ?: return getSampleInitialConversations()
        return try {
            conversationAdapter.fromJson(json) ?: getSampleInitialConversations()
        } catch (e: Exception) {
            Log.e("MyraRepository", "Error parsing saved conversations", e)
            getSampleInitialConversations()
        }
    }

    fun saveConversations(conversations: List<Conversation>) {
        try {
            val json = conversationAdapter.toJson(conversations)
            prefs.edit().putString("saved_conversations", json).apply()
            // Asynchronously sync to Firebase Firestore
            coroutineScope.launch {
                try {
                    FirebaseManager.syncConversationsToCloud(conversations)
                } catch (e: Exception) {
                    Log.w("MyraRepository", "Cloud sync notice: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("MyraRepository", "Error saving conversations", e)
        }
    }

    suspend fun generateAssistantResponse(
        prompt: String,
        history: List<ChatMessage>,
        persona: Persona,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your key in Settings or AI Studio Secrets.")
            )
        }

        try {
            // Build conversation history contents
            val contents = mutableListOf<GeminiContent>()

            // Take last 8 messages for context window efficiency
            val contextMessages = history.takeLast(8)
            for (msg in contextMessages) {
                contents.add(
                    GeminiContent(
                        role = if (msg.isUser) "user" else "model",
                        parts = listOf(GeminiPart(text = msg.text))
                    )
                )
            }

            // Append current prompt if not already last user message
            if (contextMessages.lastOrNull()?.text != prompt) {
                contents.add(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                )
            }

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = persona.promptInstruction))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 2048
                )
            )

            val response = apiService.generateContent(apiKey = apiKey, request = request)

            if (response.error != null) {
                return@withContext Result.failure(
                    Exception("Gemini API error: ${response.error.message ?: "Unknown error"}")
                )
            }

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(Exception("Empty response from AI assistant"))
            }
        } catch (e: Exception) {
            Log.e("MyraRepository", "API execution failure", e)
            Result.failure(e)
        }
    }

    private fun getSampleInitialConversations(): List<Conversation> {
        return listOf(
            Conversation(
                id = UUID.randomUUID().toString(),
                title = "Welcome & System Capabilities",
                snippet = "Hello! I am MYRA, your personal AI assistant.",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                messages = listOf(
                    ChatMessage(
                        text = "What can you do, MYRA?",
                        isUser = true,
                        timestamp = System.currentTimeMillis() - 1000 * 60 * 32
                    ),
                    ChatMessage(
                        text = "I am equipped with real-time conversational intelligence, voice processing, speech synthesis, creative synthesis, and code analysis. You can speak with me or chat anytime!",
                        isUser = false,
                        timestamp = System.currentTimeMillis() - 1000 * 60 * 30
                    )
                )
            ),
            Conversation(
                id = UUID.randomUUID().toString(),
                title = "Future of Artificial Intelligence",
                snippet = "Exploring multi-modal reasoning and quantum synergy...",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                messages = listOf(
                    ChatMessage(
                        text = "How will AI assistants evolve in the next 5 years?",
                        isUser = true,
                        timestamp = System.currentTimeMillis() - 1000 * 60 * 122
                    ),
                    ChatMessage(
                        text = "We will transition from reactive prompt engines to proactive, multimodal spatial intelligence agents capable of continuous context understanding, voice fluency, and task autonomy.",
                        isUser = false,
                        timestamp = System.currentTimeMillis() - 1000 * 60 * 120
                    )
                )
            )
        )
    }
}
