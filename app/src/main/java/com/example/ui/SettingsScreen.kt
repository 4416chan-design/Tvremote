package com.example.ui

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AppPreferencesEntity
import com.example.data.local.CustomButtonEntity
import com.example.data.local.FavoriteAppEntity
import com.example.data.local.PairedTvEntity
import com.example.data.local.RecentTvAppEntity
import com.example.data.model.CustomActionType
import com.example.data.model.TvAppCatalog
import com.example.data.model.TvAppCatalogItem
import com.example.ui.components.TvAppBadgeIcon
import com.example.ui.components.customActionIcon
import com.example.ui.theme.CharcoalBg
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalWell
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MintGreenContainer
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.OnMintGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    favoriteApps: List<FavoriteAppEntity>,
    recentApps: List<RecentTvAppEntity>,
    customButtons: List<CustomButtonEntity>,
    pairedTvs: List<PairedTvEntity>,
    preferences: AppPreferencesEntity,
    onNavigateBack: () -> Unit,
    onToggleCatalogFavorite: (TvAppCatalogItem, Boolean) -> Unit,
    onAddFavoriteFromRecent: (RecentTvAppEntity) -> Unit,
    onAddCustomFavorite: (name: String, uri: String, pkg: String) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    onMoveFavorite: (Int, Int) -> Unit,
    onUpdateCustomButton: (CustomButtonEntity) -> Unit,
    onConnectSavedTv: (PairedTvEntity) -> Unit,
    onForgetTv: (String) -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onToggleAutoKeyboard: (Boolean) -> Unit
) {
    var editingSlot by remember { mutableStateOf<CustomButtonEntity?>(null) }
    var showAddCustomAppDialog by remember { mutableStateOf(false) }

    val favoritePackages = remember(favoriteApps) { favoriteApps.map { it.packageName }.toSet() }
    val timeFormatter = remember { SimpleDateFormat("MM/dd HH:mm", Locale.KOREA) }

    Scaffold(
        containerColor = CharcoalBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalBg,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Custom 4 Function Buttons Section
                item {
                    SectionHeader(
                        title = "커스텀 기능 버튼 4개",
                        subtitle = "각 슬롯을 눌러 TV 키코드 또는 앱 실행을 지정하세요"
                    )
                }

                item {
                    val slots = (0..3).map { idx ->
                        customButtons.firstOrNull { it.slotIndex == idx } ?: CustomButtonEntity(
                            slotIndex = idx,
                            label = "버튼 ${idx + 1}",
                            iconKey = "info",
                            actionType = CustomActionType.KEYCODE.name,
                            keyCode = 165
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        slots.chunked(2).forEach { pair ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                pair.forEach { slot ->
                                    Surface(
                                        color = CharcoalSurface,
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, DividerSubtle, CircleShape)
                                            .clickable { editingSlot = slot }
                                            .testTag("settings_custom_slot_${slot.slotIndex}")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 14.dp)
                                        ) {
                                            Icon(
                                                imageVector = customActionIcon(slot.iconKey),
                                                contentDescription = null,
                                                tint = MintGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "버튼 ${slot.slotIndex + 1}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary
                                                )
                                                Text(
                                                    text = slot.label,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = TextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Rounded.Edit,
                                                contentDescription = "편집",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Pinned Top Favorites Order Management
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            SectionHeader(
                                title = "상단 즐겨찾기 칩 바",
                                subtitle = "리모컨 상단에 고정 표시되는 앱 목록 (${favoriteApps.size}개)"
                            )
                        }
                        Surface(
                            color = MintGreenContainer,
                            shape = CircleShape,
                            modifier = Modifier
                                .height(40.dp)
                                .clip(CircleShape)
                                .clickable { showAddCustomAppDialog = true }
                                .testTag("settings_add_custom_app_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = MintGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("직접 추가", style = MaterialTheme.typography.labelMedium, color = MintGreen)
                            }
                        }
                    }
                }

                if (favoriteApps.isEmpty()) {
                    item {
                        Surface(
                            color = CharcoalSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "고정된 즐겨찾기가 없습니다. 아래 최근 실행 앱 또는 TV 앱 카탈로그에서 추가하세요.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(favoriteApps.size, key = { idx -> "fav_${favoriteApps[idx].packageName}" }) { idx ->
                        val fav = favoriteApps[idx]
                        Surface(
                            color = CharcoalSurface,
                            shape = CircleShape,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                TvAppBadgeIcon(
                                    iconKey = fav.iconKey,
                                    accentHex = fav.accentHex,
                                    appName = fav.appName,
                                    size = 32.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = fav.appName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onMoveFavorite(idx, -1) },
                                    enabled = idx > 0
                                ) {
                                    Icon(
                                        Icons.Rounded.ArrowUpward,
                                        contentDescription = "위로 이동",
                                        tint = if (idx > 0) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onMoveFavorite(idx, 1) },
                                    enabled = idx < favoriteApps.lastIndex
                                ) {
                                    Icon(
                                        Icons.Rounded.ArrowDownward,
                                        contentDescription = "아래로 이동",
                                        tint = if (idx < favoriteApps.lastIndex) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { onRemoveFavorite(fav.packageName) }) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "즐겨찾기 제거",
                                        tint = ErrorCoral,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Recent Executed / TV-Reported Apps
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    SectionHeader(
                        title = "Android TV 최근 실행 앱",
                        subtitle = "TV 연결 중 감지되었거나 리모컨으로 실행한 최근 앱 기록"
                    )
                }

                if (recentApps.isEmpty()) {
                    item {
                        Surface(
                            color = CharcoalSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(Icons.Rounded.History, contentDescription = null, tint = TextSecondary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "아직 기록된 최근 실행 앱이 없습니다. TV에서 앱을 열거나 아래 카탈로그에서 즐겨찾기를 추가해 보세요.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(recentApps, key = { "recent_${it.packageName}" }) { recent ->
                        val isFav = favoritePackages.contains(recent.packageName)
                        Surface(
                            color = CharcoalSurface,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                TvAppBadgeIcon(
                                    iconKey = recent.iconKey,
                                    accentHex = recent.accentHex,
                                    appName = recent.appName,
                                    size = 34.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = recent.appName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${if (recent.source == "TV") "TV 감지" else "실행"} · ${timeFormatter.format(Date(recent.lastUsedAt))}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                FavoriteTogglePill(
                                    isFavorite = isFav,
                                    onClick = {
                                        if (isFav) onRemoveFavorite(recent.packageName)
                                        else onAddFavoriteFromRecent(recent)
                                    }
                                )
                            }
                        }
                    }
                }

                // 4. Curated Android TV App Catalog
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    SectionHeader(
                        title = "주요 Android TV 앱 카탈로그",
                        subtitle = "아이콘 + 텍스트 칩으로 상단에 바로 추가할 수 있습니다"
                    )
                }

                items(TvAppCatalog.apps, key = { "cat_${it.packageName}" }) { app ->
                    val isFav = favoritePackages.contains(app.packageName)
                    Surface(
                        color = CharcoalSurface,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            TvAppBadgeIcon(
                                iconKey = app.iconKey,
                                accentHex = app.accentHex,
                                appName = app.appName,
                                size = 34.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Text(
                                    text = app.category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                            FavoriteTogglePill(
                                isFavorite = isFav,
                                onClick = { onToggleCatalogFavorite(app, isFav) }
                            )
                        }
                    }
                }

                // 5. Saved Paired TVs & Remote Options
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    SectionHeader(
                        title = "저장된 TV 및 리모컨 환경설정",
                        subtitle = "페어링된 기기와 입력 편의 옵션을 관리합니다"
                    )
                }

                items(pairedTvs, key = { "tv_${it.host}" }) { tv ->
                    Surface(
                        color = CharcoalSurface,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CharcoalWell),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Tv, contentDescription = null, tint = MintGreen, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = tv.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                Text(
                                    text = tv.host,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = MonoFontFamily,
                                    color = TextSecondary
                                )
                            }
                            TextButton(onClick = { onConnectSavedTv(tv) }) {
                                Text("연결", color = MintGreen)
                            }
                            IconButton(onClick = { onForgetTv(tv.host) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "TV 삭제", tint = ErrorCoral)
                            }
                        }
                    }
                }

                item {
                    Surface(
                        color = CharcoalSurface,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("버튼 진동(햅틱) 피드백", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text("방향키 및 리모컨 버튼 터치 시 미세 진동", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                }
                                Switch(
                                    checked = preferences.hapticEnabled,
                                    onCheckedChange = onToggleHaptic,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = OnMintGreen,
                                        checkedTrackColor = MintGreen
                                    )
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("TV 입력창 자동 키보드 열기", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text("TV에서 검색창이 열리면 한글·영문 입력창 자동 표시", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                }
                                Switch(
                                    checked = preferences.autoShowKeyboard,
                                    onCheckedChange = onToggleAutoKeyboard,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = OnMintGreen,
                                        checkedTrackColor = MintGreen
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    editingSlot?.let { slot ->
        CustomButtonEditorSheet(
            slot = slot,
            recentApps = recentApps,
            onSave = { updated ->
                onUpdateCustomButton(updated)
                editingSlot = null
            },
            onDismiss = { editingSlot = null }
        )
    }

    if (showAddCustomAppDialog) {
        AddCustomAppDialog(
            onConfirm = { name, uri, pkg ->
                onAddCustomFavorite(name, uri, pkg)
                showAddCustomAppDialog = false
            },
            onDismiss = { showAddCustomAppDialog = false }
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun FavoriteTogglePill(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isFavorite) MintGreen else CharcoalWell,
        shape = CircleShape,
        modifier = Modifier
            .height(38.dp)
            .clip(CircleShape)
            .border(1.dp, if (isFavorite) MintGreen else DividerSubtle, CircleShape)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Rounded.Check else Icons.Rounded.Add,
                contentDescription = null,
                tint = if (isFavorite) OnMintGreen else TextPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isFavorite) "추가됨" else "추가",
                style = MaterialTheme.typography.labelMedium,
                color = if (isFavorite) OnMintGreen else TextPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomButtonEditorSheet(
    slot: CustomButtonEntity,
    recentApps: List<RecentTvAppEntity>,
    onSave: (CustomButtonEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var actionType by remember { mutableStateOf(slot.actionType) }
    var label by remember { mutableStateOf(slot.label) }
    var iconKey by remember { mutableStateOf(slot.iconKey) }
    var keyCode by remember { mutableStateOf(slot.keyCode) }
    var launchUri by remember { mutableStateOf(slot.launchUri) }
    var packageName by remember { mutableStateOf(slot.packageName) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalBg,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "커스텀 버튼 ${slot.slotIndex + 1} 설정",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = actionType == CustomActionType.KEYCODE.name,
                    onClick = { actionType = CustomActionType.KEYCODE.name },
                    label = { Text("TV 키코드 기능") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MintGreenContainer,
                        selectedLabelColor = MintGreen
                    )
                )
                FilterChip(
                    selected = actionType == CustomActionType.APP_LAUNCH.name,
                    onClick = { actionType = CustomActionType.APP_LAUNCH.name },
                    label = { Text("TV 앱 바로 실행") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MintGreenContainer,
                        selectedLabelColor = MintGreen
                    )
                )
            }

            OutlinedTextField(
                value = label,
                onValueChange = { if (it.length <= 10) label = it },
                label = { Text("버튼 표시 이름 (최대 10자)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CharcoalWell,
                    unfocusedContainerColor = CharcoalWell,
                    focusedBorderColor = MintGreen,
                    unfocusedBorderColor = DividerSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (actionType == CustomActionType.KEYCODE.name) {
                    items(TvAppCatalog.keyActionOptions, key = { it.id }) { opt ->
                        val selected = keyCode == opt.keyCode
                        Surface(
                            color = if (selected) MintGreenContainer else CharcoalSurface,
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .clickable {
                                    keyCode = opt.keyCode
                                    label = opt.label
                                    iconKey = opt.iconKey
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = customActionIcon(opt.iconKey),
                                    contentDescription = null,
                                    tint = if (selected) MintGreen else TextPrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(opt.label, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                    Text(opt.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                }
                                if (selected) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = MintGreen)
                                }
                            }
                        }
                    }
                } else {
                    val combinedApps = (recentApps.map {
                        TvAppCatalogItem(it.packageName, it.appName, it.launchUri, it.iconKey, it.accentHex, "최근 앱")
                    } + TvAppCatalog.apps).distinctBy { it.packageName }

                    items(combinedApps, key = { it.packageName }) { app ->
                        val selected = packageName == app.packageName
                        Surface(
                            color = if (selected) MintGreenContainer else CharcoalSurface,
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .clickable {
                                    packageName = app.packageName
                                    launchUri = app.launchUri
                                    label = app.appName
                                    iconKey = app.iconKey
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                TvAppBadgeIcon(
                                    iconKey = app.iconKey,
                                    accentHex = app.accentHex,
                                    appName = app.appName,
                                    size = 28.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selected) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = MintGreen)
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onSave(
                        slot.copy(
                            label = label.ifBlank { "기능 ${slot.slotIndex + 1}" },
                            iconKey = iconKey,
                            actionType = actionType,
                            keyCode = keyCode,
                            launchUri = launchUri,
                            packageName = packageName
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen, contentColor = OnMintGreen),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("저장하기", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AddCustomAppDialog(
    onConfirm: (name: String, uri: String, pkg: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var uri by remember { mutableStateOf("") }
    var pkg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalSurface,
        title = { Text("사용자 앱 즐겨찾기 추가", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("앱 이름 (예: 티빙)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uri,
                    onValueChange = { uri = it },
                    label = { Text("실행 딥링크 URI (예: https://... 또는 app://)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pkg,
                    onValueChange = { pkg = it },
                    label = { Text("패키지명 (선택사항)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val effectiveUri = uri.ifBlank {
                        if (pkg.isNotBlank()) "market://details?id=${pkg.trim()}" else ""
                    }
                    onConfirm(name, effectiveUri, pkg)
                },
                enabled = name.isNotBlank() && (uri.isNotBlank() || pkg.isNotBlank()),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen, contentColor = OnMintGreen)
            ) {
                Text("추가", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = TextSecondary)
            }
        }
    )
}
