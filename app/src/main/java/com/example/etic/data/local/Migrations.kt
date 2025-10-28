package com.example.etic.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS problemas (
                Id_Problema TEXT NOT NULL,
                Id_Tipo_Inspeccion TEXT,
                Numero_Problema INTEGER,
                Id_Sitio TEXT,
                Id_Inspeccion TEXT,
                Id_Inspeccion_Det TEXT,
                Id_Ubicacion TEXT,
                Problem_Phase TEXT,
                Reference_Phase TEXT,
                Problem_Temperature REAL,
                Reference_Temperature REAL,
                Problem_Rms REAL,
                Reference_Rms REAL,
                Additional_Info TEXT,
                Additional_Rms REAL,
                Emissivity_Check TEXT,
                Emissivity REAL,
                Indirect_Temp_Check TEXT,
                Temp_Ambient_Check TEXT,
                Temp_Ambient REAL,
                Environment_Check TEXT,
                Environment TEXT,
                Ir_File TEXT,
                Ir_File_Date TEXT,
                Ir_File_Time TEXT,
                Photo_File TEXT,
                Photo_File_Date TEXT,
                Photo_File_Time TEXT,
                Wind_Speed_Check TEXT,
                Wind_Speed REAL,
                Id_Fabricante TEXT,
                Rated_Load_Check TEXT,
                Rated_Load TEXT,
                Circuit_Voltage_Check TEXT,
                Circuit_Voltage TEXT,
                Id_Falla TEXT,
                Id_Equipo TEXT,
                Component_Comment TEXT,
                Estatus_Problema TEXT,
                Aumento_Temperatura REAL,
                Id_Severidad TEXT,
                Estatus TEXT,
                Ruta TEXT,
                hazard_Type TEXT,
                hazard_Classification TEXT,
                hazard_Group TEXT,
                hazard_Issue TEXT,
                Rpm REAL,
                Bearing_Type TEXT,
                Es_Cronico TEXT,
                Cerrado_En_Inspeccion TEXT,
                Creado_Por TEXT,
                Fecha_Creacion TEXT,
                Modificado_Por TEXT,
                Fecha_Mod TEXT,
                PRIMARY KEY(Id_Problema)
            )
            """
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS linea_base (
                Id_Linea_Base TEXT NOT NULL,
                Id_Sitio TEXT DEFAULT 'flag_export',
                Id_Ubicacion TEXT,
                Id_Inspeccion TEXT,
                Id_Inspeccion_Det TEXT,
                MTA REAL,
                Temp_max REAL,
                Temp_amb REAL,
                Notas TEXT,
                Archivo_IR TEXT,
                Archivo_ID TEXT,
                Ruta TEXT,
                Estatus TEXT,
                Creado_Por TEXT,
                Fecha_Creacion TEXT,
                Modificado_Por TEXT,
                Fecha_Mod TEXT,
                PRIMARY KEY(Id_Linea_Base)
            )
            """
        )
    }
}
