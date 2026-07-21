package com.fielddevice.healthmonitor.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.fielddevice.healthmonitor.data.model.ConnectionType
import com.fielddevice.healthmonitor.data.model.NetworkInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

interface NetworkRepository {
    /** Emits the current connectivity state immediately, then on every network change. */
    fun observeNetworkInfo(): Flow<NetworkInfo>
}

class NetworkRepositoryImpl(
    private val context: Context
) : NetworkRepository {

    private val connectivityManager: ConnectivityManager
        get() = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observeNetworkInfo(): Flow<NetworkInfo> = callbackFlow {
        fun pushCurrentState() {
            trySend(currentNetworkInfo())
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = pushCurrentState()
            override fun onLost(network: Network) = pushCurrentState()
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) =
                pushCurrentState()
            override fun onUnavailable() = pushCurrentState()
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        // Emit an initial reading right away rather than waiting for the first callback,
        // so the UI never shows a blank/loading connectivity state on screen entry.
        pushCurrentState()

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    private fun currentNetworkInfo(): NetworkInfo {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }

        if (capabilities == null) {
            return NetworkInfo(
                isConnected = false,
                connectionType = ConnectionType.NONE,
                isMetered = false,
                isValidated = false
            )
        }

        val connectionType = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> ConnectionType.VPN
            else -> ConnectionType.UNKNOWN
        }

        val hasInternetCapability = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        return NetworkInfo(
            isConnected = hasInternetCapability,
            connectionType = connectionType,
            isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED),
            isValidated = isValidated
        )
    }
}
