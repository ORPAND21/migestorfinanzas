package com.migestor.finanzas.servicio;

import static org.junit.jupiter.api.Assertions.*;

import com.migestor.finanzas.excepcion.ReglaNegocioException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas de las validaciones de datos de entrada. */
class ValidacionesTest {

    @Test
    @DisplayName("Acepta una contraseña con mayúscula, minúscula, número y símbolo")
    void passwordValida() {
        assertDoesNotThrow(() -> Validaciones.password("Clave123*"));
    }

    @Test
    @DisplayName("Rechaza contraseñas cortas o sin símbolo")
    void passwordInvalida() {
        assertThrows(ReglaNegocioException.class, () -> Validaciones.password("Ab1*"));
        assertThrows(ReglaNegocioException.class, () -> Validaciones.password("Clave1234"));
        assertThrows(ReglaNegocioException.class, () -> Validaciones.password(null));
    }

    @Test
    @DisplayName("Normaliza el correo y rechaza formatos inválidos")
    void email() {
        assertEquals("laura@demo.com", Validaciones.email("  Laura@Demo.com "));
        assertThrows(ReglaNegocioException.class, () -> Validaciones.email("laura@"));
        assertThrows(ReglaNegocioException.class, () -> Validaciones.email(""));
    }

    @Test
    @DisplayName("El monto debe ser positivo y con máximo dos decimales")
    void monto() {
        assertEquals(new BigDecimal("15000.50"), Validaciones.monto(new BigDecimal("15000.50"), "monto"));
        assertThrows(ReglaNegocioException.class, () -> Validaciones.monto(new BigDecimal("-1"), "monto"));
        assertThrows(ReglaNegocioException.class, () -> Validaciones.monto(BigDecimal.ZERO, "monto"));
        assertThrows(ReglaNegocioException.class, () -> Validaciones.monto(new BigDecimal("10.555"), "monto"));
        assertThrows(ReglaNegocioException.class, () -> Validaciones.monto(null, "monto"));
    }

    @Test
    @DisplayName("La fecha de un movimiento no puede ser futura")
    void fechaNoFutura() {
        assertEquals(LocalDate.now(), Validaciones.fechaNoFutura(LocalDate.now(), "fecha"));
        assertThrows(ReglaNegocioException.class,
                () -> Validaciones.fechaNoFutura(LocalDate.now().plusDays(1), "fecha"));
    }

    @Test
    @DisplayName("Calcula porcentajes redondeados y evita la división por cero")
    void porcentaje() {
        assertEquals(85, Validaciones.porcentaje(new BigDecimal("170000"), new BigDecimal("200000")));
        assertEquals(0, Validaciones.porcentaje(new BigDecimal("5000"), BigDecimal.ZERO));
    }
}
