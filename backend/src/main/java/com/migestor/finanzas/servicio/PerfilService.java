package com.migestor.finanzas.servicio;

import com.migestor.finanzas.dto.AuthDto;
import com.migestor.finanzas.excepcion.NoAutenticadoException;
import com.migestor.finanzas.excepcion.ReglaNegocioException;
import com.migestor.finanzas.modelo.Usuario;
import com.migestor.finanzas.repositorio.UsuarioRepository;
import com.migestor.finanzas.seguridad.HashService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta y edición del perfil del usuario (CU08) y configuración de notificaciones (RF006.3.1). */
@Service
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final HashService hashService;

    public PerfilService(UsuarioRepository usuarioRepository, HashService hashService) {
        this.usuarioRepository = usuarioRepository;
        this.hashService = hashService;
    }

    @Transactional(readOnly = true)
    public AuthDto.UsuarioSesion obtener(Long usuarioId) {
        return AuthService.aSesion(buscar(usuarioId));
    }

    @Transactional
    public AuthDto.UsuarioSesion actualizar(Long usuarioId, AuthDto.PerfilRequest req) {
        if (req == null) {
            throw new ReglaNegocioException("Debe enviar los datos del perfil.");
        }
        Usuario usuario = buscar(usuarioId);
        usuario.setNombre(Validaciones.texto(req.nombre(), "nombre", 80));
        if (req.notificaciones() != null) {
            usuario.setNotificaciones(req.notificaciones());
        }
        return AuthService.aSesion(usuarioRepository.save(usuario));
    }

    @Transactional
    public void cambiarPassword(Long usuarioId, AuthDto.CambioPasswordRequest req) {
        if (req == null || req.actual() == null || req.actual().isBlank()) {
            throw new ReglaNegocioException("Debe ingresar la contraseña actual.");
        }
        Usuario usuario = buscar(usuarioId);
        if (!hashService.verificar(req.actual(), usuario.getPasswordHash())) {
            throw new ReglaNegocioException("La contraseña actual no es correcta.");
        }
        Validaciones.password(req.nueva());
        if (req.nueva().equals(req.actual())) {
            throw new ReglaNegocioException("La nueva contraseña debe ser diferente a la actual.");
        }
        usuario.setPasswordHash(hashService.cifrar(req.nueva()));
        usuarioRepository.save(usuario);
    }

    private Usuario buscar(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NoAutenticadoException("La sesión no es válida."));
    }
}
