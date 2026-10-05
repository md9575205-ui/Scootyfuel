package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ActivaEcoGreen
import com.example.ui.theme.ActivaNeonCyan
import com.example.ui.theme.ActivaOrange
import com.example.ui.theme.ActivaRedAlert
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.GaugeBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    currentSpeedKmh: Double,
    maxGaugeSpeed: Double = 100.0,
    topSpeedRecorded: Double = 0.0,
    isEcoZone: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = currentSpeedKmh.toFloat().coerceIn(0f, maxGaugeSpeed.toFloat()),
        animationSpec = tween(durationMillis = 350),
        label = "SpeedAnimation"
    )

    Box(
        modifier = modifier
            .size(280.dp)
            .testTag("speedometer_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 24.dp.toPx()
            val strokeWidth = 14.dp.toPx()

            // Sweep angles: 135 deg to 405 deg (270 deg sweep)
            val startAngle = 135f
            val totalSweep = 270f

            // 1. Background Arc Track
            drawArc(
                color = GaugeBackground,
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Eco Zone Arc Track (30 to 50 km/h = 30% to 50% of 100 km/h)
            val ecoStartAngle = startAngle + (30f / maxGaugeSpeed.toFloat()) * totalSweep
            val ecoSweepAngle = (20f / maxGaugeSpeed.toFloat()) * totalSweep
            drawArc(
                color = ActivaEcoGreen.copy(alpha = 0.35f),
                startAngle = ecoStartAngle,
                sweepAngle = ecoSweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth + 4.dp.toPx(), cap = StrokeCap.Butt)
            )

            // 3. Danger / High Speed Zone (75 to 100 km/h)
            val redStartAngle = startAngle + (75f / maxGaugeSpeed.toFloat()) * totalSweep
            val redSweepAngle = (25f / maxGaugeSpeed.toFloat()) * totalSweep
            drawArc(
                color = ActivaRedAlert.copy(alpha = 0.35f),
                startAngle = redStartAngle,
                sweepAngle = redSweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth + 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Active Progress Arc
            val activeFraction = (animatedSpeed / maxGaugeSpeed.toFloat()).coerceIn(0f, 1f)
            if (activeFraction > 0.01f) {
                val activeSweep = activeFraction * totalSweep
                val arcColor = when {
                    animatedSpeed in 30.0..50.0 -> ActivaEcoGreen
                    animatedSpeed > 75.0 -> ActivaRedAlert
                    else -> ActivaNeonCyan
                }

                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to ActivaNeonCyan,
                        0.5f to ActivaEcoGreen,
                        0.85f to ActivaOrange,
                        1.0f to ActivaRedAlert
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 5. Dial Ticks and Numbers
            val numMajorTicks = 10 // 0, 10, 20, 30 ... 100
            for (i in 0..numMajorTicks) {
                val fraction = i / numMajorTicks.toFloat()
                val angleDeg = startAngle + fraction * totalSweep
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val tickInnerRadius = radius - 16.dp.toPx()
                val tickOuterRadius = radius - 4.dp.toPx()

                val startTick = Offset(
                    (center.x + tickInnerRadius * cos(angleRad)).toFloat(),
                    (center.y + tickInnerRadius * sin(angleRad)).toFloat()
                )
                val endTick = Offset(
                    (center.x + tickOuterRadius * cos(angleRad)).toFloat(),
                    (center.y + tickOuterRadius * sin(angleRad)).toFloat()
                )

                val tickColor = when {
                    i in 3..5 -> ActivaEcoGreen
                    i >= 8 -> ActivaRedAlert
                    else -> Color(0xFF64748B)
                }

                drawLine(
                    color = tickColor,
                    start = startTick,
                    end = endTick,
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 6. Needle
            val needleAngleDeg = startAngle + (animatedSpeed / maxGaugeSpeed.toFloat()) * totalSweep
            val needleLength = radius - 8.dp.toPx()

            rotate(degrees = needleAngleDeg + 90f, pivot = center) {
                // Needle shadow / glow
                drawLine(
                    color = ActivaOrange.copy(alpha = 0.4f),
                    start = center,
                    end = Offset(center.x, center.y - needleLength),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Main Needle
                drawLine(
                    color = ActivaOrange,
                    start = center,
                    end = Offset(center.x, center.y - needleLength),
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Center Pin Cap
            drawCircle(
                color = CockpitCard,
                radius = 16.dp.toPx(),
                center = center
            )
            drawCircle(
                color = ActivaOrange,
                radius = 8.dp.toPx(),
                center = center
            )
        }

        // Digital Speed Display in Center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "${animatedSpeed.toInt()}",
                color = if (isEcoZone) ActivaEcoGreen else TextPrimary,
                fontSize = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-1).sp
            )
            Text(
                text = "KM / H",
                color = if (isEcoZone) ActivaEcoGreen else TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (isEcoZone) {
                Text(
                    text = "• ECO 52 KM/L •",
                    color = ActivaEcoGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            } else if (animatedSpeed > 70) {
                Text(
                    text = "• HIGH SPEED •",
                    color = ActivaRedAlert,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "ACTIVA 110",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
