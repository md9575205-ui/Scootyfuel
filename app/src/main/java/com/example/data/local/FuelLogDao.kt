package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FuelLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelLogDao {
    @Query("SELECT * FROM fuel_logs ORDER BY dateMillis DESC")
    fun getAllFuelLogs(): Flow<List<FuelLog>>

    @Query("SELECT * FROM fuel_logs ORDER BY odometerKm DESC LIMIT 1")
    suspend fun getLatestFuelLog(): FuelLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelLog(log: FuelLog): Long

    @Update
    suspend fun updateFuelLog(log: FuelLog)

    @Delete
    suspend fun deleteFuelLog(log: FuelLog)

    @Query("DELETE FROM fuel_logs")
    suspend fun deleteAllFuelLogs()
}
