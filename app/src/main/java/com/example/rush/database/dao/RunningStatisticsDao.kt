package com.example.rush.database.dao

import androidx.room.*
import com.example.rush.database.entities.RunningStatisticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RunningStatisticsDao {
    
    @Query("SELECT * FROM running_statistics ORDER BY lastUpdated DESC LIMIT 1")
    fun getLatestStatistics(): Flow<RunningStatisticsEntity?>
    
    @Query("SELECT * FROM running_statistics WHERE id = :id")
    suspend fun getStatisticsById(id: Long): RunningStatisticsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(statistics: RunningStatisticsEntity)
    
    @Update
    suspend fun updateStatistics(statistics: RunningStatisticsEntity)
    
    @Query("DELETE FROM running_statistics")
    suspend fun deleteAllStatistics()
    
    @Query("UPDATE running_statistics SET lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateLastUpdated(id: Long, timestamp: Long)
}