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
import com.example.etic.data.local.dao.CausaPrincipalDao
import com.example.etic.data.local.dao.ClienteDao
import com.example.etic.data.local.dao.EstatusInspeccionDao
import com.example.etic.data.local.dao.FallaDao
import com.example.etic.data.local.dao.FaseDao
import com.example.etic.data.local.dao.GrupoDao
import com.example.etic.data.local.dao.GrupoSitiosDao
import com.example.etic.data.local.dao.HistorialProblemaDao
import com.example.etic.data.local.dao.RecomendacionDao
import com.example.etic.data.local.dao.SitioDao
import com.example.etic.data.local.dao.TipoAmbienteDao
import com.example.etic.data.local.dao.TipoFallaDao
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
import com.example.etic.data.local.entities.CausaPrincipal
import com.example.etic.data.local.entities.Cliente
import com.example.etic.data.local.entities.DatosReporte
import com.example.etic.data.local.entities.EstatusColorText
import com.example.etic.data.local.entities.EstatusInspeccion
import com.example.etic.data.local.entities.Falla
import com.example.etic.data.local.entities.Fase
import com.example.etic.data.local.entities.Grupo
import com.example.etic.data.local.entities.GrupoSitios
import com.example.etic.data.local.entities.HistorialProblema
import com.example.etic.data.local.entities.Recomendacion
import com.example.etic.data.local.entities.Sitio
import com.example.etic.data.local.entities.TipoAmbiente
import com.example.etic.data.local.entities.TipoFalla

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
        TipoInspeccion::class,
        // Nuevas entidades
        CausaPrincipal::class,
        Cliente::class,
        DatosReporte::class,
        EstatusColorText::class,
        EstatusInspeccion::class,
        Falla::class,
        Fase::class,
        Grupo::class,
        GrupoSitios::class,
        HistorialProblema::class,
        Recomendacion::class,
        Sitio::class,
        TipoAmbiente::class,
        TipoFalla::class,
    ],
    version = 10,
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
    // Nuevos DAOs (solo lectura - activos)
    abstract fun causaPrincipalDao(): CausaPrincipalDao
    abstract fun clienteDao(): ClienteDao
    abstract fun estatusInspeccionDao(): EstatusInspeccionDao
    abstract fun fallaDao(): FallaDao
    abstract fun faseDao(): FaseDao
    abstract fun grupoDao(): GrupoDao
    abstract fun grupoSitiosDao(): GrupoSitiosDao
    abstract fun historialProblemaDao(): HistorialProblemaDao
    abstract fun recomendacionDao(): RecomendacionDao
    abstract fun sitioDao(): SitioDao
    abstract fun tipoAmbienteDao(): TipoAmbienteDao
    abstract fun tipoFallaDao(): TipoFallaDao
}
