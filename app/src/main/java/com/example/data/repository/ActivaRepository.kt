package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.model.FuelLog
import com.example.data.model.MaintenanceScheduleItem
import com.example.data.model.RideTrip
import com.example.data.model.ServiceLog
import com.example.data.model.VehicleInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ActivaRepository(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val fuelLogDao = database.fuelLogDao()
    private val serviceLogDao = database.serviceLogDao()
    private val rideTripDao = database.rideTripDao()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("activa_pilot_prefs", Context.MODE_PRIVATE)

    private val _vehicleInfo = MutableStateFlow(loadVehicleInfo())
    val vehicleInfo: StateFlow<VehicleInfo> = _vehicleInfo.asStateFlow()

    val fuelLogs: Flow<List<FuelLog>> = fuelLogDao.getAllFuelLogs()
    val serviceLogs: Flow<List<ServiceLog>> = serviceLogDao.getAllServiceLogs()
    val rideTrips: Flow<List<RideTrip>> = rideTripDao.getAllTrips()

    private fun loadVehicleInfo(): VehicleInfo {
        return VehicleInfo(
            makeModel = prefs.getString("makeModel", "Honda Activa (2014)") ?: "Honda Activa (2014)",
            registrationNumber = prefs.getString("regNumber", "WB 06 K5136") ?: "WB 06 K5136",
            engineCapacityCc = "109.2 cc HET",
            fuelTankCapacityLitres = prefs.getFloat("fuelTankCapacity", 5.3f).toDouble(),
            reserveTankCapacityLitres = 1.3,
            currentOdometerKm = prefs.getFloat("odometerKm", 28450.0f).toDouble(),
            currentEstimatedFuelLitres = prefs.getFloat("estimatedFuel", 3.8f).toDouble(),
            baseMileageKmPerL = prefs.getFloat("baseMileage", 46.5f).toDouble(),
            petrolPricePerLitre = prefs.getFloat("petrolPrice", 103.50f).toDouble()
        )
    }

    suspend fun saveVehicleInfo(info: VehicleInfo) = withContext(Dispatchers.IO) {
        prefs.edit().apply {
            putString("makeModel", info.makeModel)
            putString("regNumber", info.registrationNumber)
            putFloat("fuelTankCapacity", info.fuelTankCapacityLitres.toFloat())
            putFloat("odometerKm", info.currentOdometerKm.toFloat())
            putFloat("estimatedFuel", info.currentEstimatedFuelLitres.toFloat())
            putFloat("baseMileage", info.baseMileageKmPerL.toFloat())
            putFloat("petrolPrice", info.petrolPricePerLitre.toFloat())
            apply()
        }
        _vehicleInfo.value = info
    }

    suspend fun updateFuelLevel(newFuelLitres: Double) = withContext(Dispatchers.IO) {
        val clamped = newFuelLitres.coerceIn(0.0, _vehicleInfo.value.fuelTankCapacityLitres)
        val updated = _vehicleInfo.value.copy(currentEstimatedFuelLitres = clamped)
        saveVehicleInfo(updated)
    }

    suspend fun recordDistanceDriven(km: Double, fuelBurnedLitres: Double) = withContext(Dispatchers.IO) {
        val current = _vehicleInfo.value
        val newOdo = current.currentOdometerKm + km
        val newFuel = (current.currentEstimatedFuelLitres - fuelBurnedLitres).coerceAtLeast(0.0)
        val updated = current.copy(
            currentOdometerKm = newOdo,
            currentEstimatedFuelLitres = newFuel
        )
        saveVehicleInfo(updated)
    }

    suspend fun addFuelLog(
        odometerKm: Double,
        litres: Double,
        pricePerLitre: Double,
        totalCost: Double,
        isFullTank: Boolean,
        stationName: String,
        notes: String
    ): Long = withContext(Dispatchers.IO) {
        // Calculate mileage if previous full tank log exists
        val previousLog = fuelLogDao.getLatestFuelLog()
        var calculatedMileage: Double? = null
        var costPerKm: Double? = null

        if (previousLog != null && odometerKm > previousLog.odometerKm && litres > 0) {
            val dist = odometerKm - previousLog.odometerKm
            val mpg = dist / litres
            if (mpg in 20.0..70.0) {
                calculatedMileage = mpg
                costPerKm = totalCost / dist
            }
        }

        val log = FuelLog(
            odometerKm = odometerKm,
            litres = litres,
            pricePerLitre = pricePerLitre,
            totalCost = totalCost,
            isFullTank = isFullTank,
            stationName = stationName,
            notes = notes,
            calculatedMileageKmPerL = calculatedMileage,
            costPerKmRupees = costPerKm
        )

        val id = fuelLogDao.insertFuelLog(log)

        // Update vehicle state: add fuel to tank (up to 5.3L) and update odo if higher
        val current = _vehicleInfo.value
        val newFuel = if (isFullTank) current.fuelTankCapacityLitres else {
            (current.currentEstimatedFuelLitres + litres).coerceAtMost(current.fuelTankCapacityLitres)
        }
        val newOdo = maxOf(current.currentOdometerKm, odometerKm)
        val newBaseMileage = calculatedMileage ?: current.baseMileageKmPerL
        saveVehicleInfo(
            current.copy(
                currentEstimatedFuelLitres = newFuel,
                currentOdometerKm = newOdo,
                baseMileageKmPerL = newBaseMileage,
                petrolPricePerLitre = pricePerLitre
            )
        )

        id
    }

    suspend fun deleteFuelLog(log: FuelLog) = withContext(Dispatchers.IO) {
        fuelLogDao.deleteFuelLog(log)
    }

    suspend fun addServiceLog(
        odometerKm: Double,
        serviceCategory: String,
        cost: Double,
        garageName: String,
        partsReplaced: String,
        notes: String
    ): Long = withContext(Dispatchers.IO) {
        val log = ServiceLog(
            odometerKm = odometerKm,
            serviceCategory = serviceCategory,
            cost = cost,
            garageName = garageName,
            partsReplaced = partsReplaced,
            notes = notes
        )
        val id = serviceLogDao.insertServiceLog(log)

        // If odometer in service is higher than current, update vehicle odo
        val current = _vehicleInfo.value
        if (odometerKm > current.currentOdometerKm) {
            saveVehicleInfo(current.copy(currentOdometerKm = odometerKm))
        }

        id
    }

    suspend fun deleteServiceLog(log: ServiceLog) = withContext(Dispatchers.IO) {
        serviceLogDao.deleteServiceLog(log)
    }

    suspend fun saveRideTrip(trip: RideTrip): Long = withContext(Dispatchers.IO) {
        val id = rideTripDao.insertTrip(trip)
        recordDistanceDriven(trip.distanceKm, trip.fuelConsumedLitres)
        id
    }

    suspend fun deleteTrip(trip: RideTrip) = withContext(Dispatchers.IO) {
        rideTripDao.deleteTrip(trip)
    }

    fun getActivaStandardMaintenanceSchedule(
        currentOdometer: Double,
        serviceLogsList: List<ServiceLog>
    ): List<MaintenanceScheduleItem> {
        val baseItems = listOf(
            MaintenanceScheduleItem(
                id = "engine_oil",
                title = "Engine Oil Replacement (10W-30)",
                description = "Recommended 4-stroke genuine Honda 10W-30 engine oil. Replace every 3,000 - 4,000 km.",
                intervalKm = 3500.0,
                intervalMonths = 4,
                isCritical = true
            ),
            MaintenanceScheduleItem(
                id = "air_filter",
                title = "Viscous Air Filter Clean / Replace",
                description = "Clean or replace viscous paper filter to maintain optimal airflow and 45+ km/l mileage.",
                intervalKm = 4000.0,
                intervalMonths = 6,
                isCritical = false
            ),
            MaintenanceScheduleItem(
                id = "spark_plug",
                title = "Spark Plug Check (NGK MR7C-9D)",
                description = "Inspect gap (0.8 - 0.9 mm) and carbon deposits. Replace every 8,000 km.",
                intervalKm = 8000.0,
                intervalMonths = 12,
                isCritical = false
            ),
            MaintenanceScheduleItem(
                id = "brake_shoes",
                title = "Combi-Brake Shoes (Front & Rear)",
                description = "Inspect brake liner wear indicator groove and drum contact. Vital for braking safety.",
                intervalKm = 4000.0,
                intervalMonths = 6,
                isCritical = true
            ),
            MaintenanceScheduleItem(
                id = "gear_oil",
                title = "Transmission / Final Drive Gear Oil",
                description = "Replace final drive gear lubricant to protect rear differential & bearings.",
                intervalKm = 8000.0,
                intervalMonths = 12,
                isCritical = false
            ),
            MaintenanceScheduleItem(
                id = "drive_belt",
                title = "CVT Drive Belt & Roller Weights",
                description = "Inspect V-belt width, cracks, and variator rollers for smooth CVT transmission.",
                intervalKm = 8000.0,
                intervalMonths = 12,
                isCritical = true
            ),
            MaintenanceScheduleItem(
                id = "battery",
                title = "12V 3Ah Maintenance-Free Battery",
                description = "Inspect terminal cleanliness, self-starter crank voltage, and charging circuit.",
                intervalKm = 6000.0,
                intervalMonths = 6,
                isCritical = false
            ),
            MaintenanceScheduleItem(
                id = "tyres",
                title = "Tyres (90/100-10) Pressure & Tread",
                description = "Maintain 22 PSI (Front), 29/36 PSI (Rear). Check tread wear indicators.",
                intervalKm = 3000.0,
                intervalMonths = 3,
                isCritical = false
            ),
            MaintenanceScheduleItem(
                id = "pucc",
                title = "PUCC (Pollution Certificate)",
                description = "Mandatory Indian emission compliance certificate for WB 06 K5136. Renew every 6 months.",
                intervalKm = 5000.0,
                intervalMonths = 6,
                isCritical = true
            )
        )

        return baseItems.map { item ->
            // Find most recent matching service log
            val matchedLog = serviceLogsList.firstOrNull { log ->
                log.serviceCategory.contains(item.title.take(10), ignoreCase = true) ||
                        log.notes.contains(item.title.take(10), ignoreCase = true) ||
                        log.partsReplaced.contains(item.title.take(10), ignoreCase = true)
            }
            if (matchedLog != null) {
                item.copy(
                    lastDoneOdometerKm = matchedLog.odometerKm,
                    lastDoneDateMillis = matchedLog.dateMillis
                )
            } else {
                // If not found in logs, assume last service was at nearest past interval
                val simulatedLastOdo = ((currentOdometer / item.intervalKm).toInt() * item.intervalKm)
                    .coerceAtMost(currentOdometer - 500)
                item.copy(lastDoneOdometerKm = simulatedLastOdo)
            }
        }
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val hasSeeded = prefs.getBoolean("has_seeded_initial_data_v1", false)
        if (hasSeeded) return@withContext

        // Check if fuel logs exist
        val existingFuel = fuelLogDao.getAllFuelLogs().firstOrNull()
        if (existingFuel.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val dayMillis = 24L * 60 * 60 * 1000

            // Seed realistic past fuel logs for Activa 2014 WB 06 K5136
            fuelLogDao.insertFuelLog(
                FuelLog(
                    dateMillis = now - 21 * dayMillis,
                    odometerKm = 27720.0,
                    litres = 4.8,
                    pricePerLitre = 103.50,
                    totalCost = 496.80,
                    isFullTank = true,
                    stationName = "Indian Oil Pump, Park Circus",
                    notes = "Full tank refill",
                    calculatedMileageKmPerL = 46.2,
                    costPerKmRupees = 2.24
                )
            )
            fuelLogDao.insertFuelLog(
                FuelLog(
                    dateMillis = now - 12 * dayMillis,
                    odometerKm = 27950.0,
                    litres = 4.9,
                    pricePerLitre = 103.50,
                    totalCost = 507.15,
                    isFullTank = true,
                    stationName = "HP Petrol Station, Salt Lake",
                    notes = "Smooth highway + city riding",
                    calculatedMileageKmPerL = 46.9,
                    costPerKmRupees = 2.20
                )
            )
            fuelLogDao.insertFuelLog(
                FuelLog(
                    dateMillis = now - 4 * dayMillis,
                    odometerKm = 28185.0,
                    litres = 5.0,
                    pricePerLitre = 103.50,
                    totalCost = 517.50,
                    isFullTank = true,
                    stationName = "Bharat Petroleum, Newtown",
                    notes = "Full tank refill before weekend trip",
                    calculatedMileageKmPerL = 47.0,
                    costPerKmRupees = 2.20
                )
            )
        }

        // Seed initial service logs
        val existingServices = serviceLogDao.getAllServiceLogs().firstOrNull()
        if (existingServices.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val dayMillis = 24L * 60 * 60 * 1000

            serviceLogDao.insertServiceLog(
                ServiceLog(
                    dateMillis = now - 75 * dayMillis,
                    odometerKm = 26500.0,
                    serviceCategory = "Engine Oil (10W-30)",
                    cost = 450.0,
                    garageName = "Authorized Honda Service Centre",
                    partsReplaced = "Honda 10W-30 4T Oil (800ml), Drain bolt washer",
                    notes = "Engine running smooth and quiet"
                )
            )
            serviceLogDao.insertServiceLog(
                ServiceLog(
                    dateMillis = now - 75 * dayMillis,
                    odometerKm = 26500.0,
                    serviceCategory = "Air Filter",
                    cost = 280.0,
                    garageName = "Authorized Honda Service Centre",
                    partsReplaced = "Genuine Activa Viscous Air Filter",
                    notes = "Restored pickup and throttle response"
                )
            )
            serviceLogDao.insertServiceLog(
                ServiceLog(
                    dateMillis = now - 40 * dayMillis,
                    odometerKm = 27400.0,
                    serviceCategory = "Brake Shoes",
                    cost = 550.0,
                    garageName = "City Auto Garage",
                    partsReplaced = "Front and Rear Brake Shoe Set",
                    notes = "Combi brake cable adjusted and tightened"
                )
            )
            serviceLogDao.insertServiceLog(
                ServiceLog(
                    dateMillis = now - 20 * dayMillis,
                    odometerKm = 27850.0,
                    serviceCategory = "PUCC (Pollution Certificate)",
                    cost = 100.0,
                    garageName = "West Bengal Transport Testing Centre",
                    partsReplaced = "Certificate renewed (Valid for 6 months)",
                    notes = "CO and HC levels well within BS-III / BS-IV limits"
                )
            )
        }

        // Seed sample ride trip
        val existingTrips = rideTripDao.getAllTrips().firstOrNull()
        if (existingTrips.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val samplePath = JSONArray().apply {
                put(JSONObject().put("lat", 22.5726).put("lng", 88.3639).put("spd", 0.0))
                put(JSONObject().put("lat", 22.5760).put("lng", 88.3700).put("spd", 38.0))
                put(JSONObject().put("lat", 22.5810).put("lng", 88.3820).put("spd", 46.5))
                put(JSONObject().put("lat", 22.5890).put("lng", 88.3950).put("spd", 52.0))
                put(JSONObject().put("lat", 22.5930).put("lng", 88.4080).put("spd", 42.0))
                put(JSONObject().put("lat", 22.5990).put("lng", 88.4200).put("spd", 0.0))
            }.toString()

            rideTripDao.insertTrip(
                RideTrip(
                    startTimeMillis = now - 1800000L,
                    endTimeMillis = now - 300000L,
                    distanceKm = 12.8,
                    durationSeconds = 1500L,
                    maxSpeedKmh = 54.5,
                    avgSpeedKmh = 30.7,
                    fuelConsumedLitres = 0.27,
                    tripCostRupees = 27.95,
                    ecoScorePercent = 91,
                    startLocationName = "Central Avenue, Kolkata",
                    endLocationName = "Salt Lake Sector V",
                    pathPointsJson = samplePath
                )
            )
        }

        prefs.edit().putBoolean("has_seeded_initial_data_v1", true).apply()
    }
}
