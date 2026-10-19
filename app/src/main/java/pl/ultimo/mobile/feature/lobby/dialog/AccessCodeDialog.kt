package pl.ultimo.mobile.feature.lobby.dialog

import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import pl.ultimo.mobile.ui.theme.SurfaceContainerDark
import pl.ultimo.mobile.ui.theme.SurfaceDark
import pl.ultimo.mobile.ui.theme.SurfaceElevatedDark
import pl.ultimo.mobile.ui.theme.TextPrimary
import pl.ultimo.mobile.ui.theme.TextSecondary
import pl.ultimo.mobile.ui.theme.UltimoBlue
import pl.ultimo.mobile.ui.theme.UltimoYellow

/**
 * Prompt modal requesting 4-letter secret access code when joining a private room.
 */
@Composable
fun AccessCodeDialog(
    roomName: String,
    onConfirmCode: (code: String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    val isValid = code.length == 4

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .border(1.dp, SurfaceElevatedDark, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Text(
                    text = "🔒 Private Room",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter 4-letter access code to join $roomName:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { input ->
                        val clean = input.filter { it.isLetterOrDigit() }.uppercase().take(4)
                        code = clean
                    },
                    label = { Text("Access Code") },
                    placeholder = { Text("e.g. K9X2") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = UltimoYellow,
                        unfocusedTextColor = UltimoYellow,
                        focusedBorderColor = UltimoYellow,
                        unfocusedBorderColor = SurfaceContainerDark
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                        onClick = { onConfirmCode(code) },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(containerColor = UltimoBlue)
                    ) {
                        Text("Join Room", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
