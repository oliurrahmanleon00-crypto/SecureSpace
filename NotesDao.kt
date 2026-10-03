package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SecureNote
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao {

    @Query("SELECT * FROM secure_notes WHERE isDecoy = :isDecoy ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(isDecoy: Boolean = false): Flow<List<SecureNote>>

    @Query("SELECT * FROM secure_notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): SecureNote?

    @Query("SELECT COUNT(*) FROM secure_notes WHERE isDecoy = :isDecoy")
    fun getNotesCount(isDecoy: Boolean = false): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: SecureNote): Long

    @Update
    suspend fun updateNote(note: SecureNote)

    @Delete
    suspend fun deleteNote(note: SecureNote)

    @Query("DELETE FROM secure_notes WHERE isDecoy = :isDecoy")
    suspend fun deleteAllNotes(isDecoy: Boolean = false)
}
