package com.migestor.finanzas.repositorio;

import com.migestor.finanzas.modelo.Presupuesto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    Optional<Presupuesto> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Presupuesto> findByUsuarioIdAndAnioAndMesOrderByIdAsc(Long usuarioId, Integer anio, Integer mes);

    Optional<Presupuesto> findByUsuarioIdAndCategoriaIdAndAnioAndMes(Long usuarioId, Long categoriaId, Integer anio, Integer mes);
}
