package com.example.rush.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.rush.database.converters.LocationConverters
import com.example.rush.database.dao.RunningSessionDao
import com.example.rush.database.dao.RunningStatisticsDao
import com.example.rush.database.entities.RunningSessionEntity
import com.example.rush.database.entities.RunningStatisticsEntity

@Database(
    entities = [
        RunningSessionEntity::class,
        RunningStatisticsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocationConverters::class)
abstract class RunningDatabase : RoomDatabase() {
    
    abstract fun runningSessionDao(): RunningSessionDao
    abstract fun runningStatisticsDao(): RunningStatisticsDao
    
    companion object {
        @Volatile
        private var INSTANCE: RunningDatabase? = null
        
        fun getDatabase(context: Context): RunningDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RunningDatabase::class.java,
                    "running_database"
                )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        // For testing purposes
        fun getInMemoryDatabase(context: Context): RunningDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                RunningDatabase::class.java
            ).build()
        }
    }
}