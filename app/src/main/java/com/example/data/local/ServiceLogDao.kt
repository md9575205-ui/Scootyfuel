package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ServiceLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceLogDao {
    @Query("SELECT * FROM service_logs ORDER BY dateMillis DESC")
    fun getAllServiceLogs(): Flow<List<ServiceLog>>

    @Query("SELECT * FROM service_logs WHERE serviceCategory = :category ORDER BY odometerKm DESC LIMIT 1")
    suspend fun getLatestServiceForCategory(category: String): ServiceLog?

    @Query("SELECT SUM(cost) FROM service_logs")
    fun getTotalServiceCost(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceLog(log: ServiceLog): Long

    @Delete
    suspend fun deleteServiceLog(log: ServiceLog)

    @Query("DELETE FROM service_logs")
    suspend fun deleteAllServiceLogs()
}
