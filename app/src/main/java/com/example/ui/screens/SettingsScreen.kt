package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.model.FemaleVoiceProfiles
import com.example.data.model.Persona
import com.example.ui.components.GlassCard
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
fun SettingsScreen(
    viewModel: MyraViewModel,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()
    val persona by viewModel.selectedPersona.collectAsState()
    val autoSpeak by viewModel.autoSpeakInChat.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val currentVoiceProfile by viewModel.voiceManager.currentVoiceProfile.collectAsState()
    val isContinuousMode by viewModel.voiceManager.isContinuousMode.collectAsState()
    val volume by viewModel.voiceManager.volume.collectAsState()
    val currentPitch by viewModel.voiceManager.pitch.collectAsState()
    val currentSpeed by viewModel.voiceManager.speechRate.collectAsState()
    val systemVoices by viewModel.voiceManager.availableFemaleVoices.collectAsState()
    val selectedSystemVoiceName by viewModel.voiceManager.selectedSystemVoiceName.collectAsState()

    var nameInput by remember(userName) { mutableStateOf(userName) }
    var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
    var keySavedFeedback by remember { mutableStateOf(false) }

    val hasSystemKey = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
    val isKeyConfigured = hasSystemKey || customApiKey.isNotBlank()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackgroundBrush)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Settings",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Voice engine, multilingual settings, and Gemini intelligence",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Female Voice Selection & Persona Profiles Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_voice_profiles_card"),
                    backgroundColor = SurfaceCard,
                    borderBrush = Brush.linearGradient(
                        listOf(Color(0x80E040FB), Color(0x30BD00FF), Color(0x15FFFFFF))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = NeonMagenta,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Natural Female Voice Profiles",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Select a soft, human-like voice personality. Supports English, Hindi, Bengali, and Assamese naturally.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            FemaleVoiceProfiles.forEach { profile ->
                                val isSelected = profile.id == currentVoiceProfile.id
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("settings_voice_option_${profile.id}")
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) Color(0x406A0DAD) else Color(0x2015092A))
                                        .border(
                                            1.dp,
                                            if (isSelected) NeonMagenta else Color(0x30BD00FF),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { viewModel.setVoiceProfile(profile) }
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = profile.name,
                                                    color = if (isSelected) Color.White else TextPrimary,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) NeonMagenta else Color(0x25BD00FF))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = profile.toneTag,
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = profile.description,
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    1.5.dp,
                                                    if (isSelected) NeonMagenta else TextMuted,
                                                    CircleShape
                                                )
                                                .then(
                                                    if (isSelected) Modifier.background(NeonMagenta) else Modifier
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Installed Android System Female Voices Submenu (if available)
                        if (systemVoices.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Installed System Female Voices (${systemVoices.size} found):",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                systemVoices.take(4).forEach { sysVoice ->
                                    val isChosen = selectedSystemVoiceName == sysVoice.name
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isChosen) Color(0x30BD00FF) else Color(0x15FFFFFF))
                                            .clickable { viewModel.selectSystemVoice(sysVoice.name) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = sysVoice.name,
                                            color = if (isChosen) ElectricCyan else TextSecondary,
                                            fontSize = 11.sp
                                        )
                                        if (isChosen) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = ElectricCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Speech Audio Engine Tuning (Speed, Pitch, Volume, Continuous)
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceCard
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Speech & Conversation Tuning",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Continuous Two-Way Conversation Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Continuous Two-Way Voice Mode",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Automatically listens after speaking for fluid hands-free dialogue",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = isContinuousMode,
                                onCheckedChange = { viewModel.toggleContinuousMode() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NeonPurple,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color(0x351C0C38)
                                ),
                                modifier = Modifier.testTag("settings_continuous_mode_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Auto-speak in chat toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Automatically Speak Responses Aloud",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Reads text replies aloud in her sweet female voice",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = autoSpeak,
                                onCheckedChange = { viewModel.setAutoSpeakInChat(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NeonPurple,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color(0x351C0C38)
                                ),
                                modifier = Modifier.testTag("settings_auto_speak_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Volume Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Speech Volume", color = TextPrimary, fontSize = 13.sp)
                            Text("${(volume * 100).toInt()}%", color = ElectricCyan, fontSize = 13.sp)
                        }
                        Slider(
                            value = volume,
                            onValueChange = { viewModel.setVolume(it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricCyan,
                                activeTrackColor = ElectricCyan,
                                inactiveTrackColor = Color(0x3500F0FF)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Speech Pitch Slider (Sweet soft female tuning)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Voice Pitch (Softness & Sweetness)", color = TextPrimary, fontSize = 13.sp)
                            Text("${"%.2f".format(currentPitch)}x", color = NeonMagenta, fontSize = 13.sp)
                        }
                        Slider(
                            value = currentPitch,
                            onValueChange = { viewModel.setSpeechPitch(it) },
                            valueRange = 0.8f..1.6f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonMagenta,
                                activeTrackColor = NeonPurple,
                                inactiveTrackColor = Color(0x35BD00FF)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Speech Speed Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Speaking Cadence / Speed", color = TextPrimary, fontSize = 13.sp)
                            Text("${"%.2f".format(currentSpeed)}x", color = ElectricCyan, fontSize = 13.sp)
                        }
                        Slider(
                            value = currentSpeed,
                            onValueChange = { viewModel.setSpeechRate(it) },
                            valueRange = 0.6f..1.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricCyan,
                                activeTrackColor = ElectricCyan,
                                inactiveTrackColor = Color(0x3500F0FF)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Test Voice Button
                        Button(
                            onClick = { viewModel.testVoice() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x40BD00FF)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_test_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = NeonMagenta,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Current Female Voice", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Android Background Service & Wake-Word Settings Card
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                val isBgRunning by viewModel.isBackgroundServiceRunning.collectAsState()
                val isWakeWord by viewModel.isWakeWordEnabled.collectAsState()

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_background_voice_card"),
                    backgroundColor = SurfaceCard,
                    borderBrush = Brush.linearGradient(
                        listOf(Color(0x8000F0FF), Color(0x40BD00FF), Color(0x15FFFFFF))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Background Voice Service",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isBgRunning) Color(0x2510B981) else Color(0x25F59E0B))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isBgRunning) StatusOnline else Color(0xFFF59E0B))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBgRunning) "ACTIVE" else "STOPPED",
                                    color = if (isBgRunning) StatusOnline else Color(0xFFF59E0B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Maintains a persistent foreground notification with microphone access so you can speak with MYRA even when the app is in the background or screen is locked.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Start / Stop Background Service Button
                        Button(
                            onClick = {
                                if (isBgRunning) {
                                    viewModel.stopBackgroundService(context)
                                } else {
                                    viewModel.startBackgroundService(context)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBgRunning) CyberPink else NeonPurple
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_toggle_bg_service_btn")
                        ) {
                            Text(
                                text = if (isBgRunning) "Stop Background Service" else "Start Persistent Voice Service",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Wake-Word Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "\"Hey MYRA\" Wake-Word Detection",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "On-device processing only. Audio is never recorded or uploaded without your explicit command.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = isWakeWord,
                                onCheckedChange = { viewModel.setWakeWordEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NeonMagenta,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color(0x351C0C38)
                                ),
                                modifier = Modifier.testTag("settings_wake_word_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Floating Orb Toggle
                        val isOrbVisible by viewModel.isFloatingOrbVisible.collectAsState()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Floating AI Screen Orb (Overlay)",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Shows a movable glowing orb over other apps. Tap to return to MYRA instantly.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = isOrbVisible,
                                onCheckedChange = { enable ->
                                    if (enable) {
                                        if (viewModel.canDrawOverlays(context)) {
                                            viewModel.showFloatingOrb(context)
                                        } else {
                                            viewModel.requestOverlayPermission(context)
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
                                modifier = Modifier.testTag("settings_floating_orb_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Battery Optimization Exemption Shortcut Button
                        Button(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback to general settings
                                    val fallback = android.content.Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(fallback)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x2500F0FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_battery_opt_btn")
                        ) {
                            Text(
                                text = "Battery Optimization: Exclude MYRA AI",
                                color = ElectricCyan,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Firebase Cloud Connection Card
            item {
                val isFirebaseConnected by viewModel.isFirebaseConnected.collectAsState()
                val firebaseStatus by viewModel.firebaseStatus.collectAsState()
                val firebaseLastSync by viewModel.firebaseLastSync.collectAsState()

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_firebase_card"),
                    backgroundColor = SurfaceCard,
                    borderBrush = Brush.linearGradient(
                        listOf(Color(0xFFFF9100), Color(0xFFBD00FF), Color(0x15FFFFFF))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9100),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Firebase Cloud Backend",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Status badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isFirebaseConnected) Color(0x2510B981) else Color(0x25F59E0B))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isFirebaseConnected) StatusOnline else Color(0xFFF59E0B))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFirebaseConnected) "CONNECTED" else "CONNECTING",
                                    color = if (isFirebaseConnected) StatusOnline else Color(0xFFF59E0B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Connected Project: myra-ai-assistant-58d3f\nRealtime DB & Cloud Firestore ready for cross-device conversation sync.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status detail bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x181C0C38))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = NeonMagenta,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = firebaseStatus,
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.testFirebasePing() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x2500F0FF)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_firebase_ping_btn")
                            ) {
                                Text(
                                    text = "Test Ping",
                                    color = ElectricCyan,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = { viewModel.syncWithFirebase() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_firebase_sync_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Sync Cloud",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Gemini API Configuration Card
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_api_card"),
                    backgroundColor = SurfaceCard,
                    borderBrush = Brush.linearGradient(
                        listOf(Color(0x80BD00FF), Color(0x3000F0FF), Color(0x15FFFFFF))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gemini API Connection",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Status badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isKeyConfigured) Color(0x2510B981) else Color(0x25F59E0B))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isKeyConfigured) StatusOnline else Color(0xFFF59E0B))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isKeyConfigured) "ACTIVE" else "KEY NEEDED",
                                    color = if (isKeyConfigured) StatusOnline else Color(0xFFF59E0B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Model: gemini-3.5-flash with conversational voice prompts. API keys are handled securely via Secrets panel.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = {
                                apiKeyInput = it
                                keySavedFeedback = false
                            },
                            placeholder = {
                                Text(
                                    text = if (hasSystemKey) "Secured via AI Studio Secrets" else "Enter Gemini API Key (AIza...)",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonMagenta,
                                unfocusedBorderColor = Color(0x35BD00FF),
                                focusedContainerColor = Color(0x251C0C38),
                                unfocusedContainerColor = Color(0x251C0C38)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_api_key_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (keySavedFeedback) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = StatusOnline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Key saved", color = StatusOnline, fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.setCustomApiKey(apiKeyInput)
                                    keySavedFeedback = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("settings_save_api_key_button")
                            ) {
                                Text("Save Key", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // User Profile Section
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceCard
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "User Profile",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = {
                                nameInput = it
                                if (it.isNotBlank()) viewModel.setUserName(it)
                            },
                            label = { Text("Your Preferred Name", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonMagenta,
                                unfocusedBorderColor = Color(0x35BD00FF),
                                focusedContainerColor = Color(0x251C0C38),
                                unfocusedContainerColor = Color(0x251C0C38)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_name_input")
                        )
                    }
                }
            }

            // About MYRA AI
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color(0x181C0C38)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = NeonMagenta,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "MYRA AI — Realistic Female Voice",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Conversational AI assistant with continuous two-way dialogue, barge-in interruption, and multilingual speech.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
