package com.example.etic.data.repository

import com.example.etic.data.local.dao.ProblemaDao
import com.example.etic.data.local.entities.Problema

class ProblemaRepository(private val dao: ProblemaDao) {
    suspend fun getAll(): List<Problema> = dao.getAll()
    suspend fun getByInspeccion(idInspeccion: String): List<Problema> = dao.getByInspeccion(idInspeccion)
}

