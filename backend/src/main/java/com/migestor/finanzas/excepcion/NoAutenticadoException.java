package com.migestor.finanzas.excepcion;

/** Credenciales incorrectas o sesion vencida. Se responde con HTTP 401. */
public class NoAutenticadoException extends RuntimeException {
    public NoAutenticadoException(String mensaje) { super(mensaje); }
}
