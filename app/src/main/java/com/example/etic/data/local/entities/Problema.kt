package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "problemas")
data class Problema(
    @PrimaryKey
    @ColumnInfo(name = "Id_Problema")
    val idProblema: String,

    @ColumnInfo(name = "Numero_Problema")
    val numeroProblema: Int? = null,

    @ColumnInfo(name = "Id_Tipo_Inspeccion")
    val idTipoInspeccion: String? = null,

    @ColumnInfo(name = "Id_Sitio")
    val idSitio: String? = null,

    @ColumnInfo(name = "Id_Inspeccion")
    val idInspeccion: String? = null,

    @ColumnInfo(name = "Id_Inspeccion_Det")
    val idInspeccionDet: String? = null,

    @ColumnInfo(name = "Id_Ubicacion")
    val idUbicacion: String? = null,

    @ColumnInfo(name = "Problem_Phase")
    val problemPhase: String? = null,

    @ColumnInfo(name = "Reference_Phase")
    val referencePhase: String? = null,

    @ColumnInfo(name = "Problem_Temperature")
    val problemTemperature: Double? = null,

    @ColumnInfo(name = "Reference_Temperature")
    val referenceTemperature: Double? = null,

    @ColumnInfo(name = "Problem_Rms")
    val problemRms: Double? = null,

    @ColumnInfo(name = "Reference_Rms")
    val referenceRms: Double? = null,

    @ColumnInfo(name = "Additional_Info")
    val additionalInfo: String? = null,

    @ColumnInfo(name = "Additional_Rms")
    val additionalRms: Double? = null,

    @ColumnInfo(name = "Emissivity_Check")
    val emissivityCheck: String? = null,

    @ColumnInfo(name = "Emissivity")
    val emissivity: Double? = null,

    @ColumnInfo(name = "Indirect_Temp_Check")
    val indirectTempCheck: String? = null,

    @ColumnInfo(name = "Temp_Ambient_Check")
    val tempAmbientCheck: String? = null,

    @ColumnInfo(name = "Temp_Ambient")
    val tempAmbient: Double? = null,

    @ColumnInfo(name = "Environment_Check")
    val environmentCheck: String? = null,

    @ColumnInfo(name = "Environment")
    val environment: String? = null,

    @ColumnInfo(name = "Ir_File")
    val irFile: String? = null,

    @ColumnInfo(name = "Ir_File_Date")
    val irFileDate: String? = null,

    @ColumnInfo(name = "Ir_File_Time")
    val irFileTime: String? = null,

    @ColumnInfo(name = "Photo_File")
    val photoFile: String? = null,

    @ColumnInfo(name = "Photo_File_Date")
    val photoFileDate: String? = null,

    @ColumnInfo(name = "Photo_File_Time")
    val photoFileTime: String? = null,

    @ColumnInfo(name = "Wind_Speed_Check")
    val windSpeedCheck: String? = null,

    @ColumnInfo(name = "Wind_Speed")
    val windSpeed: Double? = null,

    @ColumnInfo(name = "Id_Fabricante")
    val idFabricante: String? = null,

    @ColumnInfo(name = "Rated_Load_Check")
    val ratedLoadCheck: String? = null,

    @ColumnInfo(name = "Rated_Load")
    val ratedLoad: String? = null,

    @ColumnInfo(name = "Circuit_Voltage_Check")
    val circuitVoltageCheck: String? = null,

    @ColumnInfo(name = "Circuit_Voltage")
    val circuitVoltage: String? = null,

    @ColumnInfo(name = "Id_Falla")
    val idFalla: String? = null,

    @ColumnInfo(name = "Id_Equipo")
    val idEquipo: String? = null,

    @ColumnInfo(name = "Component_Comment")
    val componentComment: String? = null,

    @ColumnInfo(name = "Estatus_Problema")
    val estatusProblema: String? = null,

    @ColumnInfo(name = "Aumento_Temperatura")
    val aumentoTemperatura: Double? = null,

    @ColumnInfo(name = "Id_Severidad")
    val idSeveridad: String? = null,

    @ColumnInfo(name = "Estatus")
    val estatus: String? = null,

    @ColumnInfo(name = "Ruta")
    val ruta: String? = null,

    @ColumnInfo(name = "hazard_Type")
    val hazardType: String? = null,

    @ColumnInfo(name = "hazard_Classification")
    val hazardClassification: String? = null,

    @ColumnInfo(name = "hazard_Group")
    val hazardGroup: String? = null,

    @ColumnInfo(name = "hazard_Issue")
    val hazardIssue: String? = null,

    @ColumnInfo(name = "Rpm")
    val rpm: Double? = null,

    @ColumnInfo(name = "Bearing_Type")
    val bearingType: String? = null,

    @ColumnInfo(name = "Es_Cronico")
    val esCronico: String? = null,

    @ColumnInfo(name = "Cerrado_En_Inspeccion")
    val cerradoEnInspeccion: String? = null,

    @ColumnInfo(name = "Creado_Por")
    val creadoPor: String? = null,

    @ColumnInfo(name = "Fecha_Creacion")
    val fechaCreacion: String? = null,

    @ColumnInfo(name = "Modificado_Por")
    val modificadoPor: String? = null,

    @ColumnInfo(name = "Fecha_Mod")
    val fechaMod: String? = null
)

