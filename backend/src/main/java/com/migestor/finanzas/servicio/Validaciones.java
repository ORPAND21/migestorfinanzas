package com.migestor.finanzas.servicio;

import com.migestor.finanzas.excepcion.ReglaNegocioException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/** Validaciones de datos de entrada compartidas por los servicios. */
public final class Validaciones {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[A-Za-z]{2,}$");
    /** Minimo 8 caracteres con mayuscula, minuscula, numero y simbolo (RNF de seguridad). */
    private static final Pattern PASSWORD = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");
    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("999999999999.99");

    private Validaciones() { }

    public static String texto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("El campo " + campo + " es obligatorio.");
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new ReglaNegocioException("El campo " + campo + " admite máximo " + maximo + " caracteres.");
        }
        return limpio;
    }

    public static String textoOpcional(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return texto(valor, campo, maximo);
    }

    public static String email(String valor) {
        String limpio = texto(valor, "correo", 120).toLowerCase();
        if (!EMAIL.matcher(limpio).matches()) {
            throw new ReglaNegocioException("El correo electrónico no tiene un formato válido.");
        }
        return limpio;
    }

    public static void password(String valor) {
        if (valor == null || !PASSWORD.matcher(valor).matches()) {
            throw new ReglaNegocioException("La contraseña debe tener mínimo 8 caracteres e incluir "
                    + "mayúscula, minúscula, número y símbolo.");
        }
    }

    public static BigDecimal monto(BigDecimal valor, String campo) {
        if (valor == null) {
            throw new ReglaNegocioException("El campo " + campo + " es obligatorio.");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El " + campo + " debe ser mayor que cero.");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new ReglaNegocioException("El " + campo + " admite máximo dos decimales.");
        }
        if (valor.compareTo(MONTO_MAXIMO) > 0) {
            throw new ReglaNegocioException("El " + campo + " supera el valor máximo permitido.");
        }
        return valor;
    }

    public static LocalDate fechaNoFutura(LocalDate fecha, String campo) {
        if (fecha == null) {
            throw new ReglaNegocioException("El campo " + campo + " es obligatorio.");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new ReglaNegocioException("La " + campo + " no puede ser posterior a hoy.");
        }
        return fecha;
    }

    public static LocalDate fechaFutura(LocalDate fecha, String campo) {
        if (fecha == null) {
            throw new ReglaNegocioException("El campo " + campo + " es obligatorio.");
        }
        if (!fecha.isAfter(LocalDate.now())) {
            throw new ReglaNegocioException("La " + campo + " debe ser posterior a hoy.");
        }
        return fecha;
    }

    public static void periodo(Integer anio, Integer mes) {
        if (anio == null || mes == null) {
            throw new ReglaNegocioException("Debe indicar el año y el mes.");
        }
        if (mes < 1 || mes > 12) {
            throw new ReglaNegocioException("El mes debe estar entre 1 y 12.");
        }
        if (anio < 2000 || anio > 2100) {
            throw new ReglaNegocioException("El año no es válido.");
        }
    }

    /** Porcentaje entero (0 si la base es cero). */
    public static int porcentaje(BigDecimal parte, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return parte.multiply(BigDecimal.valueOf(100))
                .divide(total, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }
}
