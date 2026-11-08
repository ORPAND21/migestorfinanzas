package com.migestor.finanzas.excepcion;

/** Dato invalido o regla de negocio incumplida. Se responde con HTTP 400. */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) { super(mensaje); }
}
