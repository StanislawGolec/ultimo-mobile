package pl.ultimo.mobile.feature.lobby.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import pl.ultimo.mobile.core.datastore.UserPreferences
import pl.ultimo.mobile.ui.theme.AccentCyan
import pl.ultimo.mobile.ui.theme.SurfaceContainerDark
import pl.ultimo.mobile.ui.theme.SurfaceDark
import pl.ultimo.mobile.ui.theme.SurfaceElevatedDark
import pl.ultimo.mobile.ui.theme.TextMuted
import pl.ultimo.mobile.ui.theme.TextPrimary
import pl.ultimo.mobile.ui.theme.TextSecondary
import pl.ultimo.mobile.ui.theme.UltimoBlue
import pl.ultimo.mobile.ui.theme.UltimoYellow

/**
 * Dialog allowing configuration of WebSocket server endpoints and toggling
 * between Direct Server and Go-Chaos-Proxy.
 */
@Composable
fun ServerConfigDialog(
    currentServerUrl: String,
    useChaosProxy: Boolean,
    chaosProxyUrl: String,
    onSaveConfig: (serverUrl: String, useChaos: Boolean, chaosUrl: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var serverUrl by remember { mutableStateOf(currentServerUrl) }
    var chaosUrl by remember { mutableStateOf(chaosProxyUrl) }
    var chaosEnabled by remember { mutableStateOf(useChaosProxy) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .border(1.dp, SurfaceElevatedDark, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Server Configuration",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Presets
                Text(
                    text = "QUICK PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                PresetItem(
                    label = "Local Server (Emulator)",
                    url = "ws://10.0.2.2:8080/ws",
                    onClick = {
                        serverUrl = "ws://10.0.2.2:8080/ws"
                        chaosEnabled = false
                    }
                )

                PresetItem(
                    label = "Go-Chaos-Proxy (Jitter & Drops)",
                    url = "ws://10.0.2.2:9090/ws",
                    onClick = {
                        chaosUrl = "ws://10.0.2.2:9090/ws"
                        chaosEnabled = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Server URL input
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server WebSocket URI") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = UltimoBlue,
                        unfocusedBorderColor = SurfaceContainerDark
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Chaos Proxy Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerDark.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Route via Go-Chaos-Proxy",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Simulates network latency, jitter, and packet loss",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (chaosEnabled) UltimoYellow else TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = chaosEnabled,
                        onCheckedChange = { chaosEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = UltimoYellow
                        )
                    )
                }

                if (chaosEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = chaosUrl,
                        onValueChange = { chaosUrl = it },
                        label = { Text("Chaos Proxy Endpoint") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = UltimoYellow,
                            unfocusedBorderColor = SurfaceContainerDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions: Cancel / Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            onSaveConfig(serverUrl, chaosEnabled, chaosUrl)
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = UltimoBlue)
                    ) {
                        Text("Save & Connect", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetItem(
    label: String,
    url: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevatedDark.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(text = url, style = MaterialTheme.typography.labelSmall, color = AccentCyan, fontSize = 10.sp)
        }
    }
}
