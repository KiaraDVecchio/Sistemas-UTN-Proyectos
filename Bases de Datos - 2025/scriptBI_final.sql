USE master;
GO

IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'INNER_JOY')
BEGIN
    EXEC('CREATE SCHEMA INNER_JOY');
END
GO

---------------------------------------- 1. ELIMINAR OBJETOS ----------------------------------------
-- Vistas
DROP VIEW IF EXISTS INNER_JOY.VW_CategoriasTurnosMasSolicitados;
DROP VIEW IF EXISTS INNER_JOY.VW_TasaRechazoInscripciones;
DROP VIEW IF EXISTS INNER_JOY.VW_Desempeno_Cursada_Sede;
DROP VIEW IF EXISTS INNER_JOY.VW_Tiempo_Promedio_Finalizacion_Curso;
DROP VIEW IF EXISTS INNER_JOY.VW_NotaPromedio_Finales;
DROP VIEW IF EXISTS INNER_JOY.VW_TasaAusentismo_Finales;
DROP VIEW IF EXISTS INNER_JOY.VW_Desvio_Pagos;
DROP VIEW IF EXISTS INNER_JOY.VW_Tasa_Morosidad_Mensual;
DROP VIEW IF EXISTS INNER_JOY.VW_Ingresos_Top3_Categoria_Por_Sede;
DROP VIEW IF EXISTS INNER_JOY.VW_Indice_Satisfaccion;

-- Procedimientos
DROP PROCEDURE IF EXISTS INNER_JOY.migrar_tiempo_dim;
DROP PROCEDURE IF EXISTS INNER_JOY.migrar_sede_dim;
DROP PROCEDURE IF EXISTS INNER_JOY.rango_etario_alumno_dim;
DROP PROCEDURE IF EXISTS INNER_JOY.migrar_turno_dim;
DROP PROCEDURE IF EXISTS INNER_JOY.rango_etario_profesor_dim;
DROP PROCEDURE IF EXISTS INNER_JOY.migrar_medio_pago_dim;
DROP PROCEDURE IF EXISTS INNER_JOY.bloques_satisfaccion_dim;
DROP PROCEDURE IF EXISTS INNER_JOY.migrar_categoria_curso_dim;
DROP PROCEDURE IF EXISTS INNER_JOY.migrar_hecho_inscripcion;
DROP PROCEDURE IF EXISTS INNER_JOY.migrar_hecho_cursada;
DROP PROCEDURE IF EXISTS INNER_JOY.migrar_hecho_final;
DROP PROCEDURE IF EXISTS INNER_JOY.migrar_hecho_pago;
DROP PROCEDURE IF EXISTS INNER_JOY.migrar_hecho_encuesta;

-- Funciones (YA NO SE USARÁN, LAS BORRAMOS PARA LIMPIAR)
DROP FUNCTION IF EXISTS INNER_JOY.FN_Obtener_Tiempo_Id;
DROP FUNCTION IF EXISTS INNER_JOY.FN_Obtener_Rango_Etario_Profesor_Id;
DROP FUNCTION IF EXISTS INNER_JOY.FN_Obtener_Bloque_Satisfaccion_Id;

-- Tablas Hechos
DROP TABLE IF EXISTS INNER_JOY.BI_Hecho_Encuesta;
DROP TABLE IF EXISTS INNER_JOY.BI_Hecho_Pago;
DROP TABLE IF EXISTS INNER_JOY.BI_Hechos_Final;
DROP TABLE IF EXISTS INNER_JOY.BI_Hecho_Inscripcion;
DROP TABLE IF EXISTS INNER_JOY.BI_Hecho_Cursada;

-- Tablas Dimensiones
DROP TABLE IF EXISTS INNER_JOY.BI_Tiempo;
DROP TABLE IF EXISTS INNER_JOY.BI_Sede;
DROP TABLE IF EXISTS INNER_JOY.BI_RangoEtarioAlumno;
DROP TABLE IF EXISTS INNER_JOY.BI_RangoEtarioProfesor;
DROP TABLE IF EXISTS INNER_JOY.BI_TurnoCurso;
DROP TABLE IF EXISTS INNER_JOY.BI_CategoriaCurso;
DROP TABLE IF EXISTS INNER_JOY.BI_BloqueSatisfaccion;
DROP TABLE IF EXISTS INNER_JOY.BI_MedioPago;
GO

---------------------------------------- 2. CREACION TABLAS DIMENSIONES ----------------------------------------
CREATE TABLE INNER_JOY.BI_Tiempo (
    tiempo_id  BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    anio       SMALLINT NOT NULL,
    semestre   TINYINT  NOT NULL CHECK (semestre BETWEEN 1 AND 2),
    mes        TINYINT  NOT NULL CHECK (mes BETWEEN 1 AND 12)
);
GO

CREATE TABLE INNER_JOY.BI_Sede (
    sede_id BIGINT NOT NULL PRIMARY KEY,
    sede_nombre NVARCHAR(255) NOT NULL
);
GO

CREATE TABLE INNER_JOY.BI_RangoEtarioAlumno (
    rango_etario_alumno_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    etiqueta NVARCHAR(50) NOT NULL,
    edad_min SMALLINT,
    edad_max SMALLINT,
);
GO

CREATE TABLE INNER_JOY.BI_RangoEtarioProfesor (
    rango_etario_profesor_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    etiqueta NVARCHAR(50) NOT NULL,
    edad_min SMALLINT,
    edad_max SMALLINT
);
GO

CREATE TABLE INNER_JOY.BI_TurnoCurso (
    turno_curso_id BIGINT NOT NULL PRIMARY KEY,
    turno_descripcion NVARCHAR(255) NOT NULL,
);
GO

CREATE TABLE INNER_JOY.BI_CategoriaCurso (
    categoria_curso_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    categoria_descripcion NVARCHAR(255) NOT NULL,
);
GO

CREATE TABLE INNER_JOY.BI_MedioPago (
    medio_pago_id BIGINT NOT NULL PRIMARY KEY,
    medio_pago_desc NVARCHAR(255) NOT NULL,
);
GO

CREATE TABLE INNER_JOY.BI_BloqueSatisfaccion (
    bloque_satisfaccion_id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    etiqueta NVARCHAR(50) NOT NULL,
    nota_min DECIMAL(5,2) NOT NULL,
    nota_max DECIMAL(5,2) NOT NULL,
);
GO

---------------------------------------- 3. CREACION TABLAS HECHOS ----------------------------------------
CREATE TABLE INNER_JOY.BI_Hecho_Inscripcion (
    categoria_dim_id BIGINT NOT NULL,
    turno_dim_id BIGINT NOT NULL,
    sede_dim_id BIGINT NOT NULL,
    tiempo_dim_id BIGINT NOT NULL,
    cant_inscriptos INT NOT NULL,
    cant_rechazados INT NOT NULL,
    PRIMARY KEY (categoria_dim_id, turno_dim_id, sede_dim_id, tiempo_dim_id),
    FOREIGN KEY (categoria_dim_id) REFERENCES INNER_JOY.BI_CategoriaCurso(categoria_curso_id),
    FOREIGN KEY (turno_dim_id) REFERENCES INNER_JOY.BI_TurnoCurso(turno_curso_id),
    FOREIGN KEY (sede_dim_id) REFERENCES INNER_JOY.BI_Sede(sede_id),
    FOREIGN KEY (tiempo_dim_id) REFERENCES INNER_JOY.BI_Tiempo(tiempo_id)
);
GO

CREATE TABLE INNER_JOY.BI_Hecho_Cursada (
    sede_dim_id BIGINT NOT NULL,
    tiempo_dim_id BIGINT NOT NULL,
    categoria_dim_id BIGINT NOT NULL,
    cant_alumnos_cursada INT NOT NULL,
    cant_aprobados INT NOT NULL,
    promedio_aprobacion_meses INT NULL,
    PRIMARY KEY (sede_dim_id, tiempo_dim_id, categoria_dim_id),
    FOREIGN KEY (sede_dim_id) REFERENCES INNER_JOY.BI_Sede(sede_id),
    FOREIGN KEY (tiempo_dim_id) REFERENCES INNER_JOY.BI_Tiempo(tiempo_id),
    FOREIGN KEY (categoria_dim_id) REFERENCES INNER_JOY.BI_CategoriaCurso(categoria_curso_id)
);
GO

CREATE TABLE INNER_JOY.BI_Hechos_Final (
    tiempo_dim_id BIGINT NOT NULL,
    sede_dim_id BIGINT NOT NULL,
    categoria_dim_id BIGINT NOT NULL,
    rango_etario_id BIGINT NOT NULL,
    cant_inscriptos INT NOT NULL,
    cant_ausentes INT NOT NULL,
    cant_finales_tomados INT NOT NULL,
    sumatoria_notas DECIMAL(18,2) NOT NULL,
    PRIMARY KEY (tiempo_dim_id, sede_dim_id, categoria_dim_id, rango_etario_id),
    FOREIGN KEY (tiempo_dim_id) REFERENCES INNER_JOY.BI_Tiempo(tiempo_id),
    FOREIGN KEY (sede_dim_id) REFERENCES INNER_JOY.BI_Sede(sede_id),
    FOREIGN KEY (categoria_dim_id) REFERENCES INNER_JOY.BI_CategoriaCurso(categoria_curso_id),
    FOREIGN KEY (rango_etario_id) REFERENCES INNER_JOY.BI_RangoEtarioAlumno(rango_etario_alumno_id)
);
GO

CREATE TABLE INNER_JOY.BI_Hecho_Pago (
    tiempo_dim_id BIGINT NOT NULL,
    sede_dim_id BIGINT NOT NULL,
    medio_pago_dim_id BIGINT NOT NULL,
    categoria_dim_id BIGINT NOT NULL,
    cant_pagos INT NOT NULL,
    cant_pagos_fuera_termino INT NOT NULL,
    facturacion_esperada DECIMAL(18,2) NOT NULL,
    facturacion_cobrada DECIMAL(18,2) NOT NULL,
    PRIMARY KEY (tiempo_dim_id, sede_dim_id, medio_pago_dim_id, categoria_dim_id),
    FOREIGN KEY (tiempo_dim_id) REFERENCES INNER_JOY.BI_Tiempo(tiempo_id),
    FOREIGN KEY (sede_dim_id) REFERENCES INNER_JOY.BI_Sede(sede_id),
    FOREIGN KEY (medio_pago_dim_id) REFERENCES INNER_JOY.BI_MedioPago(medio_pago_id),
    FOREIGN KEY (categoria_dim_id) REFERENCES INNER_JOY.BI_CategoriaCurso(categoria_curso_id)
);
GO

CREATE TABLE INNER_JOY.BI_Hecho_Encuesta (
    sede_dim_id BIGINT NOT NULL,
    tiempo_dim_id BIGINT NOT NULL,
    rango_etario_profesor_id BIGINT NOT NULL,
    bloque_satisfaccion_id BIGINT NOT NULL,
    cant_respuestas INT NOT NULL,
    PRIMARY KEY (sede_dim_id, tiempo_dim_id, rango_etario_profesor_id, bloque_satisfaccion_id),
    FOREIGN KEY (sede_dim_id) REFERENCES INNER_JOY.BI_Sede(sede_id),
    FOREIGN KEY (tiempo_dim_id) REFERENCES INNER_JOY.BI_Tiempo(tiempo_id),
    FOREIGN KEY (rango_etario_profesor_id) REFERENCES INNER_JOY.BI_RangoEtarioProfesor(rango_etario_profesor_id),
    FOREIGN KEY (bloque_satisfaccion_id) REFERENCES INNER_JOY.BI_BloqueSatisfaccion(bloque_satisfaccion_id)
);
GO

---------------------------------------- 4. POPULACION DIMENSIONES ----------------------------------------
CREATE OR ALTER PROCEDURE INNER_JOY.migrar_tiempo_dim
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO INNER_JOY.BI_Tiempo (anio, semestre, mes)
    SELECT DISTINCT
        YEAR(T.fecha),
        CASE WHEN MONTH(T.fecha) BETWEEN 1 AND 6 THEN 1 ELSE 2 END,
        MONTH(T.fecha)
    FROM (
        SELECT curso_fecha_inicio AS fecha FROM INNER_JOY.Curso
        UNION ALL SELECT curso_fecha_fin FROM INNER_JOY.Curso
        UNION ALL SELECT encuesta_fecha_registro FROM INNER_JOY.EncuestaCompletada
        UNION ALL SELECT inscrip_fecha FROM INNER_JOY.InscripcionCurso
        UNION ALL SELECT inscrip_fecha_respuesta FROM INNER_JOY.InscripcionCurso
        UNION ALL SELECT factura_fecha_emision FROM INNER_JOY.Factura
        UNION ALL SELECT factura_fecha_vencimiento FROM INNER_JOY.Factura
        UNION ALL SELECT pago_fecha_pago FROM INNER_JOY.Pago
        UNION ALL SELECT inscrip_fecha FROM INNER_JOY.InscripcionFinal
        UNION ALL SELECT final_fecha FROM INNER_JOY.Final
        UNION ALL SELECT usuario_fecha_nacimiento FROM INNER_JOY.Usuario
        UNION ALL SELECT tp_fecha FROM INNER_JOY.Tp
    ) AS T
    WHERE T.fecha IS NOT NULL
      AND NOT EXISTS (
        SELECT 1 FROM INNER_JOY.BI_Tiempo bt 
        WHERE bt.anio = YEAR(T.fecha) 
          AND bt.mes = MONTH(T.fecha)
    );
END
GO

CREATE OR ALTER PROCEDURE INNER_JOY.migrar_sede_dim
AS
BEGIN
    INSERT INTO INNER_JOY.BI_Sede (sede_id, sede_nombre)
    SELECT DISTINCT CAST(s.sede_id AS BIGINT), CAST(s.sede_nombre AS NVARCHAR(100))
    FROM INNER_JOY.Sede s
    WHERE NOT EXISTS (SELECT 1 FROM INNER_JOY.BI_Sede b WHERE b.sede_id = s.sede_id);
END
GO

CREATE OR ALTER PROCEDURE INNER_JOY.migrar_turno_dim
AS
BEGIN
    INSERT INTO INNER_JOY.BI_TurnoCurso (turno_curso_id, turno_descripcion)
    SELECT DISTINCT t.turno_id, t.turno_descripcion
    FROM INNER_JOY.Turno t
    WHERE t.turno_descripcion IS NOT NULL
      AND NOT EXISTS (SELECT 1 FROM INNER_JOY.BI_TurnoCurso d WHERE d.turno_curso_id = t.turno_id);
END
GO

CREATE OR ALTER PROCEDURE inner_joy.rango_etario_alumno_dim
AS
BEGIN
    INSERT INTO INNER_JOY.BI_RangoEtarioAlumno (edad_min, edad_max, etiqueta)
    VALUES (0, 24, 'Menor a 25'), (25, 34, 'Entre 25 y 34'), (35, 50, 'Entre 35 y 50'), (51, 150, 'Mayor a 50');
END
GO

CREATE OR ALTER PROCEDURE inner_joy.rango_etario_profesor_dim
AS
BEGIN
    INSERT INTO INNER_JOY.BI_RangoEtarioProfesor (edad_min, edad_max, etiqueta)
    VALUES (25, 34, 'Entre 25 y 34'), (35, 50, 'Entre 35 y 50'), (51, 150, 'Mayor a 50');
END
GO

CREATE OR ALTER PROCEDURE INNER_JOY.migrar_medio_pago_dim
AS
BEGIN
    INSERT INTO INNER_JOY.BI_MedioPago (medio_pago_id, medio_pago_desc)
    SELECT DISTINCT mp.medio_de_pago_id, mp.medio_de_pago_descripcion
    FROM INNER_JOY.MedioDePago mp
    WHERE mp.medio_de_pago_descripcion IS NOT NULL
      AND NOT EXISTS (SELECT 1 FROM INNER_JOY.BI_MedioPago d WHERE d.medio_pago_id = mp.medio_de_pago_id);
END
GO

CREATE OR ALTER PROCEDURE INNER_JOY.bloques_satisfaccion_dim
AS
BEGIN
    INSERT INTO INNER_JOY.BI_BloqueSatisfaccion (nota_min, nota_max, etiqueta)
    VALUES (7, 10, 'Satisfechos'), (5, 6, 'Neutrales'), (1, 4, 'Insatisfechos');
END
GO

CREATE OR ALTER PROCEDURE INNER_JOY.migrar_categoria_curso_dim
AS
BEGIN
    INSERT INTO INNER_JOY.BI_CategoriaCurso (categoria_descripcion)
    SELECT DISTINCT c.curso_categoria
    FROM INNER_JOY.Curso c
    WHERE c.curso_categoria IS NOT NULL
      AND NOT EXISTS (SELECT 1 FROM INNER_JOY.BI_CategoriaCurso d WHERE d.categoria_descripcion = c.curso_categoria);
END
GO

---------------------------------------- 5. POPULACION HECHOS (OPTIMIZADO CON JOINS) ----------------------------------------

CREATE OR ALTER PROCEDURE INNER_JOY.migrar_hecho_inscripcion
AS
BEGIN
    SET NOCOUNT ON;
    TRUNCATE TABLE INNER_JOY.BI_Hecho_Inscripcion;

    INSERT INTO INNER_JOY.BI_Hecho_Inscripcion (
        categoria_dim_id, turno_dim_id, sede_dim_id, tiempo_dim_id, cant_inscriptos, cant_rechazados
    )
    SELECT
        catDim.categoria_curso_id,
        turDim.turno_curso_id,
        sedDim.sede_id,
        tDim.tiempo_id,
        COUNT(*),
        SUM(CASE WHEN UPPER(est.estado_descripcion) = 'RECHAZADA' THEN 1 ELSE 0 END)
    FROM INNER_JOY.InscripcionCurso ic
    JOIN INNER_JOY.Curso c ON c.curso_codigo = ic.inscrip_curso_codigo
    JOIN INNER_JOY.Sede s ON s.sede_id = c.curso_sede_id
    JOIN INNER_JOY.Turno t ON t.turno_id = c.curso_turno_id
    LEFT JOIN INNER_JOY.EstadoDeInscripcion est ON est.estado_id = ic.inscrip_estado
    
    JOIN INNER_JOY.BI_Sede sedDim ON sedDim.sede_id = s.sede_id
    JOIN INNER_JOY.BI_TurnoCurso turDim ON turDim.turno_curso_id = t.turno_id
    JOIN INNER_JOY.BI_CategoriaCurso catDim ON catDim.categoria_descripcion = c.curso_categoria
    JOIN INNER_JOY.BI_Tiempo tDim 
        ON tDim.anio = YEAR(ic.inscrip_fecha) 
        AND tDim.mes = MONTH(ic.inscrip_fecha)
    
    WHERE ic.inscrip_fecha IS NOT NULL
    GROUP BY
        catDim.categoria_curso_id,
        turDim.turno_curso_id,
        sedDim.sede_id,
        tDim.tiempo_id;
END
GO

CREATE OR ALTER PROCEDURE INNER_JOY.migrar_hecho_cursada
AS
BEGIN
    SET NOCOUNT ON;
    TRUNCATE TABLE INNER_JOY.BI_Hecho_Cursada;

    WITH alumnos_curso AS (
        SELECT
            bs.sede_id AS sede_dim_id,
            tDim.tiempo_id AS tiempo_dim_id,
            bc.categoria_curso_id AS categoria_dim_id,
            c.curso_codigo,
            c.curso_fecha_inicio,
            ic.inscrip_alumno_legajo
        FROM INNER_JOY.InscripcionCurso ic
        JOIN INNER_JOY.Curso c ON c.curso_codigo = ic.inscrip_curso_codigo
        JOIN INNER_JOY.Sede s ON s.sede_id = c.curso_sede_id
        JOIN INNER_JOY.BI_Sede bs ON bs.sede_id = s.sede_id
        LEFT JOIN INNER_JOY.BI_CategoriaCurso bc ON bc.categoria_descripcion = c.curso_categoria
        JOIN INNER_JOY.EstadoDeInscripcion ei ON ei.estado_id = ic.inscrip_estado
        JOIN INNER_JOY.BI_Tiempo tDim 
            ON tDim.anio = YEAR(c.curso_fecha_inicio) 
            AND tDim.mes = MONTH(c.curso_fecha_inicio)
        WHERE ei.estado_descripcion = 'Confirmada'
    ),
    cursada_agregada AS (
        SELECT
            ac.sede_dim_id,
            ac.tiempo_dim_id,
            ac.categoria_dim_id,
            ac.curso_codigo,
            ac.curso_fecha_inicio,
            ac.inscrip_alumno_legajo,
            CASE WHEN 
                (SELECT COUNT(*) FROM INNER_JOY.Modulo m WHERE m.modulo_curso_codigo = ac.curso_codigo) > 0
                AND
                (SELECT COUNT(DISTINCT m2.modulo_id)
                 FROM INNER_JOY.Modulo m2
                 JOIN INNER_JOY.Evaluacion e ON e.evaluacion_modulo_id = m2.modulo_id
                 JOIN INNER_JOY.EvaluacionPorAlumno ea ON ea.evaluacion_id = e.evaluacion_id
                 WHERE m2.modulo_curso_codigo = ac.curso_codigo
                   AND ea.evaluacion_alumno_legajo = ac.inscrip_alumno_legajo
                   AND ea.evaluacion_nota >= 4)
                =
                (SELECT COUNT(*) FROM INNER_JOY.Modulo m3 WHERE m3.modulo_curso_codigo = ac.curso_codigo)
                AND
                EXISTS (SELECT 1 FROM INNER_JOY.Tp t JOIN INNER_JOY.TpPorAlumno tpa ON tpa.tp_id = t.tp_id
                        WHERE t.tp_curso_codigo = ac.curso_codigo AND tpa.tp_alumno_legajo = ac.inscrip_alumno_legajo AND tpa.tp_nota >= 4)
            THEN 1 ELSE 0 END AS aprobado,
            (
                SELECT TOP 1 DATEDIFF(MONTH, ac.curso_fecha_inicio, f.final_fecha)
                FROM INNER_JOY.Final f
                JOIN INNER_JOY.FinalPorAlumno fa ON fa.final_id = f.final_id AND fa.final_alumno_legajo = ac.inscrip_alumno_legajo
                WHERE f.final_curso_codigo = ac.curso_codigo AND fa.final_nota >= 4
                ORDER BY f.final_fecha
            ) AS tiempo_finalizacion
        FROM alumnos_curso ac
    )
    INSERT INTO INNER_JOY.BI_Hecho_Cursada (
        sede_dim_id, tiempo_dim_id, categoria_dim_id, cant_alumnos_cursada, cant_aprobados, promedio_aprobacion_meses
    )
    SELECT
        sede_dim_id, tiempo_dim_id, categoria_dim_id,
        COUNT(*),
        SUM(aprobado),
        CASE WHEN SUM(CASE WHEN tiempo_finalizacion IS NOT NULL THEN 1 ELSE 0 END) = 0 THEN NULL
             ELSE CAST(AVG(CAST(tiempo_finalizacion AS DECIMAL(10,2))) AS DECIMAL(10,2)) 
        END
    FROM cursada_agregada
    GROUP BY sede_dim_id, tiempo_dim_id, categoria_dim_id;
END
GO

CREATE OR ALTER PROCEDURE INNER_JOY.migrar_hecho_final
AS
BEGIN
    SET NOCOUNT ON;
    TRUNCATE TABLE INNER_JOY.BI_Hechos_Final;

    INSERT INTO INNER_JOY.BI_Hechos_Final (
        tiempo_dim_id, sede_dim_id, categoria_dim_id, rango_etario_id, cant_inscriptos, cant_ausentes, cant_finales_tomados, sumatoria_notas
    )
    SELECT
        tDim.tiempo_id,
        bs.sede_id,
        bc.categoria_curso_id,
        rea.rango_etario_alumno_id,
        COUNT(*),
        SUM(CASE WHEN fpa.final_presente = 0 OR fpa.final_presente IS NULL THEN 1 ELSE 0 END),
        SUM(CASE WHEN fpa.final_presente = 1 AND fpa.final_nota IS NOT NULL THEN 1 ELSE 0 END),
        SUM(CASE WHEN fpa.final_presente = 1 AND fpa.final_nota IS NOT NULL THEN CAST(fpa.final_nota AS DECIMAL(18,2)) ELSE 0 END)
    FROM INNER_JOY.FinalPorAlumno fpa
    JOIN INNER_JOY.Final f ON f.final_id = fpa.final_id
    JOIN INNER_JOY.Curso c ON c.curso_codigo = f.final_curso_codigo
    JOIN INNER_JOY.Sede s ON s.sede_id = c.curso_sede_id
    JOIN INNER_JOY.Alumno a ON a.alumno_legajo = fpa.final_alumno_legajo
    JOIN INNER_JOY.Usuario u ON u.usuario_id = a.alumno_usuario_id
    
    
    JOIN INNER_JOY.BI_Sede bs ON bs.sede_id = s.sede_id
    LEFT JOIN INNER_JOY.BI_CategoriaCurso bc ON bc.categoria_descripcion = c.curso_categoria
    JOIN INNER_JOY.BI_Tiempo tDim 
        ON tDim.anio = YEAR(f.final_fecha) 
        AND tDim.mes = MONTH(f.final_fecha)
    JOIN INNER_JOY.BI_RangoEtarioAlumno rea 
        ON DATEDIFF(YEAR, u.usuario_fecha_nacimiento, f.final_fecha) BETWEEN rea.edad_min AND rea.edad_max

    GROUP BY tDim.tiempo_id, bs.sede_id, bc.categoria_curso_id, rea.rango_etario_alumno_id;
END
GO

CREATE OR ALTER PROCEDURE INNER_JOY.migrar_hecho_pago
AS
BEGIN
    SET NOCOUNT ON;
    TRUNCATE TABLE INNER_JOY.BI_Hecho_Pago;

    INSERT INTO INNER_JOY.BI_Hecho_Pago (
        tiempo_dim_id, sede_dim_id, medio_pago_dim_id, categoria_dim_id, cant_pagos, cant_pagos_fuera_termino, facturacion_esperada, facturacion_cobrada
    )
    SELECT
        tDim.tiempo_id,
        bs.sede_id,
        bmp.medio_pago_id,
        bc.categoria_curso_id,
        SUM(CASE WHEN p.pago_id IS NOT NULL THEN 1 ELSE 0 END),
        SUM(CASE WHEN p.pago_id IS NOT NULL AND p.pago_fecha_pago > f.factura_fecha_vencimiento THEN 1 ELSE 0 END),
        SUM(df.detalle_factura_importe),
        SUM(CASE WHEN p.pago_id IS NOT NULL AND p.pago_fecha_pago <= f.factura_fecha_vencimiento THEN p.pago_importe_total ELSE 0 END)
    FROM INNER_JOY.Factura f
    JOIN INNER_JOY.DetalleDeFactura df ON df.detalle_factura_numero = f.factura_numero
    JOIN INNER_JOY.Curso c ON c.curso_codigo = df.detalle_factura_curso_id
    LEFT JOIN INNER_JOY.Pago p ON p.pago_factura_numero = f.factura_numero 
    
    
    JOIN INNER_JOY.BI_Sede bs ON bs.sede_id = c.curso_sede_id 
    LEFT JOIN INNER_JOY.BI_CategoriaCurso bc ON bc.categoria_descripcion = c.curso_categoria
    LEFT JOIN INNER_JOY.MedioDePago mdp ON mdp.medio_de_pago_id = p.medio_pago
    LEFT JOIN INNER_JOY.BI_MedioPago bmp ON bmp.medio_pago_id = mdp.medio_de_pago_id
    JOIN INNER_JOY.BI_Tiempo tDim 
        ON tDim.anio = YEAR(f.factura_fecha_vencimiento) 
        AND tDim.mes = MONTH(f.factura_fecha_vencimiento)
    
    WHERE bmp.medio_pago_id IS NOT NULL 
    GROUP BY tDim.tiempo_id, bs.sede_id, bmp.medio_pago_id, bc.categoria_curso_id;
END
GO

CREATE OR ALTER PROCEDURE INNER_JOY.migrar_hecho_encuesta
AS
BEGIN
    SET NOCOUNT ON;
    TRUNCATE TABLE INNER_JOY.BI_Hecho_Encuesta;

    INSERT INTO INNER_JOY.BI_Hecho_Encuesta (
        sede_dim_id, tiempo_dim_id, rango_etario_profesor_id, bloque_satisfaccion_id, cant_respuestas
    )
    SELECT
        bs.sede_id,
        tDim.tiempo_id,
        rep.rango_etario_profesor_id,
        bbs.bloque_satisfaccion_id,
        COUNT(*)
    FROM INNER_JOY.EncuestaCompletada ec
    JOIN INNER_JOY.Curso c ON c.curso_codigo = ec.encuesta_curso_codigo
    JOIN INNER_JOY.Sede s ON s.sede_id = c.curso_sede_id
    JOIN INNER_JOY.Profesor p ON p.profesor_id = c.curso_profesor_id
    JOIN INNER_JOY.Usuario u ON u.usuario_id = p.profesor_usuario_id
    JOIN INNER_JOY.Respuesta r ON r.detalle_encuesta_id = ec.encuesta_id
    
    -- Dimensiones con Joins
    JOIN INNER_JOY.BI_Sede bs ON bs.sede_id = s.sede_id
    JOIN INNER_JOY.BI_Tiempo tDim 
        ON tDim.anio = YEAR(ec.encuesta_fecha_registro) 
        AND tDim.mes = MONTH(ec.encuesta_fecha_registro)
    JOIN INNER_JOY.BI_RangoEtarioProfesor rep 
        ON DATEDIFF(YEAR, u.usuario_fecha_nacimiento, ec.encuesta_fecha_registro) BETWEEN rep.edad_min AND rep.edad_max
    JOIN INNER_JOY.BI_BloqueSatisfaccion bbs 
        ON CAST(r.detalle_nota AS DECIMAL(5,2)) BETWEEN bbs.nota_min AND bbs.nota_max

    GROUP BY bs.sede_id, tDim.tiempo_id, rep.rango_etario_profesor_id, bbs.bloque_satisfaccion_id;
END
GO

---------------------------------------- 6. EJECUCION PROCEDURES ----------------------------------------
BEGIN TRY
    BEGIN TRANSACTION
        EXEC INNER_JOY.migrar_tiempo_dim
        EXEC INNER_JOY.migrar_sede_dim
        EXEC INNER_JOY.rango_etario_alumno_dim
        EXEC INNER_JOY.migrar_turno_dim
        EXEC INNER_JOY.rango_etario_profesor_dim
        EXEC INNER_JOY.migrar_medio_pago_dim
        EXEC INNER_JOY.bloques_satisfaccion_dim
        EXEC INNER_JOY.migrar_categoria_curso_dim
        
        EXEC INNER_JOY.migrar_hecho_inscripcion
        EXEC INNER_JOY.migrar_hecho_cursada
        EXEC INNER_JOY.migrar_hecho_final
        EXEC INNER_JOY.migrar_hecho_pago
        EXEC INNER_JOY.migrar_hecho_encuesta
    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    ROLLBACK TRANSACTION;
    DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
    THROW 50000, @ErrorMessage, 1;
END CATCH
GO

---------------------------------------- 7. CREACION DE VISTAS ----------------------------------------
CREATE OR ALTER VIEW INNER_JOY.VW_CategoriasTurnosMasSolicitados AS
WITH Agregado AS (
    SELECT t.anio, s.sede_nombre AS sede, c.categoria_descripcion AS categoria, tc.turno_descripcion AS turno, SUM(h.cant_inscriptos) AS cant_inscriptos
    FROM INNER_JOY.BI_Hecho_Inscripcion h
    JOIN INNER_JOY.BI_Tiempo t ON t.tiempo_id = h.tiempo_dim_id
    JOIN INNER_JOY.BI_Sede s ON s.sede_id = h.sede_dim_id
    JOIN INNER_JOY.BI_CategoriaCurso c ON c.categoria_curso_id = h.categoria_dim_id
    JOIN INNER_JOY.BI_TurnoCurso tc ON tc.turno_curso_id = h.turno_dim_id
    GROUP BY t.anio, s.sede_nombre, c.categoria_descripcion, tc.turno_descripcion
),
Data AS (
    SELECT anio, sede, categoria, turno, cant_inscriptos, ROW_NUMBER() OVER (PARTITION BY anio, sede ORDER BY cant_inscriptos DESC) AS clasificacion
    FROM Agregado
)
SELECT anio, sede, categoria, turno, cant_inscriptos, clasificacion FROM Data WHERE clasificacion <= 3;
GO

CREATE OR ALTER VIEW INNER_JOY.VW_TasaRechazoInscripciones AS
SELECT t.anio, t.mes, s.sede_nombre AS sede, SUM(h.cant_inscriptos) AS cant_inscriptos, SUM(h.cant_rechazados) AS cant_rechazados,
    CASE WHEN SUM(h.cant_inscriptos) = 0 THEN 0 ELSE CAST(SUM(h.cant_rechazados) * 100.0 / SUM(h.cant_inscriptos) AS DECIMAL(10,2)) END AS tasa_rechazo_porcentaje
FROM INNER_JOY.BI_Hecho_Inscripcion h
JOIN INNER_JOY.BI_Tiempo t ON t.tiempo_id = h.tiempo_dim_id
JOIN INNER_JOY.BI_Sede s ON s.sede_id = h.sede_dim_id
GROUP BY t.anio, t.mes, s.sede_nombre;
GO

CREATE OR ALTER VIEW INNER_JOY.VW_Desempeno_Cursada_Sede AS
SELECT t.anio, s.sede_nombre AS sede,
    CASE WHEN SUM(h.cant_alumnos_cursada) = 0 THEN 0 ELSE CAST(SUM(h.cant_aprobados) * 100.0 / SUM(h.cant_alumnos_cursada) AS DECIMAL(10,2)) END AS porcentaje_aprobacion
FROM INNER_JOY.BI_Hecho_Cursada h
JOIN INNER_JOY.BI_Tiempo t ON t.tiempo_id = h.tiempo_dim_id
JOIN INNER_JOY.BI_Sede s ON s.sede_id = h.sede_dim_id
GROUP BY t.anio, s.sede_nombre;
GO

CREATE OR ALTER VIEW INNER_JOY.VW_Tiempo_Promedio_Finalizacion_Curso AS
SELECT t.anio, c.categoria_descripcion AS categoria, CAST(AVG(CAST(h.promedio_aprobacion_meses AS DECIMAL(10,2))) AS DECIMAL(10,2)) AS tiempo_promedio_meses
FROM INNER_JOY.BI_Hecho_Cursada h
JOIN INNER_JOY.BI_Tiempo t ON t.tiempo_id = h.tiempo_dim_id
JOIN INNER_JOY.BI_CategoriaCurso c ON c.categoria_curso_id = h.categoria_dim_id
WHERE h.promedio_aprobacion_meses IS NOT NULL
GROUP BY t.anio, c.categoria_descripcion;
GO

CREATE OR ALTER VIEW INNER_JOY.VW_NotaPromedio_Finales AS
SELECT t.anio, t.semestre, r.etiqueta AS rango_etario, c.categoria_descripcion AS categoria,
    CAST(SUM(h.sumatoria_notas) * 1.0 / SUM(h.cant_finales_tomados) AS DECIMAL(10,2)) AS nota_promedio
FROM INNER_JOY.BI_Hechos_Final h
JOIN INNER_JOY.BI_Tiempo t ON t.tiempo_id = h.tiempo_dim_id
JOIN INNER_JOY.BI_RangoEtarioAlumno r ON r.rango_etario_alumno_id = h.rango_etario_id
JOIN INNER_JOY.BI_CategoriaCurso c ON c.categoria_curso_id = h.categoria_dim_id
GROUP BY t.anio, t.semestre, r.etiqueta, c.categoria_descripcion
HAVING SUM(h.cant_finales_tomados) > 0;  
GO

CREATE OR ALTER VIEW INNER_JOY.VW_TasaAusentismo_Finales AS
SELECT t.anio, t.semestre, s.sede_nombre AS sede, CAST(100.0 * SUM(h.cant_ausentes) / NULLIF(SUM(h.cant_inscriptos), 0) AS DECIMAL(10,2)) AS tasa_ausentismo_porcentaje
FROM INNER_JOY.BI_Hechos_Final h
JOIN INNER_JOY.BI_Tiempo t ON t.tiempo_id = h.tiempo_dim_id
JOIN INNER_JOY.BI_Sede s ON s.sede_id = h.sede_dim_id
GROUP BY t.anio, t.semestre, s.sede_nombre;
GO

CREATE OR ALTER VIEW INNER_JOY.VW_Desvio_Pagos AS
WITH datos AS (
    SELECT t.anio, t.semestre, SUM(h.cant_pagos) AS total_pagos, SUM(h.cant_pagos_fuera_termino) AS pagos_fuera_termino
    FROM INNER_JOY.BI_Hecho_Pago h JOIN INNER_JOY.BI_Tiempo t ON t.tiempo_id = h.tiempo_dim_id
    GROUP BY t.anio, t.semestre
)
SELECT anio, semestre, total_pagos, pagos_fuera_termino, CASE WHEN total_pagos = 0 THEN 0 ELSE CAST(100.0 * pagos_fuera_termino / total_pagos AS DECIMAL(5,2)) END AS porcentaje_desvio
FROM datos;
GO

CREATE OR ALTER VIEW INNER_JOY.VW_Tasa_Morosidad_Mensual AS
SELECT t.anio, t.mes, SUM(h.facturacion_esperada) AS facturacion_esperada, SUM(h.facturacion_esperada - h.facturacion_cobrada) AS monto_adeudado,
    CASE WHEN SUM(h.facturacion_esperada) = 0 THEN 0 ELSE CAST(SUM(h.facturacion_esperada - h.facturacion_cobrada) * 100.0 / SUM(h.facturacion_esperada) AS DECIMAL(10,2)) END AS tasa_morosidad_porcentaje
FROM INNER_JOY.BI_Hecho_Pago h JOIN INNER_JOY.BI_Tiempo t ON t.tiempo_id = h.tiempo_dim_id
GROUP BY t.anio, t.mes;
GO

CREATE OR ALTER VIEW INNER_JOY.VW_Ingresos_Top3_Categoria_Por_Sede AS
WITH datos AS (
    SELECT t.anio, s.sede_nombre AS sede, c.categoria_descripcion AS categoria, SUM(h.facturacion_cobrada) AS ingresos
    FROM INNER_JOY.BI_Hecho_Pago h JOIN INNER_JOY.BI_Tiempo t ON t.tiempo_id = h.tiempo_dim_id
    JOIN INNER_JOY.BI_Sede s ON s.sede_id = h.sede_dim_id JOIN INNER_JOY.BI_CategoriaCurso c ON c.categoria_curso_id = h.categoria_dim_id
    GROUP BY t.anio, s.sede_nombre, c.categoria_descripcion
),
ranked AS (
    SELECT anio, sede, categoria, ingresos, RANK() OVER (PARTITION BY anio, sede ORDER BY ingresos DESC) AS clasificacion FROM datos
)
SELECT anio, sede, categoria, ingresos, clasificacion FROM ranked WHERE clasificacion <= 3;
GO

CREATE OR ALTER VIEW INNER_JOY.VW_Indice_Satisfaccion AS
SELECT t.anio, s.sede_nombre AS sede, r.etiqueta AS rango_etario,
    CASE WHEN SUM(h.cant_respuestas) = 0 THEN NULL
        ELSE CAST(((100.0 * SUM(CASE WHEN b.etiqueta = 'Satisfechos' THEN h.cant_respuestas ELSE 0 END) / SUM(h.cant_respuestas)
                  - 100.0 * SUM(CASE WHEN b.etiqueta = 'Insatisfechos' THEN h.cant_respuestas ELSE 0 END) / SUM(h.cant_respuestas)) + 100.0) / 2.0 AS DECIMAL(10,2))
    END AS indice_satisfaccion
FROM INNER_JOY.BI_Hecho_Encuesta h
JOIN INNER_JOY.BI_Tiempo t ON t.tiempo_id = h.tiempo_dim_id
JOIN INNER_JOY.BI_Sede s ON s.sede_id = h.sede_dim_id
JOIN INNER_JOY.BI_RangoEtarioProfesor r ON r.rango_etario_profesor_id = h.rango_etario_profesor_id
JOIN INNER_JOY.BI_BloqueSatisfaccion b ON b.bloque_satisfaccion_id = h.bloque_satisfaccion_id
GROUP BY t.anio, s.sede_nombre, r.etiqueta;
GO

--Mostrar las vistas
SELECT * FROM INNER_JOY.VW_CategoriasTurnosMasSolicitados;
SELECT * FROM INNER_JOY.VW_TasaRechazoInscripciones;
SELECT * FROM INNER_JOY.VW_Desempeno_Cursada_Sede;
SELECT * FROM INNER_JOY.VW_Tiempo_Promedio_Finalizacion_Curso;
SELECT * FROM INNER_JOY.VW_NotaPromedio_Finales;
SELECT * FROM INNER_JOY.VW_TasaAusentismo_Finales;
SELECT * FROM INNER_JOY.VW_Desvio_Pagos;
SELECT * FROM INNER_JOY.VW_Tasa_Morosidad_Mensual;
SELECT * FROM INNER_JOY.VW_Ingresos_Top3_Categoria_Por_Sede;
SELECT * FROM INNER_JOY.VW_Indice_Satisfaccion;