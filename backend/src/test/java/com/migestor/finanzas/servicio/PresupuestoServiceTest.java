package com.migestor.finanzas.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas del cálculo del estado de un presupuesto (RF006.2 y RF006.3). */
class PresupuestoServiceTest {

    private static final BigDecimal LIMITE = new BigDecimal("500000");

    @Test
    @DisplayName("Menos del 80 % del límite es NORMAL")
    void normal() {
        assertEquals("NORMAL", PresupuestoService.calcularEstado(new BigDecimal("300000"), LIMITE));
    }

    @Test
    @DisplayName("Desde el 80 % hasta el límite es ADVERTENCIA")
    void advertencia() {
        assertEquals("ADVERTENCIA", PresupuestoService.calcularEstado(new BigDecimal("400000"), LIMITE));
        assertEquals("ADVERTENCIA", PresupuestoService.calcularEstado(new BigDecimal("500000"), LIMITE));
    }

    @Test
    @DisplayName("Superar el límite es EXCEDIDO")
    void excedido() {
        assertEquals("EXCEDIDO", PresupuestoService.calcularEstado(new BigDecimal("500001"), LIMITE));
    }
}
