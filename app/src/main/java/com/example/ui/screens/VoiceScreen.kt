package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AssistantState
import com.example.data.model.FemaleVoiceProfiles
import com.example.ui.components.AnimatedAiOrb
import com.example.ui.components.GlassCard
import com.example.ui.components.WaveVisualizer
import com.example.ui.theme.CosmicBackgroundBrush
import com.example.ui.theme.CyberPink
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MyraViewModel

@Composable
fun VoiceScreen(
    viewModel: MyraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assistantState by viewModel.assistantState.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val audioLevel by viewModel.voiceManager.audioRmsLevel.collectAsState()
    val liveTranscript by viewModel.liveVoiceTranscript.collectAsState()
    val lastResponse by viewModel.lastAiVoiceResponse.collectAsState()
    val currentVoiceProfile by viewModel.voiceManager.currentVoiceProfile.collectAsState()
    val isContinuousMode by viewModel.voiceManager.isContinuousMode.collectAsState()
    val isMuted by viewModel.voiceManager.isMuted.collectAsState()
    val isBackgroundServiceRunning by viewModel.isBackgroundServiceRunning.collectAsState()
    val isWakeWordEnabled by viewModel.isWakeWordEnabled.collectAsState()
    val isFloatingOrbVisible by viewModel.isFloatingOrbVisible.collectAsState()

    var alternativeTextInput by remember { mutableStateOf("") }
    var permissionDeniedNotice by remember { mutableStateOf(false) }
    var showWakeWordConsentDialog by remember { mutableStateOf(false) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionDeniedNotice = false
            viewModel.startListening()
        } else {
            permissionDeniedNotice = true
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            viewModel.startBackgroundService(context)
        }
    }

    fun handleMicClick() {
        if (isSpeaking) {
            // Barge-in: interrupt assistant talking and immediately start listening to user
            viewModel.interruptSpeaking()
            viewModel.startListening()
            return
        }

        if (isListening) {
            viewModel.stopListening()
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                permissionDeniedNotice = false
                viewModel.startListening()
            } else {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    fun toggleBackgroundService() {
        if (isBackgroundServiceRunning) {
            viewModel.stopBackgroundService(context)
        } else {
            val micGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (!micGranted) {
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notifGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (notifGranted) {
                    viewModel.startBackgroundService(context)
                } else {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                viewModel.startBackgroundService(context)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status & Badges
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status pill
                    val statusText = when {
                        isSpeaking -> "MYRA SPEAKING • TAP MIC TO INTERRUPT"
                        isListening -> "LISTENING TO YOU..."
                        assistantState == AssistantState.THINKING -> "THINKING & PROCESSING..."
                        isBackgroundServiceRunning -> "BACKGROUND SERVICE ACTIVE"
                        else -> "SWEET & SOFT FEMALE VOICE"
                    }

                    val statusColor = when {
                        isSpeaking -> NeonMagenta
                        isListening -> ElectricCyan
                        assistantState == AssistantState.THINKING -> Color(0xFFF59E0B)
                        isBackgroundServiceRunning -> StatusOnline
                        else -> StatusOnline
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x351F0D42))
                            .border(1.dp, statusColor.copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Continuous Mode Toggle Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .testTag("voice_continuous_mode_toggle")
                            .clip(CircleShape)
                            .background(if (isContinuousMode) Color(0x35BD00FF) else Color(0x201F0D42))
                            .border(
                                1.dp,
                                if (isContinuousMode) NeonMagenta else Color(0x35FFFFFF),
                                CircleShape
                            )
                            .clickable { viewModel.toggleContinuousMode() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = if (isContinuousMode) ElectricCyan else TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isContinuousMode) "Auto 2-Way: ON" else "Auto 2-Way: OFF",
                            color = if (isContinuousMode) Color.White else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Supported Languages Bar (Assamese, Bengali, Hindi, English)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x20120524))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Speaks: English • हिन्दी • বাংলা • অসমীয়া",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Hero Animated AI Orb & Sound Waves
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                AnimatedAiOrb(
                    state = assistantState,
                    size = 210.dp,
                    audioLevel = audioLevel
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Live Equalizer Visualizer
                WaveVisualizer(
                    isAnimating = isListening || isSpeaking || assistantState == AssistantState.THINKING,
                    barCount = 11,
                    maxHeight = 26.dp,
                    barWidth = 3.5.dp,
                    barSpacing = 4.dp,
                    audioLevel = audioLevel
                )
            }

            // Subtitle Card: User transcript + MYRA Response
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_transcript_card"),
                backgroundColor = Color(0x30180B35),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    if (isListening && liveTranscript.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "YOU SAID:",
                                color = ElectricCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = liveTranscript,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = NeonMagenta,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MYRA (${currentVoiceProfile.toneTag}):",
                                color = NeonMagenta,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isSpeaking) {
                            Text(
                                text = "Speaking aloud...",
                                color = ElectricCyan,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = lastResponse,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        maxLines = 4
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Background Voice Assistant & Wake-Word Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_background_service_card"),
                backgroundColor = Color(0x28180A30),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (isBackgroundServiceRunning) StatusOnline else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Background Voice Service",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isBackgroundServiceRunning) "Running • Lockscreen & background active" else "Stopped • Tap switch to start service",
                                    color = if (isBackgroundServiceRunning) StatusOnline else TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Switch(
                            checked = isBackgroundServiceRunning,
                            onCheckedChange = { toggleBackgroundService() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonPurple,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = Color(0x351C0C38)
                            ),
                            modifier = Modifier.testTag("voice_bg_service_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "\"Hey MYRA\" Wake-Word (On-Device)",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Switch(
                            checked = isWakeWordEnabled,
                            onCheckedChange = {
                                if (it) {
                                    showWakeWordConsentDialog = true
                                } else {
                                    viewModel.setWakeWordEnabled(false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonMagenta,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = Color(0x351C0C38)
                            ),
                            modifier = Modifier.testTag("voice_wake_word_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Floating AI Screen Orb (Overlay)",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Draggable glowing orb over apps • Tap to open MYRA",
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }

                        Switch(
                            checked = isFloatingOrbVisible,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (viewModel.canDrawOverlays(context)) {
                                        viewModel.showFloatingOrb(context)
                                    } else {
                                        showOverlayPermissionDialog = true
                                    }
                                } else {
                                    viewModel.hideFloatingOrb(context)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ElectricCyan,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = Color(0x351C0C38)
                            ),
                            modifier = Modifier.testTag("voice_floating_orb_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Voice Persona Selector Carousel
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "SELECT FEMALE VOICE:",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(FemaleVoiceProfiles) { profile ->
                        val isSelected = profile.id == currentVoiceProfile.id
                        Box(
                            modifier = Modifier
                                .testTag("voice_profile_chip_${profile.id}")
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0x40BD00FF) else Color(0x2015092A))
                                .border(
                                    1.dp,
                                    if (isSelected) NeonMagenta else Color(0x35BD00FF),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.setVoiceProfile(profile) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) NeonMagenta else TextMuted)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = profile.name.replace(" (Default)", ""),
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            if (permissionDeniedNotice) {
                Text(
                    text = "Microphone access is required for real voice conversation. Please grant permission in device settings.",
                    color = CyberPink,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Controls Row: Mute, Main Mic Button (with Barge-in), Stop Speaking Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 60.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Mute / Unmute Button
                    Box(
                        modifier = Modifier
                            .testTag("voice_mute_button")
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) Color(0x40F43F5E) else Color(0x30180B35))
                            .border(1.dp, if (isMuted) CyberPink else Color(0x40BD00FF), CircleShape)
                            .clickable(
                                indication = ripple(color = NeonMagenta),
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                onClick = { viewModel.toggleMute() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = if (isMuted) CyberPink else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    // Primary Glowing Microphone FAB (Supports Instant Barge-in Interruption)
                    Box(
                        modifier = Modifier
                            .testTag("voice_mic_fab")
                            .size(72.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = CircleShape,
                                ambientColor = if (isListening) ElectricCyan else NeonPurple,
                                spotColor = if (isListening) ElectricCyan else NeonMagenta
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    if (isListening) {
                                        listOf(ElectricCyan, Color(0xFF0284C7))
                                    } else if (isSpeaking) {
                                        listOf(NeonMagenta, Color(0xFFEC4899))
                                    } else {
                                        listOf(NeonMagenta, NeonPurple, Color(0xFF6B21A8))
                                    }
                                )
                            )
                            .clickable(
                                indication = ripple(color = Color.White),
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                onClick = { handleMicClick() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop Listening" else "Start Listening / Interrupt",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(18.dp))

                    // Stop Speaking Button (Instant interruption)
                    Box(
                        modifier = Modifier
                            .testTag("voice_stop_speaking_button")
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isSpeaking) Color(0x40F43F5E) else Color(0x20180B35))
                            .border(1.dp, if (isSpeaking) CyberPink else Color(0x25BD00FF), CircleShape)
                            .clickable(
                                enabled = isSpeaking,
                                indication = ripple(color = CyberPink),
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                onClick = { viewModel.stopSpeaking() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Speaking",
                            tint = if (isSpeaking) CyberPink else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Alternative Text Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = alternativeTextInput,
                        onValueChange = { alternativeTextInput = it },
                        placeholder = { Text("Ask a question, or say 'Open Camera', 'Set alarm'...", fontSize = 11.sp, color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonMagenta,
                            unfocusedBorderColor = Color(0x35BD00FF),
                            focusedContainerColor = Color(0x251C0C38),
                            unfocusedContainerColor = Color(0x251C0C38)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("voice_alt_text_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (alternativeTextInput.isNotBlank()) {
                                viewModel.sendMessage(alternativeTextInput, isVoice = true)
                                alternativeTextInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(NeonMagenta, NeonPurple)))
                            .testTag("voice_alt_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // User Consent Dialog for Wake-Word Listening
        if (showWakeWordConsentDialog) {
            AlertDialog(
                onDismissRequest = { showWakeWordConsentDialog = false },
                containerColor = Color(0xFF16082C),
                title = {
                    Text(
                        text = "Enable \"Hey MYRA\" Wake-Word?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Privacy & Explicit Consent:",
                            color = NeonMagenta,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "When enabled, MYRA checks for the phrase \"Hey MYRA\" locally on your device. Audio is never continuously recorded or uploaded to servers without your explicit command.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Supported Commands: Opening apps, setting alarms, timer, reminders, camera, and multilingual queries.",
                            color = ElectricCyan,
                            fontSize = 11.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.setWakeWordEnabled(true)
                            showWakeWordConsentDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Enable & Consent", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWakeWordConsentDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }

        // Overlay Permission Dialog for Floating AI Orb
        if (showOverlayPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showOverlayPermissionDialog = false },
                containerColor = Color(0xFF16082C),
                title = {
                    Text(
                        text = "Display Over Other Apps",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Android Overlay Permission:",
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To show the floating AI orb over your other apps and home screen, Android requires the 'Display over other apps' permission. Tapping Grant will open your Android system settings.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.requestOverlayPermission(context)
                            showOverlayPermissionDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Open Settings", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOverlayPermissionDialog = false }) {
                        Text("Later", color = TextSecondary)
                    }
                }
            )
        }
    }
}
