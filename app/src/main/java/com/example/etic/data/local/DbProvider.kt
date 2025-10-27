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
                .createFromAsset("databases/etic.db") // 👈 tu archivo real en assets/databases/
                .build()
                .also { INSTANCE = it }
        }
}
