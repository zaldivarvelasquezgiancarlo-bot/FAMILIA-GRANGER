package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SentinelLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SentinelLogDao {
    @Query("SELECT * FROM sentinel_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SentinelLogEntity>>

    @Query("SELECT * FROM sentinel_logs WHERE sessionDate = :date ORDER BY timestamp DESC")
    fun getLogsForDate(date: String): Flow<List<SentinelLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SentinelLogEntity): Long

    @Query("DELETE FROM sentinel_logs")
    suspend fun clearLogs()
}
