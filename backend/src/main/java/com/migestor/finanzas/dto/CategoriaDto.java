package com.migestor.finanzas.dto;

import com.migestor.finanzas.modelo.TipoMovimiento;

public record CategoriaDto(Long id, String nombre, TipoMovimiento tipo) { }
