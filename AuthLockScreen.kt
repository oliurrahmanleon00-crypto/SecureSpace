package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberSlate800
import com.example.ui.theme.CyberSlate900
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SapphireBlue
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.SecureSpaceViewModel

@Composable
fun AuthLockScreen(
    viewModel: SecureSpaceViewModel,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val pinSetupCompleted by viewModel.pinSetupCompleted.collectAsState()
    val failedAttempts by viewModel.failedAttempts.collectAsState()
    val lockoutSeconds by viewModel.lockoutTimeSeconds.collectAsState()

    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSettingUp by remember { mutableStateOf(!pinSetupCompleted) }

    // Setup fields
    var setupNewPin by remember { mutableStateOf("") }
    var setupDuressPin by remember { mutableStateOf("9999") }

    // Attempt auto-biometrics on entry if already setup
    LaunchedEffect(pinSetupCompleted) {
        if (pinSetupCompleted && activity != null && lockoutSeconds == 0) {
            viewModel.authenticateWithBiometrics(activity) { success ->
                if (success) onUnlocked()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CyberNavyDark, CyberSlate900, CyberNavyDark)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {

            // Knox Shield Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(CyberSlate800)
                    .border(2.dp, ElectricCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Knox Shield",
                    tint = ElectricCyan,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SECURE SPACE",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp,
                    color = ElectricCyan
                )
            )

            Text(
                text = "Android 10 Knox-Grade Sandbox Vault",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SapphireBlue,
                    fontFamily = FontFamily.Monospace
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!pinSetupCompleted || isSettingUp) {
                // PIN Setup Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSlate800),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Set Up Master Credentials",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Choose a 4-6 digit Master PIN and an optional Duress Decoy PIN.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = setupNewPin,
                            onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) setupNewPin = it },
                            label = { Text("Master PIN (e.g. 1234)") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setup_pin_field"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = setupDuressPin,
                            onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) setupDuressPin = it },
                            label = { Text("Duress / Decoy PIN (e.g. 9999)") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setup_duress_field"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (setupNewPin.length >= 4) {
                                    viewModel.setupMasterPin(setupNewPin, setupDuressPin)
                                    isSettingUp = false
                                    onUnlocked()
                                } else {
                                    errorMessage = "PIN must be at least 4 digits"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("confirm_setup_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                        ) {
                            Text("Initialize Knox Sandbox", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    repeat(4) { index ->
                        val filled = index < enteredPin.length
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (filled) ElectricCyan else CyberSlate800)
                                .border(
                                    width = 1.dp,
                                    color = if (filled) ElectricCyan else Color.Gray,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Lockout indicator
                if (lockoutSeconds > 0) {
                    Text(
                        text = "Anti-Tamper Lockout active: retry in ${lockoutSeconds}s",
                        color = CriticalRed,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = CriticalRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Numeric Keypad
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    val keyRows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9")
                    )

                    keyRows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            row.forEach { digit ->
                                KeypadButton(
                                    text = digit,
                                    onClick = {
                                        if (lockoutSeconds == 0 && enteredPin.length < 6) {
                                            val next = enteredPin + digit
                                            enteredPin = next
                                            if (next.length >= 4) {
                                                val verified = viewModel.verifyPin(next)
                                                if (verified) {
                                                    enteredPin = ""
                                                    errorMessage = null
                                                    onUnlocked()
                                                } else {
                                                    if (next.length >= 4) {
                                                        enteredPin = ""
                                                        errorMessage = "Invalid Credentials"
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Bottom Row: Biometrics, 0, Backspace
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Biometric Button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(CyberSlate800)
                                .clickable {
                                    if (activity != null && lockoutSeconds == 0) {
                                        viewModel.authenticateWithBiometrics(activity) { success ->
                                            if (success) onUnlocked()
                                        }
                                    }
                                }
                                .testTag("biometric_auth_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Biometric Unlock",
                                tint = NeonEmerald,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        KeypadButton(
                            text = "0",
                            onClick = {
                                if (lockoutSeconds == 0 && enteredPin.length < 6) {
                                    val next = enteredPin + "0"
                                    enteredPin = next
                                    if (next.length >= 4) {
                                        val verified = viewModel.verifyPin(next)
                                        if (verified) {
                                            enteredPin = ""
                                            onUnlocked()
                                        } else {
                                            enteredPin = ""
                                            errorMessage = "Invalid Credentials"
                                        }
                                    }
                                }
                            }
                        )

                        // Backspace
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(CyberSlate800)
                                .clickable {
                                    if (enteredPin.isNotEmpty()) {
                                        enteredPin = enteredPin.dropLast(1)
                                    }
                                }
                                .testTag("keypad_backspace"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = Color.LightGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Security Notice Footer
                Text(
                    text = "Hardware StrongBox & TEE Enforced • Duress PIN: 9999",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = CyberSlate800,
        modifier = Modifier
            .size(64.dp)
            .testTag("keypad_btn_$text")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}
