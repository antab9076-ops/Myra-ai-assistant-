package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppNavDestination
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.theme.CosmicBackgroundBrush
import com.example.ui.theme.CyberPink
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MyraViewModel

@Composable
fun HistoryScreen(
    viewModel: MyraViewModel,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.conversations.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showClearConfirmation by remember { mutableStateOf(false) }

    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) {
            conversations
        } else {
            conversations.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.snippet.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBackgroundBrush)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "History",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${conversations.size} saved sessions",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (conversations.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmation = true },
                            modifier = Modifier.testTag("history_clear_all_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear all",
                                tint = CyberPink,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            viewModel.startNewConversation()
                            viewModel.navigateTo(AppNavDestination.CHAT)
                        },
                        modifier = Modifier.testTag("history_new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Session",
                            tint = NeonMagenta,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search conversations...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NeonMagenta,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = NeonMagenta,
                    unfocusedBorderColor = Color(0x40BD00FF),
                    focusedContainerColor = Color(0x251C0C38),
                    unfocusedContainerColor = Color(0x251C0C38)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("history_search_field")
            )

            // Conversations List
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredConversations.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isBlank()) "No conversation history" else "No matching conversations",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start a discussion to review it here anytime.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            GlowingButton(
                                text = "Start New Chat",
                                onClick = {
                                    viewModel.startNewConversation()
                                    viewModel.navigateTo(AppNavDestination.CHAT)
                                },
                                testTag = "history_empty_start_chat_button"
                            )
                        }
                    }
                } else {
                    items(filteredConversations, key = { it.id }) { conversation ->
                        ConversationItemCard(
                            conversation = conversation,
                            onClick = { viewModel.selectConversation(conversation.id) },
                            onDelete = { viewModel.deleteConversation(conversation.id) }
                        )
                    }
                }
            }
        }

        // Clear Confirmation Dialog
        if (showClearConfirmation) {
            AlertDialog(
                onDismissRequest = { showClearConfirmation = false },
                containerColor = Color(0xFF140828),
                title = {
                    Text(
                        text = "Clear History",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete all saved conversations? This action cannot be undone.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllData()
                            showClearConfirmation = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPink),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Delete All", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmation = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}
