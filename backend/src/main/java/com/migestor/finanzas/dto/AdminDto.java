package com.migestor.finanzas.dto;

import com.migestor.finanzas.modelo.Rol;
import java.time.LocalDateTime;

/** Objetos del modulo de administracion. */
public final class AdminDto {

    private AdminDto() { }

    public record UsuarioAdmin(Long id, String nombre, String email, Rol rol, boolean activo, LocalDateTime fechaRegistro) { }

    public record EstadoRequest(Boolean activo) { }

    public record Estadisticas(long totalUsuarios, long usuariosActivos, long usuariosInactivos,
                               long totalTransacciones, long totalMetas, long totalPresupuestos) { }
}
