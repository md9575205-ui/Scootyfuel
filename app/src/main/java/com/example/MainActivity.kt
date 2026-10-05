package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddFuelDialog
import com.example.ui.components.AddServiceDialog
import com.example.ui.components.EditVehicleDialog
import com.example.ui.screens.CockpitScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FuelScreen
import com.example.ui.screens.MaintenanceScreen
import com.example.ui.screens.RouteHistoryScreen
import com.example.ui.theme.ActivaEcoGreen
import com.example.ui.theme.ActivaNeonCyan
import com.example.ui.theme.ActivaOrange
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.ActivaViewModel

enum class ActivaNavTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Overview", Icons.Default.Dashboard),
    COCKPIT("Cockpit", Icons.Default.Speed),
    FUEL("Fuel & Mileage", Icons.Default.LocalGasStation),
    MAINTENANCE("Service", Icons.Default.Build),
    ROUTES("Routes", Icons.Default.Map)
}

class MainActivity : ComponentActivity() {
    private val viewModel: ActivaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: ActivaViewModel) {
    var currentTab by remember { mutableStateOf(ActivaNavTab.DASHBOARD) }

    val vehicleInfo by viewModel.vehicleInfo.collectAsStateWithLifecycle()
    val trackingState by viewModel.trackingState.collectAsStateWithLifecycle()
    val fuelLogs by viewModel.fuelLogs.collectAsStateWithLifecycle()
    val serviceLogs by viewModel.serviceLogs.collectAsStateWithLifecycle()
    val rideTrips by viewModel.rideTrips.collectAsStateWithLifecycle()
    val overallMetrics by viewModel.overallMetrics.collectAsStateWithLifecycle()
    val selectedTrip by viewModel.selectedTrip.collectAsStateWithLifecycle()

    var showAddFuelDialog by remember { mutableStateOf(false) }
    var showAddServiceDialog by remember { mutableStateOf(false) }
    var serviceDialogCategory by remember { mutableStateOf("Engine Oil (10W-30)") }
    var showEditVehicleDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CockpitBackground,
        bottomBar = {
            NavigationBar(
                containerColor = CockpitCard,
                contentColor = ActivaNeonCyan,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                ActivaNavTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) ActivaNeonCyan else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ActivaNeonCyan else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ActivaNeonCyan,
                            selectedTextColor = ActivaNeonCyan,
                            indicatorColor = ActivaNeonCyan.copy(alpha = 0.15f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ActivaNavTab.DASHBOARD -> DashboardScreen(
                    vehicleInfo = vehicleInfo,
                    overallMetrics = overallMetrics,
                    fuelLogs = fuelLogs,
                    serviceLogs = serviceLogs,
                    rideTrips = rideTrips,
                    onNavigateToCockpit = { currentTab = ActivaNavTab.COCKPIT },
                    onOpenAddFuel = { showAddFuelDialog = true },
                    onOpenAddService = {
                        serviceDialogCategory = "Engine Oil (10W-30)"
                        showAddServiceDialog = true
                    },
                    onOpenEditVehicle = { showEditVehicleDialog = true }
                )

                ActivaNavTab.COCKPIT -> CockpitScreen(
                    trackingState = trackingState,
                    vehicleInfo = vehicleInfo,
                    onStartTracking = { isSim -> viewModel.startRide(isSim) },
                    onStopTracking = { viewModel.stopRideAndSave() },
                    onRefillClick = { showAddFuelDialog = true }
                )

                ActivaNavTab.FUEL -> FuelScreen(
                    vehicleInfo = vehicleInfo,
                    fuelLogs = fuelLogs,
                    overallMetrics = overallMetrics,
                    onAddFuelClick = { showAddFuelDialog = true },
                    onDeleteFuelLog = { viewModel.deleteFuelLog(it) }
                )

                ActivaNavTab.MAINTENANCE -> MaintenanceScreen(
                    vehicleInfo = vehicleInfo,
                    scheduleItems = viewModel.getMaintenanceSchedule(),
                    serviceLogs = serviceLogs,
                    overallMetrics = overallMetrics,
                    onAddServiceClick = { category ->
                        serviceDialogCategory = category
                        showAddServiceDialog = true
                    },
                    onDeleteServiceLog = { viewModel.deleteServiceLog(it) }
                )

                ActivaNavTab.ROUTES -> RouteHistoryScreen(
                    rideTrips = rideTrips,
                    selectedTrip = selectedTrip,
                    onSelectTrip = { viewModel.selectTrip(it) },
                    onDeleteTrip = { viewModel.deleteTrip(it) },
                    onStartRideClick = { currentTab = ActivaNavTab.COCKPIT }
                )
            }
        }
    }

    // Add Fuel Modal Dialog
    if (showAddFuelDialog) {
        AddFuelDialog(
            initialOdometerKm = vehicleInfo.currentOdometerKm,
            defaultPricePerLitre = vehicleInfo.petrolPricePerLitre,
            onDismiss = { showAddFuelDialog = false },
            onConfirm = { odo, litres, price, total, isFullTank, station, notes ->
                viewModel.addFuelRefill(odo, litres, price, total, isFullTank, station, notes)
                showAddFuelDialog = false
            }
        )
    }

    // Add Service Modal Dialog
    if (showAddServiceDialog) {
        AddServiceDialog(
            initialOdometerKm = vehicleInfo.currentOdometerKm,
            preselectedCategory = serviceDialogCategory,
            onDismiss = { showAddServiceDialog = false },
            onConfirm = { odo, cat, cost, garage, parts, notes ->
                viewModel.addServiceLog(odo, cat, cost, garage, parts, notes)
                showAddServiceDialog = false
            }
        )
    }

    // Edit Vehicle Profile Dialog
    if (showEditVehicleDialog) {
        EditVehicleDialog(
            vehicle = vehicleInfo,
            onDismiss = { showEditVehicleDialog = false },
            onConfirm = { updated ->
                viewModel.updateVehicleInfo(updated)
                showEditVehicleDialog = false
            }
        )
    }
}
