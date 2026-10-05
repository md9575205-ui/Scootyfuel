package com.example.data.model

data class VehicleInfo(
    val makeModel: String = "Honda Activa (2014)",
    val registrationNumber: String = "WB 06 K5136",
    val engineCapacityCc: String = "109.2 cc HET",
    val fuelTankCapacityLitres: Double = 5.3,
    val reserveTankCapacityLitres: Double = 1.3,
    val currentOdometerKm: Double = 28450.0,
    val currentEstimatedFuelLitres: Double = 3.6,
    val baseMileageKmPerL: Double = 46.5,
    val petrolPricePerLitre: Double = 103.50,
    val insuranceExpiryDateMillis: Long = System.currentTimeMillis() + 180L * 24 * 60 * 60 * 1000,
    val puccExpiryDateMillis: Long = System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000
) {
    val estimatedRangeKm: Double
        get() = currentEstimatedFuelLitres * baseMileageKmPerL

    val fuelPercentage: Float
        get() = (currentEstimatedFuelLitres / fuelTankCapacityLitres).toFloat().coerceIn(0f, 1f)

    val isLowFuel: Boolean
        get() = currentEstimatedFuelLitres <= reserveTankCapacityLitres
}
