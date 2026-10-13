package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PrimaryGlowGradient
import com.example.ui.theme.TextPrimary

@Composable
fun GlowingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    testTag: String = "glowing_button",
    shape: Shape = CircleShape,
    brush: Brush = PrimaryGlowGradient
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .testTag(testTag)
            .defaultMinSize(minHeight = 52.dp)
            .shadow(
                elevation = if (enabled) 12.dp else 0.dp,
                shape = shape,
                ambientColor = NeonPurple,
                spotColor = NeonMagenta
            )
            .clip(shape)
            .background(
                brush = if (enabled) brush else Brush.linearGradient(
                    listOf(Color(0xFF2D1E40), Color(0xFF1D142C))
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                enabled = enabled && !isLoading,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                }
                Text(
                    text = text,
                    color = if (enabled) Color.White else Color(0xFF887D9E),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color = TextPrimary,
    backgroundColor: Color = Color(0x3528124D),
    testTag: String = "glass_icon_button"
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .testTag(testTag)
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(listOf(Color(0x60BD00FF), Color(0x20FFFFFF)))
                ),
                CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = NeonMagenta),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
