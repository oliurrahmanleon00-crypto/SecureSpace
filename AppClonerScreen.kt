package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sandbox.SandboxManager
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.CyberSlate700
import com.example.ui.theme.CyberSlate800
import com.example.ui.theme.CyberSlate900
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.SapphireBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SecureSpaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppClonerScreen(
    viewModel: SecureSpaceViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val installedApps by viewModel.installedApps.collectAsState()
    val clonedApps by viewModel.clonedApps.collectAsState()
    val clonedPackages = remember(clonedApps) { clonedApps.map { it.packageName }.toSet() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedAppToClone by remember { mutableStateOf<SandboxManager.DeviceAppInfo?>(null) }
    var customAlias by remember { mutableStateOf("") }
    var accountTag by remember { mutableStateOf("Knox Instance 1") }

    val filteredApps = remember(installedApps, searchQuery) {
        if (searchQuery.isBlank()) installedApps
        else installedApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = CyberNavyDark,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberSlate900),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("cloner_back_btn")) {
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
                            text = "Dual-Space App Cloner",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "${installedApps.size} Installed Candidate Apps",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = ElectricCyan)
                },
                placeholder = { Text("Search installed packages...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("app_cloner_search_field"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ElectricCyan, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Scanning device launcher activities...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val isAlreadyCloned = clonedPackages.contains(app.packageName)
                        AppCandidateCard(
                            app = app,
                            isAlreadyCloned = isAlreadyCloned,
                            onSelect = {
                                if (!isAlreadyCloned) {
                                    selectedAppToClone = app
                                    customAlias = app.appName
                                    accountTag = "Knox Instance 1"
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Clone configuration dialog
    selectedAppToClone?.let { app ->
        AlertDialog(
            onDismissRequest = { selectedAppToClone = null },
            containerColor = CyberSlate900,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = ElectricCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clone ${app.appName}", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "This creates an isolated storage container in the secure sandbox. App data will remain completely separated from your personal profile.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )

                    OutlinedTextField(
                        value = customAlias,
                        onValueChange = { customAlias = it },
                        label = { Text("Custom App Alias") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("clone_alias_input")
                    )

                    OutlinedTextField(
                        value = accountTag,
                        onValueChange = { accountTag = it },
                        label = { Text("Account Identifier Tag") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("clone_account_tag_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cloneAppIntoSandbox(app, customAlias, accountTag)
                        selectedAppToClone = null
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    modifier = Modifier.testTag("confirm_clone_btn")
                ) {
                    Text("Add to Sandbox", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAppToClone = null }) {
                    Text("Cancel", color = Color.LightGray)
                }
            }
        )
    }
}

@Composable
fun AppCandidateCard(
    app: SandboxManager.DeviceAppInfo,
    isAlreadyCloned: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isAlreadyCloned) { onSelect() }
            .testTag("app_candidate_${app.packageName}"),
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
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberSlate800),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = if (isAlreadyCloned) NeonEmerald else SapphireBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isAlreadyCloned) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonEmerald.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CLONED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonEmerald,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ElectricCyan.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CLONE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
