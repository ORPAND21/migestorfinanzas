package com.migestor.finanzas.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Objetivo de ahorro de un usuario (ej. "Fondo de emergencia"). */
@Entity
@Table(name = "meta_ahorro")
public class MetaAhorro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "monto_objetivo", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoObjetivo;

    @Column(name = "monto_ahorrado", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoAhorrado = BigDecimal.ZERO;

    @Column(name = "fecha_limite", nullable = false)
    private LocalDate fechaLimite;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion = LocalDate.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public BigDecimal getMontoObjetivo() { return montoObjetivo; }
    public void setMontoObjetivo(BigDecimal montoObjetivo) { this.montoObjetivo = montoObjetivo; }
    public BigDecimal getMontoAhorrado() { return montoAhorrado; }
    public void setMontoAhorrado(BigDecimal montoAhorrado) { this.montoAhorrado = montoAhorrado; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
