package com.migestor.finanzas.controlador;

import com.migestor.finanzas.dto.AdminDto;
import com.migestor.finanzas.seguridad.AuthInterceptor;
import com.migestor.finanzas.servicio.AdminService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/** Rutas exclusivas del rol ADMIN (el AuthInterceptor bloquea a los demás roles). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/usuarios")
    public List<AdminDto.UsuarioAdmin> usuarios() {
        return adminService.usuarios();
    }

    @PutMapping("/usuarios/{id}/estado")
    public AdminDto.UsuarioAdmin cambiarEstado(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long adminId,
                                               @PathVariable("id") Long id,
                                               @RequestBody AdminDto.EstadoRequest req) {
        return adminService.cambiarEstado(adminId, id, req);
    }

    @GetMapping("/estadisticas")
    public AdminDto.Estadisticas estadisticas() {
        return adminService.estadisticas();
    }
}
