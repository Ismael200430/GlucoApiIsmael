-- Esquema para MySQL 8 / MySQL-compatible (Filess.io, TiDB Cloud Starter).
-- Ejecutar sobre la base de datos ya creada, por ejemplo gluco_api.
-- No se crea FOREIGN KEY para mantener compatibilidad con TiDB.
-- La API valida que el usuario exista antes de insertar una medida.

CREATE TABLE IF NOT EXISTS usuario (
    idUsuario INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(40) NOT NULL,
    primerApellido VARCHAR(40) NOT NULL,
    segundoApellido VARCHAR(40) NULL,
    email VARCHAR(50) NOT NULL,
    PRIMARY KEY (idUsuario)
) DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS medida (
    idMedida INT NOT NULL AUTO_INCREMENT,
    idUsuario INT NOT NULL,
    glucosa INT NOT NULL,
    fechaHora DATETIME NULL,
    contexto VARCHAR(40) NULL,
    notas VARCHAR(100) NULL,
    PRIMARY KEY (idMedida),
    KEY idx_medida_usuario (idUsuario)
) DEFAULT CHARSET = utf8mb4;
