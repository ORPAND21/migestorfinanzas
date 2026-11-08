package com.migestor.finanzas.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Persona registrada en el sistema. */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "email", nullable = false, unique = true, length = 120)
    private String email;

    /** Contrasena cifrada con PBKDF2 (nunca se guarda en texto plano). */
    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol = Rol.USUARIO;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    /** Si es false, el sistema no muestra alertas de presupuesto. */
    @Column(name = "notificaciones", nullable = false)
    private boolean notificaciones = true;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public boolean isNotificaciones() { return notificaciones; }
    public void setNotificaciones(boolean notificaciones) { this.notificaciones = notificaciones; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
