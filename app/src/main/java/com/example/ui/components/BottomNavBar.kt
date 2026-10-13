package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

enum class AppNavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    CHAT("Chat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline, "nav_chat"),
    VOICE("Voice", Icons.Filled.Mic, Icons.Outlined.MicNone, "nav_voice"),
    HISTORY("History", Icons.Filled.History, Icons.Outlined.History, "nav_history"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

@Composable
fun BottomNavBar(
    currentDestination: AppNavDestination,
    onNavigate: (AppNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = NeonPurple,
                    spotColor = NeonMagenta
                )
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xE60D061F))
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0x60BD00FF),
                                Color(0x20E040FB),
                                Color(0x15FFFFFF)
                            )
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppNavDestination.entries.forEach { destination ->
                    val isSelected = destination == currentDestination
                    val iconTint = animateColorAsState(
                        targetValue = if (isSelected) Color.White else TextMuted,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "iconTint"
                    )

                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .testTag(destination.tag)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(
                                onClick = { onNavigate(destination) }
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(
                                                        Color(0x80BD00FF),
                                                        Color(0x30E040FB),
                                                        Color(0x00000000)
                                                    )
                                                )
                                            )
                                    } else Modifier
                                )
                        ) {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title,
                                tint = iconTint.value,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Text(
                            text = destination.title,
                            color = if (isSelected) TextPrimary else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
