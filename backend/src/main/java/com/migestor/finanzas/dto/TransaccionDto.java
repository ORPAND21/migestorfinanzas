package com.migestor.finanzas.dto;

import com.migestor.finanzas.modelo.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Objetos de entrada y salida del modulo de ingresos y gastos. */
public final class TransaccionDto {

    private TransaccionDto() { }

    public record Request(TipoMovimiento tipo, Long categoriaId, BigDecimal monto, LocalDate fecha, String descripcion) { }

    public record Response(Long id, TipoMovimiento tipo, Long categoriaId, String categoriaNombre,
                           BigDecimal monto, LocalDate fecha, String descripcion) { }

    /** Aviso de presupuesto: nivel ADVERTENCIA (80 % o mas) o EXCEDIDO (mas del 100 %). */
    public record Alerta(String nivel, String mensaje) { }

    public record Guardada(Response transaccion, Alerta alerta) { }

    public record Resumen(BigDecimal totalIngresos, BigDecimal totalGastos,
                          BigDecimal totalAhorrado, BigDecimal saldoDisponible) { }
}
