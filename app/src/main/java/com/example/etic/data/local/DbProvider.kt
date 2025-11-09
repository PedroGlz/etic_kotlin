package com.example.etic.data.local

import android.content.Context
import androidx.room.Room

object DbProvider {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase =
        INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "etic.db"
            )
                .createFromAsset("databases/etic.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                .build()
                .also { INSTANCE = it }
        }

    fun closeAndReset() {
        synchronized(this) {
            try { INSTANCE?.close() } catch (_: Exception) { }
            INSTANCE = null
        }
    }
}
