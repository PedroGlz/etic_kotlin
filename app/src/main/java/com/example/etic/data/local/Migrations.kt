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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // No-op if table already exists in the prepackaged DB. Create it otherwise.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ubicaciones (
                Id_Ubicacion TEXT NOT NULL,
                Id_Sitio TEXT,
                Id_Ubicacion_padre TEXT,
                Id_Tipo_Prioridad TEXT,
                Id_Tipo_Inspeccion TEXT,
                Ubicacion TEXT,
                Descripcion TEXT,
                Es_Equipo TEXT,
                Codigo_Barras TEXT,
                Nivel_arbol INTEGER,
                LIMITE REAL,
                Fabricante TEXT,
                Nombre_Foto TEXT,
                Ruta TEXT,
                Estatus TEXT,
                Creado_Por TEXT,
                Fecha_Creacion TEXT,
                Modificado_Por TEXT,
                Fecha_Mod TEXT,
                Id_Inspeccion TEXT,
                PRIMARY KEY(Id_Ubicacion)
            )
            """
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS estatus_inspeccion_det (
                Id_Status_Inspeccion_Det TEXT NOT NULL,
                Estatus_Inspeccion_Det   TEXT DEFAULT NULL,
                Desc_Estatus_Det         TEXT DEFAULT NULL,
                Estatus                  TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por               TEXT DEFAULT NULL,
                Fecha_Creacion           TEXT DEFAULT NULL,
                Modificado_Por           TEXT DEFAULT NULL,
                Fecha_Mod                TEXT DEFAULT NULL,
                Id_Inspeccion            TEXT DEFAULT 'flag_export',
                Id_Sitio                 TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Status_Inspeccion_Det)
            )
            """
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Ensure tables exist if not present in the prepackaged DB
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS tipo_prioridades (
                Id_Tipo_Prioridad TEXT NOT NULL,
                Tipo_Prioridad    TEXT DEFAULT NULL,
                Desc_Prioridad    TEXT DEFAULT NULL,
                Estatus           TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por        TEXT DEFAULT NULL,
                Fecha_Creacion    TEXT DEFAULT NULL,
                Modificado_Por    TEXT DEFAULT NULL,
                Fecha_Mod         TEXT DEFAULT NULL,
                Id_Inspeccion     TEXT DEFAULT 'flag_export',
                Id_Sitio          TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Tipo_Prioridad)
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fabricantes (
                Id_Fabricante      TEXT NOT NULL,
                Id_Tipo_Inspeccion TEXT DEFAULT NULL,
                Fabricante         TEXT DEFAULT NULL,
                Desc_Fabricante    TEXT DEFAULT NULL,
                Estatus            TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por         TEXT DEFAULT NULL,
                Fecha_Creacion     TEXT DEFAULT NULL,
                Modificado_Por     TEXT DEFAULT NULL,
                Fecha_Mod          TEXT DEFAULT NULL,
                Id_Inspeccion      TEXT DEFAULT 'flag_export',
                Id_Sitio           TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Fabricante)
            )
            """
        )
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS inspecciones_det (
                Id_Inspeccion_Det        TEXT NOT NULL,
                Id_Inspeccion            TEXT DEFAULT NULL,
                Id_Ubicacion             TEXT DEFAULT NULL,
                Id_Status_Inspeccion_Det TEXT DEFAULT NULL,
                Notas_Inspeccion         TEXT DEFAULT NULL,
                Estatus                  TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) NOT NULL DEFAULT 'Activo',
                Id_Estatus_Color_Text    INTEGER DEFAULT 1,
                expanded                 TEXT CHECK (expanded IN ('1', '0') ) DEFAULT '0',
                selected                 TEXT CHECK (selected IN ('1', '0') ) DEFAULT '0',
                Creado_Por               TEXT DEFAULT NULL,
                Fecha_Creacion           TEXT DEFAULT NULL,
                Modificado_Por           TEXT DEFAULT NULL,
                Fecha_Mod                TEXT DEFAULT NULL,
                Id_Sitio                 TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Inspeccion_Det)
            )
            """
        )
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS inspecciones (
                Id_Inspeccion        TEXT NOT NULL,
                Id_Sitio             TEXT,
                Id_Grupo_Sitios      TEXT,
                Id_Cliente           TEXT,
                Id_Status_Inspeccion TEXT,
                Fecha_Inicio         TEXT,
                Fecha_Fin            TEXT,
                Fotos_Ruta           TEXT,
                IR_Imagen_Inicial    TEXT,
                DIG_Imagen_Inicial   TEXT,
                No_Dias              INTEGER,
                Unidad_Temp          TEXT,
                No_Inspeccion        INTEGER,
                No_Inspeccion_Ant    INTEGER,
                Estatus              TEXT,
                Creado_Por           TEXT,
                Fecha_Creacion       TEXT,
                Modificado_Por       TEXT,
                Fecha_Mod            TEXT,
                PRIMARY KEY (Id_Inspeccion)
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS severidades (
                Id_Severidad  TEXT NOT NULL,
                Severidad     TEXT,
                Descripcion   TEXT,
                Id_Inspeccion TEXT DEFAULT 'flag_export',
                Id_Sitio      TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Severidad)
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS equipos (
                Id_Equipo      TEXT NOT NULL,
                Equipo         TEXT,
                Descr_equipo   TEXT,
                Estatus        TEXT,
                Creado_Por     TEXT,
                Fecha_Creacion TEXT,
                Modificado_Por TEXT,
                Fecha_Mod      TEXT,
                Id_Inspeccion  TEXT DEFAULT 'flag_export',
                Id_Sitio       TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Equipo)
            )
            """
        )
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS tipo_inspecciones (
                Id_Tipo_Inspeccion TEXT NOT NULL,
                Tipo_Inspeccion    TEXT DEFAULT NULL,
                Desc_Inspeccion    TEXT DEFAULT NULL,
                Estatus            TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por         TEXT DEFAULT NULL,
                Fecha_Creacion     TEXT DEFAULT NULL,
                Modificado_Por     TEXT DEFAULT NULL,
                Fecha_Mod          TEXT DEFAULT NULL,
                Id_Inspeccion      TEXT DEFAULT 'flag_export',
                Id_Sitio           TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Tipo_Inspeccion)
            )
            """
        )
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS causa_principal (
                Id_Causa_Raiz      TEXT NOT NULL,
                Id_Tipo_Inspeccion TEXT DEFAULT NULL,
                Id_Falla           TEXT DEFAULT NULL,
                Causa_Raiz         TEXT DEFAULT NULL,
                Estatus            TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por         TEXT DEFAULT NULL,
                Fecha_Creacion     TEXT DEFAULT NULL,
                Modificado_Por     TEXT DEFAULT NULL,
                Fecha_Mod          TEXT DEFAULT NULL,
                Id_Inspeccion      TEXT DEFAULT 'flag_export',
                Id_Sitio           TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Causa_Raiz)
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS clientes (
                Id_Cliente       TEXT NOT NULL,
                Id_Compania      TEXT DEFAULT NULL,
                Id_Giro          TEXT DEFAULT NULL,
                Razon_Social     TEXT DEFAULT NULL,
                Nombre_Comercial TEXT DEFAULT NULL,
                RFC              TEXT DEFAULT NULL,
                Imagen_Cliente   TEXT DEFAULT NULL,
                Estatus          TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por       TEXT DEFAULT NULL,
                Fecha_Creacion   TEXT DEFAULT NULL,
                Modificado_Por   TEXT DEFAULT NULL,
                Fecha_Mod        TEXT DEFAULT NULL,
                Id_Inspeccion    TEXT DEFAULT 'flag_export',
                Id_Sitio         TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Cliente)
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS datos_reporte (
                Id_Datos_Reporte            INTEGER PRIMARY KEY AUTOINCREMENT,
                Id_Inspeccion               TEXT    DEFAULT NULL,
                detalle_ubicacion           TEXT    DEFAULT NULL,
                nombre_contacto             TEXT    DEFAULT NULL,
                puesto_contacto             TEXT    DEFAULT NULL,
                fecha_inicio_ra             TEXT    DEFAULT NULL,
                fecha_fin_ra                TEXT    DEFAULT NULL,
                nombre_img_portada          TEXT    DEFAULT NULL,
                descripcion_reporte         TEXT    DEFAULT NULL,
                areas_inspeccionadas        TEXT    DEFAULT NULL,
                recomendacion_reporte       TEXT    DEFAULT NULL,
                imagen_recomendacion        TEXT    DEFAULT NULL,
                imagen_recomendacion_2      TEXT    DEFAULT NULL,
                referencia_reporte          TEXT    DEFAULT NULL,
                arrayElementosSeleccionados TEXT    DEFAULT NULL,
                arrayProblemasSeleccionados TEXT    DEFAULT NULL,
                Id_Sitio                    TEXT    DEFAULT 'flag_export'
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS estatus_color_text (
                Id_Estatus_Color_Text INTEGER PRIMARY KEY AUTOINCREMENT,
                Color_Text            TEXT    DEFAULT NULL,
                Descripcion           TEXT    NOT NULL,
                Id_Inspeccion         TEXT    DEFAULT 'flag_export',
                Id_Sitio              TEXT    DEFAULT 'flag_export'
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS estatus_inspeccion (
                Id_Status_Inspeccion TEXT NOT NULL,
                Status_Inspeccion    TEXT DEFAULT NULL,
                Desc_Status          TEXT DEFAULT NULL,
                Estatus              TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por           TEXT DEFAULT NULL,
                Fecha_Creacion       TEXT DEFAULT NULL,
                Modificado_Por       TEXT DEFAULT NULL,
                Fecha_Mod            TEXT DEFAULT NULL,
                Id_Inspeccion        TEXT DEFAULT 'flag_export',
                Id_Sitio             TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Status_Inspeccion)
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fallas (
                Id_Falla           TEXT NOT NULL,
                Id_Tipo_Falla      TEXT DEFAULT NULL,
                Id_Tipo_Inspeccion TEXT DEFAULT NULL,
                Falla              TEXT DEFAULT NULL,
                Estatus            TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por         TEXT DEFAULT NULL,
                Fecha_Creacion     TEXT DEFAULT NULL,
                Modificado_Por     TEXT DEFAULT NULL,
                Fecha_Mod          TEXT DEFAULT NULL,
                Id_Inspeccion      TEXT DEFAULT 'flag_export',
                Id_Sitio           TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Falla)
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS fases (
                Id_Fase        TEXT NOT NULL,
                Nombre_Fase    TEXT DEFAULT NULL,
                Descripcion    TEXT DEFAULT NULL,
                Estatus        TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por     TEXT DEFAULT NULL,
                Fecha_Creacion TEXT DEFAULT NULL,
                Modificado_Por TEXT DEFAULT NULL,
                Fecha_Mod      TEXT DEFAULT NULL,
                Id_Inspeccion  TEXT DEFAULT 'flag_export',
                Id_Sitio       TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Fase)
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS grupos (
                Id_Grupo       TEXT NOT NULL,
                Grupo          TEXT DEFAULT NULL,
                Estatus        TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por     TEXT DEFAULT NULL,
                Fecha_Creacion TEXT DEFAULT NULL,
                Modificado_Por TEXT DEFAULT NULL,
                Fecha_Mod      TEXT DEFAULT NULL,
                Id_Inspeccion  TEXT DEFAULT 'flag_export',
                Id_Sitio       TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Grupo)
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS grupos_sitios (
                Id_Grupo_Sitios TEXT NOT NULL,
                Id_Cliente      TEXT DEFAULT NULL,
                Grupo           TEXT DEFAULT NULL,
                Estatus         TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por      TEXT DEFAULT NULL,
                Fecha_Creacion  TEXT DEFAULT NULL,
                Modificado_Por  TEXT DEFAULT NULL,
                Fecha_Mod       TEXT DEFAULT NULL,
                Id_Inspeccion   TEXT DEFAULT 'flag_export',
                Id_Sitio        TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Grupo_Sitios)
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS historial_problemas (
                Id_Historial_Problema TEXT NOT NULL,
                Id_Problema           TEXT DEFAULT NULL,
                Id_Problema_Anterior  TEXT DEFAULT NULL,
                Id_Problema_Original  TEXT DEFAULT NULL,
                Estatus               TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por            TEXT DEFAULT NULL,
                Fecha_Creacion        TEXT DEFAULT NULL,
                Modificado_Por        TEXT DEFAULT NULL,
                Fecha_Mod             TEXT DEFAULT NULL,
                Id_Inspeccion         TEXT DEFAULT 'flag_export',
                Id_Sitio              TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Historial_Problema)
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS recomendaciones (
                Id_Recomendacion   TEXT NOT NULL,
                Id_Tipo_Inspeccion TEXT DEFAULT NULL,
                Id_Causa_Raiz      TEXT DEFAULT NULL,
                Recomendacion      TEXT DEFAULT NULL,
                Estatus            TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por         TEXT DEFAULT NULL,
                Fecha_Creacion     TEXT DEFAULT NULL,
                Modificado_Por     TEXT DEFAULT NULL,
                Fecha_Mod          TEXT DEFAULT NULL,
                Id_Inspeccion      TEXT DEFAULT 'flag_export',
                Id_Sitio           TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Recomendacion)
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sitios (
                Id_Sitio          TEXT NOT NULL,
                Id_Cliente        TEXT DEFAULT NULL,
                Id_Grupo_Sitios   TEXT DEFAULT NULL,
                Sitio             TEXT DEFAULT NULL,
                Desc_Sitio        TEXT DEFAULT NULL,
                Direccion         TEXT DEFAULT NULL,
                Colonia           TEXT DEFAULT NULL,
                Estado            TEXT DEFAULT NULL,
                Municipio         TEXT DEFAULT NULL,
                Folder            TEXT DEFAULT NULL,
                Contacto_1        TEXT DEFAULT NULL,
                Puesto_Contacto_1 TEXT DEFAULT NULL,
                Contacto_2        TEXT DEFAULT NULL,
                Puesto_Contacto_2 TEXT DEFAULT NULL,
                Contacto_3        TEXT DEFAULT NULL,
                Puesto_Contacto_3 TEXT DEFAULT NULL,
                Estatus           TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por        TEXT DEFAULT NULL,
                Fecha_Creacion    TEXT DEFAULT NULL,
                Modificado_Por    TEXT DEFAULT NULL,
                Fecha_Mod         TEXT DEFAULT NULL,
                Id_Inspeccion     TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Sitio)
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS tipo_ambientes (
                Id_Tipo_Ambiente TEXT NOT NULL,
                Nombre           TEXT DEFAULT NULL,
                Descripcion      TEXT DEFAULT NULL,
                Adjust           REAL DEFAULT NULL,
                Estatus          TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Id_Inspeccion    TEXT DEFAULT 'flag_export',
                Id_Sitio         TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Tipo_Ambiente)
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS tipo_fallas (
                Id_Tipo_Falla      TEXT NOT NULL,
                Id_Tipo_Inspeccion TEXT DEFAULT NULL,
                Tipo_Falla         TEXT DEFAULT NULL,
                Desc_Tipo_Falla    TEXT DEFAULT NULL,
                Estatus            TEXT CHECK (Estatus IN ('Activo', 'Inactivo') ) DEFAULT 'Activo',
                Creado_Por         TEXT DEFAULT NULL,
                Fecha_Creacion     TEXT DEFAULT NULL,
                Modificado_Por     TEXT DEFAULT NULL,
                Fecha_Mod          TEXT DEFAULT NULL,
                Id_Inspeccion      TEXT DEFAULT 'flag_export',
                Id_Sitio           TEXT DEFAULT 'flag_export',
                PRIMARY KEY (Id_Tipo_Falla)
            )
            """
        )
    }
}
