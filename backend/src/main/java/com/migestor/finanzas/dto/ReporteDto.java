package com.migestor.finanzas.dto;

import java.math.BigDecimal;
import java.util.List;

/** Objetos de salida del modulo de reportes y del panel de inicio. */
public final class ReporteDto {

    private ReporteDto() { }

    public record TotalCategoria(String categoria, BigDecimal total, int porcentaje) { }

    public record Mensual(int anio, int mes, BigDecimal totalIngresos, BigDecimal totalGastos, BigDecimal ahorroNeto,
                          List<TotalCategoria> ingresosPorCategoria, List<TotalCategoria> gastosPorCategoria,
                          List<TransaccionDto.Response> movimientos) { }

    public record Mes(int mes, BigDecimal ingresos, BigDecimal gastos, BigDecimal balance) { }

    public record Anual(int anio, List<Mes> meses, BigDecimal totalIngresos, BigDecimal totalGastos, BigDecimal balance) { }

    public record Panel(TransaccionDto.Resumen resumen, BigDecimal ingresosMes, BigDecimal gastosMes,
                        List<TransaccionDto.Response> ultimosMovimientos, List<TransaccionDto.Alerta> alertas,
                        long metasEnProgreso) { }
}
