package com.fielddevice.healthmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fielddevice.healthmonitor.ui.navigation.FieldMonitorApp
import com.fielddevice.healthmonitor.ui.theme.FieldHealthMonitorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as FieldHealthMonitorApp).container

        // Deployment ("kiosk") mode simulates a dedicated learning tablet, so it also drives
        // the system chrome: system bars are hidden while it's active and restored on exit.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appContainer.kioskModeRepository.isDeploymentModeEnabled.collect { isEnabled ->
                    setImmersiveMode(isEnabled)
                }
            }
        }

        setContent {
            FieldHealthMonitorTheme {
                FieldMonitorApp(appContainer = appContainer)
            }
        }
    }

    private fun setImmersiveMode(enabled: Boolean) {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (enabled) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
