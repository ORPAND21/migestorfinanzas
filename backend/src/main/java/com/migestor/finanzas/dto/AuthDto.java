package com.migestor.finanzas.dto;

import com.migestor.finanzas.modelo.Rol;

/** Objetos de entrada y salida del modulo de autenticacion y perfil. */
public final class AuthDto {

    private AuthDto() { }

    public record RegistroRequest(String nombre, String email, String password) { }

    public record LoginRequest(String email, String password) { }

    public record UsuarioSesion(Long id, String nombre, String email, Rol rol, boolean notificaciones) { }

    public record LoginResponse(String token, UsuarioSesion usuario) { }

    public record PerfilRequest(String nombre, Boolean notificaciones) { }

    public record CambioPasswordRequest(String actual, String nueva) { }
}
