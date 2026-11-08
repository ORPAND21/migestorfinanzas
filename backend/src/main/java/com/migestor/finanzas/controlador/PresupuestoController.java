package com.migestor.finanzas.controlador;

import com.migestor.finanzas.dto.PresupuestoDto;
import com.migestor.finanzas.seguridad.AuthInterceptor;
import com.migestor.finanzas.servicio.PresupuestoService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/presupuestos")
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    public PresupuestoController(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
    }

    /** GET /api/presupuestos?anio=2026&mes=11 */
    @GetMapping
    public List<PresupuestoDto.Response> listar(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                                @RequestParam(name = "anio") Integer anio,
                                                @RequestParam(name = "mes") Integer mes) {
        return presupuestoService.listar(usuarioId, anio, mes);
    }

    @PostMapping
    public ResponseEntity<PresupuestoDto.Response> crear(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                                         @RequestBody PresupuestoDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(presupuestoService.crear(usuarioId, req));
    }

    @PutMapping("/{id}")
    public PresupuestoDto.Response actualizar(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                              @PathVariable("id") Long id,
                                              @RequestBody PresupuestoDto.Request req) {
        return presupuestoService.actualizar(usuarioId, id, req);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> eliminar(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                        @PathVariable("id") Long id) {
        presupuestoService.eliminar(usuarioId, id);
        return Map.of("mensaje", "Presupuesto eliminado correctamente.");
    }
}
