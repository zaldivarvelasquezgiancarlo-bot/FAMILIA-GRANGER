package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE sessionDate = :date ORDER BY studentName ASC")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records ORDER BY sessionDate DESC, id DESC")
    fun getAllAttendanceRecords(): Flow<List<AttendanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecordEntity): Long

    @Update
    suspend fun updateRecord(record: AttendanceRecordEntity)

    @Query("UPDATE attendance_records SET status = :newStatus, notes = :notes WHERE id = :recordId")
    suspend fun updateStatus(recordId: Long, newStatus: String, notes: String)

    @Query("SELECT * FROM attendance_records WHERE sessionDate = :date AND studentId = :studentId LIMIT 1")
    suspend fun getRecordForStudent(date: String, studentId: String): AttendanceRecordEntity?

    @Query("DELETE FROM attendance_records WHERE sessionDate = :date")
    suspend fun clearRecordsForDate(date: String)
}
