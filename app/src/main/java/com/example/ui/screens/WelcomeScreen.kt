package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantState
import com.example.ui.components.AnimatedAiOrb
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CosmicBackgroundBrush
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MyraViewModel

@Composable
fun WelcomeScreen(
    viewModel: MyraViewModel,
    modifier: Modifier = Modifier
) {
    var showAuthModal by remember { mutableStateOf(false) }
    var authModeIsLogin by remember { mutableStateOf(true) }
    var tempNameInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: MYRA AI Logo Badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x35BD00FF))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NEXT-GEN INTELLIGENCE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "MYRA AI",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
            }

            // Center Stage: Animated Glowing Orb
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                AnimatedAiOrb(
                    state = AssistantState.IDLE,
                    size = 230.dp
                )
            }

            // Bottom Presentation & Actions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Your Intelligent AI Assistant",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Experience fluid conversational voice interaction, contextual reasoning, and intelligent automation powered by Gemini.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Get Started Button
                GlowingButton(
                    text = "Get Started",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { viewModel.completeWelcome() },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "welcome_get_started_button"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sign in / Create Account options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            authModeIsLogin = true
                            showAuthModal = true
                        },
                        modifier = Modifier.testTag("welcome_login_button")
                    ) {
                        Text(
                            text = "Sign In",
                            color = NeonMagenta,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "•",
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    TextButton(
                        onClick = {
                            authModeIsLogin = false
                            showAuthModal = true
                        },
                        modifier = Modifier.testTag("welcome_register_button")
                    ) {
                        Text(
                            text = "Create Account",
                            color = ElectricCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Account Auth / Profile Customization Dialog
        if (showAuthModal) {
            AlertDialog(
                onDismissRequest = { showAuthModal = false },
                containerColor = Color(0xFF140828),
                title = {
                    Text(
                        text = if (authModeIsLogin) "Welcome Back" else "Create Account",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = if (authModeIsLogin)
                                "Sign in to personalize your MYRA assistant profile."
                            else
                                "Enter your name to customize MYRA's greetings and personality.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = tempNameInput,
                            onValueChange = { tempNameInput = it },
                            placeholder = { Text("Your name (e.g. Alex)") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = NeonMagenta)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonMagenta,
                                unfocusedBorderColor = Color(0x55BD00FF),
                                focusedContainerColor = Color(0x33000000),
                                unfocusedContainerColor = Color(0x33000000)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input_field")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempNameInput.isNotBlank()) {
                                viewModel.setUserName(tempNameInput.trim())
                            }
                            showAuthModal = false
                            viewModel.completeWelcome()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("auth_submit_button")
                    ) {
                        Text(if (authModeIsLogin) "Continue" else "Create & Start", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAuthModal = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}
