package com.example.dpc

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Enterprise Device Admin Receiver for SecureSpace.
 *
 * Serves as the system entry point for Device Policy Controller (DPC) operations,
 * Managed Profile provisioning lifecycle, and hardware security callback events
 * under Android 10 (API Level 29).
 */
class SecureSpaceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "SecureSpaceDPC"

        /**
         * Returns the ComponentName identifying this receiver.
         */
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, SecureSpaceAdminReceiver::class.java)
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "SecureSpace Device Admin successfully enabled.")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w(TAG, "SecureSpace Device Admin has been disabled.")
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Log.i(TAG, "Android 10 Managed Profile provisioning complete! Configuring initial security baseline.")
        // Here the DPC can initialize baseline policies for the new work profile
    }

    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)
        Log.w(TAG, "Device challenge authentication failed event received by DPC.")
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent) {
        super.onPasswordSucceeded(context, intent)
        Log.i(TAG, "Device challenge authentication succeeded.")
    }
}
