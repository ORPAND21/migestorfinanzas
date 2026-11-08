package com.migestor.finanzas.servicio;

import com.migestor.finanzas.dto.MetaDto;
import com.migestor.finanzas.excepcion.NoAutenticadoException;
import com.migestor.finanzas.excepcion.RecursoNoEncontradoException;
import com.migestor.finanzas.excepcion.ReglaNegocioException;
import com.migestor.finanzas.modelo.AporteMeta;
import com.migestor.finanzas.modelo.MetaAhorro;
import com.migestor.finanzas.repositorio.AporteMetaRepository;
import com.migestor.finanzas.repositorio.MetaAhorroRepository;
import com.migestor.finanzas.repositorio.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Metas de ahorro: creación, seguimiento del progreso y aportes (RF005). */
@Service
public class MetaAhorroService {

    private final MetaAhorroRepository metaRepository;
    private final AporteMetaRepository aporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransaccionService transaccionService;

    public MetaAhorroService(MetaAhorroRepository metaRepository, AporteMetaRepository aporteRepository,
                             UsuarioRepository usuarioRepository, TransaccionService transaccionService) {
        this.metaRepository = metaRepository;
        this.aporteRepository = aporteRepository;
        this.usuarioRepository = usuarioRepository;
        this.transaccionService = transaccionService;
    }

    @Transactional(readOnly = true)
    public List<MetaDto.Response> listar(Long usuarioId) {
        return metaRepository.findByUsuarioIdOrderByFechaLimiteAsc(usuarioId).stream()
                .map(MetaAhorroService::aResponse)
                .toList();
    }

    @Transactional
    public MetaDto.Response crear(Long usuarioId, MetaDto.Request req) {
        validarRequest(req);
        MetaAhorro meta = new MetaAhorro();
        meta.setUsuario(usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NoAutenticadoException("La sesión no es válida.")));
        meta.setNombre(Validaciones.texto(req.nombre(), "nombre de la meta", 80));
        meta.setMontoObjetivo(Validaciones.monto(req.montoObjetivo(), "monto objetivo"));
        meta.setFechaLimite(Validaciones.fechaFutura(req.fechaLimite(), "fecha límite"));
        return aResponse(metaRepository.save(meta));
    }

    @Transactional
    public MetaDto.Response actualizar(Long usuarioId, Long id, MetaDto.Request req) {
        validarRequest(req);
        MetaAhorro meta = buscar(usuarioId, id);
        BigDecimal objetivo = Validaciones.monto(req.montoObjetivo(), "monto objetivo");
        if (objetivo.compareTo(meta.getMontoAhorrado()) < 0) {
            throw new ReglaNegocioException("El monto objetivo no puede ser menor a lo ya ahorrado ("
                    + Formato.pesos(meta.getMontoAhorrado()) + ").");
        }
        meta.setNombre(Validaciones.texto(req.nombre(), "nombre de la meta", 80));
        meta.setMontoObjetivo(objetivo);
        meta.setFechaLimite(Validaciones.fechaFutura(req.fechaLimite(), "fecha límite"));
        return aResponse(metaRepository.save(meta));
    }

    /** Al eliminar la meta, lo ahorrado vuelve al saldo disponible. */
    @Transactional
    public void eliminar(Long usuarioId, Long id) {
        MetaAhorro meta = buscar(usuarioId, id);
        aporteRepository.deleteByMetaId(meta.getId());
        metaRepository.delete(meta);
    }

    @Transactional
    public MetaDto.Response aportar(Long usuarioId, Long id, MetaDto.AporteRequest req) {
        MetaAhorro meta = buscar(usuarioId, id);
        BigDecimal monto = Validaciones.monto(req == null ? null : req.monto(), "aporte");
        BigDecimal restante = meta.getMontoObjetivo().subtract(meta.getMontoAhorrado());
        if (restante.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("La meta ya fue completada.");
        }
        if (monto.compareTo(restante) > 0) {
            throw new ReglaNegocioException("El aporte supera lo que falta para completar la meta ("
                    + Formato.pesos(restante) + ").");
        }
        BigDecimal saldo = transaccionService.resumen(usuarioId).saldoDisponible();
        if (monto.compareTo(saldo) > 0) {
            throw new ReglaNegocioException("No tienes saldo disponible suficiente. Saldo actual: "
                    + Formato.pesos(saldo.max(BigDecimal.ZERO)) + ".");
        }
        AporteMeta aporte = new AporteMeta();
        aporte.setMeta(meta);
        aporte.setMonto(monto);
        aporteRepository.save(aporte);
        meta.setMontoAhorrado(meta.getMontoAhorrado().add(monto));
        return aResponse(metaRepository.save(meta));
    }

    @Transactional(readOnly = true)
    public List<MetaDto.AporteResponse> aportes(Long usuarioId, Long id) {
        MetaAhorro meta = buscar(usuarioId, id);
        return aporteRepository.findByMetaIdOrderByFechaDescIdDesc(meta.getId()).stream()
                .map(a -> new MetaDto.AporteResponse(a.getId(), a.getMonto(), a.getFecha()))
                .toList();
    }

    /** COMPLETADA si se alcanzó el objetivo, VENCIDA si pasó la fecha límite, si no EN_PROGRESO. */
    public static String calcularEstado(BigDecimal ahorrado, BigDecimal objetivo, LocalDate fechaLimite) {
        if (ahorrado.compareTo(objetivo) >= 0) {
            return "COMPLETADA";
        }
        if (fechaLimite.isBefore(LocalDate.now())) {
            return "VENCIDA";
        }
        return "EN_PROGRESO";
    }

    public static MetaDto.Response aResponse(MetaAhorro m) {
        BigDecimal restante = m.getMontoObjetivo().subtract(m.getMontoAhorrado()).max(BigDecimal.ZERO);
        return new MetaDto.Response(m.getId(), m.getNombre(), m.getMontoObjetivo(), m.getMontoAhorrado(), restante,
                Math.min(100, Validaciones.porcentaje(m.getMontoAhorrado(), m.getMontoObjetivo())),
                m.getFechaLimite(), m.getFechaCreacion(),
                calcularEstado(m.getMontoAhorrado(), m.getMontoObjetivo(), m.getFechaLimite()));
    }

    private void validarRequest(MetaDto.Request req) {
        if (req == null) {
            throw new ReglaNegocioException("Debe enviar los datos de la meta.");
        }
    }

    private MetaAhorro buscar(Long usuarioId, Long id) {
        return metaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La meta de ahorro no existe."));
    }
}
