package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalWell
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MintGreenContainer
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Full Bullnose horizontal dial-style slider:
 * - Left circular icon toggles mode (e.g., Mute or Night 30% / Day 60%).
 * - Center track uses relative horizontal drag gestures (+1 / -1 step per drag increment)
 *   so values never jump abruptly to 100 on tap.
 * - Right fixed-width numeric readout aligns identically across stacked sliders.
 */
@Composable
fun FullBullnoseDialSlider(
    icon: ImageVector,
    iconContentDescription: String,
    iconActive: Boolean,
    value: Int,
    minValue: Int,
    maxValue: Int,
    valueText: String,
    hapticEnabled: Boolean,
    height: Dp = 54.dp,
    accentColor: Color = MintGreen,
    dimmed: Boolean = false,
    iconTestTag: String,
    sliderTestTag: String,
    valueTestTag: String,
    onIconClick: () -> Unit,
    onStep: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val currentOnStep by rememberUpdatedState(onStep)
    val currentHapticEnabled by rememberUpdatedState(hapticEnabled)
    val density = LocalDensity.current

    val range = (maxValue - minValue).coerceAtLeast(1)
    val targetFraction = ((value - minValue).toFloat() / range.toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.85f),
        label = "dial_slider_fraction"
    )

    val iconBg by animateColorAsState(
        targetValue = if (iconActive) MintGreenContainer else CharcoalSurface,
        label = "dial_icon_bg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (iconActive) accentColor else TextPrimary,
        label = "dial_icon_tint"
    )
    val trackFillColor by animateColorAsState(
        targetValue = if (dimmed) TextSecondary.copy(alpha = 0.45f) else accentColor,
        label = "dial_track_fill"
    )

    Surface(
        color = CharcoalWell,
        shape = CircleShape,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .border(1.dp, Color(0xFF2B2D31), CircleShape)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp)
        ) {
            // Left circular toggle icon (42dp visible circle inside 48dp touch area)
            val iconInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg)
                    .clickable(
                        interactionSource = iconInteraction,
                        indication = ripple(bounded = true, color = accentColor),
                        onClickLabel = iconContentDescription,
                        onClick = {
                            if (currentHapticEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onIconClick()
                        }
                    )
                    .testTag(iconTestTag),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    tint = iconTint,
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Center smooth relative-drag dial track
            val stepThresholdPx = with(density) { 5.5.dp.toPx() }
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(stepThresholdPx) {
                        var dragAccumulator = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { dragAccumulator = 0f },
                            onDragEnd = { dragAccumulator = 0f },
                            onDragCancel = { dragAccumulator = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                dragAccumulator += dragAmount
                                while (dragAccumulator >= stepThresholdPx) {
                                    dragAccumulator -= stepThresholdPx
                                    if (currentHapticEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    currentOnStep(1)
                                }
                                while (dragAccumulator <= -stepThresholdPx) {
                                    dragAccumulator += stepThresholdPx
                                    if (currentHapticEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    currentOnStep(-1)
                                }
                            }
                        )
                    }
                    .testTag(sliderTestTag),
                contentAlignment = Alignment.CenterStart
            ) {
                val trackWidth = maxWidth
                // Recessed Full Bullnose track background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(CircleShape)
                        .background(CharcoalSurface)
                ) {
                    // Active Full Bullnose fill bar
                    val fillWidth = (trackWidth * animatedFraction).coerceAtLeast(if (animatedFraction > 0f) 18.dp else 0.dp)
                    if (fillWidth > 0.dp) {
                        Box(
                            modifier = Modifier
                                .width(fillWidth)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(trackFillColor.copy(alpha = 0.88f)),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0B2513).copy(alpha = 0.7f))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right aligned numeric display (fixed width so stacked sliders align cleanly)
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = MonoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = if (dimmed) TextSecondary else TextPrimary,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .width(46.dp)
                    .padding(end = 8.dp)
                    .testTag(valueTestTag)
            )
        }
    }
}
