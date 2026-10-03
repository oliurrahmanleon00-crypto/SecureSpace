package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SecurityAuditLog
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
fun AuditLogsScreen(
    viewModel: SecureSpaceViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val auditLogs by viewModel.auditLogs.collectAsState()

    Scaffold(
        containerColor = CyberNavyDark,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberSlate900),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("audit_back_btn")) {
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
                            text = "Security Audit Trail",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "${auditLogs.size} Tamper-Evident Records",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SapphireBlue,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearAuditLogs() },
                        modifier = Modifier.testTag("clear_logs_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Logs",
                            tint = TextMuted
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (auditLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Audit Trail Clean",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(auditLogs, key = { it.id }) { log ->
                    AuditLogRowCard(log)
                }
            }
        }
    }
}

@Composable
fun AuditLogRowCard(log: SecurityAuditLog) {
    val (severityColor, severityBg) = when (log.severity) {
        "CRITICAL" -> Pair(CriticalRed, CriticalRed.copy(alpha = 0.15f))
        "WARNING" -> Pair(WarningAmber, WarningAmber.copy(alpha = 0.15f))
        else -> Pair(ElectricCyan, ElectricCyan.copy(alpha = 0.12f))
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("audit_log_${log.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSlate900),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSlate700)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = severityBg
                ) {
                    Text(
                        text = log.eventType,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = severityColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = formatDate(log.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = log.summary,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            if (log.details.isNotBlank()) {
                Text(
                    text = log.details,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
