package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SandboxedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface SandboxedAppDao {

    @Query("SELECT * FROM sandboxed_apps ORDER BY lastLaunchedAt DESC, id ASC")
    fun getAllClonedApps(): Flow<List<SandboxedApp>>

    @Query("SELECT * FROM sandboxed_apps WHERE packageName = :pkg LIMIT 1")
    suspend fun getAppByPackage(pkg: String): SandboxedApp?

    @Query("SELECT COUNT(*) FROM sandboxed_apps")
    fun getClonedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClonedApp(app: SandboxedApp): Long

    @Update
    suspend fun updateClonedApp(app: SandboxedApp)

    @Delete
    suspend fun deleteClonedApp(app: SandboxedApp)

    @Query("UPDATE sandboxed_apps SET launchCount = launchCount + 1, lastLaunchedAt = :time WHERE id = :id")
    suspend fun incrementLaunch(id: Long, time: Long = System.currentTimeMillis())
}
