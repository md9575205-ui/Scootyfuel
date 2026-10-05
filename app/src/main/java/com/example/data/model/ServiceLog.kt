package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_logs")
data class ServiceLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val odometerKm: Double,
    val serviceCategory: String, // "Engine Oil (10W-30)", "Gear Oil", "Air Filter", "Spark Plug", "Brake Shoes", "Drive Belt", "General Service"
    val cost: Double,
    val garageName: String = "",
    val partsReplaced: String = "",
    val notes: String = ""
)

data class MaintenanceScheduleItem(
    val id: String,
    val title: String,
    val description: String,
    val intervalKm: Double,
    val intervalMonths: Int,
    val isCritical: Boolean = false,
    val lastDoneOdometerKm: Double = 0.0,
    val lastDoneDateMillis: Long = 0L
) {
    fun getKmRemaining(currentOdometerKm: Double): Double {
        val nextDueKm = lastDoneOdometerKm + intervalKm
        return (nextDueKm - currentOdometerKm).coerceAtLeast(-9999.0)
    }

    fun isDue(currentOdometerKm: Double): Boolean {
        return getKmRemaining(currentOdometerKm) <= 0
    }

    fun isDueSoon(currentOdometerKm: Double): Boolean {
        val remaining = getKmRemaining(currentOdometerKm)
        return remaining in 0.1..300.0
    }
}
