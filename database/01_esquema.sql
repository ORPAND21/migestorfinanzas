-- =====================================================================
-- MiGestorFinanzas - Script de creación de la base de datos
-- Motor: MySQL 8 / MariaDB 10 (XAMPP)
-- Ejecutar en phpMyAdmin (pestaña SQL) o en la consola de MySQL.
-- ADVERTENCIA: borra las tablas si ya existen (reinstalación limpia).
-- =====================================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS migestorfinanzas_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE migestorfinanzas_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS aporte_meta;
DROP TABLE IF EXISTS meta_ahorro;
DROP TABLE IF EXISTS presupuesto;
DROP TABLE IF EXISTS transaccion;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS usuario;
SET FOREIGN_KEY_CHECKS = 1;

-- Personas registradas. La contraseña se guarda cifrada con PBKDF2.
CREATE TABLE usuario (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  nombre          VARCHAR(80)  NOT NULL,
  email           VARCHAR(120) NOT NULL,
  password_hash   VARCHAR(200) NOT NULL,
  rol             VARCHAR(20)  NOT NULL DEFAULT 'USUARIO',  -- USUARIO | ADMIN
  activo          BIT(1)       NOT NULL DEFAULT b'1',
  notificaciones  BIT(1)       NOT NULL DEFAULT b'1',
  fecha_registro  DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_usuario_email (email)
) ENGINE=InnoDB;

-- Categorías predefinidas para ingresos y gastos.
CREATE TABLE categoria (
  id      BIGINT      NOT NULL AUTO_INCREMENT,
  nombre  VARCHAR(60) NOT NULL,
  tipo    VARCHAR(20) NOT NULL,  -- INGRESO | GASTO
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Ingresos y gastos de cada usuario.
CREATE TABLE transaccion (
  id            BIGINT        NOT NULL AUTO_INCREMENT,
  usuario_id    BIGINT        NOT NULL,
  categoria_id  BIGINT        NOT NULL,
  tipo          VARCHAR(20)   NOT NULL,  -- INGRESO | GASTO
  monto         DECIMAL(15,2) NOT NULL,
  fecha         DATE          NOT NULL,
  descripcion   VARCHAR(150)  NULL,
  PRIMARY KEY (id),
  KEY idx_transaccion_usuario_fecha (usuario_id, fecha),
  CONSTRAINT fk_transaccion_usuario   FOREIGN KEY (usuario_id)   REFERENCES usuario (id),
  CONSTRAINT fk_transaccion_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
  CONSTRAINT chk_transaccion_monto CHECK (monto > 0)
) ENGINE=InnoDB;

-- Límite de gasto mensual por categoría. Uno por usuario, categoría y mes.
CREATE TABLE presupuesto (
  id            BIGINT        NOT NULL AUTO_INCREMENT,
  usuario_id    BIGINT        NOT NULL,
  categoria_id  BIGINT        NOT NULL,
  anio          INT           NOT NULL,
  mes           INT           NOT NULL,
  monto_limite  DECIMAL(15,2) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_presupuesto_usuario_categoria_mes (usuario_id, categoria_id, anio, mes),
  CONSTRAINT fk_presupuesto_usuario   FOREIGN KEY (usuario_id)   REFERENCES usuario (id),
  CONSTRAINT fk_presupuesto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
  CONSTRAINT chk_presupuesto_mes CHECK (mes BETWEEN 1 AND 12),
  CONSTRAINT chk_presupuesto_monto CHECK (monto_limite > 0)
) ENGINE=InnoDB;

-- Metas de ahorro.
CREATE TABLE meta_ahorro (
  id              BIGINT        NOT NULL AUTO_INCREMENT,
  usuario_id      BIGINT        NOT NULL,
  nombre          VARCHAR(80)   NOT NULL,
  monto_objetivo  DECIMAL(15,2) NOT NULL,
  monto_ahorrado  DECIMAL(15,2) NOT NULL DEFAULT 0,
  fecha_limite    DATE          NOT NULL,
  fecha_creacion  DATE          NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_meta_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE=InnoDB;

-- Historial de aportes a cada meta.
CREATE TABLE aporte_meta (
  id       BIGINT        NOT NULL AUTO_INCREMENT,
  meta_id  BIGINT        NOT NULL,
  monto    DECIMAL(15,2) NOT NULL,
  fecha    DATE          NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_aporte_meta FOREIGN KEY (meta_id) REFERENCES meta_ahorro (id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Datos maestros
-- ---------------------------------------------------------------------
INSERT INTO categoria (id, nombre, tipo) VALUES
 (1, 'Salario', 'INGRESO'),
 (2, 'Bono', 'INGRESO'),
 (3, 'Regalo', 'INGRESO'),
 (4, 'Ventas', 'INGRESO'),
 (5, 'Otros ingresos', 'INGRESO'),
 (6, 'Alimentación', 'GASTO'),
 (7, 'Transporte', 'GASTO'),
 (8, 'Vivienda', 'GASTO'),
 (9, 'Servicios públicos', 'GASTO'),
 (10, 'Salud', 'GASTO'),
 (11, 'Educación', 'GASTO'),
 (12, 'Ocio', 'GASTO'),
 (13, 'Otros gastos', 'GASTO');

-- Administrador: admin@migestor.com / Admin123*
INSERT INTO usuario (id, nombre, email, password_hash, rol, activo, notificaciones, fecha_registro) VALUES
 (1, 'Administrador', 'admin@migestor.com',
  'pbkdf2$65536$SrhQmdOYRqF/R/0y13sMJg==$8HtzDg3y0OSDaHGvTnSU4TbIcI/4yzCToQUOJE+0pp8=',
  'ADMIN', b'1', b'1', NOW());
