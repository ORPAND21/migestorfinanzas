package com.migestor.finanzas.excepcion;

/** El usuario no tiene el rol requerido. Se responde con HTTP 403. */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) { super(mensaje); }
}
