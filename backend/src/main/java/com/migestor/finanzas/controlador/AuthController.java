package com.migestor.finanzas.controlador;

import com.migestor.finanzas.dto.AuthDto;
import com.migestor.finanzas.seguridad.AuthInterceptor;
import com.migestor.finanzas.servicio.AuthService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/registro - Crea una cuenta nueva (CU01). */
    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody AuthDto.RegistroRequest req) {
        AuthDto.UsuarioSesion usuario = authService.registrar(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Cuenta creada correctamente. Ya puedes iniciar sesión.", "usuario", usuario));
    }

    /** POST /api/auth/login - Inicia sesión y devuelve el token (CU02). */
    @PostMapping("/login")
    public AuthDto.LoginResponse login(@RequestBody AuthDto.LoginRequest req) {
        return authService.iniciarSesion(req);
    }

    /** POST /api/auth/logout - Cierra la sesión actual. */
    @PostMapping("/logout")
    public Map<String, String> logout(@RequestAttribute(AuthInterceptor.ATRIBUTO_TOKEN) String token) {
        authService.cerrarSesion(token);
        return Map.of("mensaje", "Sesión cerrada.");
    }
}
