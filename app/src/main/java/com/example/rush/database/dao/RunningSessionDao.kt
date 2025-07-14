package com.example.rush.database.dao

import androidx.room.*
import com.example.rush.database.entities.RunningSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RunningSessionDao {
    
    @Query("SELECT * FROM running_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<RunningSessionEntity>>
    
    @Query("SELECT * FROM running_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): RunningSessionEntity?
    
    @Query("SELECT * FROM running_sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<RunningSessionEntity>>
    
    @Query("SELECT * FROM running_sessions WHERE startTime BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getSessionsInDateRange(startDate: Long, endDate: Long): Flow<List<RunningSessionEntity>>
    
    @Query("SELECT COUNT(*) FROM running_sessions")
    suspend fun getTotalSessionCount(): Int
    
    @Query("SELECT SUM(distance) FROM running_sessions")
    suspend fun getTotalDistance(): Float?
    
    @Query("SELECT SUM(duration) FROM running_sessions")
    suspend fun getTotalDuration(): Long?
    
    @Query("SELECT SUM(calories) FROM running_sessions")
    suspend fun getTotalCalories(): Int?
    
    @Query("SELECT AVG(avgPace) FROM running_sessions WHERE avgPace > 0")
    suspend fun getAveragePace(): Float?
    
    @Query("SELECT MIN(avgPace) FROM running_sessions WHERE avgPace > 0")
    suspend fun getBestPace(): Float?
    
    @Query("SELECT MAX(distance) FROM running_sessions")
    suspend fun getLongestDistance(): Float?
    
    @Query("SELECT MAX(duration) FROM running_sessions")
    suspend fun getLongestDuration(): Long?
    
    @Query("SELECT * FROM running_sessions WHERE distance = (SELECT MAX(distance) FROM running_sessions)")
    suspend fun getLongestRun(): RunningSessionEntity?
    
    @Query("SELECT * FROM running_sessions WHERE avgPace = (SELECT MIN(avgPace) FROM running_sessions WHERE avgPace > 0)")
    suspend fun getFastestRun(): RunningSessionEntity?
    
    @Query("SELECT * FROM running_sessions WHERE startTime >= :weekStart ORDER BY startTime DESC")
    fun getThisWeekSessions(weekStart: Long): Flow<List<RunningSessionEntity>>
    
    @Query("SELECT * FROM running_sessions WHERE startTime >= :monthStart ORDER BY startTime DESC")
    fun getThisMonthSessions(monthStart: Long): Flow<List<RunningSessionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: RunningSessionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<RunningSessionEntity>)
    
    @Update
    suspend fun updateSession(session: RunningSessionEntity)
    
    @Delete
    suspend fun deleteSession(session: RunningSessionEntity)
    
    @Query("DELETE FROM running_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)
    
    @Query("DELETE FROM running_sessions")
    suspend fun deleteAllSessions()
    
    @Query("DELETE FROM running_sessions WHERE startTime < :cutoffDate")
    suspend fun deleteOldSessions(cutoffDate: Long)
}