package com.example.etic.data.repository

import com.example.etic.data.local.dao.EstatusInspeccionDetDao
import com.example.etic.data.local.entities.EstatusInspeccionDet

class EstatusInspeccionDetRepository(private val dao: EstatusInspeccionDetDao) {
    suspend fun getAll(): List<EstatusInspeccionDet> = dao.getAll()
    suspend fun getById(id: String): EstatusInspeccionDet? = dao.getById(id)
}
