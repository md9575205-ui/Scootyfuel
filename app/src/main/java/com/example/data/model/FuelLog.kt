package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_logs")
data class FuelLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val odometerKm: Double,
    val litres: Double,
    val pricePerLitre: Double,
    val totalCost: Double,
    val isFullTank: Boolean = true,
    val stationName: String = "",
    val notes: String = "",
    val calculatedMileageKmPerL: Double? = null,
    val costPerKmRupees: Double? = null
)
