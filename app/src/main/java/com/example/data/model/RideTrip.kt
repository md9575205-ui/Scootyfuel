package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ride_trips")
data class RideTrip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMillis: Long = System.currentTimeMillis(),
    val endTimeMillis: Long = System.currentTimeMillis(),
    val distanceKm: Double,
    val durationSeconds: Long,
    val maxSpeedKmh: Double,
    val avgSpeedKmh: Double,
    val fuelConsumedLitres: Double,
    val tripCostRupees: Double,
    val ecoScorePercent: Int, // 0 - 100%
    val startLocationName: String = "Kolkata, WB",
    val endLocationName: String = "Destination",
    val pathPointsJson: String = "" // Serialized list of RoutePoint
)

data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double,
    val timestampMillis: Long,
    val altitudeMeters: Double = 0.0
)
