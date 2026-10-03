package com.example

import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.security.AntiTamperGuard
import com.example.ui.screens.AppClonerScreen
import com.example.ui.screens.AuditLogsScreen
import com.example.ui.screens.AuthLockScreen
import com.example.ui.screens.DpcPolicyCenterScreen
import com.example.ui.screens.IsolatedClipboardDialog
import com.example.ui.screens.SandboxDashboardScreen
import com.example.ui.screens.SandboxedBrowserScreen
import com.example.ui.screens.SecureNotesScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.CyberNavyDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SecureSpaceViewModel
import java.io.File

class MainActivity : FragmentActivity() {

    private val viewModel: SecureSpaceViewModel by viewModels()
    private var pendingCameraTempFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Apply initial FLAG_SECURE to prevent screenshot / recent app snapshot leaks
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            MyApplicationTheme {
                val screenCaptureProtection by viewModel.screenCaptureProtection.collectAsState()

                // Keep FLAG_SECURE synchronized with policy setting
                LaunchedEffect(screenCaptureProtection) {
                    AntiTamperGuard.setScreenCaptureProtection(this@MainActivity, screenCaptureProtection)
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberNavyDark
                ) {
                    SecureSpaceApp(
                        viewModel = viewModel,
                        onLaunchSecureCamera = { onPhotoSuccess ->
                            launchDirectSecureCamera(onPhotoSuccess)
                        }
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Auto-lock when user backgrounds the app or turns off screen
        if (viewModel.autoLockOnAppSwitch.value) {
            viewModel.lockVault()
        }
    }

    private var onCameraCaptureCallback: (() -> Unit)? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraTempFile?.let { tempFile ->
                if (tempFile.exists() && tempFile.length() > 0) {
                    viewModel.savePhotoDirectToVault(tempFile)
                    onCameraCaptureCallback?.invoke()
                }
            }
        }
    }

    private fun launchDirectSecureCamera(onSuccess: () -> Unit) {
        onCameraCaptureCallback = onSuccess
        val tempDir = File(cacheDir, "camera_staging").apply { mkdirs() }
        val tempFile = File(tempDir, "temp_capture_${System.currentTimeMillis()}.jpg")
        pendingCameraTempFile = tempFile

        val photoUri: Uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            tempFile
        )
        takePictureLauncher.launch(photoUri)
    }
}

@Composable
fun SecureSpaceApp(
    viewModel: SecureSpaceViewModel,
    onLaunchSecureCamera: (onSuccess: () -> Unit) -> Unit
) {
    val navController = rememberNavController()
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    var showClipboardDialog by remember { mutableStateOf(false) }

    // Navigation synchronization with unlock state
    LaunchedEffect(isUnlocked) {
        if (!isUnlocked) {
            navController.navigate("auth_lock") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isUnlocked) "dashboard" else "auth_lock"
    ) {
        composable("auth_lock") {
            AuthLockScreen(
                viewModel = viewModel,
                onUnlocked = {
                    navController.navigate("dashboard") {
                        popUpTo("auth_lock") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            SandboxDashboardScreen(
                viewModel = viewModel,
                onNavigateToCloner = { navController.navigate("cloner") },
                onNavigateToVault = { navController.navigate("vault") },
                onNavigateToNotes = { navController.navigate("notes") },
                onNavigateToBrowser = { navController.navigate("browser") },
                onNavigateToDpcPolicies = { navController.navigate("dpc") },
                onNavigateToAuditLogs = { navController.navigate("audit") },
                onOpenClipboardModal = { showClipboardDialog = true },
                onLaunchCameraDirect = {
                    onLaunchSecureCamera {
                        navController.navigate("vault")
                    }
                }
            )
        }

        composable("cloner") {
            AppClonerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("vault") {
            VaultScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("notes") {
            SecureNotesScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("dpc") {
            DpcPolicyCenterScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("audit") {
            AuditLogsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("browser") {
            SandboxedBrowserScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }

    if (showClipboardDialog) {
        IsolatedClipboardDialog(
            viewModel = viewModel,
            onDismiss = { showClipboardDialog = false }
        )
    }
}
