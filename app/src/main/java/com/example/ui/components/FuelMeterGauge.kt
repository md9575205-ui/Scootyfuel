package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ActivaAmber
import com.example.ui.theme.ActivaEcoGreen
import com.example.ui.theme.ActivaNeonCyan
import com.example.ui.theme.ActivaOrange
import com.example.ui.theme.ActivaRedAlert
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FuelMeterGauge(
    currentFuelLitres: Double,
    tankCapacityLitres: Double = 5.3,
    reserveThresholdLitres: Double = 1.3,
    estimatedRangeKm: Double,
    onRefillClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fuelFraction = (currentFuelLitres / tankCapacityLitres).toFloat().coerceIn(0f, 1f)
    val isReserve = currentFuelLitres <= reserveThresholdLitres

    val animatedFraction by animateFloatAsState(
        targetValue = fuelFraction,
        animationSpec = tween(500),
        label = "FuelFractionAnim"
    )

    val gaugeColor by animateColorAsState(
        targetValue = when {
            isReserve -> ActivaRedAlert
            animatedFraction < 0.35f -> ActivaAmber
            else -> ActivaNeonCyan
        },
        label = "FuelColorAnim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CockpitCard)
            .border(1.dp, if (isReserve) ActivaRedAlert.copy(alpha = 0.5f) else CockpitCardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("fuel_meter_gauge")
    ) {
        Column {
            // Header Row: Fuel Icon, Label, and Quick Refill CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(gaugeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isReserve) Icons.Default.Warning else Icons.Default.LocalGasStation,
                            contentDescription = "Fuel Icon",
                            tint = gaugeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI FUEL METER",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Activa 5.3L Tank • WB 06 K5136",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = onRefillClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isReserve) ActivaRedAlert else ActivaNeonCyan.copy(alpha = 0.2f),
                        contentColor = if (isReserve) Color.White else ActivaNeonCyan
                    ),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("quick_refill_button")
                ) {
                    Text(
                        text = if (isReserve) "Refill Now!" else "+ Refill",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Bar Indicator (E to F)
            Column(modifier = Modifier.fillMaxWidth()) {
                // Segmented track bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                ) {
                    // Reserve mark background at 24.5% (1.3 / 5.3)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(reserveThresholdLitres.toFloat() / tankCapacityLitres.toFloat())
                            .height(16.dp)
                            .background(ActivaRedAlert.copy(alpha = 0.2f))
                    )

                    // Fill Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedFraction)
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (isReserve) {
                                        listOf(ActivaRedAlert, ActivaOrange)
                                    } else {
                                        listOf(ActivaAmber, ActivaEcoGreen, ActivaNeonCyan)
                                    }
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Labels E, Reserve, 1/2, F
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "E", color = ActivaRedAlert, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "RES (1.3L)", color = if (isReserve) ActivaRedAlert else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "1/2", color = TextMuted, fontSize = 10.sp)
                    Text(text = "3/4", color = TextMuted, fontSize = 10.sp)
                    Text(text = "F (5.3L)", color = ActivaEcoGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats Row: Remaining Litres, Remaining Range, Tank %
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Remaining Litres
                Column {
                    Text(
                        text = "FUEL REMAINING",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format("%.2f L", currentFuelLitres),
                        color = gaugeColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // AI Distance To Empty
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "EST. RANGE (DTE)",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${estimatedRangeKm.toInt()} km",
                        color = if (isReserve) ActivaRedAlert else ActivaEcoGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Level percentage
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TANK LEVEL",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(fuelFraction * 100).toInt()}%",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Warning Banner if in reserve
            if (isReserve) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ActivaRedAlert.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = ActivaRedAlert,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reserve warning! Approx ${estimatedRangeKm.toInt()} km left. Refill petrol soon.",
                            color = ActivaRedAlert,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
