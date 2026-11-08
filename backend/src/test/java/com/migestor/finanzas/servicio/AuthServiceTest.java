package com.migestor.finanzas.servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.migestor.finanzas.dto.AuthDto;
import com.migestor.finanzas.excepcion.AccesoDenegadoException;
import com.migestor.finanzas.excepcion.NoAutenticadoException;
import com.migestor.finanzas.excepcion.ReglaNegocioException;
import com.migestor.finanzas.modelo.Rol;
import com.migestor.finanzas.modelo.Usuario;
import com.migestor.finanzas.repositorio.UsuarioRepository;
import com.migestor.finanzas.seguridad.HashService;
import com.migestor.finanzas.seguridad.SesionService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas del registro e inicio de sesión (RF001). */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private SesionService sesionService;
    private final HashService hashService = new HashService();
    private AuthService authService;

    @BeforeEach
    void crearServicio() {
        authService = new AuthService(usuarioRepository, hashService, sesionService);
    }

    @Test
    @DisplayName("No permite registrar un correo que ya existe")
    void correoDuplicado() {
        when(usuarioRepository.existsByEmailIgnoreCase("laura@demo.com")).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () -> authService.registrar(
                new AuthDto.RegistroRequest("Laura", "laura@demo.com", "Clave123*")));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Inicia sesión con credenciales correctas y entrega un token")
    void loginCorrecto() {
        when(usuarioRepository.findByEmailIgnoreCase("laura@demo.com")).thenReturn(Optional.of(usuario(true)));
        when(sesionService.crear(2L, Rol.USUARIO)).thenReturn("token-prueba");

        AuthDto.LoginResponse respuesta = authService.iniciarSesion(new AuthDto.LoginRequest("laura@demo.com", "Clave123*"));

        assertEquals("token-prueba", respuesta.token());
        assertEquals("Laura", respuesta.usuario().nombre());
    }

    @Test
    @DisplayName("Rechaza una contraseña incorrecta")
    void passwordIncorrecta() {
        when(usuarioRepository.findByEmailIgnoreCase("laura@demo.com")).thenReturn(Optional.of(usuario(true)));

        assertThrows(NoAutenticadoException.class,
                () -> authService.iniciarSesion(new AuthDto.LoginRequest("laura@demo.com", "Mala123*")));
    }

    @Test
    @DisplayName("Una cuenta desactivada no puede iniciar sesión")
    void cuentaInactiva() {
        when(usuarioRepository.findByEmailIgnoreCase("laura@demo.com")).thenReturn(Optional.of(usuario(false)));

        assertThrows(AccesoDenegadoException.class,
                () -> authService.iniciarSesion(new AuthDto.LoginRequest("laura@demo.com", "Clave123*")));
        verify(sesionService, never()).crear(any(), any());
    }

    private Usuario usuario(boolean activo) {
        Usuario u = new Usuario();
        u.setId(2L);
        u.setNombre("Laura");
        u.setEmail("laura@demo.com");
        u.setRol(Rol.USUARIO);
        u.setActivo(activo);
        u.setPasswordHash(hashService.cifrar("Clave123*"));
        return u;
    }
}
