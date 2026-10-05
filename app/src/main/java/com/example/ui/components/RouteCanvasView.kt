package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoutePoint
import com.example.ui.theme.ActivaEcoGreen
import com.example.ui.theme.ActivaNeonCyan
import com.example.ui.theme.ActivaOrange
import com.example.ui.theme.ActivaRedAlert
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.TextMuted
import org.json.JSONArray

@Composable
fun RouteCanvasView(
    points: List<RoutePoint> = emptyList(),
    pointsJson: String = "",
    modifier: Modifier = Modifier
) {
    // Parse points from JSON if points list is empty
    val parsedPoints = remember(points, pointsJson) {
        if (points.isNotEmpty()) {
            points
        } else if (pointsJson.isNotBlank()) {
            try {
                val list = mutableListOf<RoutePoint>()
                val jsonArr = JSONArray(pointsJson)
                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    list.add(
                        RoutePoint(
                            latitude = obj.optDouble("lat", 0.0),
                            longitude = obj.optDouble("lng", 0.0),
                            speedKmh = obj.optDouble("spd", 0.0),
                            timestampMillis = 0L
                        )
                    )
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF070B16))
            .border(1.dp, CockpitCardBorder, RoundedCornerShape(16.dp))
            .testTag("route_canvas_view")
    ) {
        if (parsedPoints.size < 2) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Awaiting route breadcrumbs...",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                val minLat = parsedPoints.minOf { it.latitude }
                val maxLat = parsedPoints.maxOf { it.latitude }
                val minLng = parsedPoints.minOf { it.longitude }
                val maxLng = parsedPoints.maxOf { it.longitude }

                val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
                val lngSpan = (maxLng - minLng).coerceAtLeast(0.0001)

                fun toOffset(lat: Double, lng: Double): Offset {
                    val x = (((lng - minLng) / lngSpan) * size.width).toFloat()
                    // Invert Y because latitude increases northward (up)
                    val y = ((1.0 - ((lat - minLat) / latSpan)) * size.height).toFloat()
                    return Offset(
                        x.coerceIn(0f, size.width),
                        y.coerceIn(0f, size.height)
                    )
                }

                // Draw background grid lines
                val gridColor = Color(0x1A00E5FF)
                for (gx in 0..4) {
                    val xPos = size.width * (gx / 4f)
                    drawLine(
                        color = gridColor,
                        start = Offset(xPos, 0f),
                        end = Offset(xPos, size.height),
                        strokeWidth = 1f
                    )
                }
                for (gy in 0..3) {
                    val yPos = size.height * (gy / 3f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, yPos),
                        end = Offset(size.width, yPos),
                        strokeWidth = 1f
                    )
                }

                // Draw connecting path segments colored by speed
                for (i in 0 until parsedPoints.size - 1) {
                    val p1 = parsedPoints[i]
                    val p2 = parsedPoints[i + 1]
                    val o1 = toOffset(p1.latitude, p1.longitude)
                    val o2 = toOffset(p2.latitude, p2.longitude)

                    val segmentColor = when {
                        p2.speedKmh in 30.0..50.0 -> ActivaEcoGreen
                        p2.speedKmh > 55.0 -> ActivaOrange
                        else -> ActivaNeonCyan
                    }

                    // Glow line
                    drawLine(
                        color = segmentColor.copy(alpha = 0.35f),
                        start = o1,
                        end = o2,
                        strokeWidth = 8.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Core line
                    drawLine(
                        color = segmentColor,
                        start = o1,
                        end = o2,
                        strokeWidth = 3.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Start Marker (Green Circle)
                val startOffset = toOffset(parsedPoints.first().latitude, parsedPoints.first().longitude)
                drawCircle(color = ActivaEcoGreen.copy(alpha = 0.4f), radius = 10.dp.toPx(), center = startOffset)
                drawCircle(color = ActivaEcoGreen, radius = 5.dp.toPx(), center = startOffset)

                // End Marker (Orange / Red Puck)
                val endOffset = toOffset(parsedPoints.last().latitude, parsedPoints.last().longitude)
                drawCircle(color = ActivaOrange.copy(alpha = 0.4f), radius = 12.dp.toPx(), center = endOffset)
                drawCircle(color = ActivaOrange, radius = 6.dp.toPx(), center = endOffset)
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = endOffset)
            }

            // Legend Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color(0x99000000), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "GPS Trace • ${parsedPoints.size} pts",
                    color = ActivaNeonCyan,
                    fontSize = 9.sp
                )
            }
        }
    }
}
