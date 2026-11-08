package com.migestor.finanzas.servicio;

import com.migestor.finanzas.dto.CategoriaDto;
import com.migestor.finanzas.modelo.Categoria;
import com.migestor.finanzas.modelo.TipoMovimiento;
import com.migestor.finanzas.repositorio.CategoriaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta de las categorías predefinidas de ingresos y gastos. */
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDto> listar(TipoMovimiento tipo) {
        List<Categoria> categorias = (tipo == null)
                ? categoriaRepository.findAllByOrderByTipoAscNombreAsc()
                : categoriaRepository.findByTipoOrderByNombreAsc(tipo);
        return categorias.stream()
                .map(c -> new CategoriaDto(c.getId(), c.getNombre(), c.getTipo()))
                .toList();
    }
}
