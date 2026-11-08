-- =====================================================================
-- MiGestorFinanzas - Datos de prueba (ejecutar después de 01_esquema.sql)
-- Usuario de demostración: laura@demo.com / Demo123*
-- Usuario inactivo para probar el bloqueo: carlos@demo.com / Demo123*
-- =====================================================================
SET NAMES utf8mb4;
USE migestorfinanzas_db;

INSERT INTO usuario (id, nombre, email, password_hash, rol, activo, notificaciones, fecha_registro) VALUES
 (2, 'Laura Gómez', 'laura@demo.com',
  'pbkdf2$65536$ymx9VgpERdFK+PkM+bgJfw==$iQywRSbyNZeYJKVCeYYD9T0oBKulTb7NqhRTQ0fmP18=',
  'USUARIO', b'1', b'1', '2026-09-01 08:00:00'),
 (3, 'Carlos Pérez', 'carlos@demo.com',
  'pbkdf2$65536$ymx9VgpERdFK+PkM+bgJfw==$iQywRSbyNZeYJKVCeYYD9T0oBKulTb7NqhRTQ0fmP18=',
  'USUARIO', b'0', b'1', '2026-09-15 10:30:00');

-- Movimientos de septiembre a noviembre de 2026
INSERT INTO transaccion (usuario_id, categoria_id, tipo, monto, fecha, descripcion) VALUES
 (2, 1,  'INGRESO', 2800000.00, '2026-09-01', 'Salario de septiembre'),
 (2, 8,  'GASTO',    850000.00, '2026-09-02', 'Arriendo'),
 (2, 6,  'GASTO',    520000.00, '2026-09-10', 'Mercado del mes'),
 (2, 7,  'GASTO',    180000.00, '2026-09-12', 'Recargas de transporte'),
 (2, 9,  'GASTO',    210000.00, '2026-09-15', 'Agua, luz y gas'),
 (2, 12, 'GASTO',    150000.00, '2026-09-20', 'Cine y comida'),
 (2, 1,  'INGRESO', 2800000.00, '2026-10-01', 'Salario de octubre'),
 (2, 4,  'INGRESO',  350000.00, '2026-10-08', 'Venta de bicicleta usada'),
 (2, 8,  'GASTO',    850000.00, '2026-10-02', 'Arriendo'),
 (2, 6,  'GASTO',    610000.00, '2026-10-11', 'Mercado del mes'),
 (2, 7,  'GASTO',    175000.00, '2026-10-13', 'Recargas de transporte'),
 (2, 9,  'GASTO',    198000.00, '2026-10-15', 'Agua, luz y gas'),
 (2, 10, 'GASTO',     95000.00, '2026-10-22', 'Medicamentos'),
 (2, 12, 'GASTO',    240000.00, '2026-10-25', 'Concierto'),
 (2, 1,  'INGRESO', 2800000.00, '2026-11-01', 'Salario de noviembre'),
 (2, 2,  'INGRESO',  400000.00, '2026-11-03', 'Bono por desempeño'),
 (2, 8,  'GASTO',    850000.00, '2026-11-02', 'Arriendo'),
 (2, 6,  'GASTO',    430000.00, '2026-11-04', 'Mercado quincenal'),
 (2, 7,  'GASTO',     90000.00, '2026-11-05', 'Recargas de transporte'),
 (2, 12, 'GASTO',    220000.00, '2026-11-06', 'Salida de fin de semana'),
 (2, 11, 'GASTO',    120000.00, '2026-11-07', 'Curso virtual de inglés');

-- Presupuestos de noviembre de 2026 (Alimentación en advertencia y Ocio excedido)
INSERT INTO presupuesto (usuario_id, categoria_id, anio, mes, monto_limite) VALUES
 (2, 6,  2026, 11, 500000.00),
 (2, 7,  2026, 11, 200000.00),
 (2, 12, 2026, 11, 200000.00);

-- Metas de ahorro
INSERT INTO meta_ahorro (id, usuario_id, nombre, monto_objetivo, monto_ahorrado, fecha_limite, fecha_creacion) VALUES
 (1, 2, 'Fondo de emergencia', 3000000.00, 900000.00, '2027-06-30', '2026-09-05'),
 (2, 2, 'Viaje a Cartagena',   1500000.00, 300000.00, '2027-01-15', '2026-10-01');

INSERT INTO aporte_meta (meta_id, monto, fecha) VALUES
 (1, 400000.00, '2026-09-05'),
 (1, 500000.00, '2026-10-05'),
 (2, 300000.00, '2026-10-10');
