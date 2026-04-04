package com.example.etic.core.saf

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object ReportsRefreshBus {
    private val _tick = MutableStateFlow(0)
    val tick = _tick.asStateFlow()

    fun notifyChanged() {
        _tick.update { current -> current + 1 }
    }
}
