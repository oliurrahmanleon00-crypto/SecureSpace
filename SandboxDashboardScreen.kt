package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SandboxedApp
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberSlate700
import com.example.ui.theme.CyberSlate800
import com.example.ui.theme.CyberSlate900
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SapphireBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.SecureSpaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandboxDashboardScreen(
    viewModel: SecureSpaceViewModel,
    onNavigateToCloner: () -> Unit,
    onNavigateToVault: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToBrowser: () -> Unit,
    onNavigateToDpcPolicies: () -> Unit,
    onNavigateToAuditLogs: () -> Unit,
    onOpenClipboardModal: () -> Unit,
    onLaunchCameraDirect: () -> Unit
) {
    val isDecoyMode by viewModel.isDecoyMode.collectAsState()
    val clonedApps by viewModel.clonedApps.collectAsState()
    val vaultItems by if (isDecoyMode) viewModel.decoyVaultItems.collectAsState() else viewModel.vaultItems.collectAsState()
    val secureNotes by viewModel.secureNotes.collectAsState()
    val screenCaptureProtection by viewModel.screenCaptureProtection.collectAsState()

    var activeSpaceMode by remember { mutableStateOf("SANDBOX") } // "PERSONAL" vs "SANDBOX"

    Scaffold(
        containerColor = CyberNavyDark,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CyberSlate900
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (isDecoyMode) WarningAmber else ElectricCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isDecoyMode) "GUEST SANDBOX" else "SECURE SPACE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = if (isDecoyMode) WarningAmber else Color.White
                                )
                            )
                            Text(
                                text = if (isDecoyMode) "Decoy Sandbox Space" else "Hardware AES-256 Isolated",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isDecoyMode) WarningAmber else SapphireBlue,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Lock Action
                    IconButton(
                        onClick = { viewModel.lockVault() },
                        modifier = Modifier.testTag("dashboard_lock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Secure Space",
                            tint = ElectricCyan
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

            // Decoy Alert Notice (if decoy mode was triggered by duress PIN)
            if (isDecoyMode) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CriticalRed.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CriticalRed, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CriticalRed)
                            Column {
                                Text(
                                    text = "DURESS DECOY ENVIRONMENT",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CriticalRed
                                    )
                                )
                                Text(
                                    text = "Main vault contents hidden. Master cryptographic keys isolated.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                )
                            }
                        }
                    }
                }
            }

            // Dual Space Profile Switcher Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSlate900),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ElectricCyan, SapphireBlue)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DUAL SPACE CONTAINER",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyan,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (activeSpaceMode == "SANDBOX") NeonEmerald.copy(alpha = 0.2f) else CyberSlate800,
                                border = if (activeSpaceMode == "SANDBOX") androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (activeSpaceMode == "SANDBOX") NeonEmerald else Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (activeSpaceMode == "SANDBOX") "Knox Profile Active" else "Personal Space",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (activeSpaceMode == "SANDBOX") NeonEmerald else Color.LightGray,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Security metrics strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricItem(label = "Encrypted Vault", value = "${vaultItems.size} items")
                            MetricItem(label = "Cloned Apps", value = "${clonedApps.size} active")
                            MetricItem(label = "DLP Guard", value = if (screenCaptureProtection) "SECURED" else "INACTIVE")
                        }
                    }
                }
            }

            // Cloned Apps Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SANDBOXED APPLICATIONS",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    )

                    Surface(
                        onClick = onNavigateToCloner,
                        shape = RoundedCornerShape(12.dp),
                        color = CyberSlate800,
                        modifier = Modifier.testTag("add_cloned_app_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clone App", style = MaterialTheme.typography.labelSmall.copy(color = ElectricCyan, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Cloned Apps Grid
            if (clonedApps.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToCloner() },
                        colors = CardDefaults.cardColors(containerColor = CyberSlate900),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Clone Your First App to Dual-Space",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Run duplicate accounts (WhatsApp, Telegram, Banking) with isolated storage.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        clonedApps.forEach { app ->
                            ClonedAppItemRow(
                                app = app,
                                onLaunch = { viewModel.launchAppInSandbox(app) },
                                onDelete = { viewModel.removeClonedApp(app) }
                            )
                        }
                    }
                }
            }

            // Core Security Modules Section
            item {
                Text(
                    text = "HARDWARE ISOLATION MODULES",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Modules Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SecurityModuleCard(
                            modifier = Modifier.weight(1f),
                            title = "Encrypted Vault",
                            subtitle = "${vaultItems.size} items",
                            icon = Icons.Default.Folder,
                            tint = ElectricCyan,
                            onClick = onNavigateToVault
                        )
                        SecurityModuleCard(
                            modifier = Modifier.weight(1f),
                            title = "Secure Notes",
                            subtitle = "${secureNotes.size} notes",
                            icon = Icons.Default.NoteAlt,
                            tint = SapphireBlue,
                            onClick = onNavigateToNotes
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SecurityModuleCard(
                            modifier = Modifier.weight(1f),
                            title = "Incognito Browser",
                            subtitle = "Zero Cache",
                            icon = Icons.Default.Language,
                            tint = NeonEmerald,
                            onClick = onNavigateToBrowser
                        )
                        SecurityModuleCard(
                            modifier = Modifier.weight(1f),
                            title = "Secure Camera",
                            subtitle = "Direct to Vault",
                            icon = Icons.Default.CameraAlt,
                            tint = WarningAmber,
                            onClick = onLaunchCameraDirect
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SecurityModuleCard(
                            modifier = Modifier.weight(1f),
                            title = "Isolated Clipboard",
                            subtitle = "Auto-Wipe 30s",
                            icon = Icons.Default.ContentPaste,
                            tint = ElectricCyan,
                            onClick = onOpenClipboardModal
                        )
                        SecurityModuleCard(
                            modifier = Modifier.weight(1f),
                            title = "DPC Policy Center",
                            subtitle = "Android 10 Controls",
                            icon = Icons.Default.Security,
                            tint = SapphireBlue,
                            onClick = onNavigateToDpcPolicies
                        )
                    }

                    SecurityModuleCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "Security Audit Trail",
                        subtitle = "Hardware-attested cryptographic access logs",
                        icon = Icons.Default.History,
                        tint = NeonEmerald,
                        onClick = onNavigateToAuditLogs
                    )
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

@Composable
fun ClonedAppItemRow(
    app: SandboxedApp,
    onLaunch: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLaunch() }
            .testTag("cloned_app_${app.packageName}"),
        colors = CardDefaults.cardColors(containerColor = CyberSlate900),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSlate700)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberSlate800)
                        .border(1.dp, ElectricCyan, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.customAlias,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeonEmerald.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "KNOX",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonEmerald,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "${app.accountTag} • Container: ${app.isolatedDirName}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        ),
                        maxLines = 1
                    )
                }
            }

            Surface(
                onClick = onLaunch,
                shape = RoundedCornerShape(8.dp),
                color = ElectricCyan,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "Launch",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun SecurityModuleCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("module_card_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = CyberSlate900),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSlate700)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f))
                    .border(1.dp, tint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
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
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary
                    )
                )
            }
        }
    }
}
