package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AuditDao
import com.example.data.dao.NotesDao
import com.example.data.dao.SandboxedAppDao
import com.example.data.dao.VaultDao
import com.example.data.model.SandboxedApp
import com.example.data.model.SecureNote
import com.example.data.model.SecurityAuditLog
import com.example.data.model.VaultItem

@Database(
    entities = [
        VaultItem::class,
        SecureNote::class,
        SandboxedApp::class,
        SecurityAuditLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vaultDao(): VaultDao
    abstract fun notesDao(): NotesDao
    abstract fun sandboxedAppDao(): SandboxedAppDao
    abstract fun auditDao(): AuditDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "securespace_encrypted_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
