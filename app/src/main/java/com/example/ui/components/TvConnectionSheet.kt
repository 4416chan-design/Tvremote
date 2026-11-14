package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiFind
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.PairedTvEntity
import com.example.data.model.DiscoveredTv
import com.example.data.model.TvConnectionState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvConnectionSheet(
    discoveredTvs: List<DiscoveredTv>,
    pairedTvs: List<PairedTvEntity>,
    isScanning: Boolean,
    scanError: String?,
    connectionState: TvConnectionState,
    onRefreshScan: () -> Unit,
    onConnectTv: (host: String, name: String, remotePort: Int, pairingPort: Int) -> Unit,
    onDisconnect: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var manualIp by remember { mutableStateOf("") }
    var manualName by remember { mutableStateOf("") }

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
                .padding(bottom = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.WifiFind,
                    contentDescription = null,
                    tint = MintGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "같은 Wi-Fi의 Android TV 연결",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isScanning) "mDNS(_androidtvremote2._tcp) 검색 중..." else "검색 완료 · TV를 선택하세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                if (isScanning) {
                    CircularProgressIndicator(
                        color = MintGreen,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = onRefreshScan,
                    modifier = Modifier.testTag("refresh_tv_scan_button")
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "다시 검색", tint = TextPrimary)
                }
            }

            if (connectionState is TvConnectionState.Connected) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MintGreenContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Rounded.Tv, contentDescription = null, tint = MintGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${connectionState.tvName} 연결됨",
                                style = MaterialTheme.typography.titleSmall,
                                color = MintGreen
                            )
                            Text(
                                text = connectionState.host,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = MonoFontFamily,
                                color = TextSecondary
                            )
                        }
                        TextButton(onClick = onDisconnect) {
                            Text("연결 해제", color = ErrorCoral)
                        }
                    }
                }
            }

            if (!scanError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = scanError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ErrorCoral
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                if (discoveredTvs.isNotEmpty()) {
                    item {
                        Text(
                            text = "Wi-Fi에서 발견된 TV",
                            style = MaterialTheme.typography.labelLarge,
                            color = MintGreen
                        )
                    }
                    items(discoveredTvs, key = { "mdns_${it.host}" }) { tv ->
                        TvDeviceRow(
                            name = tv.serviceName,
                            host = tv.host,
                            badge = "mDNS",
                            onClick = { onConnectTv(tv.host, tv.serviceName, tv.port, tv.pairingPort) }
                        )
                    }
                }

                if (pairedTvs.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "저장된 TV",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                    }
                    items(pairedTvs, key = { "saved_${it.host}" }) { tv ->
                        TvDeviceRow(
                            name = tv.name,
                            host = tv.host,
                            badge = "저장됨",
                            onClick = { onConnectTv(tv.host, tv.name, tv.remotePort, tv.pairingPort) }
                        )
                    }
                }

                if (discoveredTvs.isEmpty() && pairedTvs.isEmpty()) {
                    item {
                        Surface(
                            color = CharcoalSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Wifi,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isScanning) "동일 Wi-Fi 네트워크의 TV를 찾고 있습니다..."
                                    else "검색된 Android TV가 없습니다",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "스마트폰과 TV가 같은 Wi-Fi 공유기에 연결되어 있는지 확인하거나 아래에서 IP 주소를 직접 입력하세요.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "IP 주소 직접 연결",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = manualIp,
                    onValueChange = { manualIp = it },
                    placeholder = { Text("192.168.0.10", fontFamily = MonoFontFamily) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = MonoFontFamily),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (manualIp.isNotBlank()) {
                                onConnectTv(manualIp, manualName.ifBlank { "거실 TV" }, 6466, 6467)
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CharcoalWell,
                        unfocusedContainerColor = CharcoalWell,
                        focusedBorderColor = MintGreen,
                        unfocusedBorderColor = DividerSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("manual_tv_ip_input")
                )

                Button(
                    onClick = {
                        onConnectTv(manualIp, manualName.ifBlank { "안방 TV" }, 6466, 6467)
                    },
                    enabled = manualIp.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MintGreen,
                        contentColor = OnMintGreen
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("manual_tv_connect_button")
                ) {
                    Text("연결", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TvDeviceRow(
    name: String,
    host: String,
    badge: String,
    onClick: () -> Unit
) {
    Surface(
        color = CharcoalSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CharcoalWell),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Tv, contentDescription = null, tint = MintGreen, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text(
                    text = host,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = MonoFontFamily,
                    color = TextSecondary
                )
            }
            Surface(
                color = CharcoalWell,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = MintGreen,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PairingPinDialog(
    state: TvConnectionState.PairingRequired,
    onSubmitPin: (String) -> Unit,
    onCancel: () -> Unit
) {
    var pin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = CharcoalSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text(
                text = "${state.tvName} 페어링 PIN 입력",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "TV 화면 오른쪽 아래에 표시된 6자리 코드(숫자·영문)를 입력해 주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { input ->
                        if (input.length <= 6) {
                            pin = input.uppercase()
                        }
                    },
                    placeholder = { Text("예: A1B2C3", fontFamily = MonoFontFamily) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontFamily = MonoFontFamily),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (pin.length == 6) onSubmitPin(pin)
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CharcoalWell,
                        unfocusedContainerColor = CharcoalWell,
                        focusedBorderColor = MintGreen,
                        unfocusedBorderColor = DividerSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pairing_pin_input")
                )
                if (!state.errorMessage.isNullOrBlank()) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ErrorCoral
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmitPin(pin) },
                enabled = pin.length == 6,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintGreen,
                    contentColor = OnMintGreen
                ),
                modifier = Modifier.testTag("pairing_confirm_button")
            ) {
                Text("페어링 완료", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.testTag("pairing_cancel_button")
            ) {
                Text("취소", color = TextSecondary)
            }
        }
    )
}
