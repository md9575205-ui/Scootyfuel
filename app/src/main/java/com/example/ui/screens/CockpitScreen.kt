package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.VehicleInfo
import com.example.location.TrackingState
import com.example.ui.components.FuelMeterGauge
import com.example.ui.components.RouteCanvasView
import com.example.ui.components.SpeedometerGauge
import com.example.ui.theme.ActivaAmber
import com.example.ui.theme.ActivaEcoGreen
import com.example.ui.theme.ActivaNeonCyan
import com.example.ui.theme.ActivaOrange
import com.example.ui.theme.ActivaRedAlert
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CockpitScreen(
    trackingState: TrackingState,
    vehicleInfo: VehicleInfo,
    onStartTracking: (isSimulation: Boolean) -> Unit,
    onStopTracking: () -> Unit,
    onRefillClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasLocationPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            onStartTracking(false)
        }
    }

    var isSimulationModeSelected by remember { mutableStateOf(false) }
    var isHudModeActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header: Status Badges, GPS Lock & Simulation Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scooter Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (trackingState.isRecording) ActivaEcoGreen else TextMuted)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ACTIVA • WB 06 K5136",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            // GPS Status & Simulation indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (trackingState.hasGpsLock) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                    contentDescription = null,
                    tint = if (trackingState.hasGpsLock) ActivaNeonCyan else TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (trackingState.isSimulationMode) "SIMULATION" else if (trackingState.hasGpsLock) "GPS LIVE" else "ACQUIRING",
                    color = if (trackingState.isSimulationMode) ActivaAmber else if (trackingState.hasGpsLock) ActivaNeonCyan else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Simulation Mode Card (Ideal for testing or safety simulation)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CockpitCard)
                .border(1.dp, CockpitCardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Safety Simulation Mode",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isSimulationModeSelected) "Realistic Activa test ride (Eco-cruise & stops)" else "Use mobile GPS hardware sensors",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = isSimulationModeSelected,
                    onCheckedChange = {
                        isSimulationModeSelected = it
                        if (trackingState.isRecording) {
                            onStopTracking()
                            onStartTracking(it)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CockpitBackground,
                        checkedTrackColor = ActivaAmber,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Color(0xFF0F172A)
                    ),
                    modifier = Modifier.testTag("simulation_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Speedometer Gauge
        SpeedometerGauge(
            currentSpeedKmh = trackingState.currentSpeedKmh,
            topSpeedRecorded = trackingState.topSpeedKmh,
            isEcoZone = trackingState.isEcoZone,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Live Trip Metrics Strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CockpitCard)
                .border(1.dp, CockpitCardBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Trip Distance
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "TRIP DIST", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%.2f km", trackingState.currentDistanceKm),
                        color = ActivaNeonCyan,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Duration
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "DURATION", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    val mins = trackingState.durationSeconds / 60
                    val secs = trackingState.durationSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Top Speed
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "TOP SPEED", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${trackingState.topSpeedKmh.toInt()} km/h",
                        color = ActivaOrange,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Fuel Burned
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "FUEL USED", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = String.format("%.2f L", trackingState.estimatedFuelBurnedLitres),
                        color = ActivaEcoGreen,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Start / Stop Tracking Primary Button
        Button(
            onClick = {
                if (trackingState.isRecording) {
                    onStopTracking()
                } else {
                    if (isSimulationModeSelected) {
                        onStartTracking(true)
                    } else if (hasLocationPermission) {
                        onStartTracking(false)
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (trackingState.isRecording) ActivaRedAlert else ActivaEcoGreen,
                contentColor = CockpitBackground
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("ride_tracking_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (trackingState.isRecording) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (trackingState.isRecording) "FINISH RIDE & SAVE TRIP" else "START RIDE TRACKING",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Fuel Meter & DTE
        FuelMeterGauge(
            currentFuelLitres = vehicleInfo.currentEstimatedFuelLitres,
            tankCapacityLitres = vehicleInfo.fuelTankCapacityLitres,
            reserveThresholdLitres = vehicleInfo.reserveTankCapacityLitres,
            estimatedRangeKm = vehicleInfo.estimatedRangeKm,
            onRefillClick = onRefillClick
        )

        // Live Route Breadcrumb Map preview while recording
        AnimatedVisibility(visible = trackingState.points.size >= 2) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE ROUTE BREADCRUMBS",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Lat: ${String.format("%.4f", trackingState.latitude)}, Lng: ${String.format("%.4f", trackingState.longitude)}",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                RouteCanvasView(points = trackingState.points)
            }
        }
    }
}
