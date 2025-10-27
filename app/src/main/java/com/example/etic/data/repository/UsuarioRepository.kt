package com.example.etic.data.repository

import com.example.etic.data.local.dao.UsuarioDao
import com.example.etic.data.local.entities.Usuario

class UsuarioRepository(private val dao: UsuarioDao) {
    suspend fun getByUsuario(usuario: String): Usuario? =
        dao.getByUsuario(usuario)
}
