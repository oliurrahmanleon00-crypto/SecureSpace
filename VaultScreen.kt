package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VaultItem
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
import com.example.ui.viewmodel.SecureSpaceViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: SecureSpaceViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val isDecoyMode by viewModel.isDecoyMode.collectAsState()
    val vaultItems by if (isDecoyMode) viewModel.decoyVaultItems.collectAsState() else viewModel.vaultItems.collectAsState()
    val activeCategory by viewModel.activeCategoryFilter.collectAsState()

    var selectedItemForDetail by remember { mutableStateOf<VaultItem?>(null) }
    var decryptedFilePreview by remember { mutableStateOf<File?>(null) }
    var decryptedContentText by remember { mutableStateOf<String?>(null) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            var fileName = "import_${System.currentTimeMillis()}"
            var mimeType = "application/octet-stream"

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                }
            }
            context.contentResolver.getType(uri)?.let { mimeType = it }

            val category = when {
                mimeType.startsWith("image/") -> "PHOTO"
                mimeType.startsWith("audio/") -> "AUDIO"
                mimeType.contains("pdf") || mimeType.contains("text") || mimeType.contains("document") -> "DOCUMENT"
                mimeType.contains("zip") || mimeType.contains("tar") || mimeType.contains("rar") -> "ARCHIVE"
                else -> "OTHER"
            }

            viewModel.importFile(uri, fileName, mimeType, category)
        }
    }

    val categories = listOf("ALL", "PHOTO", "DOCUMENT", "ARCHIVE", "AUDIO", "OTHER")

    val filteredItems = remember(vaultItems, activeCategory) {
        if (activeCategory == "ALL") vaultItems
        else vaultItems.filter { it.category == activeCategory }
    }

    Scaffold(
        containerColor = CyberNavyDark,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberSlate900),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("vault_back_btn")) {
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
                            text = "Hardware Encrypted Vault",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "${vaultItems.size} Files Encrypted (AES-256-GCM)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ElectricCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePickerLauncher.launch("*/*") },
                containerColor = ElectricCyan,
                contentColor = Color.Black,
                modifier = Modifier.testTag("import_file_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Encrypt File", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == activeCategory
                    Surface(
                        onClick = { viewModel.setCategoryFilter(cat) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ElectricCyan else CyberSlate800,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, CyberSlate700),
                        modifier = Modifier.testTag("filter_chip_$cat")
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSelected) Color.Black else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(CyberSlate800),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Encrypted Items Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Import photos, confidential documents, or audio files to secure them with hardware-backed AES-256 encryption.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                            modifier = Modifier.padding(top = 6.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        VaultItemCard(
                            item = item,
                            onClick = {
                                selectedItemForDetail = item
                                decryptedFilePreview = null
                                decryptedContentText = null
                            }
                        )
                    }
                }
            }
        }
    }

    // Vault Item Details & Decryption Modal
    selectedItemForDetail?.let { item ->
        AlertDialog(
            onDismissRequest = {
                selectedItemForDetail = null
                decryptedFilePreview = null
                decryptedContentText = null
            },
            containerColor = CyberSlate900,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(getCategoryIcon(item.category), contentDescription = null, tint = ElectricCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.fileName, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Encrypted Size: ${formatFileSize(item.fileSizeBytes)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontFamily = FontFamily.Monospace)
                    )
                    Text(
                        text = "Cipher: AES-256-GCM / Hardware StrongBox",
                        style = MaterialTheme.typography.bodySmall.copy(color = SapphireBlue, fontFamily = FontFamily.Monospace)
                    )
                    Text(
                        text = "Added: ${formatDate(item.createdAt)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )

                    if (decryptedContentText != null) {
                        Surface(
                            color = CyberSlate800,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = decryptedContentText ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontFamily = FontFamily.Monospace),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Decrypt Action Button
                    Button(
                        onClick = {
                            val decrypted = viewModel.decryptFileToCache(item)
                            decryptedFilePreview = decrypted
                            if (decrypted != null && item.mimeType.startsWith("text/")) {
                                decryptedContentText = try {
                                    decrypted.readText(Charsets.UTF_8).take(500)
                                } catch (_: Exception) {
                                    "Binary or unreadable text content."
                                }
                            } else if (decrypted != null) {
                                decryptedContentText = "Decrypted successfully into isolated cache (${decrypted.length()} bytes). Preview ready."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireBlue),
                        modifier = Modifier.fillMaxWidth().testTag("decrypt_preview_btn")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Decrypt & Verify Integrity", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    // Zeroize & Delete Action Button
                    Button(
                        onClick = {
                            viewModel.deleteVaultItem(item)
                            selectedItemForDetail = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
                        modifier = Modifier.fillMaxWidth().testTag("zeroize_delete_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cryptographic Zeroize & Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedItemForDetail = null
                        decryptedFilePreview = null
                        decryptedContentText = null
                    }
                ) {
                    Text("Close", color = ElectricCyan)
                }
            }
        )
    }
}

@Composable
fun VaultItemCard(
    item: VaultItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("vault_item_${item.id}"),
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
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberSlate800),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(item.category),
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = item.fileName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatFileSize(item.fileSizeBytes)} • AES-256 GCM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Encrypted",
                tint = NeonEmerald,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "PHOTO" -> Icons.Default.Image
        "DOCUMENT" -> Icons.Default.Description
        "ARCHIVE" -> Icons.Default.FolderZip
        "AUDIO" -> Icons.Default.AudioFile
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    return String.format(Locale.US, "%.1f MB", mb)
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
