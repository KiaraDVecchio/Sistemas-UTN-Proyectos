USE GD2C2025;
GO

--Creación de esquema
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'INNER_JOY')
    BEGIN
        EXEC('CREATE SCHEMA INNER_JOY');
    END
GO

--Eliminacion de tablas si ya existen
DROP TABLE IF EXISTS INNER_JOY.EncuestaCompletada;
DROP TABLE IF EXISTS INNER_JOY.Pregunta;
DROP TABLE IF EXISTS INNER_JOY.Final;
DROP TABLE IF EXISTS INNER_JOY.Evaluacion;
DROP TABLE IF EXISTS INNER_JOY.TP;
DROP TABLE IF EXISTS INNER_JOY.MedioDePago;
DROP TABLE IF EXISTS INNER_JOY.Factura;
DROP TABLE IF EXISTS INNER_JOY.EstadoDeInscripcion;
DROP TABLE IF EXISTS INNER_JOY.Modulo;
DROP TABLE IF EXISTS INNER_JOY.Curso;
DROP TABLE IF EXISTS INNER_JOY.Dia;
DROP TABLE IF EXISTS INNER_JOY.Alumno;
DROP TABLE IF EXISTS INNER_JOY.Profesor;
DROP TABLE IF EXISTS INNER_JOY.Usuario;
DROP TABLE IF EXISTS INNER_JOY.Sede;
DROP TABLE IF EXISTS INNER_JOY.Institucion;
DROP TABLE IF EXISTS INNER_JOY.Localidad;
DROP TABLE IF EXISTS INNER_JOY.Provincia;
DROP TABLE IF EXISTS INNER_JOY.Turno;
DROP TABLE IF EXISTS INNER_JOY.DiaPorCurso;
DROP TABLE IF EXISTS INNER_JOY.InscripcionCurso;
DROP TABLE IF EXISTS INNER_JOY.Periodo;
DROP TABLE IF EXISTS INNER_JOY.DetalleDeFactura;
DROP TABLE IF EXISTS INNER_JOY.Pago;
DROP TABLE IF EXISTS INNER_JOY.TpPorAlumno;
DROP TABLE IF EXISTS INNER_JOY.EvaluacionPorAlumno;
DROP TABLE IF EXISTS INNER_JOY.FinalPorAlumno;
DROP TABLE IF EXISTS INNER_JOY.InscripcionFinal;
DROP TABLE IF EXISTS INNER_JOY.Respuesta;
GO

--CREACION DE TABLAS

CREATE TABLE INNER_JOY.Provincia (
    provincia_id      BIGINT IDENTITY(1,1) PRIMARY KEY,
    provincia_nombre  NVARCHAR(255) NOT NULL
);

CREATE TABLE INNER_JOY.Localidad (
    localidad_id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    localidad_nombre      NVARCHAR(255) NOT NULL,
    localidad_provincia_id BIGINT NOT NULL,
    FOREIGN KEY (localidad_provincia_id) REFERENCES INNER_JOY.Provincia (provincia_id)
);

CREATE TABLE INNER_JOY.Institucion (
    institucion_id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    institucion_nombre      NVARCHAR(255) NOT NULL,
    institucion_razon_social NVARCHAR(255),
    institucion_cuit        NVARCHAR(255)
);

CREATE TABLE INNER_JOY.Sede (
    sede_id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    sede_direccion_id   NVARCHAR(255),
    sede_mail           NVARCHAR(255),
    sede_nombre         NVARCHAR(255),
    sede_telefono       NVARCHAR(255),
    sede_institucion_id BIGINT NOT NULL,
    sede_localidad_id   BIGINT NOT NULL,
    FOREIGN KEY (sede_institucion_id) REFERENCES INNER_JOY.Institucion (institucion_id),
    FOREIGN KEY (sede_localidad_id)   REFERENCES INNER_JOY.Localidad (localidad_id)
);

CREATE TABLE INNER_JOY.Usuario (
    usuario_id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    usuario_nombre           NVARCHAR(255) NOT NULL,
    usuario_apellido         NVARCHAR(255) NOT NULL,
    usuario_fecha_nacimiento DATETIME2(6),
    usuario_dni              NVARCHAR(255),
    usuario_mail             NVARCHAR(255),
    usuario_telefono         NVARCHAR(255),
    usuario_direccion        NVARCHAR(255),
    usuario_localidad_id     BIGINT,
    FOREIGN KEY (usuario_localidad_id) REFERENCES INNER_JOY.Localidad (localidad_id)
);

CREATE TABLE INNER_JOY.Profesor (
    profesor_id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    profesor_usuario_id BIGINT NOT NULL,
    FOREIGN KEY (profesor_usuario_id) REFERENCES INNER_JOY.Usuario (usuario_id)
);

CREATE TABLE INNER_JOY.Alumno (
    alumno_legajo     BIGINT PRIMARY KEY,
    alumno_usuario_id BIGINT NOT NULL,
    FOREIGN KEY (alumno_usuario_id) REFERENCES INNER_JOY.Usuario (usuario_id)
);

CREATE TABLE INNER_JOY.Dia (
    dia_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    dia_nombre VARCHAR(255) NOT NULL
);

CREATE TABLE INNER_JOY.Turno (
    turno_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    turno_descripcion VARCHAR(255) NOT NULL
);

CREATE TABLE INNER_JOY.Curso (
    curso_codigo        BIGINT PRIMARY KEY,
    curso_sede_id       BIGINT NOT NULL,
    curso_profesor_id   BIGINT NOT NULL,
    curso_nombre        VARCHAR(255) NOT NULL,
    curso_descripcion   VARCHAR(255),
    curso_categoria     VARCHAR(255),
    curso_fecha_inicio  DATETIME2(6),
    curso_fecha_fin     DATETIME2(6),
    curso_duracion_meses BIGINT,
    curso_turno_id       BIGINT,
    curso_precio_mensual DECIMAL(38,2),
    FOREIGN KEY (curso_sede_id) REFERENCES INNER_JOY.Sede (sede_id),
    FOREIGN KEY (curso_profesor_id) REFERENCES INNER_JOY.Profesor (profesor_id),
    FOREIGN KEY (curso_turno_id)    REFERENCES INNER_JOY.Turno (turno_id) 
);

CREATE TABLE INNER_JOY.Modulo (
    modulo_id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    modulo_curso_codigo BIGINT NOT NULL,
    modulo_nombre      VARCHAR(255),
    modulo_descripcion VARCHAR(255),
    FOREIGN KEY (modulo_curso_codigo) REFERENCES INNER_JOY.Curso (curso_codigo)
);

CREATE TABLE INNER_JOY.DiaPorCurso (
    dia_id       BIGINT NOT NULL,
    curso_id     BIGINT NOT NULL,
    PRIMARY KEY (dia_id, curso_id),
    FOREIGN KEY (dia_id) REFERENCES INNER_JOY.Dia (dia_id),
    FOREIGN KEY (curso_id) REFERENCES INNER_JOY.Curso (curso_codigo)
);

CREATE TABLE INNER_JOY.EstadoDeInscripcion (
    estado_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    estado_descripcion VARCHAR(255) NOT NULL
);

CREATE TABLE INNER_JOY.InscripcionCurso (
    inscrip_numero        BIGINT PRIMARY KEY,
    inscrip_fecha         DATETIME2(6) DEFAULT GETDATE(),
    inscrip_alumno_legajo BIGINT NOT NULL,
    inscrip_curso_codigo  BIGINT NOT NULL,
    inscrip_estado        BIGINT NOT NULL,
    inscrip_fecha_respuesta DATETIME2(6) -- CHECK (inscrip_fecha_respuesta IN ('pendiente','aprobada','rechazada')),
    FOREIGN KEY (inscrip_alumno_legajo) REFERENCES INNER_JOY.Alumno (alumno_legajo),
    FOREIGN KEY (inscrip_curso_codigo)  REFERENCES INNER_JOY.Curso  (curso_codigo),
    FOREIGN KEY (inscrip_estado) REFERENCES INNER_JOY.EstadoDeInscripcion (estado_id)
);

CREATE TABLE INNER_JOY.Factura (
    factura_numero          BIGINT PRIMARY KEY,
    factura_fecha_emision   DATETIME2(6) DEFAULT GETDATE(),
    factura_fecha_vencimiento DATETIME2(6),
    factura_importe_total   DECIMAL(18,2),
    factura_alumno_id       BIGINT NOT NULL,
    FOREIGN KEY (factura_alumno_id) REFERENCES INNER_JOY.Alumno (alumno_legajo)
);

CREATE TABLE INNER_JOY.Periodo (
    periodo_id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    periodo_mes     BIGINT NOT NULL,
    periodo_anio    BIGINT NOT NULL
);

CREATE TABLE INNER_JOY.DetalleDeFactura (
    detalle_factura_id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    detalle_factura_numero    BIGINT NOT NULL,   
    detalle_factura_curso_id  BIGINT NOT NULL,
    detalle_factura_importe DECIMAL(18,2) NOT NULL,
    detalle_factura_periodo_id BIGINT NOT NULL,
    FOREIGN KEY (detalle_factura_numero)   REFERENCES INNER_JOY.Factura (factura_numero),
    FOREIGN KEY (detalle_factura_curso_id) REFERENCES INNER_JOY.Curso (curso_codigo),
    FOREIGN KEY (detalle_factura_periodo_id) REFERENCES INNER_JOY.Periodo (periodo_id)

);

CREATE TABLE INNER_JOY.MedioDePago (
    medio_de_pago_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    medio_de_pago_descripcion VARCHAR(255) NOT NULL
);

CREATE TABLE INNER_JOY.Pago (
    pago_id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    pago_factura_numero BIGINT NOT NULL,
    pago_fecha_pago     DATETIME2(6),
    pago_importe_total  DECIMAL(18,2),
    medio_pago          BIGINT NOT NULL,
    FOREIGN KEY (pago_factura_numero) REFERENCES INNER_JOY.Factura (factura_numero),
    FOREIGN KEY (medio_pago) REFERENCES INNER_JOY.MedioDePago (medio_de_pago_id)
);

CREATE TABLE INNER_JOY.TP (
    tp_id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    tp_curso_codigo BIGINT NOT NULL,
    tp_fecha        DATETIME2(6),
    FOREIGN KEY (tp_curso_codigo) REFERENCES INNER_JOY.Curso (curso_codigo)
);

CREATE TABLE INNER_JOY.TpPorAlumno (
    tp_id           BIGINT NOT NULL,
    tp_alumno_legajo BIGINT NOT NULL,
    tp_nota         BIGINT,
    PRIMARY KEY (tp_id, tp_alumno_legajo),
    FOREIGN KEY (tp_id)            REFERENCES INNER_JOY.TP     (tp_id),
    FOREIGN KEY (tp_alumno_legajo) REFERENCES INNER_JOY.Alumno (alumno_legajo)
);


CREATE TABLE INNER_JOY.Evaluacion (
    evaluacion_id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    evaluacion_fecha    DATETIME2(6),
    evaluacion_modulo_id BIGINT NOT NULL,
    FOREIGN KEY (evaluacion_modulo_id) REFERENCES INNER_JOY.Modulo (modulo_id)
);

CREATE TABLE INNER_JOY.EvaluacionPorAlumno (
    evaluacion_id          BIGINT NOT NULL,
    evaluacion_alumno_legajo BIGINT NOT NULL,
    evaluacion_nota        BIGINT,
    evaluacion_presente    BIT,
    evaluacion_instancia   BIGINT,
    PRIMARY KEY (evaluacion_id, evaluacion_alumno_legajo),
    FOREIGN KEY (evaluacion_id)          REFERENCES INNER_JOY.Evaluacion (evaluacion_id),
    FOREIGN KEY (evaluacion_alumno_legajo) REFERENCES INNER_JOY.Alumno     (alumno_legajo)
);

CREATE TABLE INNER_JOY.Final (
    final_id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    final_fecha       DATETIME2(6),
    final_hora        VARCHAR(255),
    final_descripcion VARCHAR(255),
    final_curso_codigo BIGINT NOT NULL,
    FOREIGN KEY (final_curso_codigo) REFERENCES INNER_JOY.Curso (curso_codigo)
);

CREATE TABLE INNER_JOY.FinalPorAlumno (
    final_alumno_legajo BIGINT NOT NULL,
    final_id            BIGINT NOT NULL,
    final_presente      BIT,
    final_nota          BIGINT,
    final_profesor_id   BIGINT,
    PRIMARY KEY (final_alumno_legajo, final_id),
    FOREIGN KEY (final_id)            REFERENCES INNER_JOY.Final    (final_id),
    FOREIGN KEY (final_alumno_legajo) REFERENCES INNER_JOY.Alumno   (alumno_legajo),
    FOREIGN KEY (final_profesor_id)  REFERENCES INNER_JOY.Profesor (profesor_id)
);

CREATE TABLE INNER_JOY.InscripcionFinal (
    inscrip_numero       BIGINT PRIMARY KEY,
    inscrip_fecha        DATETIME2(6) DEFAULT GETDATE(),
    inscrip_alumno_legajo BIGINT NOT NULL,
    inscrip_final_id     BIGINT NOT NULL,
    FOREIGN KEY (inscrip_alumno_legajo) REFERENCES INNER_JOY.Alumno (alumno_legajo),
    FOREIGN KEY (inscrip_final_id)      REFERENCES INNER_JOY.Final  (final_id)
);


CREATE TABLE INNER_JOY.EncuestaCompletada (
    encuesta_id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    encuesta_curso_codigo BIGINT NOT NULL,
    encuesta_fecha_registro DATETIME2(6),
    encuesta_observacion  VARCHAR(255),
    FOREIGN KEY (encuesta_curso_codigo) REFERENCES INNER_JOY.Curso (curso_codigo)
);

CREATE TABLE INNER_JOY.Pregunta (
    pregunta_id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    pregunta_texto VARCHAR(255) NOT NULL
);

CREATE TABLE INNER_JOY.Respuesta (
    detalle_id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    detalle_pregunta_id BIGINT NOT NULL,
    detalle_encuesta_id BIGINT NOT NULL,
    detalle_nota        BIGINT, 
    CHECK (detalle_nota BETWEEN 1 AND 10),
    FOREIGN KEY (detalle_pregunta_id) REFERENCES INNER_JOY.Pregunta (pregunta_id),
    FOREIGN KEY (detalle_encuesta_id) REFERENCES INNER_JOY.EncuestaCompletada (encuesta_id)
);
GO

--Eliminacion de si procedures si ya existen

DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_provincias;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_localidades;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_instituciones;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_sedes;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_usuarios;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_profesores;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_alumnos;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_dia;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_turno;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_cursos;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_modulo;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_dia_por_curso;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_estado_de_inscripcion;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_inscripcion_curso;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_factura;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_periodo;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_detalle_de_factura;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_medio_de_pago;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_pago;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_tps;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_tp_por_alumno;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_evaluacion;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_evaluacion_por_alumno;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_final;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_final_por_alumno;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_inscripcion_final;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_encuesta_completada;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_pregunta;
DROP PROCEDURE IF EXISTS INNER_JOY.sp_migrar_respuesta;
GO


--Creación de stored procedures

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_provincias
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.Provincia (provincia_nombre)
    SELECT DISTINCT m.Profesor_Provincia
    FROM gd_esquema.Maestra m
    WHERE m.Profesor_Provincia IS NOT NULL
    AND NOT EXISTS (
        SELECT 1 
        FROM INNER_JOY.Provincia p 
        WHERE p.provincia_nombre = m.Profesor_Provincia
    )

    UNION

    SELECT DISTINCT m.Alumno_Provincia
    FROM gd_esquema.Maestra m
    WHERE m.Alumno_Provincia IS NOT NULL
    AND NOT EXISTS (
        SELECT 1 
        FROM INNER_JOY.Provincia p 
        WHERE p.provincia_nombre = m.Alumno_Provincia
    );
END;
GO


CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_localidades
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.Localidad (localidad_nombre, localidad_provincia_id)
    SELECT DISTINCT 
        localidad_nombre,
        provincia_id
    FROM (
        SELECT 
            m.Profesor_Localidad AS localidad_nombre,
            p.provincia_id
        FROM GD_ESQUEMA.Maestra AS m
        INNER JOIN INNER_JOY.Provincia AS p 
            ON p.provincia_nombre = m.Profesor_Provincia
        WHERE m.Profesor_Localidad IS NOT NULL
          AND m.Profesor_Provincia IS NOT NULL

        UNION

        SELECT 
            m.Alumno_Localidad AS localidad_nombre,
            p.provincia_id
        FROM GD_ESQUEMA.Maestra AS m
        INNER JOIN INNER_JOY.Provincia AS p 
            ON p.provincia_nombre = m.Alumno_Provincia
        WHERE m.Alumno_Localidad IS NOT NULL
          AND m.Alumno_Provincia IS NOT NULL

        UNION

        SELECT 
            m.Sede_Provincia AS localidad_nombre,
            p.provincia_id
        FROM GD_ESQUEMA.Maestra AS m
        INNER JOIN INNER_JOY.Provincia AS p 
            ON p.provincia_nombre = m.Sede_Localidad
        WHERE m.Sede_Provincia IS NOT NULL
          AND m.Sede_Localidad IS NOT NULL
    ) AS localidades
    WHERE NOT EXISTS (
        SELECT 1
        FROM INNER_JOY.Localidad l
        WHERE l.localidad_nombre = localidades.localidad_nombre
          AND l.localidad_provincia_id = localidades.provincia_id
    );
END;
GO



CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_instituciones
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.Institucion (
        institucion_nombre,
        institucion_razon_social,
        institucion_cuit
    )
    SELECT DISTINCT
        m.Institucion_Nombre,
        m.Institucion_RazonSocial,
        m.Institucion_Cuit
    FROM GD_ESQUEMA.Maestra AS m
    WHERE m.Institucion_Nombre IS NOT NULL
      AND NOT EXISTS (
            SELECT 1 
            FROM INNER_JOY.Institucion i
            WHERE i.institucion_nombre = m.Institucion_Nombre
      );
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_sedes
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.Sede (
        sede_direccion_id,   
        sede_mail,
        sede_nombre,
        sede_telefono,
        sede_institucion_id,
        sede_localidad_id
    )
    SELECT DISTINCT
        m.Sede_Direccion,
        m.Sede_Mail,
        m.Sede_Nombre,
        m.Sede_Telefono,
        i.institucion_id,
        l.localidad_id
    FROM GD_ESQUEMA.Maestra AS m
    JOIN INNER_JOY.Provincia  AS p
         ON m.Sede_Localidad = p.provincia_nombre
    JOIN INNER_JOY.Localidad  AS l
         ON m.Sede_Provincia = l.localidad_nombre
        AND l.localidad_provincia_id = p.provincia_id
    JOIN INNER_JOY.Institucion AS i
         ON m.Institucion_Nombre = i.institucion_nombre
    WHERE m.Sede_Nombre IS NOT NULL;
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_usuarios
AS
BEGIN
    SET NOCOUNT ON;

    ;WITH Candidatos AS (
        -- Profesores (prioridad 1)
        SELECT
            m.Profesor_DNI                     AS usuario_dni,
            m.Profesor_Nombre                                AS usuario_nombre,
            m.Profesor_Apellido                              AS usuario_apellido,
            m.Profesor_FechaNacimiento                       AS usuario_fecha_nacimiento,
            m.Profesor_Mail                                  AS usuario_mail,
            m.Profesor_Telefono                              AS usuario_telefono,
            m.Profesor_Direccion                             AS usuario_direccion,
            m.Profesor_Provincia                              AS prov_nombre,
            m.Profesor_Localidad                              AS loca_nombre,
            1                                                AS rol_prioridad,
            (CASE WHEN m.Profesor_Mail      IS NOT NULL THEN 1 ELSE 0 END +
             CASE WHEN m.Profesor_Telefono  IS NOT NULL THEN 1 ELSE 0 END +
             CASE WHEN m.Profesor_Direccion IS NOT NULL THEN 1 ELSE 0 END +
             CASE WHEN m.Profesor_FechaNacimiento IS NOT NULL THEN 1 ELSE 0 END) AS score
        FROM GD_ESQUEMA.Maestra m
        WHERE m.Profesor_DNI IS NOT NULL AND m.Profesor_DNI <> ''

        UNION ALL

        -- Alumnos (prioridad 2)
        SELECT
            m.Alumno_DNI,
            m.Alumno_Nombre,
            m.Alumno_Apellido,
            m.Alumno_FechaNacimiento,
            m.Alumno_Mail,
            m.Alumno_Telefono,
            m.Alumno_Direccion,
            m.Alumno_Provincia,
            m.Alumno_Localidad,
            2,
            (CASE WHEN m.Alumno_Mail      IS NOT NULL THEN 1 ELSE 0 END +
             CASE WHEN m.Alumno_Telefono  IS NOT NULL THEN 1 ELSE 0 END +
             CASE WHEN m.Alumno_Direccion IS NOT NULL THEN 1 ELSE 0 END +
             CASE WHEN m.Alumno_FechaNacimiento IS NOT NULL THEN 1 ELSE 0 END)
        FROM GD_ESQUEMA.Maestra m
        WHERE m.Alumno_DNI IS NOT NULL AND m.Alumno_DNI <> ''
    ),
    Canonicos AS (
        SELECT *
             , ROW_NUMBER() OVER (
                   PARTITION BY usuario_dni
                   ORDER BY rol_prioridad ASC, score DESC, usuario_apellido, usuario_nombre
               ) AS rn
        FROM Candidatos
    ),
    PorDNI AS (
        SELECT
            usuario_dni,
            usuario_nombre,
            usuario_apellido,
            usuario_fecha_nacimiento,
            usuario_mail,
            usuario_telefono,
            usuario_direccion,
            prov_nombre,
            loca_nombre
        FROM Canonicos
        WHERE rn = 1
    )
    INSERT INTO INNER_JOY.Usuario (
        usuario_nombre,
        usuario_apellido,
        usuario_fecha_nacimiento,
        usuario_dni,
        usuario_mail,
        usuario_telefono,
        usuario_direccion,
        usuario_localidad_id
    )
    SELECT
        d.usuario_nombre,
        d.usuario_apellido,
        d.usuario_fecha_nacimiento,
        d.usuario_dni,
        d.usuario_mail,
        d.usuario_telefono,
        d.usuario_direccion,
        l.localidad_id
    FROM PorDNI d
    -- 1) Filtrá ya existentes por DNI (barato)
    LEFT JOIN INNER_JOY.Usuario u
           ON u.usuario_dni = d.usuario_dni
    -- 2) Recién después resolvés catálogos
    JOIN INNER_JOY.Provincia p
         ON p.provincia_nombre = d.prov_nombre
    JOIN INNER_JOY.Localidad l
         ON l.localidad_provincia_id = p.provincia_id
        AND l.localidad_nombre = d.loca_nombre
    WHERE u.usuario_dni IS NULL;
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_profesores
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.Profesor (profesor_usuario_id)
    SELECT DISTINCT
        u.usuario_id
    FROM GD_ESQUEMA.Maestra m
    JOIN INNER_JOY.Usuario u
      ON u.usuario_dni = m.Profesor_DNI
    LEFT JOIN INNER_JOY.Profesor p
      ON p.profesor_usuario_id = u.usuario_id
    WHERE m.Profesor_DNI IS NOT NULL 
      AND m.Profesor_DNI <> ''
      AND p.profesor_usuario_id IS NULL;
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_alumnos
AS
BEGIN
    SET NOCOUNT ON;

    ;WITH src AS (
        SELECT
            m.Alumno_Legajo AS alumno_legajo,
            u.usuario_id,
            ROW_NUMBER() OVER (
                PARTITION BY m.Alumno_Legajo
                ORDER BY u.usuario_id
            ) AS rn
        FROM GD_ESQUEMA.Maestra m
        JOIN INNER_JOY.Usuario u
          ON u.usuario_dni = m.Alumno_DNI
        WHERE m.Alumno_Legajo IS NOT NULL
          AND m.Alumno_Legajo <> ''
          AND m.Alumno_DNI IS NOT NULL
          AND m.Alumno_DNI <> ''
    )
    INSERT INTO INNER_JOY.Alumno (alumno_legajo, alumno_usuario_id)
    SELECT
        s.alumno_legajo,
        s.usuario_id
    FROM src s
    LEFT JOIN INNER_JOY.Alumno a
      ON a.alumno_legajo = s.alumno_legajo
    WHERE s.rn = 1
      AND a.alumno_legajo IS NULL;
END;
GO


CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_dia
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO INNER_JOY.Dia (dia_nombre)
    SELECT DISTINCT m.Curso_Dia
    FROM GD_ESQUEMA.Maestra m
    WHERE m.Curso_Dia IS NOT NULL
      AND NOT EXISTS (
            SELECT 1 
            FROM INNER_JOY.Dia d
            WHERE d.dia_nombre = m.Curso_Dia
      );
END;
GO


CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_turno
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO INNER_JOY.Turno (turno_descripcion)
    SELECT DISTINCT m.Curso_Turno
    FROM GD_ESQUEMA.Maestra m
    WHERE m.Curso_Turno IS NOT NULL
      AND NOT EXISTS (
            SELECT 1 
            FROM INNER_JOY.Turno t
            WHERE t.turno_descripcion = m.Curso_Turno
      );
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_cursos
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.Curso (
        curso_codigo,
        curso_sede_id,
        curso_profesor_id,
        curso_nombre,
        curso_descripcion,
        curso_categoria,
        curso_fecha_inicio,
        curso_fecha_fin,
        curso_duracion_meses,
        curso_turno_id,
        curso_precio_mensual
    )
    SELECT DISTINCT
        m.Curso_Codigo,
        s.sede_id,
        p.profesor_id,
        m.Curso_Nombre,
        m.Curso_Descripcion,
        m.Curso_Categoria,
        m.Curso_FechaInicio,
        m.Curso_FechaFin,
        m.Curso_DuracionMeses,
        t.turno_id,
        m.Curso_PrecioMensual
    FROM GD_ESQUEMA.Maestra m
    LEFT JOIN INNER_JOY.Sede s
        ON m.Sede_Nombre = s.sede_nombre
    LEFT JOIN INNER_JOY.Usuario u
        ON m.Profesor_DNI = u.usuario_dni
    LEFT JOIN INNER_JOY.Profesor p
        ON p.profesor_usuario_id = u.usuario_id
    LEFT JOIN INNER_JOY.Turno t
        ON m.Curso_Turno = t.turno_descripcion
    WHERE m.Curso_Codigo IS NOT NULL
      AND m.Sede_Nombre IS NOT NULL
      AND m.Profesor_DNI IS NOT NULL
      AND m.Curso_Nombre IS NOT NULL;
END;
GO


CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_modulo
AS
BEGIN 
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.Modulo (
        modulo_curso_codigo,
        modulo_nombre,
        modulo_descripcion
    )
    SELECT DISTINCT
        c.curso_codigo,
        m.Modulo_Nombre,
        m.Modulo_Descripcion
    FROM GD_ESQUEMA.Maestra AS m
    LEFT JOIN INNER_JOY.Curso c
        ON c.curso_codigo = m.Curso_Codigo
    LEFT JOIN INNER_JOY.Modulo mo
        ON mo.modulo_curso_codigo = c.curso_codigo
       AND mo.modulo_nombre = m.Modulo_Nombre
    WHERE m.Curso_Codigo   IS NOT NULL
      AND m.Modulo_Nombre  IS NOT NULL
      AND c.curso_codigo   IS NOT NULL
      AND mo.modulo_id     IS NULL;
END;
GO


CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_dia_por_curso
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO INNER_JOY.DiaPorCurso (dia_id, curso_id)
    SELECT DISTINCT
        d.dia_id,
        c.curso_codigo
    FROM GD_ESQUEMA.Maestra AS m
    INNER JOIN INNER_JOY.Curso c
        ON c.curso_codigo = m.Curso_Codigo
    INNER JOIN INNER_JOY.Dia d
        ON d.dia_nombre = m.Curso_Dia
    WHERE m.Curso_Dia IS NOT NULL
      AND NOT EXISTS (
            SELECT 1 
            FROM INNER_JOY.DiaPorCurso dc
            WHERE dc.dia_id = d.dia_id
              AND dc.curso_id = c.curso_codigo
      );
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_estado_de_inscripcion
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO INNER_JOY.EstadoDeInscripcion (estado_descripcion)
    SELECT DISTINCT m.Inscripcion_Estado
    FROM GD_ESQUEMA.Maestra AS m
    WHERE m.Inscripcion_Estado IS NOT NULL
      AND NOT EXISTS (
            SELECT 1 
            FROM INNER_JOY.EstadoDeInscripcion e
            WHERE e.estado_descripcion = m.Inscripcion_Estado
      );
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_inscripcion_curso
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.InscripcionCurso (
        inscrip_numero,
        inscrip_fecha,
        inscrip_alumno_legajo,
        inscrip_curso_codigo,
        inscrip_estado,
        inscrip_fecha_respuesta
    )
    SELECT DISTINCT
        m.Inscripcion_Numero,
        m.Inscripcion_Fecha,
        a.alumno_legajo,
        c.curso_codigo,
        e.estado_id,
        m.Inscripcion_FechaRespuesta
    FROM GD_ESQUEMA.Maestra m
    LEFT JOIN INNER_JOY.Alumno a
        ON a.alumno_legajo = m.Alumno_Legajo
    LEFT JOIN INNER_JOY.Curso c
        ON c.curso_codigo = m.Curso_Codigo
    LEFT JOIN INNER_JOY.EstadoDeInscripcion e
        ON e.estado_descripcion = m.Inscripcion_Estado
    LEFT JOIN INNER_JOY.InscripcionCurso ic
        ON ic.inscrip_numero = m.Inscripcion_Numero
    WHERE m.Inscripcion_Numero  IS NOT NULL
      AND m.Alumno_Legajo       IS NOT NULL
      AND m.Curso_Codigo        IS NOT NULL
      AND m.Inscripcion_Estado  IS NOT NULL
      AND ic.inscrip_numero     IS NULL;
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_factura
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.Factura (
        factura_numero,
        factura_fecha_emision,
        factura_fecha_vencimiento,
        factura_importe_total,
        factura_alumno_id
    )
    SELECT DISTINCT
        m.Factura_Numero,
        m.Factura_FechaEmision,
        m.Factura_FechaVencimiento,
        m.Factura_Total,
        a.alumno_legajo
    FROM GD_ESQUEMA.Maestra m
    LEFT JOIN INNER_JOY.Alumno a
        ON a.alumno_legajo = m.Alumno_Legajo
    LEFT JOIN INNER_JOY.Factura f
        ON f.factura_numero = m.Factura_Numero
    WHERE m.Factura_Numero        IS NOT NULL
      AND m.Factura_FechaEmision  IS NOT NULL
      AND m.Factura_Total         IS NOT NULL
      AND m.Alumno_Legajo         IS NOT NULL
      AND a.alumno_legajo         IS NOT NULL
      AND f.factura_numero        IS NULL;
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_periodo
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO INNER_JOY.Periodo (periodo_mes, periodo_anio)
    SELECT DISTINCT
        m.Periodo_Mes,
        m.Periodo_Anio
    FROM GD_ESQUEMA.Maestra AS m
    WHERE m.Periodo_Mes IS NOT NULL
      AND m.Periodo_Anio IS NOT NULL
      AND NOT EXISTS (
            SELECT 1 
            FROM INNER_JOY.Periodo p
            WHERE p.periodo_mes = m.Periodo_Mes
              AND p.periodo_anio = m.Periodo_Anio
      );
END;
GO


CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_detalle_de_factura
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.DetalleDeFactura (
        detalle_factura_numero,
        detalle_factura_curso_id,
        detalle_factura_importe,
        detalle_factura_periodo_id
    )
    SELECT DISTINCT
        f.factura_numero,
        c.curso_codigo,
        m.Detalle_Factura_Importe,
        p.periodo_id
    FROM GD_ESQUEMA.Maestra m
    LEFT JOIN INNER_JOY.Factura f
        ON f.factura_numero = m.Factura_Numero
    LEFT JOIN INNER_JOY.Curso c
        ON c.curso_codigo = m.Curso_Codigo
    LEFT JOIN INNER_JOY.Periodo p
        ON p.periodo_mes = m.Periodo_Mes
       AND p.periodo_anio = m.Periodo_Anio
    LEFT JOIN INNER_JOY.DetalleDeFactura df
        ON df.detalle_factura_numero = f.factura_numero
       AND df.detalle_factura_curso_id = c.curso_codigo
       AND df.detalle_factura_periodo_id = p.periodo_id
       AND df.detalle_factura_importe = m.Detalle_Factura_Importe
    WHERE m.Factura_Numero IS NOT NULL
      AND m.Curso_Codigo IS NOT NULL
      AND m.Periodo_Mes  IS NOT NULL
      AND m.Periodo_Anio IS NOT NULL
      AND m.Detalle_Factura_Importe IS NOT NULL
      AND f.factura_numero IS NOT NULL
      AND c.curso_codigo  IS NOT NULL
      AND p.periodo_id    IS NOT NULL
      AND df.detalle_factura_numero IS NULL;
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_medio_de_pago
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO INNER_JOY.MedioDePago (medio_de_pago_descripcion)
    SELECT DISTINCT m.Pago_MedioPago
    FROM GD_ESQUEMA.Maestra m
    WHERE m.Pago_MedioPago IS NOT NULL
      AND NOT EXISTS (
            SELECT 1 
            FROM INNER_JOY.MedioDePago mp
            WHERE mp.medio_de_pago_descripcion = m.Pago_MedioPago
      );
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_pago
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.Pago (
        pago_factura_numero,
        pago_fecha_pago,
        pago_importe_total,
        medio_pago
    )
    SELECT DISTINCT
        f.factura_numero,
        m.Pago_Fecha,
        m.Pago_Importe,
        mp.medio_de_pago_id
    FROM GD_ESQUEMA.Maestra m
    LEFT JOIN INNER_JOY.Factura f
        ON f.factura_numero = m.Factura_Numero
    LEFT JOIN INNER_JOY.MedioDePago mp
        ON mp.medio_de_pago_descripcion = m.Pago_MedioPago
    LEFT JOIN INNER_JOY.Pago p
        ON p.pago_factura_numero = f.factura_numero
       AND p.pago_fecha_pago     = m.Pago_Fecha
       AND p.medio_pago          = mp.medio_de_pago_id
    WHERE m.Factura_Numero   IS NOT NULL
      AND m.Pago_Fecha       IS NOT NULL
      AND m.Pago_Importe     IS NOT NULL
      AND m.Pago_MedioPago   IS NOT NULL
      AND f.factura_numero   IS NOT NULL
      AND mp.medio_de_pago_id IS NOT NULL
      AND p.pago_id          IS NULL;
END;
GO


CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_tps
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.TP (
        tp_curso_codigo,
        tp_fecha
    )
    SELECT DISTINCT
        c.curso_codigo,
        m.Trabajo_Practico_FechaEvaluacion
    FROM GD_ESQUEMA.Maestra m
    LEFT JOIN INNER_JOY.Curso c
        ON c.curso_codigo = m.Curso_Codigo
    LEFT JOIN INNER_JOY.TP tp
        ON tp.tp_curso_codigo = c.curso_codigo
       AND tp.tp_fecha        = m.Trabajo_Practico_FechaEvaluacion
    WHERE m.Curso_Codigo IS NOT NULL
      AND m.Trabajo_Practico_FechaEvaluacion IS NOT NULL
      AND c.curso_codigo IS NOT NULL
      AND tp.tp_id IS NULL;
END;
GO


CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_tp_por_alumno
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.TpPorAlumno (
        tp_id,
        tp_alumno_legajo,
        tp_nota
    )
    SELECT DISTINCT
        tp.tp_id,
        m.Alumno_Legajo,
        m.Trabajo_Practico_Nota
    FROM GD_ESQUEMA.Maestra m
    LEFT JOIN INNER_JOY.TP tp
        ON tp.tp_curso_codigo = m.Curso_Codigo
       AND tp.tp_fecha        = m.Trabajo_Practico_FechaEvaluacion
    LEFT JOIN INNER_JOY.TpPorAlumno tpa
        ON tpa.tp_id = tp.tp_id
       AND tpa.tp_alumno_legajo = m.Alumno_Legajo
    WHERE m.Alumno_Legajo                     IS NOT NULL
      AND m.Curso_Codigo                      IS NOT NULL
      AND m.Trabajo_Practico_FechaEvaluacion  IS NOT NULL
      AND m.Trabajo_Practico_Nota             IS NOT NULL
      AND tp.tp_id                            IS NOT NULL
      AND tpa.tp_id                           IS NULL;
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_evaluacion
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.Evaluacion (
        evaluacion_fecha,
        evaluacion_modulo_id
    )
    SELECT DISTINCT
        m.Evaluacion_Curso_fechaEvaluacion,
        mo.modulo_id
    FROM GD_ESQUEMA.Maestra m
    LEFT JOIN INNER_JOY.Modulo mo
        ON mo.modulo_curso_codigo = m.Curso_Codigo
       AND mo.modulo_nombre       = m.Modulo_Nombre
    LEFT JOIN INNER_JOY.Evaluacion e
        ON e.evaluacion_modulo_id = mo.modulo_id
       AND e.evaluacion_fecha     = m.Evaluacion_Curso_fechaEvaluacion
    WHERE m.Curso_Codigo                         IS NOT NULL
      AND m.Modulo_Nombre                        IS NOT NULL
      AND m.Evaluacion_Curso_fechaEvaluacion     IS NOT NULL
      AND mo.modulo_id                           IS NOT NULL
      AND e.evaluacion_id                        IS NULL;
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_evaluacion_por_alumno
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.EvaluacionPorAlumno (
        evaluacion_id,
        evaluacion_alumno_legajo,
        evaluacion_nota,
        evaluacion_presente,
        evaluacion_instancia
    )
    SELECT DISTINCT
        e.evaluacion_id,
        m.Alumno_Legajo,
        m.Evaluacion_Curso_Nota,
        m.Evaluacion_Curso_Presente,
        m.Evaluacion_Curso_Instancia
    FROM GD_ESQUEMA.Maestra m
    LEFT JOIN INNER_JOY.Modulo mo
        ON mo.modulo_curso_codigo = m.Curso_Codigo
       AND mo.modulo_nombre       = m.Modulo_Nombre
    LEFT JOIN INNER_JOY.Evaluacion e
        ON e.evaluacion_modulo_id = mo.modulo_id
       AND e.evaluacion_fecha     = m.Evaluacion_Curso_fechaEvaluacion
    LEFT JOIN INNER_JOY.EvaluacionPorAlumno ea
        ON ea.evaluacion_id            = e.evaluacion_id
       AND ea.evaluacion_alumno_legajo = m.Alumno_Legajo
    WHERE m.Alumno_Legajo                     IS NOT NULL
      AND m.Curso_Codigo                      IS NOT NULL
      AND m.Modulo_Nombre                     IS NOT NULL
      AND m.Evaluacion_Curso_fechaEvaluacion  IS NOT NULL
      AND e.evaluacion_id                     IS NOT NULL
      AND ea.evaluacion_id                    IS NULL;
END;
GO


CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_final_por_alumno
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.FinalPorAlumno (
        final_alumno_legajo,
        final_id,
        final_presente,
        final_nota,
        final_profesor_id
    )
    SELECT DISTINCT
        m.Alumno_Legajo,
        f.final_id,
        m.Evaluacion_Final_Presente,
        m.Evaluacion_Final_Nota,
        p.profesor_id
    FROM GD_ESQUEMA.Maestra m
    LEFT JOIN INNER_JOY.Final f
        ON f.final_curso_codigo = m.Curso_Codigo
       AND f.final_fecha        = m.Examen_Final_Fecha
       AND f.final_hora         = m.Examen_Final_Hora
    LEFT JOIN INNER_JOY.Usuario up
        ON up.usuario_dni = m.Profesor_DNI
    LEFT JOIN INNER_JOY.Profesor p
        ON p.profesor_usuario_id = up.usuario_id
    LEFT JOIN INNER_JOY.FinalPorAlumno fa
        ON fa.final_id            = f.final_id
       AND fa.final_alumno_legajo = m.Alumno_Legajo
    WHERE m.Alumno_Legajo           IS NOT NULL
      AND m.Curso_Codigo            IS NOT NULL
      AND m.Examen_Final_Fecha      IS NOT NULL
      AND m.Examen_Final_Hora       IS NOT NULL
      AND f.final_id                IS NOT NULL
      AND (
            m.Evaluacion_Final_Nota IS NOT NULL
            OR (m.Evaluacion_Final_Nota IS NULL AND m.Evaluacion_Final_Presente = 0)
          )
      AND fa.final_id IS NULL;
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_final
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.Final (
        final_fecha,
        final_hora,
        final_descripcion,
        final_curso_codigo
    )
    SELECT DISTINCT
        m.Examen_Final_Fecha,
        m.Examen_Final_Hora,
        m.Examen_Final_Descripcion,
        c.curso_codigo
    FROM GD_ESQUEMA.Maestra m
    LEFT JOIN INNER_JOY.Curso c
        ON c.curso_codigo = m.Curso_Codigo
    LEFT JOIN INNER_JOY.Final f
        ON f.final_curso_codigo = c.curso_codigo
       AND f.final_fecha        = m.Examen_Final_Fecha
       AND f.final_hora         = m.Examen_Final_Hora
    WHERE m.Curso_Codigo       IS NOT NULL
      AND m.Examen_Final_Fecha IS NOT NULL
      AND m.Examen_Final_Hora  IS NOT NULL
      AND c.curso_codigo       IS NOT NULL
      AND f.final_id           IS NULL;
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_inscripcion_final
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.InscripcionFinal (
        inscrip_numero,
        inscrip_fecha,
        inscrip_alumno_legajo,
        inscrip_final_id
    )
    SELECT DISTINCT
        m.Inscripcion_Final_Nro,
        m.Inscripcion_Final_Fecha,
        m.Alumno_Legajo,
        f.final_id
    FROM GD_ESQUEMA.Maestra m
    LEFT JOIN INNER_JOY.Final f
        ON f.final_curso_codigo = m.Curso_Codigo
       AND f.final_fecha        = m.Examen_Final_Fecha
       AND f.final_hora         = m.Examen_Final_Hora
    LEFT JOIN INNER_JOY.InscripcionFinal ifinal
        ON ifinal.inscrip_numero = m.Inscripcion_Final_Nro
    WHERE m.Inscripcion_Final_Nro   IS NOT NULL
      AND m.Inscripcion_Final_Fecha IS NOT NULL
      AND m.Curso_Codigo            IS NOT NULL
      AND m.Alumno_Legajo           IS NOT NULL
      AND f.final_id                IS NOT NULL
      AND ifinal.inscrip_numero     IS NULL;
END;
GO


CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_encuesta_completada
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO INNER_JOY.EncuestaCompletada (
        encuesta_curso_codigo,
        encuesta_fecha_registro,
        encuesta_observacion
    )
    SELECT DISTINCT
        c.curso_codigo,
        m.Encuesta_FechaRegistro,
        m.Encuesta_Observacion
    FROM GD_ESQUEMA.Maestra AS m
    INNER JOIN INNER_JOY.Curso c
        ON c.curso_codigo = m.Curso_Codigo
    WHERE m.Encuesta_FechaRegistro IS NOT NULL
      AND NOT EXISTS (
            SELECT 1 
            FROM INNER_JOY.EncuestaCompletada ec
            WHERE ec.encuesta_curso_codigo = c.curso_codigo
              AND ec.encuesta_fecha_registro = m.Encuesta_FechaRegistro
      );
END;
GO


CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_pregunta
AS
BEGIN
    SET NOCOUNT ON;

    WITH TodasLasPreguntas AS (
        SELECT Encuesta_Pregunta1 AS Pregunta_Texto FROM GD_ESQUEMA.Maestra WHERE Encuesta_Pregunta1 IS NOT NULL
        UNION 
        SELECT Encuesta_Pregunta2 AS Pregunta_Texto FROM GD_ESQUEMA.Maestra WHERE Encuesta_Pregunta2 IS NOT NULL
        UNION
        SELECT Encuesta_Pregunta3 AS Pregunta_Texto FROM GD_ESQUEMA.Maestra WHERE Encuesta_Pregunta3 IS NOT NULL
        UNION
        SELECT Encuesta_Pregunta4 AS Pregunta_Texto FROM GD_ESQUEMA.Maestra WHERE Encuesta_Pregunta4 IS NOT NULL
    )
    INSERT INTO INNER_JOY.Pregunta (pregunta_texto)
    SELECT
        tp.Pregunta_Texto
    FROM
        TodasLasPreguntas tp
    WHERE
        NOT EXISTS (
            SELECT 1
            FROM INNER_JOY.Pregunta p
            WHERE p.pregunta_texto = tp.Pregunta_Texto
        );
END;
GO

CREATE OR ALTER PROCEDURE INNER_JOY.sp_migrar_respuesta
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO INNER_JOY.Respuesta (
        detalle_pregunta_id,
        detalle_encuesta_id,
        detalle_nota
    )
    SELECT DISTINCT
        p.pregunta_id,
        ec.encuesta_id,
        v.nota
    FROM GD_ESQUEMA.Maestra m
    CROSS APPLY (VALUES
        (m.Encuesta_Pregunta1, m.Encuesta_Nota1),
        (m.Encuesta_Pregunta2, m.Encuesta_Nota2),
        (m.Encuesta_Pregunta3, m.Encuesta_Nota3),
        (m.Encuesta_Pregunta4, m.Encuesta_Nota4)
    ) AS v(pregunta_texto, nota)
    LEFT JOIN INNER_JOY.Curso c
        ON c.curso_codigo = m.Curso_Codigo
    LEFT JOIN INNER_JOY.EncuestaCompletada ec
        ON ec.encuesta_curso_codigo = c.curso_codigo
       AND ec.encuesta_fecha_registro = m.Encuesta_FechaRegistro
    LEFT JOIN INNER_JOY.Pregunta p
        ON p.pregunta_texto = v.pregunta_texto
    LEFT JOIN INNER_JOY.Respuesta r
        ON r.detalle_pregunta_id = p.pregunta_id
       AND r.detalle_encuesta_id = ec.encuesta_id
    WHERE m.Curso_Codigo           IS NOT NULL
      AND m.Encuesta_FechaRegistro IS NOT NULL
      AND v.pregunta_texto         IS NOT NULL
      AND v.nota                   IS NOT NULL
      AND ec.encuesta_id           IS NOT NULL
      AND p.pregunta_id            IS NOT NULL
      AND r.detalle_id             IS NULL;
END;
GO


BEGIN TRANSACTION;

BEGIN TRY

    EXEC INNER_JOY.sp_migrar_provincias;
    EXEC INNER_JOY.sp_migrar_localidades;
    EXEC INNER_JOY.sp_migrar_instituciones;
    EXEC INNER_JOY.sp_migrar_sedes; 
    EXEC INNER_JOY.sp_migrar_usuarios; 
    EXEC INNER_JOY.sp_migrar_profesores;
    EXEC INNER_JOY.sp_migrar_alumnos;
    EXEC INNER_JOY.sp_migrar_dia;
    EXEC INNER_JOY.sp_migrar_turno;
    EXEC INNER_JOY.sp_migrar_cursos; 
    EXEC INNER_JOY.sp_migrar_modulo;
    EXEC INNER_JOY.sp_migrar_dia_por_curso;
    EXEC INNER_JOY.sp_migrar_estado_de_inscripcion;
    EXEC INNER_JOY.sp_migrar_inscripcion_curso;
    EXEC INNER_JOY.sp_migrar_factura; 
    EXEC INNER_JOY.sp_migrar_periodo;
    EXEC INNER_JOY.sp_migrar_detalle_de_factura; 
    EXEC INNER_JOY.sp_migrar_medio_de_pago;
    EXEC INNER_JOY.sp_migrar_pago; 
    EXEC INNER_JOY.sp_migrar_tps;
    EXEC INNER_JOY.sp_migrar_tp_por_alumno;
    EXEC INNER_JOY.sp_migrar_evaluacion;
    EXEC INNER_JOY.sp_migrar_evaluacion_por_alumno; 
    EXEC INNER_JOY.sp_migrar_final;
    EXEC INNER_JOY.sp_migrar_final_por_alumno; 
    EXEC INNER_JOY.sp_migrar_inscripcion_final;
    EXEC INNER_JOY.sp_migrar_encuesta_completada;
    EXEC INNER_JOY.sp_migrar_pregunta;   
    EXEC INNER_JOY.sp_migrar_respuesta;  

    COMMIT TRANSACTION;

END TRY
BEGIN CATCH

    ROLLBACK TRANSACTION;

    DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
    THROW 50000, @ErrorMessage, 1;

END CATCH
GO

