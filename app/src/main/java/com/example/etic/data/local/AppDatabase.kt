package com.example.etic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.etic.data.local.dao.UsuarioDao
import com.example.etic.data.local.dao.ProblemaDao
import com.example.etic.data.local.dao.LineaBaseDao
import com.example.etic.data.local.dao.UbicacionDao
import com.example.etic.data.local.dao.TipoPrioridadDao
import com.example.etic.data.local.dao.FabricanteDao
import com.example.etic.data.local.dao.EstatusInspeccionDetDao
import com.example.etic.data.local.dao.InspeccionDao
import com.example.etic.data.local.dao.SeveridadDao
import com.example.etic.data.local.dao.EquipoDao
import com.example.etic.data.local.dao.TipoInspeccionDao
import com.example.etic.data.local.dao.InspeccionDetDao
import com.example.etic.data.local.entities.Usuario
import com.example.etic.data.local.entities.Problema
import com.example.etic.data.local.entities.LineaBase
import com.example.etic.data.local.entities.Ubicacion
import com.example.etic.data.local.entities.TipoPrioridad
import com.example.etic.data.local.entities.Fabricante
import com.example.etic.data.local.entities.EstatusInspeccionDet
import com.example.etic.data.local.entities.Inspeccion
import com.example.etic.data.local.entities.Severidad
import com.example.etic.data.local.entities.Equipo
import com.example.etic.data.local.entities.TipoInspeccion
import com.example.etic.data.local.entities.InspeccionDet

@Database(
    entities = [
        Usuario::class,
        Problema::class,
        LineaBase::class,
        Ubicacion::class,
        EstatusInspeccionDet::class,
        TipoPrioridad::class,
        Fabricante::class,
        InspeccionDet::class,
        Inspeccion::class,
        Severidad::class,
        Equipo::class,
        TipoInspeccion::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun problemaDao(): ProblemaDao
    abstract fun lineaBaseDao(): LineaBaseDao
    abstract fun ubicacionDao(): UbicacionDao
    abstract fun estatusInspeccionDetDao(): EstatusInspeccionDetDao
    abstract fun tipoPrioridadDao(): TipoPrioridadDao
    abstract fun fabricanteDao(): FabricanteDao
    abstract fun inspeccionDetDao(): InspeccionDetDao
    abstract fun inspeccionDao(): InspeccionDao
    abstract fun severidadDao(): SeveridadDao
    abstract fun equipoDao(): EquipoDao
    abstract fun tipoInspeccionDao(): TipoInspeccionDao
}
