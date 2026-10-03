package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SecurityAuditLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {

    @Query("SELECT * FROM security_audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<SecurityAuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SecurityAuditLog): Long

    @Query("DELETE FROM security_audit_logs")
    suspend fun clearLogs()
}
