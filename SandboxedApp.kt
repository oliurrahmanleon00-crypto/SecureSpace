package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Metadata record for an application cloned or managed inside the SecureSpace sandbox.
 */
@Entity(tableName = "sandboxed_apps")
data class SandboxedApp(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val customAlias: String,
    val isolatedDirName: String,
    val accountTag: String = "Sandbox Main",
    val isIsolatedStorageEnabled: Boolean = true,
    val isNotificationIsolated: Boolean = true,
    val launchCount: Int = 0,
    val lastLaunchedAt: Long = 0L,
    val isCloned: Boolean = true
)
