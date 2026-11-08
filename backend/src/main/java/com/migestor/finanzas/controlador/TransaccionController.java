package com.migestor.finanzas.controlador;

import com.migestor.finanzas.dto.TransaccionDto;
import com.migestor.finanzas.modelo.TipoMovimiento;
import com.migestor.finanzas.seguridad.AuthInterceptor;
import com.migestor.finanzas.servicio.TransaccionService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    /** GET /api/transacciones?tipo=&categoriaId=&desde=&hasta= (CU06 consultar historial). */
    @GetMapping
    public List<TransaccionDto.Response> listar(
            @RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
            @RequestParam(name = "tipo", required = false) TipoMovimiento tipo,
            @RequestParam(name = "categoriaId", required = false) Long categoriaId,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return transaccionService.listar(usuarioId, tipo, categoriaId, desde, hasta);
    }

    /** GET /api/transacciones/resumen - Totales y saldo disponible. */
    @GetMapping("/resumen")
    public TransaccionDto.Resumen resumen(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId) {
        return transaccionService.resumen(usuarioId);
    }

    /** POST /api/transacciones - Registra un ingreso o gasto (CU03 y CU04). */
    @PostMapping
    public ResponseEntity<TransaccionDto.Guardada> crear(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                                         @RequestBody TransaccionDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transaccionService.crear(usuarioId, req));
    }

    @PutMapping("/{id}")
    public TransaccionDto.Guardada actualizar(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                              @PathVariable("id") Long id,
                                              @RequestBody TransaccionDto.Request req) {
        return transaccionService.actualizar(usuarioId, id, req);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> eliminar(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                        @PathVariable("id") Long id) {
        transaccionService.eliminar(usuarioId, id);
        return Map.of("mensaje", "Movimiento eliminado correctamente.");
    }
}
