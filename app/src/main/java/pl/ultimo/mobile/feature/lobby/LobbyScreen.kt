package pl.ultimo.mobile.feature.lobby

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import pl.ultimo.mobile.core.network.ConnectionStatus
import pl.ultimo.mobile.feature.lobby.dialog.AccessCodeDialog
import pl.ultimo.mobile.feature.lobby.dialog.CreateRoomDialog
import pl.ultimo.mobile.feature.lobby.dialog.RoomBrowserDialog
import pl.ultimo.mobile.feature.lobby.dialog.ServerConfigDialog
import pl.ultimo.mobile.ui.theme.AccentCyan
import pl.ultimo.mobile.ui.theme.StatusConnected
import pl.ultimo.mobile.ui.theme.StatusDisconnected
import pl.ultimo.mobile.ui.theme.StatusReconnecting
import pl.ultimo.mobile.ui.theme.SurfaceContainerDark
import pl.ultimo.mobile.ui.theme.SurfaceDark
import pl.ultimo.mobile.ui.theme.SurfaceElevatedDark
import pl.ultimo.mobile.ui.theme.TextMuted
import pl.ultimo.mobile.ui.theme.TextPrimary
import pl.ultimo.mobile.ui.theme.TextSecondary
import pl.ultimo.mobile.ui.theme.UltimoBlue
import pl.ultimo.mobile.ui.theme.UltimoRed
import pl.ultimo.mobile.ui.theme.UltimoYellow

@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel = hiltViewModel(),
    onJoinMatch: (username: String, roomId: String) -> Unit = { _, _ -> },
    onNavigateToHistory: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var isConfigDialogOpen by remember { mutableStateOf(false) }
    val handleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState.nameTakenError) {
        if (uiState.nameTakenError != null) {
            handleFocusRequester.requestFocus()
        }
    }

    if (isConfigDialogOpen) {
        ServerConfigDialog(
            currentServerUrl = uiState.serverUrl,
            useChaosProxy = uiState.useChaosProxy,
            chaosProxyUrl = uiState.chaosProxyUrl,
            onSaveConfig = { serverUrl, useChaos, chaosUrl ->
                viewModel.onServerUrlChanged(serverUrl)
                viewModel.onChaosProxyToggled(useChaos)
            },
            onDismissRequest = { isConfigDialogOpen = false }
        )
    }

    if (uiState.isRoomBrowserOpen) {
        RoomBrowserDialog(
            rooms = uiState.activeRooms,
            isRefreshing = uiState.isRefreshingRooms,
            onRefresh = { viewModel.refreshRooms() },
            onJoinRoom = { room ->
                viewModel.requestJoinRoom(room) { joinedRoomId ->
                    onJoinMatch(uiState.username, joinedRoomId)
                }
            },
            onDismissRequest = { viewModel.closeRoomBrowser() }
        )
    }

    if (uiState.isCreateRoomOpen) {
        CreateRoomDialog(
            initialRoomName = "Room_${System.currentTimeMillis() % 1000}",
            onCreateRoom = { name, visibility, code, capacity ->
                viewModel.createRoom(name, visibility, code, capacity) { createdRoomId ->
                    onJoinMatch(uiState.username, createdRoomId)
                }
            },
            onDismissRequest = { viewModel.closeCreateRoom() }
        )
    }

    val pendingRoom = uiState.pendingPrivateRoom
    if (pendingRoom != null) {
        AccessCodeDialog(
            roomName = pendingRoom.name,
            onConfirmCode = { code ->
                viewModel.joinRoom(pendingRoom.roomId, code) { joinedRoomId ->
                    onJoinMatch(uiState.username, joinedRoomId)
                }
            },
            onDismissRequest = { viewModel.dismissAccessCodeDialog() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title & Connectivity Badge
        Text(
            text = "ULTIMO",
            style = MaterialTheme.typography.displayLarge,
            color = UltimoBlue,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
        Text(
            text = "Authoritative Multiplayer Card Game",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Connection Status Indicator Pill + Settings Trigger
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ConnectionStatusPill(status = uiState.connectionStatus)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .clickable { isConfigDialogOpen = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⚙ Server",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (uiState.useChaosProxy) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⚡ GO-CHAOS-PROXY ACTIVE (Jitter & Packet Drop Simulated)",
                style = MaterialTheme.typography.labelSmall,
                color = UltimoYellow,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Lobby Card Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Text(
                    text = "Enter Game Room",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                val isHandleError = uiState.nameTakenError != null ||
                        (uiState.username.isNotBlank() && !uiState.isUsernameValid)

                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = { viewModel.onUsernameChanged(it) },
                    label = { Text("Player Handle") },
                    singleLine = true,
                    isError = isHandleError,
                    supportingText = {
                        if (uiState.nameTakenError != null) {
                            Text(
                                text = uiState.nameTakenError ?: "",
                                color = UltimoRed,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (uiState.username.isNotBlank() && !uiState.isUsernameValid) {
                            Text(
                                text = uiState.validationErrorMessage ?: "2-24 chars, no spaces/newlines",
                                color = UltimoRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                text = "${uiState.username.length}/24 characters",
                                color = TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(handleFocusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = if (isHandleError) UltimoRed else UltimoBlue,
                        unfocusedBorderColor = if (isHandleError) UltimoRed else SurfaceContainerDark,
                        errorBorderColor = UltimoRed
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.roomId,
                    onValueChange = { viewModel.onRoomIdChanged(it) },
                    label = { Text("Room ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = UltimoBlue,
                        unfocusedBorderColor = SurfaceContainerDark
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.joinMatch { room ->
                            onJoinMatch(uiState.username, room)
                        }
                    },
                    enabled = uiState.isUsernameValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UltimoBlue,
                        disabledContainerColor = SurfaceElevatedDark
                    )
                ) {
                    Text(
                        text = "Join / Create Match",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (uiState.isUsernameValid) Color.White else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Discovery & Custom Room Creation Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.openRoomBrowser() },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevatedDark)
                    ) {
                        Text(
                            text = "🔍 Browse Rooms",
                            fontSize = 12.sp,
                            color = AccentCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { viewModel.openCreateRoom() },
                        enabled = uiState.isUsernameValid,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceElevatedDark,
                            disabledContainerColor = SurfaceDark
                        )
                    ) {
                        Text(
                            text = "➕ Custom Room",
                            fontSize = 12.sp,
                            color = if (uiState.isUsernameValid) UltimoYellow else TextMuted,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onNavigateToHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text(
                        text = "Match History & Statistics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusPill(status: ConnectionStatus) {
    val (dotColor, label) = when (status) {
        ConnectionStatus.Connected -> StatusConnected to "Server Connected"
        is ConnectionStatus.Reconnecting -> StatusReconnecting to "Reconnecting (${status.nextRetrySeconds}s)..."
        ConnectionStatus.Connecting -> StatusReconnecting to "Connecting..."
        ConnectionStatus.Disconnected -> StatusDisconnected to "Offline"
        is ConnectionStatus.Failed -> StatusDisconnected to "Connection Failed"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "PillPulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DotPulse"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = if (status == ConnectionStatus.Connected) dotAlpha else 1f))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
