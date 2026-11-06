package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalZahraTheme

/**
 * Puffy 3D marshmallow tactile button with extrusion bevel and bouncy press animation.
 */
@Composable
fun PuffyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "",
    icon: ImageVector? = null,
    emoji: String? = null,
    backgroundColor: Color? = null,
    bevelColor: Color? = null,
    textColor: Color = Color.White,
    fontSize: Int = 15,
    height: Dp = 54.dp,
    shape: Shape = RoundedCornerShape(26.dp),
    testTag: String = "puffy_button",
    enabled: Boolean = true
) {
    val theme = LocalZahraTheme.current.currentTheme
    val baseBg = backgroundColor ?: theme.primaryColor
    val baseBevel = bevelColor ?: theme.buttonBevel

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 3.5.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f),
        label = "press_offset"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 420f),
        label = "scale_press"
    )

    Box(
        modifier = modifier
            .heightIn(min = height + 5.dp)
            .fillMaxWidth()
            .testTag(testTag)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // 3D Bottom Extrusion Bevel Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 4.5.dp)
                .clip(shape)
                .background(baseBevel)
        )

        // Raised Puffy Front Face
        Box(
            modifier = Modifier
                .offset(y = offsetY)
                .fillMaxWidth()
                .heightIn(min = height)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            baseBg.copy(alpha = 0.95f),
                            baseBg
                        )
                    )
                )
                // Glossy top edge highlight
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.75f),
                            Color.White.copy(alpha = 0.2f)
                        )
                    ),
                    shape = shape
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (!emoji.isNullOrBlank()) {
                    Text(
                        text = emoji,
                        fontSize = (fontSize + 3).sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )
                }
                if (text.isNotBlank()) {
                    Text(
                        text = text,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        softWrap = true,
                        lineHeight = (fontSize + 4).sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Puffy Card with marshmallow curved borders and soft dual-layer shadow.
 */
@Composable
fun PuffyCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 6.dp,
    borderWidth: Dp = 1.5.dp,
    borderColor: Color? = null,
    content: @Composable () -> Unit
) {
    val theme = LocalZahraTheme.current.currentTheme
    val cardBg = backgroundColor ?: theme.cardBackground
    val borderCol = borderColor ?: theme.primaryColor.copy(alpha = 0.22f)
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = theme.primaryColor.copy(alpha = 0.18f),
                spotColor = theme.buttonBevel.copy(alpha = 0.25f)
            )
            .clip(shape)
            .background(cardBg)
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.95f),
                        borderCol
                    )
                ),
                shape = shape
            )
    ) {
        content()
    }
}

/**
 * Puffy Badge / Tag with cute emoji and pill shape.
 */
@Composable
fun PuffyBadge(
    text: String,
    emoji: String? = null,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val theme = LocalZahraTheme.current.currentTheme
    val bg = backgroundColor ?: theme.primaryColor.copy(alpha = 0.14f)
    val fg = textColor ?: theme.primaryColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (!emoji.isNullOrBlank()) {
                Text(text = emoji, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp))
            }
            Text(
                text = text,
                color = fg,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Circular Puffy Action Icon Button (e.g. for Audio pronunciation play, mic, back)
 */
@Composable
fun PuffyIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emoji: String? = null,
    backgroundColor: Color? = null,
    tint: Color = Color.White,
    size: Dp = 46.dp,
    testTag: String = "puffy_icon_button"
) {
    val theme = LocalZahraTheme.current.currentTheme
    val bg = backgroundColor ?: theme.primaryColor
    val bevel = theme.buttonBevel

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 2.5.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 420f),
        label = "icon_offset"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Extrusion Shadow / bevel base
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 3.dp)
                .clip(CircleShape)
                .background(bevel)
        )

        // Raised button face
        Box(
            modifier = Modifier
                .offset(y = offsetY)
                .matchParentSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            bg.copy(alpha = 0.95f),
                            bg
                        )
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(size * 0.52f)
                )
            } else if (!emoji.isNullOrBlank()) {
                Text(text = emoji, fontSize = (size.value * 0.44f).sp)
            }
        }
    }
}
