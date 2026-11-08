package com.migestor.finanzas.servicio;

import com.migestor.finanzas.dto.AuthDto;
import com.migestor.finanzas.excepcion.AccesoDenegadoException;
import com.migestor.finanzas.excepcion.NoAutenticadoException;
import com.migestor.finanzas.excepcion.ReglaNegocioException;
import com.migestor.finanzas.modelo.Rol;
import com.migestor.finanzas.modelo.Usuario;
import com.migestor.finanzas.repositorio.UsuarioRepository;
import com.migestor.finanzas.seguridad.HashService;
import com.migestor.finanzas.seguridad.SesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro de usuarios, inicio y cierre de sesión (RF001). */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final HashService hashService;
    private final SesionService sesionService;

    public AuthService(UsuarioRepository usuarioRepository, HashService hashService, SesionService sesionService) {
        this.usuarioRepository = usuarioRepository;
        this.hashService = hashService;
        this.sesionService = sesionService;
    }

    @Transactional
    public AuthDto.UsuarioSesion registrar(AuthDto.RegistroRequest req) {
        if (req == null) {
            throw new ReglaNegocioException("Debe enviar los datos de registro.");
        }
        String nombre = Validaciones.texto(req.nombre(), "nombre", 80);
        String email = Validaciones.email(req.email());
        Validaciones.password(req.password());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ReglaNegocioException("Ya existe una cuenta registrada con ese correo.");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPasswordHash(hashService.cifrar(req.password()));
        usuario.setRol(Rol.USUARIO);
        return aSesion(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public AuthDto.LoginResponse iniciarSesion(AuthDto.LoginRequest req) {
        if (req == null || req.email() == null || req.email().isBlank()
                || req.password() == null || req.password().isBlank()) {
            throw new ReglaNegocioException("Debe ingresar el correo y la contraseña.");
        }
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(req.email().trim())
                .orElseThrow(() -> new NoAutenticadoException("Correo o contraseña incorrectos."));
        if (!hashService.verificar(req.password(), usuario.getPasswordHash())) {
            throw new NoAutenticadoException("Correo o contraseña incorrectos.");
        }
        if (!usuario.isActivo()) {
            throw new AccesoDenegadoException("Tu cuenta está desactivada. Comunícate con el administrador.");
        }
        String token = sesionService.crear(usuario.getId(), usuario.getRol());
        return new AuthDto.LoginResponse(token, aSesion(usuario));
    }

    public void cerrarSesion(String token) {
        sesionService.cerrar(token);
    }

    public static AuthDto.UsuarioSesion aSesion(Usuario u) {
        return new AuthDto.UsuarioSesion(u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.isNotificaciones());
    }
}
