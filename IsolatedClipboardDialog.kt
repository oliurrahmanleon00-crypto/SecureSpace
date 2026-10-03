package com.example.ui.screens

import android.os.CountDownTimer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.AntiTamperGuard
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyberSlate800
import com.example.ui.theme.CyberSlate900
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SapphireBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SecureSpaceViewModel

@Composable
fun IsolatedClipboardDialog(
    viewModel: SecureSpaceViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var clipboardContent by remember { mutableStateOf("") }
    var secondsRemaining by remember { mutableIntStateOf(30) }
    var isTimerActive by remember { mutableStateOf(false) }

    DisposableEffect(isTimerActive) {
        var timer: CountDownTimer? = null
        if (isTimerActive) {
            timer = object : CountDownTimer(30_000L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    secondsRemaining = (millisUntilFinished / 1000).toInt()
                }

                override fun onFinish() {
                    secondsRemaining = 0
                    clipboardContent = ""
                    isTimerActive = false
                    viewModel.logAuditEvent("CLIPBOARD_WIPED", "Isolated Clipboard Auto-Wiped", "30-second security policy fulfilled", "INFO")
                }
            }.start()
        }
        onDispose {
            timer?.cancel()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberSlate900,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = ElectricCyan)
                Column {
                    Text("Isolated Clipboard", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Auto-Wipes after 30s", style = MaterialTheme.typography.labelSmall.copy(color = SapphireBlue, fontFamily = FontFamily.Monospace))
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Temporary cryptographic scratchpad. Any text copied here is zeroized from system memory after the timer expires to prevent background snooping.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                OutlinedTextField(
                    value = clipboardContent,
                    onValueChange = {
                        clipboardContent = it
                        if (!isTimerActive && it.isNotBlank()) {
                            isTimerActive = true
                        }
                    },
                    label = { Text("Confidential Text") },
                    placeholder = { Text("Paste seeds, tokens, or passwords here...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth().testTag("isolated_clipboard_input")
                )

                if (isTimerActive) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Zeroization in: ${secondsRemaining}s",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (secondsRemaining <= 5) CriticalRed else NeonEmerald,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Icon(Icons.Default.Timer, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                        }
                        LinearProgressIndicator(
                            progress = { secondsRemaining / 30f },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (secondsRemaining <= 5) CriticalRed else NeonEmerald,
                            trackColor = CyberSlate800
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (clipboardContent.isNotBlank()) {
                                AntiTamperGuard.copyWithAutoZeroize(
                                    context = context,
                                    label = "SecureSpace Secret",
                                    sensitiveText = clipboardContent,
                                    delayMs = 30_000L
                                )
                                isTimerActive = true
                                secondsRemaining = 30
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        modifier = Modifier.weight(1f).testTag("copy_with_wipe_btn")
                    ) {
                        Text("Copy & Arm Timer", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            clipboardContent = ""
                            isTimerActive = false
                            secondsRemaining = 30
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
                        modifier = Modifier.weight(1f).testTag("wipe_now_btn")
                    ) {
                        Text("Wipe Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = ElectricCyan)
            }
        }
    )
}
