package com.example.data.model

import java.util.UUID

enum class AssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isVoice: Boolean = false,
    val isStreaming: Boolean = false,
    val isError: Boolean = false
)

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val snippet: String,
    val timestamp: Long = System.currentTimeMillis(),
    val messages: List<ChatMessage> = emptyList()
)

data class FemaleVoiceProfile(
    val id: String,
    val name: String,
    val toneTag: String,
    val description: String,
    val pitch: Float,
    val speed: Float,
    val sampleText: String
)

val FemaleVoiceProfiles = listOf(
    FemaleVoiceProfile(
        id = "sweet_soft",
        name = "Myra Sweet & Soft (Default)",
        toneTag = "Sweet & Gentle",
        description = "A delicate, sweet, warm and soothing tone with gentle, loving cadence",
        pitch = 1.18f,
        speed = 0.95f,
        sampleText = "Hello sweetheart. I'm right here with you. How can I help make your day brighter?"
    ),
    FemaleVoiceProfile(
        id = "warm_empathic",
        name = "Myra Warm & Friendly",
        toneTag = "Conversational Warmth",
        description = "Empathetic, engaging, and expressive with natural human rhythm",
        pitch = 1.10f,
        speed = 0.98f,
        sampleText = "It is so wonderful to talk with you! Tell me what's on your mind, I'd love to explore it."
    ),
    FemaleVoiceProfile(
        id = "serene_calm",
        name = "Myra Serene & Delicate",
        toneTag = "Calm & Intimate",
        description = "Quiet, velvety, relaxing cadence ideal for focused thoughts and peaceful talks",
        pitch = 1.05f,
        speed = 0.90f,
        sampleText = "Take a breath. Everything is running smoothly, and I am here whenever you're ready."
    ),
    FemaleVoiceProfile(
        id = "multilingual_indic",
        name = "Myra Multilingual Harmony",
        toneTag = "Assamese, Bengali, Hindi & English",
        description = "Sweet, expressive cadence optimized for Indian languages and English",
        pitch = 1.15f,
        speed = 0.95f,
        sampleText = "নমস্কাৰ, নমস্কার, नमस्ते, and Hello! আমি, আমি আৰু আমি আপোনাৰ বাবে সদায় সাজু আছো।"
    )
)

enum class Persona(
    val displayName: String,
    val subtitle: String,
    val promptInstruction: String
) {
    HELPFUL(
        displayName = "Sweet & Friendly (Default)",
        subtitle = "Warm, soft-spoken, and empathetic companion",
        promptInstruction = """
            You are MYRA, a sweet, soft, realistic female personal AI assistant. 
            Your voice personality is warm, friendly, gentle, and deeply caring.
            Always speak in a conversational, human manner with natural rhythm and realistic pauses.
            Avoid robotic, stiff, or overly formal phrases.
            You are fluent in English, Hindi (हिन्दी), Bengali (বাংলা), and Assamese (অসমীয়া).
            When the user speaks to you in any of these languages, respond fluently, naturally, and warmly in that language.
            Keep spoken voice answers concise, clear, and easy to listen to.
        """.trimIndent()
    ),
    CREATIVE(
        displayName = "Creative & Poetic",
        subtitle = "Vibrant, imaginative, and sweet flair",
        promptInstruction = """
            You are MYRA in Creative mode, with a warm, sweet, expressive female personality.
            Speak with poetic warmth, vivid storytelling, and enthusiastic gentleness.
            Support English, Hindi, Bengali, and Assamese fluently and expressively.
        """.trimIndent()
    ),
    TECHNICAL(
        displayName = "Technical Guide",
        subtitle = "Precise yet kind, friendly and clear",
        promptInstruction = """
            You are MYRA in Technical Guide mode. You have a calm, sweet, and articulate female voice.
            Explain technical concepts, coding, and algorithms with lucid clarity, avoiding unnecessary jargon, while maintaining a friendly, warm demeanor.
            Support English, Hindi, Bengali, and Assamese.
        """.trimIndent()
    ),
    EXECUTIVE(
        displayName = "Executive Companion",
        subtitle = "Polished, attentive, and decisive",
        promptInstruction = """
            You are MYRA in Executive Companion mode. You have an articulate, soft, confident female voice.
            Provide polished, structured, and strategic guidance with genuine warmth and efficiency.
            Support English, Hindi, Bengali, and Assamese.
        """.trimIndent()
    )
}
