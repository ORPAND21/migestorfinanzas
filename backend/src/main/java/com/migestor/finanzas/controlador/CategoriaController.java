package com.migestor.finanzas.controlador;

import com.migestor.finanzas.dto.CategoriaDto;
import com.migestor.finanzas.modelo.TipoMovimiento;
import com.migestor.finanzas.servicio.CategoriaService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    /** GET /api/categorias?tipo=GASTO */
    @GetMapping
    public List<CategoriaDto> listar(@RequestParam(name = "tipo", required = false) TipoMovimiento tipo) {
        return categoriaService.listar(tipo);
    }
}
