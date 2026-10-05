package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FuelLog
import com.example.data.model.MaintenanceScheduleItem
import com.example.data.model.RideTrip
import com.example.data.model.ServiceLog
import com.example.data.model.VehicleInfo
import com.example.data.repository.ActivaRepository
import com.example.location.LocationTracker
import com.example.location.TrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class OverallMetrics(
    val totalFuelSpentRupees: Double = 0.0,
    val totalFuelLitres: Double = 0.0,
    val averageMileageKmPerL: Double = 46.5,
    val totalServiceCostRupees: Double = 0.0,
    val totalDistanceTrackedKm: Double = 0.0,
    val overallRidingCostPerKm: Double = 2.45,
    val activeDueServicesCount: Int = 0,
    val isRefillNeededSoon: Boolean = false,
    val remainingDistanceToEmptyKm: Double = 167.4
)

class ActivaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ActivaRepository(application)
    private val locationTracker = LocationTracker(application)

    val vehicleInfo: StateFlow<VehicleInfo> = repository.vehicleInfo
    val fuelLogs: StateFlow<List<FuelLog>> = repository.fuelLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val serviceLogs: StateFlow<List<ServiceLog>> = repository.serviceLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val rideTrips: StateFlow<List<RideTrip>> = repository.rideTrips.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val trackingState: StateFlow<TrackingState> = locationTracker.trackingState

    // Selected trip for detail view
    private val _selectedTrip = MutableStateFlow<RideTrip?>(null)
    val selectedTrip: StateFlow<RideTrip?> = _selectedTrip.asStateFlow()

    // Calculated metrics
    val overallMetrics: StateFlow<OverallMetrics> = combine(
        vehicleInfo,
        fuelLogs,
        serviceLogs,
        rideTrips
    ) { vehicle, fLogs, sLogs, rTrips ->
        computeOverallMetrics(vehicle, fLogs, sLogs, rTrips)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = OverallMetrics()
    )

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    private fun computeOverallMetrics(
        vehicle: VehicleInfo,
        fLogs: List<FuelLog>,
        sLogs: List<ServiceLog>,
        rTrips: List<RideTrip>
    ): OverallMetrics {
        val totalFuelSpent = fLogs.sumOf { it.totalCost }
        val totalLitres = fLogs.sumOf { it.litres }
        val totalService = sLogs.sumOf { it.cost }
        val totalRidesDist = rTrips.sumOf { it.distanceKm }

        // Compute average mileage from logs that have calculated mileage
        val validMileages = fLogs.mapNotNull { it.calculatedMileageKmPerL }
        val avgMileage = if (validMileages.isNotEmpty()) {
            validMileages.average()
        } else {
            vehicle.baseMileageKmPerL
        }

        // Running fuel cost per km
        val fuelCostPerKm = vehicle.petrolPricePerLitre / avgMileage

        // Amortized service cost per km over tracked distance or estimated 10,000 km
        val serviceCostPerKm = if (totalRidesDist > 50.0) {
            totalService / totalRidesDist
        } else {
            0.28 // Standard Activa maintenance cost ~ ₹0.25 - ₹0.35/km
        }

        val fullCostPerKm = fuelCostPerKm + serviceCostPerKm

        // Check maintenance dues
        val schedule = repository.getActivaStandardMaintenanceSchedule(vehicle.currentOdometerKm, sLogs)
        val dueCount = schedule.count { it.isDue(vehicle.currentOdometerKm) || it.isDueSoon(vehicle.currentOdometerKm) }

        val remainingRange = vehicle.currentEstimatedFuelLitres * avgMileage
        val refillNeeded = vehicle.isLowFuel || remainingRange < 40.0

        return OverallMetrics(
            totalFuelSpentRupees = totalFuelSpent,
            totalFuelLitres = totalLitres,
            averageMileageKmPerL = avgMileage,
            totalServiceCostRupees = totalService,
            totalDistanceTrackedKm = totalRidesDist,
            overallRidingCostPerKm = fullCostPerKm,
            activeDueServicesCount = dueCount,
            isRefillNeededSoon = refillNeeded,
            remainingDistanceToEmptyKm = remainingRange
        )
    }

    fun getMaintenanceSchedule(): List<MaintenanceScheduleItem> {
        val v = vehicleInfo.value
        val s = serviceLogs.value
        return repository.getActivaStandardMaintenanceSchedule(v.currentOdometerKm, s)
    }

    fun startRide(isSimulation: Boolean = false) {
        locationTracker.startTracking(isSimulation)
    }

    fun stopRideAndSave() {
        val finalState = locationTracker.stopTracking()
        if (finalState.currentDistanceKm > 0.05 || finalState.durationSeconds > 10) {
            viewModelScope.launch {
                val vehicle = vehicleInfo.value
                val fuelBurned = if (finalState.estimatedFuelBurnedLitres > 0.001) {
                    finalState.estimatedFuelBurnedLitres
                } else {
                    finalState.currentDistanceKm / vehicle.baseMileageKmPerL
                }
                val cost = fuelBurned * vehicle.petrolPricePerLitre

                // Compute eco score: percentage of ride points spent in eco speed range (30-50 km/h)
                val ecoPoints = finalState.points.count { it.speedKmh in 30.0..50.0 }
                val ecoScore = if (finalState.points.isNotEmpty()) {
                    ((ecoPoints.toDouble() / finalState.points.size) * 100).toInt().coerceIn(35, 98)
                } else {
                    85
                }

                // Serialize path points
                val jsonArr = JSONArray()
                finalState.points.takeLast(100).forEach { pt ->
                    val obj = JSONObject().apply {
                        put("lat", pt.latitude)
                        put("lng", pt.longitude)
                        put("spd", pt.speedKmh)
                    }
                    jsonArr.put(obj)
                }

                val trip = RideTrip(
                    startTimeMillis = System.currentTimeMillis() - (finalState.durationSeconds * 1000L),
                    endTimeMillis = System.currentTimeMillis(),
                    distanceKm = finalState.currentDistanceKm,
                    durationSeconds = finalState.durationSeconds,
                    maxSpeedKmh = finalState.topSpeedKmh,
                    avgSpeedKmh = finalState.averageSpeedKmh,
                    fuelConsumedLitres = fuelBurned,
                    tripCostRupees = cost,
                    ecoScorePercent = ecoScore,
                    startLocationName = "Kolkata, WB",
                    endLocationName = if (finalState.isSimulationMode) "Salt Lake Bypass" else "Destination",
                    pathPointsJson = jsonArr.toString()
                )

                repository.saveRideTrip(trip)
            }
        }
    }

    fun addFuelRefill(
        odometerKm: Double,
        litres: Double,
        pricePerLitre: Double,
        totalCost: Double,
        isFullTank: Boolean,
        stationName: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addFuelLog(
                odometerKm = odometerKm,
                litres = litres,
                pricePerLitre = pricePerLitre,
                totalCost = totalCost,
                isFullTank = isFullTank,
                stationName = stationName,
                notes = notes
            )
        }
    }

    fun deleteFuelLog(log: FuelLog) {
        viewModelScope.launch {
            repository.deleteFuelLog(log)
        }
    }

    fun addServiceLog(
        odometerKm: Double,
        serviceCategory: String,
        cost: Double,
        garageName: String,
        partsReplaced: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addServiceLog(
                odometerKm = odometerKm,
                serviceCategory = serviceCategory,
                cost = cost,
                garageName = garageName,
                partsReplaced = partsReplaced,
                notes = notes
            )
        }
    }

    fun deleteServiceLog(log: ServiceLog) {
        viewModelScope.launch {
            repository.deleteServiceLog(log)
        }
    }

    fun deleteTrip(trip: RideTrip) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
            if (_selectedTrip.value?.id == trip.id) {
                _selectedTrip.value = null
            }
        }
    }

    fun selectTrip(trip: RideTrip?) {
        _selectedTrip.value = trip
    }

    fun updateVehicleInfo(updated: VehicleInfo) {
        viewModelScope.launch {
            repository.saveVehicleInfo(updated)
        }
    }

    fun setFuelTankLevel(litres: Double) {
        viewModelScope.launch {
            repository.updateFuelLevel(litres)
        }
    }
}
