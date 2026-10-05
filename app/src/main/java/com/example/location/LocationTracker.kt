package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.example.data.model.RoutePoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

data class TrackingState(
    val isRecording: Boolean = false,
    val isSimulationMode: Boolean = false,
    val currentSpeedKmh: Double = 0.0,
    val topSpeedKmh: Double = 0.0,
    val averageSpeedKmh: Double = 0.0,
    val currentDistanceKm: Double = 0.0,
    val durationSeconds: Long = 0L,
    val estimatedFuelBurnedLitres: Double = 0.0,
    val currentInstantaneousMileageKmPerL: Double = 48.0,
    val isEcoZone: Boolean = false, // 30 - 50 km/h
    val latitude: Double = 22.5726,
    val longitude: Double = 88.3639,
    val altitudeMeters: Double = 9.0,
    val points: List<RoutePoint> = emptyList(),
    val hasGpsLock: Boolean = false
)

class LocationTracker(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _trackingState = MutableStateFlow(TrackingState())
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var lastLocation: Location? = null
    private var timerJob: Job? = null
    private var simulationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private var totalSpeedAccumulator = 0.0
    private var speedSampleCount = 0

    @SuppressLint("MissingPermission")
    fun startTracking(isSimulation: Boolean = false) {
        stopTracking()

        totalSpeedAccumulator = 0.0
        speedSampleCount = 0
        lastLocation = null

        _trackingState.value = TrackingState(
            isRecording = true,
            isSimulationMode = isSimulation,
            hasGpsLock = isSimulation
        )

        // Start timer for duration
        timerJob = scope.launch {
            while (isActive && _trackingState.value.isRecording) {
                delay(1000)
                val current = _trackingState.value
                val newDuration = current.durationSeconds + 1
                val avgSpeed = if (speedSampleCount > 0) totalSpeedAccumulator / speedSampleCount else 0.0

                // If stationary, add tiny idle fuel consumption (~0.00007 L/sec = 0.25 L/hr)
                var idleFuel = 0.0
                if (current.currentSpeedKmh < 2.0) {
                    idleFuel = 0.000069
                }

                _trackingState.value = current.copy(
                    durationSeconds = newDuration,
                    averageSpeedKmh = avgSpeed,
                    estimatedFuelBurnedLitres = current.estimatedFuelBurnedLitres + idleFuel
                )
            }
        }

        if (isSimulation) {
            startSimulationMode()
        } else {
            startRealGpsTracking()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRealGpsTracking() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(500L)
            .setMinUpdateDistanceMeters(1.0f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                handleNewLocation(loc)
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback as LocationCallback,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            // Permission or hardware issue fallback
            _trackingState.value = _trackingState.value.copy(hasGpsLock = false)
        }
    }

    private fun handleNewLocation(location: Location) {
        val speedKmh = (location.speed * 3.6).coerceAtLeast(0.0)
        val lat = location.latitude
        val lng = location.longitude
        val alt = location.altitude

        var distanceDeltaKm = 0.0
        lastLocation?.let { prev ->
            val distMeters = prev.distanceTo(location)
            if (distMeters in 1.0..500.0) {
                distanceDeltaKm = distMeters / 1000.0
            }
        }
        lastLocation = location

        // Calculate instantaneous mileage based on Activa HET engine profile
        val instantMileage = when {
            speedKmh in 32.0..48.0 -> 52.0 // Optimal Eco cruise zone
            speedKmh in 20.0..32.0 -> 46.0
            speedKmh in 48.0..62.0 -> 43.0
            speedKmh > 62.0 -> 36.0 // High drag / wide open throttle
            speedKmh > 2.0 -> 40.0
            else -> 0.0
        }

        val fuelConsumedThisDelta = if (instantMileage > 0.0 && distanceDeltaKm > 0.0) {
            distanceDeltaKm / instantMileage
        } else {
            0.0
        }

        totalSpeedAccumulator += speedKmh
        speedSampleCount++

        val current = _trackingState.value
        val newDist = current.currentDistanceKm + distanceDeltaKm
        val newFuel = current.estimatedFuelBurnedLitres + fuelConsumedThisDelta
        val newTop = maxOf(current.topSpeedKmh, speedKmh)
        val isEco = speedKmh in 30.0..50.0

        val newPoint = RoutePoint(
            latitude = lat,
            longitude = lng,
            speedKmh = speedKmh,
            timestampMillis = System.currentTimeMillis(),
            altitudeMeters = alt
        )

        _trackingState.value = current.copy(
            currentSpeedKmh = speedKmh,
            topSpeedKmh = newTop,
            currentDistanceKm = newDist,
            estimatedFuelBurnedLitres = newFuel,
            currentInstantaneousMileageKmPerL = if (instantMileage > 0) instantMileage else current.currentInstantaneousMileageKmPerL,
            isEcoZone = isEco,
            latitude = lat,
            longitude = lng,
            altitudeMeters = alt,
            points = current.points + newPoint,
            hasGpsLock = true
        )
    }

    private fun startSimulationMode() {
        simulationJob = scope.launch {
            var step = 0
            var baseLat = 22.5726
            var baseLng = 88.3639
            var simulatedSpeed = 0.0

            while (isActive && _trackingState.value.isRecording) {
                delay(800)
                step++

                // Realistic scooter driving pattern: start, speed up, cruise in eco zone, slow down, turn
                val phase = (step % 40)
                simulatedSpeed = when {
                    phase < 6 -> (phase * 6.5) // Accelerating 0 to 39 km/h
                    phase < 22 -> 42.0 + 5.0 * sin(step * 0.4) // Cruising in Eco Zone 37 - 47 km/h
                    phase < 28 -> 55.0 + 3.0 * sin(step * 0.5) // Quick burst up to 58 km/h
                    phase < 34 -> (58.0 - (phase - 28) * 8.0).coerceAtLeast(0.0) // Slowing down at intersection
                    else -> 0.0 // Stopped at red signal
                }

                // Advance coordinate along realistic route
                baseLat += 0.00015 * (simulatedSpeed / 40.0)
                baseLng += 0.00010 * (simulatedSpeed / 40.0)

                val location = Location("simulated").apply {
                    latitude = baseLat
                    longitude = baseLng
                    speed = (simulatedSpeed / 3.6).toFloat()
                    altitude = 12.0 + sin(step * 0.1) * 2.0
                    time = System.currentTimeMillis()
                }
                handleNewLocation(location)
            }
        }
    }

    fun stopTracking(): TrackingState {
        val finalState = _trackingState.value

        locationCallback?.let {
            try {
                fusedLocationClient.removeLocationUpdates(it)
            } catch (_: Exception) {}
        }
        locationCallback = null

        timerJob?.cancel()
        timerJob = null
        simulationJob?.cancel()
        simulationJob = null

        _trackingState.value = _trackingState.value.copy(
            isRecording = false,
            currentSpeedKmh = 0.0
        )

        return finalState
    }
}
