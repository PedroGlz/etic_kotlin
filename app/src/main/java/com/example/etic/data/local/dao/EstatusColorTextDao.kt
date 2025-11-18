package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.EstatusColorText

@Dao
interface EstatusColorTextDao {
    @Query("SELECT * FROM estatus_color_text")
    suspend fun getAll(): List<EstatusColorText>

    @Query("SELECT * FROM estatus_color_text WHERE Color_Text = :color LIMIT 1")
    suspend fun getByColor(color: String): EstatusColorText?
}

