package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an encrypted file stored within the isolated sandbox vault.
 * All binary contents are stored encrypted on disk using AES-256-GCM.
 */
@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val encryptedFilePath: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val category: String, // PHOTO, DOCUMENT, ARCHIVE, AUDIO, OTHER
    val createdAt: Long = System.currentTimeMillis(),
    val isDecoy: Boolean = false
)
