package com.example.rush.repository

import android.content.Context
import android.util.Log
import com.example.rush.database.RunningDatabase
import com.example.rush.database.entities.RunningSessionEntity
import com.example.rush.database.entities.RunningStatisticsEntity
import com.example.rush.data.RunningSession
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

class RunningRepository(context: Context) {
    
    private val database = RunningDatabase.getDatabase(context)
    private val sessionDao = database.runningSessionDao()
    private val statisticsDao = database.runningStatisticsDao()
    
    // Convert between domain model and entity
    private fun RunningSession.toEntity(): RunningSessionEntity {
        return RunningSessionEntity(
            id = this.id,
            startTime = this.startTime,
            endTime = this.endTime,
            distance = this.distance,
            duration = this.duration,
            avgPace = this.avgPace,
            maxSpeed = this.maxSpeed,
            calories = this.calories,
            route = this.route
        )
    }
    
    private fun RunningSessionEntity.toDomainModel(): RunningSession {
        return RunningSession(
            id = this.id,
            startTime = this.startTime,
            endTime = this.endTime,
            distance = this.distance,
            duration = this.duration,
            avgPace = this.avgPace,
            maxSpeed = this.maxSpeed,
            calories = this.calories,
            locations = emptyList(), // We don't store individual locations, just route
            route = this.route
        )
    }
    
    // Session operations
    fun getAllSessions(): Flow<List<RunningSession>> {
        return sessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getSessionById(sessionId: String): RunningSession? {
        return sessionDao.getSessionById(sessionId)?.toDomainModel()
    }
    
    fun getRecentSessions(limit: Int = 10): Flow<List<RunningSession>> {
        return sessionDao.getRecentSessions(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getThisWeekSessions(): Flow<List<RunningSession>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weekStart = calendar.timeInMillis
        
        return sessionDao.getThisWeekSessions(weekStart).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getThisMonthSessions(): Flow<List<RunningSession>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val monthStart = calendar.timeInMillis
        
        return sessionDao.getThisMonthSessions(monthStart).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun insertSession(session: RunningSession) {
        try {
            sessionDao.insertSession(session.toEntity())
            updateStatistics()
            Log.d("RunningRepository", "✅ Session saved: ${session.id}")
        } catch (e: Exception) {
            Log.e("RunningRepository", "❌ Error saving session: ${e.message}", e)
            throw e
        }
    }
    
    suspend fun updateSession(session: RunningSession) {
        try {
            sessionDao.updateSession(session.toEntity())
            updateStatistics()
            Log.d("RunningRepository", "✅ Session updated: ${session.id}")
        } catch (e: Exception) {
            Log.e("RunningRepository", "❌ Error updating session: ${e.message}", e)
            throw e
        }
    }
    
    suspend fun deleteSession(sessionId: String) {
        try {
            sessionDao.deleteSessionById(sessionId)
            updateStatistics()
            Log.d("RunningRepository", "✅ Session deleted: $sessionId")
        } catch (e: Exception) {
            Log.e("RunningRepository", "❌ Error deleting session: ${e.message}", e)
            throw e
        }
    }
    
    // Statistics operations
    fun getStatistics(): Flow<RunningStatisticsEntity?> {
        return statisticsDao.getLatestStatistics()
    }
    
    suspend fun updateStatistics() {
        try {
            val totalRuns = sessionDao.getTotalSessionCount()
            val totalDistance = sessionDao.getTotalDistance() ?: 0f
            val totalDuration = sessionDao.getTotalDuration() ?: 0L
            val totalCalories = sessionDao.getTotalCalories() ?: 0
            val averagePace = sessionDao.getAveragePace() ?: 0f
            val bestPace = sessionDao.getBestPace() ?: 0f
            val longestDistance = sessionDao.getLongestDistance() ?: 0f
            val longestDuration = sessionDao.getLongestDuration() ?: 0L
            
            val statistics = RunningStatisticsEntity(
                totalRuns = totalRuns,
                totalDistance = totalDistance,
                totalDuration = totalDuration,
                totalCalories = totalCalories,
                averagePace = averagePace,
                bestPace = bestPace,
                longestRun = longestDistance,
                longestDuration = longestDuration,
                lastUpdated = System.currentTimeMillis()
            )
            
            statisticsDao.insertStatistics(statistics)
            Log.d("RunningRepository", "✅ Statistics updated")
        } catch (e: Exception) {
            Log.e("RunningRepository", "❌ Error updating statistics: ${e.message}", e)
        }
    }
    
    // Analytics operations
    suspend fun getTotalSessionCount(): Int = sessionDao.getTotalSessionCount()
    
    suspend fun getLongestRun(): RunningSession? {
        return sessionDao.getLongestRun()?.toDomainModel()
    }
    
    suspend fun getFastestRun(): RunningSession? {
        return sessionDao.getFastestRun()?.toDomainModel()
    }
    
    // Maintenance operations
    suspend fun deleteAllSessions() {
        try {
            sessionDao.deleteAllSessions()
            statisticsDao.deleteAllStatistics()
            Log.d("RunningRepository", "✅ All data cleared")
        } catch (e: Exception) {
            Log.e("RunningRepository", "❌ Error clearing data: ${e.message}", e)
            throw e
        }
    }
    
    suspend fun deleteOldSessions(daysToKeep: Int = 365) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        try {
            sessionDao.deleteOldSessions(cutoffTime)
            updateStatistics()
            Log.d("RunningRepository", "✅ Old sessions cleaned up")
        } catch (e: Exception) {
            Log.e("RunningRepository", "❌ Error cleaning old sessions: ${e.message}", e)
        }
    }
}