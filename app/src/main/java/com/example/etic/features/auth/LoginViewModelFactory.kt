package com.example.etic.features.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.etic.data.local.DbProvider
import com.example.etic.data.repository.UsuarioRepository

class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = DbProvider.get(context)
        val repo = UsuarioRepository(db.usuarioDao())
        @Suppress("UNCHECKED_CAST")
        return LoginViewModel(repo) as T
    }
}
