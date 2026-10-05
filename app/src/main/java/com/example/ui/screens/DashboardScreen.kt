package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FuelLog
import com.example.data.model.RideTrip
import com.example.data.model.ServiceLog
import com.example.data.model.VehicleInfo
import com.example.ui.components.VehicleHeaderCard
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
import com.example.viewmodel.OverallMetrics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    vehicleInfo: VehicleInfo,
    overallMetrics: OverallMetrics,
    fuelLogs: List<FuelLog>,
    serviceLogs: List<ServiceLog>,
    rideTrips: List<RideTrip>,
    onNavigateToCockpit: () -> Unit,
    onOpenAddFuel: () -> Unit,
    onOpenAddService: () -> Unit,
    onOpenEditVehicle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CockpitBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Vehicle Profile Banner with Hero Image & License Plate
        VehicleHeaderCard(
            vehicle = vehicleInfo,
            onEditClick = onOpenEditVehicle
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Refill Alert Banner if low
        if (overallMetrics.isRefillNeededSoon) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ActivaRedAlert.copy(alpha = 0.15f))
                    .border(1.dp, ActivaRedAlert.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable { onOpenAddFuel() }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ActivaRedAlert,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Petrol Refilling Alert!",
                            color = ActivaRedAlert,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Estimated range is under ${overallMetrics.remainingDistanceToEmptyKm.toInt()} km. Tap to log refill.",
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Quick Launch Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Live Cockpit CTA
            Button(
                onClick = onNavigateToCockpit,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActivaNeonCyan,
                    contentColor = CockpitBackground
                ),
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .testTag("launch_cockpit_button")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "LIVE COCKPIT", fontWeight = FontWeight.Black, fontSize = 12.sp)
            }

            // Refill Fuel CTA
            Button(
                onClick = onOpenAddFuel,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CockpitCard,
                    contentColor = ActivaAmber
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("dashboard_add_fuel_button")
            ) {
                Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "+ REFILL", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            // Service CTA
            Button(
                onClick = onOpenAddService,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CockpitCard,
                    contentColor = ActivaOrange
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("dashboard_add_service_button")
            ) {
                Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "+ SERVICE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Performance Metrics Title
        Text(
            text = "PERFORMANCE METRICS & COSTS",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2x2 Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "TOTAL RIDING COST",
                value = String.format("₹%.2f", overallMetrics.overallRidingCostPerKm),
                subtitle = "per kilometer (fuel + service)",
                icon = Icons.Default.CurrencyRupee,
                iconColor = ActivaNeonCyan,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "AVG MILEAGE",
                value = String.format("%.1f", overallMetrics.averageMileageKmPerL),
                subtitle = "km / Litre (Activa 110)",
                icon = Icons.Default.Speed,
                iconColor = ActivaEcoGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "EST. RANGE (DTE)",
                value = "${overallMetrics.remainingDistanceToEmptyKm.toInt()} km",
                subtitle = "${String.format("%.1f", vehicleInfo.currentEstimatedFuelLitres)}L fuel in tank",
                icon = Icons.Default.LocalGasStation,
                iconColor = ActivaAmber,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "SERVICE STATUS",
                value = if (overallMetrics.activeDueServicesCount > 0) "${overallMetrics.activeDueServicesCount} Due" else "All Good",
                subtitle = "Scheduled maintenance",
                icon = Icons.Default.Build,
                iconColor = if (overallMetrics.activeDueServicesCount > 0) ActivaOrange else ActivaEcoGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fuel Expenditure Summary
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CockpitCard)
                .border(1.dp, CockpitCardBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "OWNERSHIP EXPENDITURE",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "WB 06 K5136",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Total Fuel Spent", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = String.format("₹%.2f", overallMetrics.totalFuelSpentRupees),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${String.format("%.1f", overallMetrics.totalFuelLitres)} L logged",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Total Service Cost", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = String.format("₹%.2f", overallMetrics.totalServiceCostRupees),
                            color = ActivaOrange,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${serviceLogs.size} services done",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Ride Trip Preview
        val latestTrip = rideTrips.firstOrNull()
        if (latestTrip != null) {
            Text(
                text = "LATEST RIDE TRACKED",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CockpitCard)
                    .border(1.dp, CockpitCardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${latestTrip.startLocationName} → ${latestTrip.endLocationName}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val df = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                            Text(
                                text = df.format(Date(latestTrip.startTimeMillis)),
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ActivaEcoGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${latestTrip.ecoScorePercent}% ECO",
                                color = ActivaEcoGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${String.format("%.2f", latestTrip.distanceKm)} km",
                            color = ActivaNeonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Avg: ${latestTrip.avgSpeedKmh.toInt()} km/h",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Fuel: ${String.format("%.2f", latestTrip.fuelConsumedLitres)}L (₹${latestTrip.tripCostRupees.toInt()})",
                            color = ActivaAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CockpitCard)
            .border(1.dp, CockpitCardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
