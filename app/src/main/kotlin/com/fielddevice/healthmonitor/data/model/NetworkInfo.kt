package com.fielddevice.healthmonitor.data.model

/**
 * Connectivity snapshot built from [android.net.ConnectivityManager]. Deployments this app
 * targets (rural schools, mobile clinics) routinely operate with no connectivity at all,
 * so "offline" is treated as a normal, first-class state rather than an error condition.
 */
data class NetworkInfo(
    val isConnected: Boolean,
    val connectionType: ConnectionType,
    val isMetered: Boolean,
    val isValidated: Boolean
) {
    /** True when the device has no usable internet path at all — expected, not exceptional. */
    val isOfflineMode: Boolean get() = !isConnected
}

enum class ConnectionType {
    WIFI,
    CELLULAR,
    ETHERNET,
    VPN,
    NONE,
    UNKNOWN
}
