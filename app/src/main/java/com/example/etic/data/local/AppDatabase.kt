package com.example.etic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.etic.data.local.dao.UsuarioDao
import com.example.etic.data.local.dao.ProblemaDao
import com.example.etic.data.local.entities.Usuario
import com.example.etic.data.local.entities.Problema

@Database(entities = [Usuario::class, Problema::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun problemaDao(): ProblemaDao
}
