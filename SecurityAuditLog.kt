package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Immutable tamper-evident audit record capturing access, authentication,
 * cryptographic transformations, and DPC policy changes.
 */
@Entity(tableName = "security_audit_logs")
data class SecurityAuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val summary: String,
    val details: String,
    val severity: String // INFO, WARNING, CRITICAL
)
