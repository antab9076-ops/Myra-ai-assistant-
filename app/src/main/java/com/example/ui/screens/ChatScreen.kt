package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AssistantState
import com.example.data.model.ChatMessage
import com.example.ui.components.AnimatedAiOrb
import com.example.ui.components.GlassCard
import com.example.ui.theme.CosmicBackgroundBrush
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MyraViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: MyraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val messages by viewModel.currentMessages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val persona by viewModel.selectedPersona.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val promptSuggestions = listOf(
        "Summarize quantum computing principles",
        "Draft a cold outreach email for my startup",
        "Debug Kotlin coroutines Dispatchers.IO vs Main",
        "Write a cyberpunk sci-fi short story scene",
        "Plan a 3-day itinerary for Kyoto, Japan"
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    fun handleMicInput() {
        if (isListening) {
            viewModel.stopListening()
        } else {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                viewModel.startListening()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackgroundBrush)
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedAiOrb(
                        state = if (isGenerating) AssistantState.THINKING else AssistantState.IDLE,
                        size = 38.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MYRA AI",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(StatusOnline)
                            )
                        }
                        Text(
                            text = "${persona.displayName} • Gemini 3.5 Flash",
                            color = ElectricCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                // New Chat Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .testTag("chat_new_conversation_button")
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x35BD00FF))
                        .clickable { viewModel.startNewConversation() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = NeonMagenta,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Chat Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AnimatedAiOrb(
                                state = AssistantState.IDLE,
                                size = 120.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "How can I help you today?",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Select a prompt below or start typing to ask MYRA anything.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    ChatMessageBubble(
                        message = message,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(message.text))
                        },
                        onSpeak = {
                            viewModel.speakText(message.text)
                        },
                        onStopSpeaking = {
                            viewModel.stopSpeaking()
                        },
                        isSpeaking = isSpeaking
                    )
                }

                if (isGenerating) {
                    item {
                        GeneratingIndicator()
                    }
                }
            }

            // Prompt Suggestions Carousel (when few messages)
            if (messages.size <= 2) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(promptSuggestions) { prompt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x351C0C38))
                                .border(1.dp, Color(0x40BD00FF), RoundedCornerShape(16.dp))
                                .clickable { viewModel.sendMessage(prompt) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = prompt,
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Message Input Bar (Rounded glassmorphism)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    backgroundColor = Color(0xCC0D061F),
                    borderBrush = Brush.linearGradient(
                        listOf(Color(0x60BD00FF), Color(0x30E040FB), Color(0x20FFFFFF))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mic Button for instant voice dictation
                        IconButton(
                            onClick = { handleMicInput() },
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("chat_inline_mic_button")
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = if (isListening) ElectricCyan else NeonMagenta,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Text Field
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Message MYRA AI...", color = TextMuted, fontSize = 14.sp) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            maxLines = 4,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_message_input_field")
                        )

                        // Send Button
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        if (inputText.isNotBlank()) {
                                            listOf(NeonMagenta, NeonPurple)
                                        } else {
                                            listOf(Color(0x30BD00FF), Color(0x20BD00FF))
                                        }
                                    )
                                )
                                .clickable(
                                    enabled = inputText.isNotBlank() && !isGenerating,
                                    onClick = {
                                        if (inputText.isNotBlank()) {
                                            viewModel.sendMessage(inputText)
                                            inputText = ""
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Message",
                                tint = if (inputText.isNotBlank()) Color.White else TextMuted,
                                modifier = Modifier
                                    .size(18.dp)
                                    .testTag("chat_send_button")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onCopy: () -> Unit,
    onSpeak: () -> Unit,
    onStopSpeaking: () -> Unit,
    isSpeaking: Boolean
) {
    var hasCopied by remember { mutableStateOf(false) }
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    if (hasCopied) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            hasCopied = false
        }
    }

    if (message.isUser) {
        // User Message Bubble (Align Right)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp),
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 6.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 20.dp
                        )
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF8B2FC9), Color(0xFFC026D3))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedTime,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    } else {
        // Assistant Message Bubble (Align Left, Luxury Glassmorphism)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 40.dp),
            horizontalAlignment = Alignment.Start
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    topStart = 6.dp,
                    topEnd = 20.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                ),
                backgroundColor = Color(0x30180B35),
                borderBrush = Brush.linearGradient(
                    listOf(Color(0x60BD00FF), Color(0x15FFFFFF))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    // Header with MYRA badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NeonMagenta)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MYRA",
                                color = NeonMagenta,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Copy & TTS Read Aloud actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    onCopy()
                                    hasCopied = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (hasCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = "Copy message",
                                    tint = if (hasCopied) ElectricCyan else TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (isSpeaking) onStopSpeaking() else onSpeak()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Read aloud",
                                    tint = if (isSpeaking) NeonMagenta else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = message.text,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = formattedTime,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GeneratingIndicator() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x30180B35))
            .border(1.dp, Color(0x40BD00FF), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            color = NeonMagenta,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "MYRA is formulating a response...",
            color = TextSecondary,
            fontSize = 13.sp
        )
    }
}
