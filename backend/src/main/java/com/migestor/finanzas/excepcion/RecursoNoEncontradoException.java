package com.migestor.finanzas.excepcion;

/** El registro no existe o no pertenece al usuario. Se responde con HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) { super(mensaje); }
}
