package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.TvKeyCodes
import com.example.ui.theme.DpadCenter
import com.example.ui.theme.DpadCenterPressed
import com.example.ui.theme.DpadInnerRing
import com.example.ui.theme.DpadRing
import com.example.ui.theme.MintGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CircularDpadController(
    diameter: Dp,
    onKeyPress: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val centerSize = diameter * 0.37f
    val arrowOffset = diameter * 0.335f
    val sectorHitSize = (diameter * 0.28f).coerceAtLeast(52.dp)

    Box(
        modifier = modifier
            .size(diameter)
            .shadow(elevation = 10.dp, shape = CircleShape, clip = false)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(DpadInnerRing, DpadRing)
                )
            )
            .border(width = 1.dp, color = Color(0xFF2E3034), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Up
        DpadDirectionButton(
            icon = Icons.Rounded.KeyboardArrowUp,
            contentDescription = stringResource(R.string.cd_dpad_up),
            hitSize = sectorHitSize,
            testTag = "dpad_up_button",
            modifier = Modifier.offset(y = -arrowOffset),
            onClick = { onKeyPress(TvKeyCodes.DPAD_UP) }
        )

        // Down
        DpadDirectionButton(
            icon = Icons.Rounded.KeyboardArrowDown,
            contentDescription = stringResource(R.string.cd_dpad_down),
            hitSize = sectorHitSize,
            testTag = "dpad_down_button",
            modifier = Modifier.offset(y = arrowOffset),
            onClick = { onKeyPress(TvKeyCodes.DPAD_DOWN) }
        )

        // Left
        DpadDirectionButton(
            icon = Icons.Rounded.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.cd_dpad_left),
            hitSize = sectorHitSize,
            testTag = "dpad_left_button",
            modifier = Modifier.offset(x = -arrowOffset),
            onClick = { onKeyPress(TvKeyCodes.DPAD_LEFT) }
        )

        // Right
        DpadDirectionButton(
            icon = Icons.Rounded.KeyboardArrowRight,
            contentDescription = stringResource(R.string.cd_dpad_right),
            hitSize = sectorHitSize,
            testTag = "dpad_right_button",
            modifier = Modifier.offset(x = arrowOffset),
            onClick = { onKeyPress(TvKeyCodes.DPAD_RIGHT) }
        )

        // Center OK button
        val centerInteraction = remember { MutableInteractionSource() }
        val centerPressed by centerInteraction.collectIsPressedAsState()
        val centerScale by animateFloatAsState(
            targetValue = if (centerPressed) 0.93f else 1f,
            animationSpec = spring(stiffness = 650f),
            label = "dpad_center_scale"
        )
        val centerColor by animateColorAsState(
            targetValue = if (centerPressed) DpadCenterPressed else DpadCenter,
            label = "dpad_center_color"
        )

        Box(
            modifier = Modifier
                .size(centerSize)
                .scale(centerScale)
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(centerColor)
                .border(1.dp, Color(0xFF66686D), CircleShape)
                .clickable(
                    interactionSource = centerInteraction,
                    indication = ripple(bounded = true, color = MintGreen),
                    onClickLabel = stringResource(R.string.cd_dpad_ok),
                    onClick = { onKeyPress(TvKeyCodes.DPAD_CENTER) }
                )
                .testTag("dpad_ok_button"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (centerPressed) MintGreen else Color(0xFF6F7277))
            )
        }
    }
}

@Composable
private fun DpadDirectionButton(
    icon: ImageVector,
    contentDescription: String,
    hitSize: Dp,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(stiffness = 700f),
        label = "dpad_dir_scale"
    )

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(hitSize)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = MintGreen),
                onClickLabel = contentDescription,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (pressed) TextPrimary else TextSecondary,
            modifier = Modifier.size(34.dp)
        )
    }
}
