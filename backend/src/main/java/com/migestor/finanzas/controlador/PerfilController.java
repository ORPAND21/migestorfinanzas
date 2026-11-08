package com.migestor.finanzas.controlador;

import com.migestor.finanzas.dto.AuthDto;
import com.migestor.finanzas.seguridad.AuthInterceptor;
import com.migestor.finanzas.servicio.PerfilService;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public AuthDto.UsuarioSesion obtener(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId) {
        return perfilService.obtener(usuarioId);
    }

    @PutMapping
    public AuthDto.UsuarioSesion actualizar(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                            @RequestBody AuthDto.PerfilRequest req) {
        return perfilService.actualizar(usuarioId, req);
    }

    @PutMapping("/password")
    public Map<String, String> cambiarPassword(@RequestAttribute(AuthInterceptor.ATRIBUTO_USUARIO) Long usuarioId,
                                               @RequestBody AuthDto.CambioPasswordRequest req) {
        perfilService.cambiarPassword(usuarioId, req);
        return Map.of("mensaje", "Contraseña actualizada correctamente.");
    }
}
