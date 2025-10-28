package com.example.etic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.etic.data.local.dao.UsuarioDao
import com.example.etic.data.local.dao.ProblemaDao
import com.example.etic.data.local.dao.LineaBaseDao
import com.example.etic.data.local.entities.Usuario
import com.example.etic.data.local.entities.Problema
import com.example.etic.data.local.entities.LineaBase

@Database(entities = [Usuario::class, Problema::class, LineaBase::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun problemaDao(): ProblemaDao
    abstract fun lineaBaseDao(): LineaBaseDao
}
