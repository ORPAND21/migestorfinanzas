package com.migestor.finanzas.controlador;

import com.migestor.finanzas.dto.ReporteDto;
import com.migestor.finanzas.seguridad.AuthInterceptor;
import com.migestor.finanzas.servicio.ReporteService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    /** GET /api/reportes/panel - Datos del panel de inicio. */
    @GetMapping("/panel")
    public ReporteDto.Panel panel(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId) {
        return reporteService.panel(usuarioId);
    }

    /** GET /api/reportes/mensual?anio=2026&mes=11 (RF004.2.1). */
    @GetMapping("/mensual")
    public ReporteDto.Mensual mensual(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                      @RequestParam(name = "anio") Integer anio,
                                      @RequestParam(name = "mes") Integer mes) {
        return reporteService.mensual(usuarioId, anio, mes);
    }

    /** GET /api/reportes/anual?anio=2026 (RF004.2.2). */
    @GetMapping("/anual")
    public ReporteDto.Anual anual(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                  @RequestParam(name = "anio") Integer anio) {
        return reporteService.anual(usuarioId, anio);
    }
}
