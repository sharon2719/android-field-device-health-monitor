package com.fielddevice.healthmonitor.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fielddevice.healthmonitor.di.AppContainer
import com.fielddevice.healthmonitor.ui.ViewModelFactory
import com.fielddevice.healthmonitor.ui.battery.BatteryScreen
import com.fielddevice.healthmonitor.ui.battery.BatteryViewModel
import com.fielddevice.healthmonitor.ui.dashboard.DeviceInfoScreen
import com.fielddevice.healthmonitor.ui.dashboard.DeviceInfoViewModel
import com.fielddevice.healthmonitor.ui.diagnostics.DiagnosticsScreen
import com.fielddevice.healthmonitor.ui.diagnostics.DiagnosticsViewModel
import com.fielddevice.healthmonitor.ui.kiosk.KioskScreen
import com.fielddevice.healthmonitor.ui.kiosk.KioskViewModel
import com.fielddevice.healthmonitor.ui.learning.LearningRecordsScreen
import com.fielddevice.healthmonitor.ui.learning.LearningRecordsViewModel
import com.fielddevice.healthmonitor.ui.network.NetworkScreen
import com.fielddevice.healthmonitor.ui.network.NetworkViewModel
import com.fielddevice.healthmonitor.ui.ota.OtaScreen
import com.fielddevice.healthmonitor.ui.ota.OtaViewModel
import com.fielddevice.healthmonitor.ui.report.ReportScreen
import com.fielddevice.healthmonitor.ui.report.ReportViewModel
import com.fielddevice.healthmonitor.ui.storage.StorageScreen
import com.fielddevice.healthmonitor.ui.storage.StorageViewModel

/**
 * Root layout: a permanent side navigation drawer (appropriate for this app's tablet-first
 * target form factor) plus a top bar showing the active destination's title, wrapping a
 * standard Jetpack Navigation [NavHost] for the nine feature screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldMonitorApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    val factory = remember(appContainer) { ViewModelFactory(appContainer) }
    val kioskViewModel: KioskViewModel = viewModel(factory = factory)
    val isDeploymentModeActive by kioskViewModel.isDeploymentModeEnabled.collectAsState()

    if (isDeploymentModeActive) {
        // In deployment (kiosk) mode we hide the navigation chrome entirely and pin the
        // learner-facing records screen full-screen, simulating a dedicated learning tablet.
        Scaffold { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                LearningRecordsScreen(
                    viewModel = viewModel(factory = factory),
                    contentPadding = padding
                )
                // Real lock-task mode requires device-owner privileges (and typically a PIN)
                // to exit; this affordance stands in for that "supervised exit" step so the
                // prototype remains usable without needing real MDM provisioning.
                IconButton(
                    onClick = { kioskViewModel.setDeploymentModeEnabled(false) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(padding)
                        .padding(8.dp)
                ) {
                    Icon(Icons.Filled.LockOpen, contentDescription = "Exit deployment mode")
                }
            }
        }
        return
    }

    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(modifier = Modifier.width(240.dp)) {
                Text(
                    text = "Field Monitor",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                NavDrawerContents(navController)
            }
        }
    ) {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val currentScreen = Screen.all.firstOrNull { it.route == currentRoute } ?: Screen.Dashboard

        Scaffold(
            topBar = {
                TopAppBar(title = { Text(currentScreen.label) })
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Dashboard.route) {
                    val vm: DeviceInfoViewModel = viewModel(factory = factory)
                    DeviceInfoScreen(viewModel = vm, contentPadding = padding)
                }
                composable(Screen.Battery.route) {
                    val vm: BatteryViewModel = viewModel(factory = factory)
                    BatteryScreen(viewModel = vm, contentPadding = padding)
                }
                composable(Screen.Storage.route) {
                    val vm: StorageViewModel = viewModel(factory = factory)
                    StorageScreen(viewModel = vm, contentPadding = padding)
                }
                composable(Screen.Network.route) {
                    val vm: NetworkViewModel = viewModel(factory = factory)
                    NetworkScreen(viewModel = vm, contentPadding = padding)
                }
                composable(Screen.LearningRecords.route) {
                    val vm: LearningRecordsViewModel = viewModel(factory = factory)
                    LearningRecordsScreen(viewModel = vm, contentPadding = padding)
                }
                composable(Screen.Diagnostics.route) {
                    val vm: DiagnosticsViewModel = viewModel(factory = factory)
                    DiagnosticsScreen(viewModel = vm, contentPadding = padding)
                }
                composable(Screen.Report.route) {
                    val vm: ReportViewModel = viewModel(factory = factory)
                    ReportScreen(viewModel = vm, contentPadding = padding)
                }
                composable(Screen.Kiosk.route) {
                    val vm: KioskViewModel = viewModel(factory = factory)
                    KioskScreen(viewModel = vm, contentPadding = padding)
                }
                composable(Screen.OtaUpdate.route) {
                    val vm: OtaViewModel = viewModel(factory = factory)
                    OtaScreen(viewModel = vm, contentPadding = padding)
                }
            }
        }
    }
}

@Composable
private fun NavDrawerContents(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LazyColumn {
        items(Screen.all) { screen ->
            NavigationDrawerItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.width(240.dp)
            )
        }
    }
}
