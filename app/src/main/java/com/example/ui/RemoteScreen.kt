package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WifiFind
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.CustomButtonEntity
import com.example.data.local.FavoriteAppEntity
import com.example.data.model.TvConnectionState
import com.example.data.model.TvKeyCodes
import com.example.ui.components.CircularDpadController
import com.example.ui.components.FullBullnoseDialSlider
import com.example.ui.components.TvAppBadgeIcon
import com.example.ui.components.customActionIcon
import com.example.ui.theme.CharcoalBg
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalSurfaceElevated
import com.example.ui.theme.CharcoalWell
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MintGreenContainer
import com.example.ui.theme.OnMintGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WarningAmber

@Composable
fun RemoteScreen(
    connectionState: TvConnectionState,
    favoriteApps: List<FavoriteAppEntity>,
    customButtons: List<CustomButtonEntity>,
    volumeLevel: Int,
    maxVolume: Int,
    isMuted: Boolean,
    brightnessPercent: Int,
    hapticEnabled: Boolean,
    isVoiceListening: Boolean,
    snackbarHostState: SnackbarHostState,
    onOpenTvPicker: () -> Unit,
    onDisconnectTv: () -> Unit,
    onKeyPress: (Int) -> Unit,
    onLaunchFavorite: (FavoriteAppEntity) -> Unit,
    onOpenKeyboard: () -> Unit,
    onMicClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleMute: () -> Unit,
    onStepVolume: (Int) -> Unit,
    onToggleDayNightBrightness: () -> Unit,
    onStepBrightness: (Int) -> Unit,
    onExecuteCustomButton: (CustomButtonEntity) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    fun triggerHaptic() {
        if (hapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Scaffold(
        containerColor = CharcoalBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            val compactHeight = maxHeight < 710.dp
            val dpadDiameter = (maxHeight * 0.28f).coerceIn(186.dp, 242.dp)
            val bigCircleSize = if (compactHeight) 62.dp else 70.dp
            val rowGap = if (compactHeight) 10.dp else 13.dp
            val sliderHeight = if (compactHeight) 48.dp else 54.dp

            val scrollModifier = if (maxHeight < 650.dp) {
                Modifier.verticalScroll(rememberScrollState())
            } else {
                Modifier
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxSize()
                    .then(scrollModifier)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Top Header: Search/Disconnect Circle | Mint Green Full Bullnose TV Pill | Power Circle
                TopRemoteHeader(
                    connectionState = connectionState,
                    onLeftAction = {
                        triggerHaptic()
                        if (connectionState is TvConnectionState.Connected) {
                            onDisconnectTv()
                        } else {
                            onOpenTvPicker()
                        }
                    },
                    onTvSelectorClick = {
                        triggerHaptic()
                        onOpenTvPicker()
                    },
                    onPowerClick = {
                        triggerHaptic()
                        onKeyPress(TvKeyCodes.POWER)
                    }
                )

                // 2. Top Favorites Chip Bar (Icon + Text Full Bullnose Chips)
                FavoritesChipBar(
                    favorites = favoriteApps,
                    onFavoriteClick = { app ->
                        triggerHaptic()
                        onLaunchFavorite(app)
                    },
                    onAddClick = {
                        triggerHaptic()
                        onOpenSettings()
                    }
                )

                // 3. Middle Circular D-Pad
                CircularDpadController(
                    diameter = dpadDiameter,
                    onKeyPress = { code ->
                        triggerHaptic()
                        onKeyPress(code)
                    }
                )

                // 4. Bottom Controls Block
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(rowGap),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row 1: Big Circle 3 (키보드 | 홈 | 뒤로가기)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircleIconRemoteButton(
                            icon = Icons.Rounded.Keyboard,
                            contentDescription = stringResource(R.string.btn_keyboard),
                            size = bigCircleSize,
                            iconSize = 28.dp,
                            testTag = "btn_keyboard",
                            onClick = {
                                triggerHaptic()
                                onOpenKeyboard()
                            }
                        )

                        CircleIconRemoteButton(
                            icon = Icons.Rounded.Home,
                            contentDescription = stringResource(R.string.btn_home),
                            size = bigCircleSize,
                            iconSize = 28.dp,
                            testTag = "btn_home",
                            onClick = {
                                triggerHaptic()
                                onKeyPress(TvKeyCodes.HOME)
                            }
                        )

                        CircleIconRemoteButton(
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back),
                            size = bigCircleSize,
                            iconSize = 28.dp,
                            testTag = "btn_back",
                            onClick = {
                                triggerHaptic()
                                onKeyPress(TvKeyCodes.BACK)
                            }
                        )
                    }

                    // Row 2: Big Circle 3 (마이크 | Apps | 설정)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val micBg by animateColorAsState(
                            targetValue = if (isVoiceListening) MintGreen else CharcoalWell,
                            label = "mic_bg"
                        )
                        val micTint by animateColorAsState(
                            targetValue = if (isVoiceListening) OnMintGreen else TextPrimary,
                            label = "mic_tint"
                        )
                        CircleIconRemoteButton(
                            icon = Icons.Rounded.Mic,
                            contentDescription = stringResource(R.string.btn_mic),
                            size = bigCircleSize,
                            iconSize = 28.dp,
                            containerColor = micBg,
                            iconTint = micTint,
                            testTag = "btn_mic",
                            onClick = {
                                triggerHaptic()
                                onMicClick()
                            }
                        )

                        CircleIconRemoteButton(
                            icon = Icons.Rounded.Apps,
                            contentDescription = stringResource(R.string.btn_apps),
                            size = bigCircleSize,
                            iconSize = 28.dp,
                            testTag = "btn_apps",
                            onClick = {
                                triggerHaptic()
                                onKeyPress(TvKeyCodes.ALL_APPS)
                            }
                        )

                        CircleIconRemoteButton(
                            icon = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.btn_settings),
                            size = bigCircleSize,
                            iconSize = 28.dp,
                            testTag = "btn_settings",
                            onClick = {
                                triggerHaptic()
                                onOpenSettings()
                            }
                        )
                    }

                    // Row 3: Full Bullnose Volume Dial Slider (Mute Toggle Icon | Smooth +1/-1 Drag Slider | Volume Number)
                    FullBullnoseDialSlider(
                        icon = if (isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                        iconContentDescription = stringResource(R.string.cd_mute),
                        iconActive = isMuted,
                        value = volumeLevel,
                        minValue = 0,
                        maxValue = maxVolume.coerceAtLeast(1),
                        valueText = if (isMuted) "음소거" else volumeLevel.toString(),
                        hapticEnabled = hapticEnabled,
                        height = sliderHeight,
                        accentColor = MintGreen,
                        dimmed = isMuted,
                        iconTestTag = "btn_mute_toggle",
                        sliderTestTag = "volume_dial_slider",
                        valueTestTag = "volume_value_text",
                        onIconClick = onToggleMute,
                        onStep = onStepVolume
                    )

                    // Row 4: Full Bullnose Screen Brightness Slider (Sun Icon Toggle 30%/60% | Smooth +1/-1 Drag Slider | Brightness %)
                    val isNightMode = brightnessPercent <= 35
                    FullBullnoseDialSlider(
                        icon = Icons.Rounded.WbSunny,
                        iconContentDescription = "화면 밝기 전환 (야간 30% / 낮 60%)",
                        iconActive = !isNightMode,
                        value = brightnessPercent,
                        minValue = 5,
                        maxValue = 100,
                        valueText = "$brightnessPercent%",
                        hapticEnabled = hapticEnabled,
                        height = sliderHeight,
                        accentColor = WarningAmber,
                        dimmed = false,
                        iconTestTag = "btn_brightness_toggle",
                        sliderTestTag = "brightness_dial_slider",
                        valueTestTag = "brightness_value_text",
                        onIconClick = onToggleDayNightBrightness,
                        onStep = onStepBrightness
                    )

                    // Row 5: 4 Custom Function Buttons (Full Bullnose Pill Buttons)
                    CustomButtonsRow(
                        buttons = customButtons,
                        compact = compactHeight,
                        onButtonClick = { btn ->
                            triggerHaptic()
                            onExecuteCustomButton(btn)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopRemoteHeader(
    connectionState: TvConnectionState,
    onLeftAction: () -> Unit,
    onTvSelectorClick: () -> Unit,
    onPowerClick: () -> Unit
) {
    val isConnected = connectionState is TvConnectionState.Connected
    val isConnecting = connectionState is TvConnectionState.Connecting

    val pillColor by animateColorAsState(
        targetValue = when (connectionState) {
            is TvConnectionState.Connected -> MintGreen
            is TvConnectionState.Connecting -> MintGreenContainer
            is TvConnectionState.Error -> CharcoalSurface
            else -> CharcoalSurfaceElevated
        },
        label = "tv_pill_color"
    )
    val pillContentColor = when (connectionState) {
        is TvConnectionState.Connected -> OnMintGreen
        is TvConnectionState.Connecting -> MintGreen
        is TvConnectionState.Error -> ErrorCoral
        else -> TextPrimary
    }
    val pillLabel = when (connectionState) {
        is TvConnectionState.Connected -> connectionState.tvName
        is TvConnectionState.Connecting -> "${connectionState.tvName} 연결 중..."
        is TvConnectionState.PairingRequired -> "${connectionState.tvName} PIN 대기"
        is TvConnectionState.Error -> "연결 재시도"
        TvConnectionState.Disconnected -> stringResource(R.string.tv_select_needed)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CircleIconRemoteButton(
            icon = if (isConnected) Icons.Rounded.Close else Icons.Rounded.WifiFind,
            contentDescription = if (isConnected) {
                stringResource(R.string.cd_disconnect_tv)
            } else {
                stringResource(R.string.cd_search_tv)
            },
            size = 48.dp,
            iconSize = 22.dp,
            containerColor = CharcoalSurface,
            iconTint = TextPrimary,
            testTag = "btn_top_connection",
            onClick = onLeftAction
        )

        // Full Bullnose Mint Green TV Selector Pill
        Surface(
            color = pillColor,
            shape = CircleShape,
            modifier = Modifier
                .height(48.dp)
                .widthIn(min = 168.dp, max = 240.dp)
                .clip(CircleShape)
                .clickable(onClick = onTvSelectorClick)
                .testTag("btn_tv_selector_pill")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        color = MintGreen,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Tv,
                        contentDescription = null,
                        tint = pillContentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = pillLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = pillContentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(pillContentColor.copy(alpha = 0.25f))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "TV 목록 열기",
                    tint = pillContentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        CircleIconRemoteButton(
            icon = Icons.Rounded.PowerSettingsNew,
            contentDescription = stringResource(R.string.cd_power),
            size = 48.dp,
            iconSize = 22.dp,
            containerColor = CharcoalSurface,
            iconTint = TextPrimary,
            testTag = "btn_power",
            onClick = onPowerClick
        )
    }
}

@Composable
private fun FavoritesChipBar(
    favorites: List<FavoriteAppEntity>,
    onFavoriteClick: (FavoriteAppEntity) -> Unit,
    onAddClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(favorites, key = { it.packageName }) { app ->
            Surface(
                color = CharcoalWell,
                shape = CircleShape,
                modifier = Modifier
                    .height(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, DividerSubtle, CircleShape)
                    .clickable { onFavoriteClick(app) }
                    .testTag("favorite_chip_${app.iconKey}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp, end = 14.dp)
                ) {
                    TvAppBadgeIcon(
                        iconKey = app.iconKey,
                        accentHex = app.accentHex,
                        appName = app.appName,
                        size = 28.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            Surface(
                color = CharcoalSurface,
                shape = CircleShape,
                modifier = Modifier
                    .height(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, MintGreen.copy(alpha = 0.35f), CircleShape)
                    .clickable(onClick = onAddClick)
                    .testTag("favorite_chip_add")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = MintGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (favorites.isEmpty()) stringResource(R.string.add_favorite_chip) else "편집",
                        style = MaterialTheme.typography.labelLarge,
                        color = MintGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomButtonsRow(
    buttons: List<CustomButtonEntity>,
    compact: Boolean,
    onButtonClick: (CustomButtonEntity) -> Unit
) {
    val slots = remember(buttons) {
        (0..3).map { idx ->
            buttons.firstOrNull { it.slotIndex == idx } ?: CustomButtonEntity(
                slotIndex = idx,
                label = "버튼 ${idx + 1}",
                iconKey = "apps",
                actionType = "KEYCODE",
                keyCode = TvKeyCodes.INFO
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        slots.forEach { btn ->
            val interaction = remember { MutableInteractionSource() }
            val pressed by interaction.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (pressed) 0.94f else 1f,
                animationSpec = spring(stiffness = 650f),
                label = "custom_btn_scale"
            )

            Surface(
                color = CharcoalSurface,
                shape = CircleShape,
                modifier = Modifier
                    .weight(1f)
                    .height(if (compact) 48.dp else 50.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .border(1.dp, DividerSubtle, CircleShape)
                    .clickable(
                        interactionSource = interaction,
                        indication = ripple(bounded = true, color = MintGreen),
                        onClickLabel = btn.label,
                        onClick = { onButtonClick(btn) }
                    )
                    .testTag("custom_button_${btn.slotIndex}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = customActionIcon(btn.iconKey),
                        contentDescription = btn.label,
                        tint = MintGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = btn.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleIconRemoteButton(
    icon: ImageVector,
    contentDescription: String,
    size: Dp,
    iconSize: Dp,
    testTag: String,
    containerColor: Color = CharcoalWell,
    iconTint: Color = TextPrimary,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.91f else 1f,
        animationSpec = spring(stiffness = 700f),
        label = "circle_btn_scale"
    )

    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(containerColor)
            .border(1.dp, Color(0xFF2B2D31), CircleShape)
            .clickable(
                interactionSource = interaction,
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
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}
