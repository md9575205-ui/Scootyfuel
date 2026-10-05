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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FuelLog
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
fun FuelScreen(
    vehicleInfo: VehicleInfo,
    fuelLogs: List<FuelLog>,
    overallMetrics: OverallMetrics,
    onAddFuelClick: () -> Unit,
    onDeleteFuelLog: (FuelLog) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Refill Logs & Mileage, 1: Petrol Calculator & Reminder

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
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FUEL CONSUMPTION & MILEAGE",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Honda Activa 2014 • WB 06 K5136 (5.3L Tank)",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ActivaNeonCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "₹${vehicleInfo.petrolPricePerLitre}/L",
                        color = ActivaNeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub Tab Row
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = CockpitCard,
                contentColor = ActivaNeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                        color = ActivaNeonCyan
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CockpitCardBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = {
                        Text(
                            text = "MILEAGE REPORT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = {
                        Text(
                            text = "CALCULATOR & REMINDER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedSubTab == 0) {
                // Mileage Report & Refills List
                MileageReportSubTab(
                    vehicleInfo = vehicleInfo,
                    fuelLogs = fuelLogs,
                    overallMetrics = overallMetrics,
                    onDeleteFuelLog = onDeleteFuelLog,
                    onAddFuelClick = onAddFuelClick
                )
            } else {
                // Petrol Refilling Calculator & Reminder Sub Tab
                FuelCalculatorSubTab(
                    vehicleInfo = vehicleInfo,
                    overallMetrics = overallMetrics,
                    onAddFuelClick = onAddFuelClick
                )
            }
        }

        // FAB to add refill
        FloatingActionButton(
            onClick = onAddFuelClick,
            containerColor = ActivaNeonCyan,
            contentColor = CockpitBackground,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_fuel")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Refill")
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Log Petrol", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun MileageReportSubTab(
    vehicleInfo: VehicleInfo,
    fuelLogs: List<FuelLog>,
    overallMetrics: OverallMetrics,
    onDeleteFuelLog: (FuelLog) -> Unit,
    onAddFuelClick: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Summary Metrics Header Card
        item {
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
                            Text(text = "AVERAGE MILEAGE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${String.format("%.1f", overallMetrics.averageMileageKmPerL)} km/L",
                                color = ActivaEcoGreen,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "RUNNING FUEL COST", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            val fuelCostPerKm = vehicleInfo.petrolPricePerLitre / overallMetrics.averageMileageKmPerL
                            Text(
                                text = String.format("₹%.2f / km", fuelCostPerKm),
                                color = ActivaNeonCyan,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Fuel: ${String.format("%.1f", overallMetrics.totalFuelLitres)} L",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Total Spent: ₹${String.format("%.2f", overallMetrics.totalFuelSpentRupees)}",
                            color = ActivaAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REFILL HISTORY (${fuelLogs.size})",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Full-tank calculations",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        if (fuelLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No fuel refills recorded yet.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(fuelLogs, key = { it.id }) { log ->
                FuelLogItemCard(log = log, onDelete = { onDeleteFuelLog(log) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun FuelLogItemCard(
    log: FuelLog,
    onDelete: () -> Unit
) {
    val df = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CockpitCard)
            .border(1.dp, CockpitCardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("fuel_log_item_${log.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${log.odometerKm.toInt()} KM",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        if (log.isFullTank) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ActivaEcoGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "FULL TANK",
                                    color = ActivaEcoGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Text(
                        text = df.format(Date(log.dateMillis)),
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Log",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metrics row: Litres, Price, Cost, Calculated Mileage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Quantity", color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = "${String.format("%.2f", log.litres)} L",
                        color = ActivaNeonCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column {
                    Text(text = "Rate", color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = "₹${log.pricePerLitre}/L",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Column {
                    Text(text = "Total Paid", color = TextMuted, fontSize = 10.sp)
                    Text(
                        text = "₹${String.format("%.2f", log.totalCost)}",
                        color = ActivaAmber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Mileage", color = TextMuted, fontSize = 10.sp)
                    if (log.calculatedMileageKmPerL != null) {
                        Text(
                            text = "${String.format("%.1f", log.calculatedMileageKmPerL)} km/l",
                            color = ActivaEcoGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Text(
                            text = "First fill",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (log.stationName.isNotBlank() || log.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = listOf(log.stationName, log.notes).filter { it.isNotBlank() }.joinToString(" • "),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun FuelCalculatorSubTab(
    vehicleInfo: VehicleInfo,
    overallMetrics: OverallMetrics,
    onAddFuelClick: () -> Unit
) {
    var plannedDistanceKmText by remember { mutableStateOf("45") }
    var plannedBudgetText by remember { mutableStateOf("250") }

    val avgMileage = overallMetrics.averageMileageKmPerL
    val petrolRate = vehicleInfo.petrolPricePerLitre

    val calcDistKm = plannedDistanceKmText.toDoubleOrNull() ?: 0.0
    val neededLitres = if (avgMileage > 0) calcDistKm / avgMileage else 0.0
    val neededCost = neededLitres * petrolRate

    val calcBudget = plannedBudgetText.toDoubleOrNull() ?: 0.0
    val budgetLitres = if (petrolRate > 0) calcBudget / petrolRate else 0.0
    val budgetRangeKm = budgetLitres * avgMileage

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Refill Petrol Reminder Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (vehicleInfo.isLowFuel) ActivaRedAlert.copy(alpha = 0.15f) else CockpitCard)
                    .border(
                        1.dp,
                        if (vehicleInfo.isLowFuel) ActivaRedAlert.copy(alpha = 0.5f) else CockpitCardBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (vehicleInfo.isLowFuel) ActivaRedAlert else ActivaAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REFILLING PETROL REMINDER",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (vehicleInfo.isLowFuel) ActivaRedAlert else ActivaEcoGreen)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (vehicleInfo.isLowFuel) "RESERVE NOW" else "TANK OK",
                                color = CockpitBackground,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Current fuel remaining: ${String.format("%.2f", vehicleInfo.currentEstimatedFuelLitres)} Litres in Activa tank.\n" +
                                "Estimated Distance To Empty (DTE): ~${overallMetrics.remainingDistanceToEmptyKm.toInt()} km.\n" +
                                if (vehicleInfo.isLowFuel) "Warning: Fuel is in the 1.3L reserve zone! Refuel immediately."
                                else "Tip: For maximum engine longevity and 50+ km/l mileage, refill when tank drops to 1/4 level.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onAddFuelClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActivaNeonCyan,
                            contentColor = CockpitBackground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Log Petrol Refill Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Calculator 1: Distance to Petrol & Cost
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = ActivaNeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TRIP PETROL & COST ESTIMATOR",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Enter planned riding distance to calculate fuel required and cost for WB 06 K5136:",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = plannedDistanceKmText,
                        onValueChange = { plannedDistanceKmText = it },
                        label = { Text("Planned Ride Distance (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActivaNeonCyan,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Petrol Required", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = String.format("%.2f Litres", neededLitres),
                                color = ActivaNeonCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Estimated Fuel Cost", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = String.format("₹%.2f", neededCost),
                                color = ActivaAmber,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Calculator 2: Budget to Distance Range
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = ActivaOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BUDGET TO DISTANCE RANGE",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Enter how much money you want to fill to see how far you can ride:",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = plannedBudgetText,
                        onValueChange = { plannedBudgetText = it },
                        label = { Text("Budget Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActivaOrange,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Petrol You Get", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = String.format("%.2f Litres", budgetLitres),
                                color = ActivaOrange,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Expected Range", color = TextMuted, fontSize = 10.sp)
                            Text(
                                text = String.format("%.1f km", budgetRangeKm),
                                color = ActivaEcoGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
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
