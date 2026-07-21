package com.fielddevice.healthmonitor.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists whether "deployment mode" (this app's kiosk-mode prototype) is enabled, so the
 * setting survives process death the same way a real MDM-managed lock-task mode would.
 * Real kiosk mode additionally needs `DevicePolicyManager` device-owner privileges to pin
 * the app via `startLockTask()`; that requires provisioning outside a normal app install; that
 * step is intentionally out of scope for this prototype (see README's Future Improvements).
 */
interface KioskModeRepository {
    val isDeploymentModeEnabled: StateFlow<Boolean>
    suspend fun setDeploymentModeEnabled(enabled: Boolean)
}

class KioskModeRepositoryImpl(
    context: Context
) : KioskModeRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isDeploymentModeEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_DEPLOYMENT_MODE_ENABLED, false)
    )
    override val isDeploymentModeEnabled: StateFlow<Boolean> = _isDeploymentModeEnabled.asStateFlow()

    override suspend fun setDeploymentModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEPLOYMENT_MODE_ENABLED, enabled).apply()
        _isDeploymentModeEnabled.value = enabled
    }

    companion object {
        private const val PREFS_NAME = "deployment_settings"
        private const val KEY_DEPLOYMENT_MODE_ENABLED = "deployment_mode_enabled"
    }
}
