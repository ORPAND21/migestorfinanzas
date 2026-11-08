package com.migestor.finanzas.dto;

import java.math.BigDecimal;

/** Objetos de entrada y salida del modulo de presupuestos. */
public final class PresupuestoDto {

    private PresupuestoDto() { }

    public record Request(Long categoriaId, Integer anio, Integer mes, BigDecimal montoLimite) { }

    /** estado: NORMAL, ADVERTENCIA o EXCEDIDO. */
    public record Response(Long id, Long categoriaId, String categoriaNombre, Integer anio, Integer mes,
                           BigDecimal montoLimite, BigDecimal gastado, BigDecimal disponible,
                           int porcentaje, String estado) { }
}
