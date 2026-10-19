package pl.ultimo.mobile.feature.lobby.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
 * Modal dialog for creating a custom room with name, capacity slider (2-16),
 * Public/Private visibility toggle, and 4-letter uppercase code generator/field.
 */
@Composable
fun CreateRoomDialog(
    initialRoomName: String = "ProRoom_1",
    onCreateRoom: (name: String, visibility: String, accessCode: String?, capacity: Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    var roomName by remember { mutableStateOf(initialRoomName) }
    var isPrivate by remember { mutableStateOf(false) }
    var accessCode by remember { mutableStateOf(generateRandomCode()) }
    var capacityFloat by remember { mutableFloatStateOf(4f) }

    val capacity = capacityFloat.toInt().coerceIn(2, 16)
    val isValid = roomName.isNotBlank() && (!isPrivate || accessCode.length == 4)

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(20.dp, RoundedCornerShape(24.dp))
                .border(1.dp, SurfaceElevatedDark, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Text(
                    text = "Create Match Room",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Room Name
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it.replace(" ", "_") },
                    label = { Text("Room Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = UltimoBlue,
                        unfocusedBorderColor = SurfaceContainerDark
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Visibility Toggle (Public / Private)
                Text(
                    text = "ROOM VISIBILITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevatedDark)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isPrivate) UltimoBlue else Color.Transparent)
                            .clickable { isPrivate = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌐 Public",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (!isPrivate) Color.White else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPrivate) UltimoYellow else Color.Transparent)
                            .clickable {
                                isPrivate = true
                                if (accessCode.length != 4) accessCode = generateRandomCode()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔒 Private (Code)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isPrivate) Color.Black else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // If Private, display 4-letter access code field and generator
                if (isPrivate) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = accessCode,
                            onValueChange = { input ->
                                val clean = input.filter { it.isLetterOrDigit() }.uppercase().take(4)
                                accessCode = clean
                            },
                            label = { Text("4-Letter Access Code") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = UltimoYellow,
                                unfocusedTextColor = UltimoYellow,
                                focusedBorderColor = UltimoYellow,
                                unfocusedBorderColor = SurfaceContainerDark
                            )
                        )

                        Button(
                            onClick = { accessCode = generateRandomCode() },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevatedDark),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🎲 Reroll", color = AccentCyan, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Capacity Slider (2 - 16 players)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PLAYER CAPACITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$capacity Players",
                        style = MaterialTheme.typography.titleSmall,
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = capacityFloat,
                    onValueChange = { capacityFloat = it },
                    valueRange = 2f..16f,
                    steps = 13,
                    colors = SliderDefaults.colors(
                        thumbColor = UltimoBlue,
                        activeTrackColor = UltimoBlue,
                        inactiveTrackColor = SurfaceElevatedDark
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Actions: Cancel & Create Room
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
                            val visibility = if (isPrivate) "PRIVATE" else "PUBLIC"
                            val code = if (isPrivate) accessCode else null
                            onCreateRoom(roomName, visibility, code, capacity)
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(containerColor = UltimoBlue)
                    ) {
                        Text("Create Room", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun generateRandomCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..4).map { chars.random() }.joinToString("")
}
