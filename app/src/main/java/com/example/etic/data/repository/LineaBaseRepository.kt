package com.example.etic.data.repository

import com.example.etic.data.local.dao.LineaBaseDao
import com.example.etic.data.local.entities.LineaBase

class LineaBaseRepository(private val dao: LineaBaseDao) {
    suspend fun getAll(): List<LineaBase> = dao.getAll()
    suspend fun getByInspeccion(idInspeccion: String): List<LineaBase> = dao.getByInspeccion(idInspeccion)
}

