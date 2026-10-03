package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VaultItem
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    @Query("SELECT * FROM vault_items WHERE isDecoy = :isDecoy ORDER BY createdAt DESC")
    fun getAllItems(isDecoy: Boolean = false): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE isDecoy = :isDecoy AND category = :category ORDER BY createdAt DESC")
    fun getItemsByCategory(category: String, isDecoy: Boolean = false): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): VaultItem?

    @Query("SELECT COUNT(*) FROM vault_items WHERE isDecoy = :isDecoy")
    fun getVaultCount(isDecoy: Boolean = false): Flow<Int>

    @Query("SELECT SUM(fileSizeBytes) FROM vault_items WHERE isDecoy = :isDecoy")
    fun getTotalVaultSizeBytes(isDecoy: Boolean = false): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItem): Long

    @Update
    suspend fun updateItem(item: VaultItem)

    @Delete
    suspend fun deleteItem(item: VaultItem)

    @Query("DELETE FROM vault_items WHERE isDecoy = :isDecoy")
    suspend fun deleteAll(isDecoy: Boolean = false)
}
