package com.migestor.finanzas.servicio;

import com.migestor.finanzas.dto.AdminDto;
import com.migestor.finanzas.excepcion.RecursoNoEncontradoException;
import com.migestor.finanzas.excepcion.ReglaNegocioException;
import com.migestor.finanzas.modelo.Usuario;
import com.migestor.finanzas.repositorio.MetaAhorroRepository;
import com.migestor.finanzas.repositorio.PresupuestoRepository;
import com.migestor.finanzas.repositorio.TransaccionRepository;
import com.migestor.finanzas.repositorio.UsuarioRepository;
import com.migestor.finanzas.seguridad.SesionService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestión de usuarios y estadísticas del sistema para el administrador (CU09 y CU10). */
@Service
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final TransaccionRepository transaccionRepository;
    private final MetaAhorroRepository metaAhorroRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final SesionService sesionService;

    public AdminService(UsuarioRepository usuarioRepository, TransaccionRepository transaccionRepository,
                        MetaAhorroRepository metaAhorroRepository, PresupuestoRepository presupuestoRepository,
                        SesionService sesionService) {
        this.usuarioRepository = usuarioRepository;
        this.transaccionRepository = transaccionRepository;
        this.metaAhorroRepository = metaAhorroRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.sesionService = sesionService;
    }

    @Transactional(readOnly = true)
    public List<AdminDto.UsuarioAdmin> usuarios() {
        return usuarioRepository.findAllByOrderByIdAsc().stream().map(AdminService::aDto).toList();
    }

    @Transactional
    public AdminDto.UsuarioAdmin cambiarEstado(Long adminId, Long usuarioId, AdminDto.EstadoRequest req) {
        if (req == null || req.activo() == null) {
            throw new ReglaNegocioException("Debe indicar si la cuenta queda activa o inactiva.");
        }
        if (adminId.equals(usuarioId)) {
            throw new ReglaNegocioException("No puedes desactivar tu propia cuenta.");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no existe."));
        usuario.setActivo(req.activo());
        Usuario guardado = usuarioRepository.save(usuario);
        if (!req.activo()) {
            sesionService.cerrarTodas(usuarioId);
        }
        return aDto(guardado);
    }

    @Transactional(readOnly = true)
    public AdminDto.Estadisticas estadisticas() {
        long total = usuarioRepository.count();
        long activos = usuarioRepository.countByActivoTrue();
        return new AdminDto.Estadisticas(total, activos, total - activos, transaccionRepository.count(),
                metaAhorroRepository.count(), presupuestoRepository.count());
    }

    private static AdminDto.UsuarioAdmin aDto(Usuario u) {
        return new AdminDto.UsuarioAdmin(u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.isActivo(),
                u.getFechaRegistro());
    }
}
