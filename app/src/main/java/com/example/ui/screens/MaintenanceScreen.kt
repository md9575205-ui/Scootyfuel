package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MaintenanceScheduleItem
import com.example.data.model.ServiceLog
import com.example.data.model.VehicleInfo
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
fun MaintenanceScreen(
    vehicleInfo: VehicleInfo,
    scheduleItems: List<MaintenanceScheduleItem>,
    serviceLogs: List<ServiceLog>,
    overallMetrics: OverallMetrics,
    onAddServiceClick: (category: String) -> Unit,
    onDeleteServiceLog: (ServiceLog) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Service Alerts & Checklist, 1: Full Riding Cost & Logs

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CockpitBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MAINTENANCE & SERVICE ALERTS",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Activa 2014 • WB 06 K5136 • ${vehicleInfo.currentOdometerKm.toInt()} KM",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (overallMetrics.activeDueServicesCount > 0) ActivaOrange.copy(alpha = 0.2f) else ActivaEcoGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (overallMetrics.activeDueServicesCount > 0) "${overallMetrics.activeDueServicesCount} DUE" else "HEALTHY",
                        color = if (overallMetrics.activeDueServicesCount > 0) ActivaOrange else ActivaEcoGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CockpitCard,
                contentColor = ActivaOrange,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ActivaOrange
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CockpitCardBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "SERVICE ALERTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "FULL RIDING COST & LOGS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // Scheduled Maintenance Alerts Checklist
                ServiceAlertsSubTab(
                    currentOdo = vehicleInfo.currentOdometerKm,
                    scheduleItems = scheduleItems,
                    onLogServiceForCategory = { onAddServiceClick(it) }
                )
            } else {
                // Full Riding Cost & History
                FullRidingCostSubTab(
                    vehicleInfo = vehicleInfo,
                    overallMetrics = overallMetrics,
                    serviceLogs = serviceLogs,
                    onDeleteServiceLog = onDeleteServiceLog
                )
            }
        }

        // FAB to add service
        FloatingActionButton(
            onClick = { onAddServiceClick("Engine Oil (10W-30)") },
            containerColor = ActivaOrange,
            contentColor = CockpitBackground,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_service")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Service")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Log Service", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ServiceAlertsSubTab(
    currentOdo: Double,
    scheduleItems: List<MaintenanceScheduleItem>,
    onLogServiceForCategory: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(scheduleItems, key = { it.id }) { item ->
            val remainingKm = item.getKmRemaining(currentOdo)
            val isOverdue = remainingKm <= 0
            val isDueSoon = remainingKm in 0.1..300.0

            val statusColor = when {
                isOverdue -> ActivaRedAlert
                isDueSoon -> ActivaOrange
                else -> ActivaEcoGreen
            }

            val progressFraction = ((currentOdo - item.lastDoneOdometerKm) / item.intervalKm)
                .toFloat().coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CockpitCard)
                    .border(1.dp, if (isOverdue) ActivaRedAlert.copy(alpha = 0.5f) else CockpitCardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.description,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = when {
                                    isOverdue -> "OVERDUE"
                                    isDueSoon -> "DUE SOON"
                                    else -> "HEALTHY"
                                },
                                color = statusColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = statusColor,
                        trackColor = Color(0xFF0F172A),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                isOverdue -> "Overdue by ${(-remainingKm).toInt()} km"
                                else -> "${remainingKm.toInt()} km remaining"
                            },
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Button(
                            onClick = { onLogServiceForCategory(item.title) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ActivaOrange.copy(alpha = 0.2f),
                                contentColor = ActivaOrange
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(text = "Log Done", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun FullRidingCostSubTab(
    vehicleInfo: VehicleInfo,
    overallMetrics: OverallMetrics,
    serviceLogs: List<ServiceLog>,
    onDeleteServiceLog: (ServiceLog) -> Unit
) {
    val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val fuelCostPerKm = vehicleInfo.petrolPricePerLitre / overallMetrics.averageMileageKmPerL
    val serviceCostPerKm = 0.28 // Standard Activa amortized parts & lubricant wear
    val totalCostPerKm = fuelCostPerKm + serviceCostPerKm

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Full Cost Breakdown Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CockpitCard)
                    .border(1.dp, CockpitCardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "FULL RIDING COST BREAKDOWN",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Calculated per kilometer ownership cost for Honda Activa 110",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big Total Cost Callout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "TOTAL COST / KM", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format("₹%.2f / KM", totalCostPerKm),
                                color = ActivaNeonCyan,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Monthly 500 km", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = String.format("₹%.0f / mo", totalCostPerKm * 500.0),
                                color = ActivaAmber,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Component Breakdown: Fuel vs Maintenance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Fuel Share", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = String.format("₹%.2f / km", fuelCostPerKm),
                                color = ActivaEcoGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "@ ${overallMetrics.averageMileageKmPerL.toInt()} km/L mileage",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Maintenance Share", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = String.format("₹%.2f / km", serviceCostPerKm),
                                color = ActivaOrange,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Oil, belts, tyres, brake shoes",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Service History Section
        item {
            Text(
                text = "PAST SERVICE RECORDS (${serviceLogs.size})",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (serviceLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No completed service logs yet.", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            items(serviceLogs, key = { it.id }) { log ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CockpitCard)
                        .border(1.dp, CockpitCardBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = log.serviceCategory,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${log.odometerKm.toInt()} KM • ${df.format(Date(log.dateMillis))}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format("₹%.2f", log.cost),
                                    color = ActivaOrange,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onDeleteServiceLog(log) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        if (log.partsReplaced.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Parts: ${log.partsReplaced}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        if (log.garageName.isNotBlank() || log.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = listOf(log.garageName, log.notes).filter { it.isNotBlank() }.joinToString(" • "),
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
