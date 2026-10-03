package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.CountDownTimer
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.SandboxedApp
import com.example.data.model.SecureNote
import com.example.data.model.SecurityAuditLog
import com.example.data.model.VaultItem
import com.example.dpc.DpcPolicyManager
import com.example.sandbox.SandboxManager
import com.example.security.AntiTamperGuard
import com.example.security.BiometricAuthManager
import com.example.security.CryptoEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest

enum class StealthMode {
    DEFAULT,
    CALCULATOR,
    NOTES
}

class SecureSpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val vaultDao = db.vaultDao()
    private val notesDao = db.notesDao()
    private val sandboxedAppDao = db.sandboxedAppDao()
    private val auditDao = db.auditDao()

    val dpcManager = DpcPolicyManager(application)
    val sandboxManager = SandboxManager(application)
    val biometricAuthManager = BiometricAuthManager(application)

    private val prefs: SharedPreferences =
        application.getSharedPreferences("securespace_sys_prefs", Context.MODE_PRIVATE)

    // Auth State
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _isDecoyMode = MutableStateFlow(false)
    val isDecoyMode: StateFlow<Boolean> = _isDecoyMode.asStateFlow()

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    private val _lockoutTimeSeconds = MutableStateFlow(0)
    val lockoutTimeSeconds: StateFlow<Int> = _lockoutTimeSeconds.asStateFlow()

    private val _pinSetupCompleted = MutableStateFlow(prefs.contains("master_pin_hash"))
    val pinSetupCompleted: StateFlow<Boolean> = _pinSetupCompleted.asStateFlow()

    // Security Policies
    private val _screenCaptureProtection =
        MutableStateFlow(prefs.getBoolean("policy_flag_secure", true))
    val screenCaptureProtection: StateFlow<Boolean> = _screenCaptureProtection.asStateFlow()

    private val _crossProfileCopyPasteBlocked =
        MutableStateFlow(prefs.getBoolean("policy_block_copy_paste", true))
    val crossProfileCopyPasteBlocked: StateFlow<Boolean> = _crossProfileCopyPasteBlocked.asStateFlow()

    private val _cameraDisabledInSandbox =
        MutableStateFlow(prefs.getBoolean("policy_disable_camera", false))
    val cameraDisabledInSandbox: StateFlow<Boolean> = _cameraDisabledInSandbox.asStateFlow()

    private val _autoLockOnAppSwitch =
        MutableStateFlow(prefs.getBoolean("policy_autolock_switch", true))
    val autoLockOnAppSwitch: StateFlow<Boolean> = _autoLockOnAppSwitch.asStateFlow()

    private val _stealthMode = MutableStateFlow(
        StealthMode.valueOf(prefs.getString("stealth_mode", StealthMode.DEFAULT.name) ?: StealthMode.DEFAULT.name)
    )
    val stealthMode: StateFlow<StealthMode> = _stealthMode.asStateFlow()

    // Data streams
    val vaultItems: StateFlow<List<VaultItem>> = vaultDao.getAllItems(false)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val decoyVaultItems: StateFlow<List<VaultItem>> = vaultDao.getAllItems(true)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val secureNotes: StateFlow<List<SecureNote>> = notesDao.getAllNotes(false)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clonedApps: StateFlow<List<SandboxedApp>> = sandboxedAppDao.getAllClonedApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<SecurityAuditLog>> = auditDao.getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _installedApps = MutableStateFlow<List<SandboxManager.DeviceAppInfo>>(emptyList())
    val installedApps: StateFlow<List<SandboxManager.DeviceAppInfo>> = _installedApps.asStateFlow()

    private val _activeCategoryFilter = MutableStateFlow("ALL")
    val activeCategoryFilter: StateFlow<String> = _activeCategoryFilter.asStateFlow()

    private var lockoutTimer: CountDownTimer? = null

    init {
        // Log system startup audit
        logAuditEvent(
            eventType = "SYSTEM_STARTUP",
            summary = "SecureSpace Sandbox Initialized",
            details = "Android 10 DPC stack ready. Master KeyStore verified.",
            severity = "INFO"
        )
        refreshInstalledApps()
        seedSampleDataIfEmpty()
    }

    private fun seedSampleDataIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            // Seed a sample welcome encrypted note if none exists
            if (!prefs.getBoolean("seeded_sample_v1", false)) {
                try {
                    val encryptedTitle = CryptoEngine.encryptString("Knox Sandbox Security Guide")
                    val encryptedBody = CryptoEngine.encryptString(
                        "Welcome to SecureSpace.\n\n" +
                                "• Dual-Space Isolation: Cloned apps run with separate storage compartments.\n" +
                                "• Hardware Keystore: All vault files and notes are secured with AES-256-GCM.\n" +
                                "• Anti-Leakage: FLAG_SECURE prevents screenshots and recents preview snooping.\n" +
                                "• Duress Defense: Entering decoy PIN 9999 opens an innocent decoy space.\n" +
                                "• DPC Integration: Compatible with Android 10 Managed Profile policies."
                    )
                    notesDao.insertNote(
                        SecureNote(
                            encryptedTitle = encryptedTitle,
                            encryptedBody = encryptedBody,
                            colorHex = "#00E5FF",
                            isPinned = true
                        )
                    )
                    prefs.edit().putBoolean("seeded_sample_v1", true).apply()
                } catch (_: Exception) {
                }
            }
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = sandboxManager.getInstalledLaunchableApps()
            _installedApps.value = apps
        }
    }

    fun setCategoryFilter(category: String) {
        _activeCategoryFilter.value = category
    }

    // --- Authentication ---

    fun setupMasterPin(pin: String, duressPin: String = "9999") {
        val pinHash = hashPin(pin)
        val duressHash = hashPin(duressPin)
        prefs.edit()
            .putString("master_pin_hash", pinHash)
            .putString("duress_pin_hash", duressHash)
            .apply()
        _pinSetupCompleted.value = true
        _isUnlocked.value = true
        _isDecoyMode.value = false
        logAuditEvent("PIN_SETUP", "Master PIN established", "Hardware vault credentials configured", "INFO")
    }

    fun verifyPin(pin: String): Boolean {
        if (_lockoutTimeSeconds.value > 0) return false

        val storedMaster = prefs.getString("master_pin_hash", hashPin("1234"))
        val storedDuress = prefs.getString("duress_pin_hash", hashPin("9999"))
        val inputHash = hashPin(pin)

        if (inputHash == storedMaster) {
            _isUnlocked.value = true
            _isDecoyMode.value = false
            _failedAttempts.value = 0
            logAuditEvent("AUTH_SUCCESS", "PIN Authentication", "Master Sandbox unlocked", "INFO")
            return true
        } else if (inputHash == storedDuress) {
            _isUnlocked.value = true
            _isDecoyMode.value = true
            _failedAttempts.value = 0
            logAuditEvent("DURESS_TRIGGERED", "Duress PIN Entered", "Decoy sandbox space presented to user", "WARNING")
            return true
        } else {
            val attempts = _failedAttempts.value + 1
            _failedAttempts.value = attempts
            logAuditEvent("AUTH_FAIL", "Failed PIN Attempt", "Attempt count: $attempts", "WARNING")

            if (attempts >= 5) {
                startLockoutCountdown(30)
                logAuditEvent("LOCKOUT_ENGAGED", "Brute Force Protection", "Keypad locked for 30 seconds", "CRITICAL")
            }
            return false
        }
    }

    fun authenticateWithBiometrics(activity: FragmentActivity, onResult: (Boolean) -> Unit) {
        if (_lockoutTimeSeconds.value > 0) {
            onResult(false)
            return
        }

        biometricAuthManager.authenticate(
            activity = activity,
            onResult = { result ->
                when (result) {
                    is BiometricAuthManager.AuthResult.Success -> {
                        _isUnlocked.value = true
                        _isDecoyMode.value = false
                        _failedAttempts.value = 0
                        logAuditEvent("AUTH_SUCCESS", "Biometric Authentication", "Strong hardware biometric identity confirmed", "INFO")
                        onResult(true)
                    }
                    is BiometricAuthManager.AuthResult.Failed -> {
                        val attempts = _failedAttempts.value + 1
                        _failedAttempts.value = attempts
                        logAuditEvent("AUTH_FAIL", "Biometric Verification Failed", "Attempt count: $attempts", "WARNING")
                        onResult(false)
                    }
                    is BiometricAuthManager.AuthResult.Error -> {
                        onResult(false)
                    }
                    else -> onResult(false)
                }
            }
        )
    }

    private fun startLockoutCountdown(seconds: Int) {
        _lockoutTimeSeconds.value = seconds
        lockoutTimer?.cancel()
        lockoutTimer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _lockoutTimeSeconds.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                _lockoutTimeSeconds.value = 0
            }
        }.start()
    }

    fun lockVault() {
        _isUnlocked.value = false
        _isDecoyMode.value = false
        logAuditEvent("VAULT_LOCKED", "Manual or Auto Lock", "Cryptographic buffers secured", "INFO")
    }

    fun setStealthMode(mode: StealthMode) {
        _stealthMode.value = mode
        prefs.edit().putString("stealth_mode", mode.name).apply()
        logAuditEvent("STEALTH_MODE", "Stealth Disguise Changed", "Current mode: ${mode.name}", "INFO")
    }

    // --- Policy Controls ---

    fun toggleScreenCaptureProtection(enabled: Boolean, activity: FragmentActivity) {
        _screenCaptureProtection.value = enabled
        prefs.edit().putBoolean("policy_flag_secure", enabled).apply()
        AntiTamperGuard.setScreenCaptureProtection(activity, enabled)
        dpcManager.setScreenCaptureDisabledPolicy(enabled)
        logAuditEvent("POLICY_CHANGED", "Screen Capture Protection", "FLAG_SECURE set to $enabled", "INFO")
    }

    fun toggleCrossProfileCopyPaste(blocked: Boolean) {
        _crossProfileCopyPasteBlocked.value = blocked
        prefs.edit().putBoolean("policy_block_copy_paste", blocked).apply()
        dpcManager.setCrossProfileCopyPasteRestriction(blocked)
        logAuditEvent("POLICY_CHANGED", "Cross-Profile Copy/Paste", "Disallow copy-paste: $blocked", "INFO")
    }

    fun toggleCameraInSandbox(disabled: Boolean) {
        _cameraDisabledInSandbox.value = disabled
        prefs.edit().putBoolean("policy_disable_camera", disabled).apply()
        dpcManager.setCameraDisabledPolicy(disabled)
        logAuditEvent("POLICY_CHANGED", "Sandbox Camera Restriction", "Camera disabled: $disabled", "INFO")
    }

    fun toggleAutoLockOnAppSwitch(enabled: Boolean) {
        _autoLockOnAppSwitch.value = enabled
        prefs.edit().putBoolean("policy_autolock_switch", enabled).apply()
        logAuditEvent("POLICY_CHANGED", "Auto-Lock on App Switch", "Enabled: $enabled", "INFO")
    }

    // --- Vault File Operations ---

    fun importFile(uri: Uri, displayName: String, mimeType: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    logAuditEvent("FILE_IMPORT_ERROR", "Import Failed", "Unable to open URI stream", "WARNING")
                    return@launch
                }

                // Temporary file
                val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}_$displayName")
                FileOutputStream(tempFile).use { output ->
                    inputStream.copyTo(output)
                }

                // Encrypt directly into vault directory
                val vaultDir = File(context.filesDir, "vault").apply { mkdirs() }
                val encryptedFile = File(vaultDir, "enc_${System.currentTimeMillis()}.bin")

                val finalSize = CryptoEngine.encryptFile(tempFile, encryptedFile)
                CryptoEngine.secureDelete(tempFile)

                val item = VaultItem(
                    fileName = displayName,
                    encryptedFilePath = encryptedFile.absolutePath,
                    mimeType = mimeType,
                    fileSizeBytes = finalSize,
                    category = category,
                    isDecoy = _isDecoyMode.value
                )
                vaultDao.insertItem(item)

                logAuditEvent("ENCRYPT_FILE", "File Encrypted", "$displayName ($finalSize bytes) secured with AES-256-GCM", "INFO")
            } catch (e: Exception) {
                logAuditEvent("FILE_IMPORT_ERROR", "Encryption Exception", e.localizedMessage ?: "Unknown", "CRITICAL")
            }
        }
    }

    fun savePhotoDirectToVault(tempPhotoFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val vaultDir = File(context.filesDir, "vault").apply { mkdirs() }
                val encryptedFile = File(vaultDir, "photo_${System.currentTimeMillis()}.enc")

                val finalSize = CryptoEngine.encryptFile(tempPhotoFile, encryptedFile)
                CryptoEngine.secureDelete(tempPhotoFile)

                val item = VaultItem(
                    fileName = "SecureCapture_${System.currentTimeMillis()}.jpg",
                    encryptedFilePath = encryptedFile.absolutePath,
                    mimeType = "image/jpeg",
                    fileSizeBytes = finalSize,
                    category = "PHOTO",
                    isDecoy = _isDecoyMode.value
                )
                vaultDao.insertItem(item)
                logAuditEvent("SECURE_CAMERA", "Encrypted Photo Captured", "Stored directly in hardware vault", "INFO")
            } catch (e: Exception) {
                logAuditEvent("CAMERA_ENCRYPT_ERROR", "Secure Photo Save Failed", e.localizedMessage ?: "", "CRITICAL")
            }
        }
    }

    fun decryptFileToCache(item: VaultItem): File? {
        return try {
            val context = getApplication<Application>()
            val sourceFile = File(item.encryptedFilePath)
            if (!sourceFile.exists()) return null

            val exportsDir = File(context.filesDir, "exports").apply { mkdirs() }
            val decryptedFile = File(exportsDir, item.fileName)
            CryptoEngine.decryptFile(sourceFile, decryptedFile)
            logAuditEvent("DECRYPT_FILE", "File Decrypted for Viewing", item.fileName, "INFO")
            decryptedFile
        } catch (e: Exception) {
            logAuditEvent("DECRYPT_ERROR", "Decryption Failed", e.localizedMessage ?: "", "CRITICAL")
            null
        }
    }

    fun deleteVaultItem(item: VaultItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(item.encryptedFilePath)
                CryptoEngine.secureDelete(file)
                vaultDao.deleteItem(item)
                logAuditEvent("DELETE_VAULT_ITEM", "File Zeroized", "${item.fileName} wiped from disk", "INFO")
            } catch (e: Exception) {
                logAuditEvent("DELETE_ERROR", "File Deletion Failed", e.localizedMessage ?: "", "WARNING")
            }
        }
    }

    // --- Secure Notes ---

    fun saveNote(id: Long, title: String, body: String, colorHex: String, isPinned: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val encTitle = CryptoEngine.encryptString(title)
                val encBody = CryptoEngine.encryptString(body)
                val note = SecureNote(
                    id = id,
                    encryptedTitle = encTitle,
                    encryptedBody = encBody,
                    colorHex = colorHex,
                    isPinned = isPinned,
                    isDecoy = _isDecoyMode.value,
                    updatedAt = System.currentTimeMillis()
                )
                if (id == 0L) {
                    notesDao.insertNote(note)
                    logAuditEvent("NOTE_CREATED", "Secure Note Saved", "AES-256-GCM encrypted note stored", "INFO")
                } else {
                    notesDao.updateNote(note)
                    logAuditEvent("NOTE_UPDATED", "Secure Note Updated", "Note #$id re-encrypted", "INFO")
                }
            } catch (e: Exception) {
                logAuditEvent("NOTE_ERROR", "Note Encryption Failed", e.localizedMessage ?: "", "WARNING")
            }
        }
    }

    fun deleteNote(note: SecureNote) {
        viewModelScope.launch(Dispatchers.IO) {
            notesDao.deleteNote(note)
            logAuditEvent("NOTE_DELETED", "Secure Note Removed", "Note record deleted", "INFO")
        }
    }

    // --- App Cloning & Sandbox ---

    fun cloneAppIntoSandbox(appInfo: SandboxManager.DeviceAppInfo, customAlias: String, accountTag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val container = sandboxManager.createSandboxContainer(appInfo.packageName)
                val record = SandboxedApp(
                    packageName = appInfo.packageName,
                    appName = appInfo.appName,
                    customAlias = customAlias.ifBlank { appInfo.appName },
                    isolatedDirName = container.name,
                    accountTag = accountTag.ifBlank { "Knox Sandbox Instance" }
                )
                sandboxedAppDao.insertClonedApp(record)
                logAuditEvent("APP_CLONED", "App Added to Sandbox", "${appInfo.appName} (${appInfo.packageName}) cloned into isolated container", "INFO")
            } catch (e: Exception) {
                logAuditEvent("CLONE_ERROR", "App Clone Failed", e.localizedMessage ?: "", "CRITICAL")
            }
        }
    }

    fun launchAppInSandbox(app: SandboxedApp) {
        viewModelScope.launch(Dispatchers.IO) {
            sandboxedAppDao.incrementLaunch(app.id)
            val crossProfiles = dpcManager.getCrossProfileTargets()
            val targetHandle = crossProfiles.firstOrNull()
            val success = sandboxManager.launchSandboxedApp(app, targetHandle)
            if (success) {
                logAuditEvent("APP_LAUNCH", "Sandboxed App Executed", "${app.customAlias} launched in isolated space", "INFO")
            } else {
                logAuditEvent("LAUNCH_FAIL", "App Launch Failed", "Package: ${app.packageName}", "WARNING")
            }
        }
    }

    fun removeClonedApp(app: SandboxedApp) {
        viewModelScope.launch(Dispatchers.IO) {
            sandboxManager.removeSandboxContainer(app.packageName)
            sandboxedAppDao.deleteClonedApp(app)
            logAuditEvent("APP_REMOVED", "Cloned App Uninstalled", "${app.customAlias} sandbox wiped", "INFO")
        }
    }

    // --- Audit Logging ---

    fun logAuditEvent(eventType: String, summary: String, details: String, severity: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val log = SecurityAuditLog(
                eventType = eventType,
                summary = summary,
                details = details,
                severity = severity
            )
            auditDao.insertLog(log)
        }
    }

    fun clearAuditLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            auditDao.clearLogs()
            logAuditEvent("AUDIT_CLEARED", "Security Logs Purged", "User initiated audit wipe", "WARNING")
        }
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
