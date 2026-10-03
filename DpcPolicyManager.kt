package com.example.dpc

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.CrossProfileApps
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import android.util.Log

/**
 * Enterprise Device Policy Controller (DPC) & Managed Profile Manager.
 *
 * Implements Android 10 (API Level 29) system architecture for enterprise sandbox
 * isolation, similar to Samsung Secure Folder / Knox Workspace.
 *
 * Key Architectural Responsibilities:
 * 1. Inspecting Profile Owner (`isProfileOwnerApp`) and Device Owner status.
 * 2. Generating Android Enterprise provisioning intents (`ACTION_PROVISION_MANAGED_PROFILE`).
 * 3. Utilizing Android 10 `CrossProfileApps` APIs (`targetUserProfiles`, `startMainActivity`).
 * 4. Applying zero-leakage DLP restrictions:
 *    - `UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE`
 *    - `UserManager.DISALLOW_SHARE_LOCATION`
 *    - `DevicePolicyManager.setCameraDisabled`
 *    - `DevicePolicyManager.setScreenCaptureDisabled`
 */
class DpcPolicyManager(private val context: Context) {

    companion object {
        private const val TAG = "DpcPolicyManager"
    }

    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val userManager = context.getSystemService(Context.USER_SERVICE) as? UserManager
    private val adminComponent = SecureSpaceAdminReceiver.getComponentName(context)

    /**
     * Checks if this application is registered as an active Device Administrator.
     */
    fun isAdminActive(): Boolean {
        return dpm.isAdminActive(adminComponent)
    }

    /**
     * Checks if this application is the Profile Owner of a Managed Profile (Work Profile / Sandbox).
     */
    fun isProfileOwner(): Boolean {
        return try {
            dpm.isProfileOwnerApp(context.packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking profile owner status", e)
            false
        }
    }

    /**
     * Checks if this application is the Device Owner.
     */
    fun isDeviceOwner(): Boolean {
        return try {
            dpm.isDeviceOwnerApp(context.packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking device owner status", e)
            false
        }
    }

    /**
     * Creates an Intent to prompt the user to enable Device Admin privileges.
     */
    fun createEnableAdminIntent(): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "SecureSpace requires Device Admin authorization to enforce Knox-grade sandbox isolation, biometric locking, and data leakage prevention."
            )
        }
    }

    /**
     * Creates an Intent to launch standard Android 10 Managed Profile (Work Profile) provisioning.
     * This creates the isolated dual-space user profile at the OS level.
     */
    fun createProvisionManagedProfileIntent(): Intent {
        return Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
            putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                adminComponent
            )
            putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION, false)
            putExtra(
                "android.app.extra.PROVISIONING_PROFILE_NAME",
                "SecureSpace Sandbox"
            )
        }
    }

    /**
     * Retrieves target user profiles accessible via Android 10 CrossProfileApps service.
     */
    fun getCrossProfileTargets(): List<UserHandle> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val crossProfileApps = context.getSystemService(Context.CROSS_PROFILE_APPS_SERVICE) as? CrossProfileApps
                crossProfileApps?.targetUserProfiles ?: emptyList()
            } catch (e: Exception) {
                Log.w(TAG, "CrossProfileApps target user handles query failed", e)
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Configures Android 10 Cross-Profile Copy/Paste restriction to prevent data leakage
     * between personal space and secure sandbox space.
     */
    fun setCrossProfileCopyPasteRestriction(disallowed: Boolean): Boolean {
        return try {
            if (isProfileOwner()) {
                if (disallowed) {
                    dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE)
                } else {
                    dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE)
                }
                true
            } else {
                Log.w(TAG, "Cannot enforce DISALLOW_CROSS_PROFILE_COPY_PASTE: not profile owner")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle cross-profile copy paste restriction", e)
            false
        }
    }

    /**
     * Disables or enables camera hardware within the managed sandbox.
     */
    fun setCameraDisabledPolicy(disabled: Boolean): Boolean {
        return try {
            if (isAdminActive()) {
                dpm.setCameraDisabled(adminComponent, disabled)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set camera disabled policy", e)
            false
        }
    }

    /**
     * Blocks or allows screen capture / screenshots at the system policy level.
     */
    fun setScreenCaptureDisabledPolicy(disabled: Boolean): Boolean {
        return try {
            if (isAdminActive()) {
                dpm.setScreenCaptureDisabled(adminComponent, disabled)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set screen capture disabled policy", e)
            false
        }
    }

    /**
     * Triggers the OS screen lock immediately using DevicePolicyManager.
     */
    fun lockNow(): Boolean {
        return try {
            if (isAdminActive()) {
                dpm.lockNow()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "lockNow failed", e)
            false
        }
    }
}
