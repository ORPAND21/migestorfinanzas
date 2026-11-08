package com.migestor.finanzas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Objetos de entrada y salida del modulo de metas de ahorro. */
public final class MetaDto {

    private MetaDto() { }

    public record Request(String nombre, BigDecimal montoObjetivo, LocalDate fechaLimite) { }

    public record AporteRequest(BigDecimal monto) { }

    /** estado: EN_PROGRESO, COMPLETADA o VENCIDA. */
    public record Response(Long id, String nombre, BigDecimal montoObjetivo, BigDecimal montoAhorrado,
                           BigDecimal restante, int porcentaje, LocalDate fechaLimite,
                           LocalDate fechaCreacion, String estado) { }

    public record AporteResponse(Long id, BigDecimal monto, LocalDate fecha) { }
}
