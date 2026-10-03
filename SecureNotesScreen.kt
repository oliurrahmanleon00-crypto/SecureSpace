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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.PushPin
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SecureNote
import com.example.security.AntiTamperGuard
import com.example.security.CryptoEngine
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureNotesScreen(
    viewModel: SecureSpaceViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val notes by viewModel.secureNotes.collectAsState()

    var editingNote by remember { mutableStateOf<SecureNote?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    var titleInput by remember { mutableStateOf("") }
    var bodyInput by remember { mutableStateOf("") }
    var colorInput by remember { mutableStateOf("#00E5FF") }
    var isPinnedInput by remember { mutableStateOf(false) }

    val colorOptions = listOf("#00E5FF", "#00E676", "#FFB300", "#FF3366", "#38BDF8")

    Scaffold(
        containerColor = CyberNavyDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberSlate900),
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("notes_back_btn")) {
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
                            text = "Confidential Notes",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "${notes.size} Notes Encrypted at Rest",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SapphireBlue,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    titleInput = ""
                    bodyInput = ""
                    colorInput = "#00E5FF"
                    isPinnedInput = false
                    editingNote = null
                    isCreatingNew = true
                },
                containerColor = ElectricCyan,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_note_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Secure Note")
            }
        }
    ) { paddingValues ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                            imageVector = Icons.Default.NoteAlt,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Confidential Notes",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Create hardware-encrypted notes for private credentials, seeds, or confidential documentation.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    val decryptedTitle = remember(note.encryptedTitle) {
                        try {
                            CryptoEngine.decryptString(note.encryptedTitle)
                        } catch (_: Exception) {
                            "Encrypted Title"
                        }
                    }
                    val decryptedBody = remember(note.encryptedBody) {
                        try {
                            CryptoEngine.decryptString(note.encryptedBody)
                        } catch (_: Exception) {
                            "Encrypted Content"
                        }
                    }

                    NoteCard(
                        title = decryptedTitle,
                        body = decryptedBody,
                        note = note,
                        onEdit = {
                            titleInput = decryptedTitle
                            bodyInput = decryptedBody
                            colorInput = note.colorHex
                            isPinnedInput = note.isPinned
                            editingNote = note
                            isCreatingNew = false
                        },
                        onDelete = { viewModel.deleteNote(note) },
                        onCopySecurely = {
                            AntiTamperGuard.copyWithAutoZeroize(
                                context = context,
                                label = "SecureSpace Note",
                                sensitiveText = decryptedBody,
                                onCleared = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Clipboard securely wiped by AntiTamperGuard")
                                    }
                                }
                            )
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Copied! Clipboard will auto-wipe in 30 seconds")
                            }
                        }
                    )
                }
            }
        }
    }

    // Note Editor Dialog
    if (isCreatingNew || editingNote != null) {
        AlertDialog(
            onDismissRequest = {
                isCreatingNew = false
                editingNote = null
            },
            containerColor = CyberSlate900,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = ElectricCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCreatingNew) "New Encrypted Note" else "Edit Encrypted Note",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Note Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("note_title_input")
                    )

                    OutlinedTextField(
                        value = bodyInput,
                        onValueChange = { bodyInput = it },
                        label = { Text("Confidential Body") },
                        minLines = 4,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth().testTag("note_body_input")
                    )

                    // Color tag selector
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tag:", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                        colorOptions.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (colorInput == hex) 2.dp else 0.dp,
                                        color = if (colorInput == hex) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { colorInput = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            viewModel.saveNote(
                                id = editingNote?.id ?: 0L,
                                title = titleInput,
                                body = bodyInput,
                                colorHex = colorInput,
                                isPinned = isPinnedInput
                            )
                            isCreatingNew = false
                            editingNote = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    modifier = Modifier.testTag("save_note_btn")
                ) {
                    Text("Save & Encrypt", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    isCreatingNew = false
                    editingNote = null
                }) {
                    Text("Cancel", color = Color.LightGray)
                }
            }
        )
    }
}

@Composable
fun NoteCard(
    title: String,
    body: String,
    note: SecureNote,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopySecurely: () -> Unit
) {
    val tagColor = try {
        Color(android.graphics.Color.parseColor(note.colorHex))
    } catch (_: Exception) {
        ElectricCyan
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("note_card_${note.id}"),
        colors = CardDefaults.cardColors(containerColor = CyberSlate900),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tagColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(tagColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row {
                    IconButton(onClick = onCopySecurely, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Securely", tint = SapphireBlue, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CriticalRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AES-256-GCM Locked",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NeonEmerald,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = formatDate(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}
