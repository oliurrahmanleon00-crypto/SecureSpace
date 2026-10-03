package com.example.ui.screens

import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberSlate700
import com.example.ui.theme.CyberSlate800
import com.example.ui.theme.CyberSlate900
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SapphireBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.SecureSpaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DpcPolicyCenterScreen(
    viewModel: SecureSpaceViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val isAdminActive = remember { viewModel.dpcManager.isAdminActive() }
    val isProfileOwner = remember { viewModel.dpcManager.isProfileOwner() }
    val crossProfiles = remember { viewModel.dpcManager.getCrossProfileTargets() }

    val screenCaptureProtection by viewModel.screenCaptureProtection.collectAsState()
    val crossProfileCopyPasteBlocked by viewModel.crossProfileCopyPasteBlocked.collectAsState()
    val cameraDisabledInSandbox by viewModel.cameraDisabledInSandbox.collectAsState()
    val autoLockOnAppSwitch by viewModel.autoLockOnAppSwitch.collectAsState()

    Scaffold(
        containerColor = CyberNavyDark,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberSlate900),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("dpc_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "DPC Policy Control Center",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Android 10 (API 29) Knox Architecture",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SapphireBlue,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // DPC Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSlate900),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSlate700)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "ENTERPRISE DPC STACK STATUS",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        DpcStatusRow(
                            title = "Device Administrator",
                            active = isAdminActive,
                            activeDesc = "Active (Permissions Granted)",
                            inactiveDesc = "Inactive (Required for hardware lock)",
                            actionLabel = if (!isAdminActive) "Activate" else null,
                            onAction = {
                                val intent = viewModel.dpcManager.createEnableAdminIntent()
                                context.startActivity(intent)
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        DpcStatusRow(
                            title = "Managed Profile Owner",
                            active = isProfileOwner,
                            activeDesc = "Profile Owner (Isolated Work Space)",
                            inactiveDesc = "Standalone Sandbox (Container mode)",
                            actionLabel = if (!isProfileOwner) "Provision" else null,
                            onAction = {
                                val intent = viewModel.dpcManager.createProvisionManagedProfileIntent()
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cross-Profile Handles:",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Text(
                                text = "${crossProfiles.size} Target Profiles",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ElectricCyan,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Zero-Leakage Policy Toggles
            item {
                Text(
                    text = "DATA LEAKAGE PREVENTION (DLP) POLICIES",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Screenshot protection
                    PolicyToggleCard(
                        title = "Anti-Screen Capture (FLAG_SECURE)",
                        description = "Blocks screenshots, screen recording, and OS recents task thumbnail caching.",
                        icon = Icons.Default.VisibilityOff,
                        checked = screenCaptureProtection,
                        onCheckedChange = { checked ->
                            if (activity != null) {
                                viewModel.toggleScreenCaptureProtection(checked, activity)
                            }
                        }
                    )

                    // Cross profile copy/paste
                    PolicyToggleCard(
                        title = "Block Cross-Profile Copy & Paste",
                        description = "Enforces Android 10 DISALLOW_CROSS_PROFILE_COPY_PASTE to isolate clipboard.",
                        icon = Icons.Default.ContentPaste,
                        checked = crossProfileCopyPasteBlocked,
                        onCheckedChange = { viewModel.toggleCrossProfileCopyPaste(it) }
                    )

                    // Sandbox Camera
                    PolicyToggleCard(
                        title = "Disable Camera in Sandbox",
                        description = "Applies DPC setCameraDisabled policy to forbid untrusted camera access.",
                        icon = Icons.Default.NoPhotography,
                        checked = cameraDisabledInSandbox,
                        onCheckedChange = { viewModel.toggleCameraInSandbox(it) }
                    )

                    // Auto-lock on switch
                    PolicyToggleCard(
                        title = "Auto-Lock on App Backgrounding",
                        description = "Instantly clears cryptographic session buffers whenever user switches tasks.",
                        icon = Icons.Default.Lock,
                        checked = autoLockOnAppSwitch,
                        onCheckedChange = { viewModel.toggleAutoLockOnAppSwitch(it) }
                    )
                }
            }

            // Quick Device Commands
            item {
                Text(
                    text = "EMERGENCY DPC ACTIONS",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.dpcManager.lockNow() },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dpc_lock_now_btn")
                    ) {
                        Icon(Icons.Default.ScreenLockPortrait, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lock Screen", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.lockVault() },
                        colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dpc_panic_lock_btn")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Panic Lock Vault", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DpcStatusRow(
    title: String,
    active: Boolean,
    activeDesc: String,
    inactiveDesc: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (active) NeonEmerald else WarningAmber)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            Text(
                text = if (active) activeDesc else inactiveDesc,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (active) NeonEmerald else TextMuted
                )
            )
        }

        if (actionLabel != null) {
            Surface(
                onClick = onAction,
                shape = RoundedCornerShape(8.dp),
                color = ElectricCyan
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun PolicyToggleCard(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSlate900),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSlate700)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (checked) ElectricCyan.copy(alpha = 0.15f) else CyberSlate800),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (checked) ElectricCyan else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = ElectricCyan,
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = CyberSlate800
                )
            )
        }
    }
}
