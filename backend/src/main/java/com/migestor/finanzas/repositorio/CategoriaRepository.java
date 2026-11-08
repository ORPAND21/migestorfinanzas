package com.migestor.finanzas.repositorio;

import com.migestor.finanzas.modelo.Categoria;
import com.migestor.finanzas.modelo.TipoMovimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByTipoOrderByNombreAsc(TipoMovimiento tipo);

    List<Categoria> findAllByOrderByTipoAscNombreAsc();
}
