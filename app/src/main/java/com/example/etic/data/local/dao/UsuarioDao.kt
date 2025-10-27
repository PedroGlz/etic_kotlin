package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Usuario

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE Usuario = :usuario LIMIT 1")
    suspend fun getByUsuario(usuario: String): Usuario?

    /*
    @Query("SELECT * FROM usuarios")
    suspend fun getAll(): List<Usuario>
    */
}
