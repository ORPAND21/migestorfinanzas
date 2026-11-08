package com.migestor.finanzas.servicio;

import java.math.BigDecimal;
import java.util.Locale;

/** Formatos de texto para mensajes al usuario. */
public final class Formato {

    private static final Locale COLOMBIA = Locale.forLanguageTag("es-CO");
    private static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
        "agosto", "septiembre", "octubre", "noviembre", "diciembre"};

    private Formato() { }

    /** Ejemplo: 1250000 -> "$1.250.000". */
    public static String pesos(BigDecimal valor) {
        return "$" + String.format(COLOMBIA, "%,.0f", valor);
    }

    public static String mes(int mes) {
        return MESES[mes - 1];
    }
}
