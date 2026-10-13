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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantState
import com.example.data.model.Conversation
import com.example.ui.components.AnimatedAiOrb
import com.example.ui.components.AppNavDestination
import com.example.ui.components.GlassCard
import com.example.ui.components.WaveVisualizer
import com.example.ui.theme.CosmicBackgroundBrush
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MyraViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuickActionItem(
    val title: String,
    val prompt: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun HomeScreen(
    viewModel: MyraViewModel,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val persona by viewModel.selectedPersona.collectAsState()

    val quickActions = listOf(
        QuickActionItem("Sweet Voice", "Say something sweet and inspiring in your softest voice", Icons.Default.Lightbulb, "action_sweet_voice"),
        QuickActionItem("नमस्ते (Hindi)", "नमस्ते मायरा, आप कैसी हैं? मुझे कुछ अच्छी बात बताइए।", Icons.Default.MailOutline, "action_hindi"),
        QuickActionItem("বাংলা (Bengali)", "নমস্কার মাইরা, কেমন আছো? তোমার মিষ্টি গলায় কিছু বলো।", Icons.Default.School, "action_bengali"),
        QuickActionItem("অসমীয়া (Assamese)", "নমস্কাৰ মাইৰা, আজি দিনটো কেনেকৈ কটালে?", Icons.Default.Psychology, "action_assamese"),
        QuickActionItem("Quantum Physics", "Explain the concept of quantum entanglement simply with an analogy", Icons.Default.School, "action_quantum"),
        QuickActionItem("Code Review", "How do I optimize Jetpack Compose recompositions and remember keys?", Icons.Default.Code, "action_code")
    )

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
            // Header: Greeting + Status
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusOnline)
                            )
                            Text(
                                text = "ONLINE • ${persona.displayName.uppercase()}",
                                color = ElectricCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Hello, I'm MYRA",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "What would you like to achieve today, $userName?",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }

                    // Mini Orb preview in header
                    AnimatedAiOrb(
                        state = AssistantState.IDLE,
                        size = 54.dp,
                        modifier = Modifier.clickable { viewModel.navigateTo(AppNavDestination.VOICE) }
                    )
                }
            }

            // Featured Core Cards: Chat with AI & Speak with AI
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Chat with AI Card
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_chat_card"),
                        backgroundColor = SurfaceCardHigh,
                        borderBrush = Brush.linearGradient(
                            listOf(Color(0x80BD00FF), Color(0x30E040FB), Color(0x15FFFFFF))
                        ),
                        onClick = { viewModel.navigateTo(AppNavDestination.CHAT) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Brush.linearGradient(listOf(NeonPurple, Color(0xFF6B21A8)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubble,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "Chat with AI",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Ask questions, write copy, generate code",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open Chat",
                                tint = NeonMagenta,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Speak with AI Card
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_speak_card"),
                        backgroundColor = SurfaceCard,
                        borderBrush = Brush.linearGradient(
                            listOf(Color(0x8000F0FF), Color(0x40BD00FF), Color(0x15FFFFFF))
                        ),
                        onClick = { viewModel.navigateTo(AppNavDestination.VOICE) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.linearGradient(listOf(ElectricCyan, NeonPurple))
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Speak with AI",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        WaveVisualizer(
                                            isAnimating = true,
                                            barCount = 4,
                                            maxHeight = 16.dp,
                                            barWidth = 2.5.dp,
                                            barSpacing = 2.dp
                                        )
                                    }
                                    Text(
                                        text = "Fluid live voice conversation with audio synthesis",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open Voice",
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Quick Actions Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonMagenta,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Quick Actions",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(quickActions) { action ->
                            GlassCard(
                                modifier = Modifier.testTag(action.tag),
                                backgroundColor = Color(0x281C0C38),
                                shape = RoundedCornerShape(18.dp),
                                onClick = { viewModel.executeQuickAction(action.prompt) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = action.icon,
                                        contentDescription = null,
                                        tint = NeonMagenta,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = action.title,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Conversations Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Conversations",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "View All (${conversations.size})",
                        color = NeonMagenta,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .testTag("home_view_all_history")
                            .clickable { viewModel.navigateTo(AppNavDestination.HISTORY) }
                    )
                }
            }

            if (conversations.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0x181C0C38)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No conversation history yet",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Chat with AI' or 'Speak with AI' to start a session.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(conversations.take(4)) { conversation ->
                    ConversationItemCard(
                        conversation = conversation,
                        onClick = { viewModel.selectConversation(conversation.id) },
                        onDelete = { viewModel.deleteConversation(conversation.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItemCard(
    conversation: Conversation,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(conversation.timestamp))

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("conversation_card_${conversation.id}"),
        backgroundColor = Color(0x281C0C38),
        shape = RoundedCornerShape(20.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = conversation.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = conversation.snippet,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = formattedDate,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "•",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${conversation.messages.size} messages",
                        color = ElectricCyan,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("delete_conv_${conversation.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete conversation",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
