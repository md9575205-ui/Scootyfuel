package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.RideTrip
import kotlinx.coroutines.flow.Flow

@Dao
interface RideTripDao {
    @Query("SELECT * FROM ride_trips ORDER BY startTimeMillis DESC")
    fun getAllTrips(): Flow<List<RideTrip>>

    @Query("SELECT * FROM ride_trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Long): RideTrip?

    @Query("SELECT SUM(distanceKm) FROM ride_trips")
    fun getTotalDistanceTracked(): Flow<Double?>

    @Query("SELECT AVG(ecoScorePercent) FROM ride_trips")
    fun getAverageEcoScore(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: RideTrip): Long

    @Delete
    suspend fun deleteTrip(trip: RideTrip)

    @Query("DELETE FROM ride_trips")
    suspend fun deleteAllTrips()
}
