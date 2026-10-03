package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a confidential note with hardware-backed AES-256-GCM encryption.
 */
@Entity(tableName = "secure_notes")
data class SecureNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val encryptedTitle: String,
    val encryptedBody: String,
    val colorHex: String = "#00E5FF",
    val isPinned: Boolean = false,
    val isDecoy: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
