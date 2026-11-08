package com.migestor.finanzas.modelo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Abono de dinero a una meta de ahorro. Guarda el historial de aportes. */
@Entity
@Table(name = "aporte_meta")
public class AporteMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "meta_id", nullable = false)
    private MetaAhorro meta;

    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha = LocalDate.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MetaAhorro getMeta() { return meta; }
    public void setMeta(MetaAhorro meta) { this.meta = meta; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
}
