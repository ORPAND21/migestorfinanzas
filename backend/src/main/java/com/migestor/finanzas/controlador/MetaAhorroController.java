package com.migestor.finanzas.controlador;

import com.migestor.finanzas.dto.MetaDto;
import com.migestor.finanzas.seguridad.AuthInterceptor;
import com.migestor.finanzas.servicio.MetaAhorroService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metas")
public class MetaAhorroController {

    private final MetaAhorroService metaService;

    public MetaAhorroController(MetaAhorroService metaService) {
        this.metaService = metaService;
    }

    @GetMapping
    public List<MetaDto.Response> listar(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId) {
        return metaService.listar(usuarioId);
    }

    /** POST /api/metas - Crea una meta de ahorro (CU05). */
    @PostMapping
    public ResponseEntity<MetaDto.Response> crear(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                                  @RequestBody MetaDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metaService.crear(usuarioId, req));
    }

    @PutMapping("/{id}")
    public MetaDto.Response actualizar(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                       @PathVariable("id") Long id,
                                       @RequestBody MetaDto.Request req) {
        return metaService.actualizar(usuarioId, id, req);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> eliminar(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                        @PathVariable("id") Long id) {
        metaService.eliminar(usuarioId, id);
        return Map.of("mensaje", "Meta eliminada. Lo ahorrado volvió a tu saldo disponible.");
    }

    /** POST /api/metas/{id}/aportes - Abona dinero del saldo a la meta (RF005.3). */
    @PostMapping("/{id}/aportes")
    public MetaDto.Response aportar(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                    @PathVariable("id") Long id,
                                    @RequestBody MetaDto.AporteRequest req) {
        return metaService.aportar(usuarioId, id, req);
    }

    @GetMapping("/{id}/aportes")
    public List<MetaDto.AporteResponse> aportes(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                                @PathVariable("id") Long id) {
        return metaService.aportes(usuarioId, id);
    }
}
