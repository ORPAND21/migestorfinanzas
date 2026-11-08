package com.migestor.finanzas.servicio;

import com.migestor.finanzas.dto.PresupuestoDto;
import com.migestor.finanzas.dto.TransaccionDto;
import com.migestor.finanzas.excepcion.NoAutenticadoException;
import com.migestor.finanzas.excepcion.RecursoNoEncontradoException;
import com.migestor.finanzas.excepcion.ReglaNegocioException;
import com.migestor.finanzas.modelo.Categoria;
import com.migestor.finanzas.modelo.Presupuesto;
import com.migestor.finanzas.modelo.TipoMovimiento;
import com.migestor.finanzas.modelo.Usuario;
import com.migestor.finanzas.repositorio.CategoriaRepository;
import com.migestor.finanzas.repositorio.PresupuestoRepository;
import com.migestor.finanzas.repositorio.TransaccionRepository;
import com.migestor.finanzas.repositorio.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Presupuestos mensuales por categoría y alertas de gasto excesivo (RF006). */
@Service
public class PresupuestoService {

    /** A partir de este porcentaje de uso se muestra una advertencia. */
    public static final int UMBRAL_ADVERTENCIA = 80;

    private final PresupuestoRepository presupuestoRepository;
    private final CategoriaRepository categoriaRepository;
    private final TransaccionRepository transaccionRepository;
    private final UsuarioRepository usuarioRepository;

    public PresupuestoService(PresupuestoRepository presupuestoRepository, CategoriaRepository categoriaRepository,
                              TransaccionRepository transaccionRepository, UsuarioRepository usuarioRepository) {
        this.presupuestoRepository = presupuestoRepository;
        this.categoriaRepository = categoriaRepository;
        this.transaccionRepository = transaccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<PresupuestoDto.Response> listar(Long usuarioId, Integer anio, Integer mes) {
        Validaciones.periodo(anio, mes);
        return presupuestoRepository.findByUsuarioIdAndAnioAndMesOrderByIdAsc(usuarioId, anio, mes).stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional
    public PresupuestoDto.Response crear(Long usuarioId, PresupuestoDto.Request req) {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setUsuario(buscarUsuario(usuarioId));
        aplicar(presupuesto, req, usuarioId, null);
        return aResponse(presupuestoRepository.save(presupuesto));
    }

    @Transactional
    public PresupuestoDto.Response actualizar(Long usuarioId, Long id, PresupuestoDto.Request req) {
        Presupuesto presupuesto = buscar(usuarioId, id);
        aplicar(presupuesto, req, usuarioId, id);
        return aResponse(presupuestoRepository.save(presupuesto));
    }

    @Transactional
    public void eliminar(Long usuarioId, Long id) {
        presupuestoRepository.delete(buscar(usuarioId, id));
    }

    /**
     * Revisa el presupuesto de la categoría en el mes de la fecha indicada.
     * Devuelve una alerta si el gasto llegó al 80 % o superó el límite; si no, null.
     */
    @Transactional(readOnly = true)
    public TransaccionDto.Alerta alertaPara(Usuario usuario, Categoria categoria, LocalDate fecha) {
        if (!usuario.isNotificaciones() || categoria.getTipo() != TipoMovimiento.GASTO) {
            return null;
        }
        Optional<Presupuesto> presupuesto = presupuestoRepository.findByUsuarioIdAndCategoriaIdAndAnioAndMes(
                usuario.getId(), categoria.getId(), fecha.getYear(), fecha.getMonthValue());
        return presupuesto.map(p -> alertaDe(aResponse(p))).orElse(null);
    }

    /** Alertas de todos los presupuestos del mes actual (para el panel de inicio). */
    @Transactional(readOnly = true)
    public List<TransaccionDto.Alerta> alertasMesActual(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        List<TransaccionDto.Alerta> alertas = new ArrayList<>();
        if (!usuario.isNotificaciones()) {
            return alertas;
        }
        LocalDate hoy = LocalDate.now();
        for (Presupuesto p : presupuestoRepository.findByUsuarioIdAndAnioAndMesOrderByIdAsc(
                usuarioId, hoy.getYear(), hoy.getMonthValue())) {
            TransaccionDto.Alerta alerta = alertaDe(aResponse(p));
            if (alerta != null) {
                alertas.add(alerta);
            }
        }
        return alertas;
    }

    /** Calcula el estado según el porcentaje usado: NORMAL, ADVERTENCIA (80 % o más) o EXCEDIDO. */
    public static String calcularEstado(BigDecimal gastado, BigDecimal limite) {
        if (gastado.compareTo(limite) > 0) {
            return "EXCEDIDO";
        }
        if (Validaciones.porcentaje(gastado, limite) >= UMBRAL_ADVERTENCIA) {
            return "ADVERTENCIA";
        }
        return "NORMAL";
    }

    private TransaccionDto.Alerta alertaDe(PresupuestoDto.Response p) {
        String periodo = Formato.mes(p.mes()) + " de " + p.anio();
        return switch (p.estado()) {
            case "EXCEDIDO" -> new TransaccionDto.Alerta("EXCEDIDO",
                    "Superaste el presupuesto de " + p.categoriaNombre() + " de " + periodo + ": llevas "
                    + Formato.pesos(p.gastado()) + " de " + Formato.pesos(p.montoLimite()) + ".");
            case "ADVERTENCIA" -> new TransaccionDto.Alerta("ADVERTENCIA",
                    "Ya usaste el " + p.porcentaje() + " % del presupuesto de " + p.categoriaNombre()
                    + " de " + periodo + ". Te quedan " + Formato.pesos(p.disponible()) + ".");
            default -> null;
        };
    }

    private void aplicar(Presupuesto presupuesto, PresupuestoDto.Request req, Long usuarioId, Long idActual) {
        if (req == null) {
            throw new ReglaNegocioException("Debe enviar los datos del presupuesto.");
        }
        Validaciones.periodo(req.anio(), req.mes());
        BigDecimal limite = Validaciones.monto(req.montoLimite(), "monto límite");
        if (req.categoriaId() == null) {
            throw new ReglaNegocioException("Debe seleccionar una categoría.");
        }
        Categoria categoria = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new ReglaNegocioException("La categoría seleccionada no existe."));
        if (categoria.getTipo() != TipoMovimiento.GASTO) {
            throw new ReglaNegocioException("Los presupuestos solo aplican a categorías de gasto.");
        }
        presupuestoRepository.findByUsuarioIdAndCategoriaIdAndAnioAndMes(usuarioId, categoria.getId(), req.anio(), req.mes())
                .filter(existente -> !existente.getId().equals(idActual))
                .ifPresent(existente -> {
                    throw new ReglaNegocioException("Ya existe un presupuesto de " + categoria.getNombre()
                            + " para " + Formato.mes(req.mes()) + " de " + req.anio() + ".");
                });
        presupuesto.setCategoria(categoria);
        presupuesto.setAnio(req.anio());
        presupuesto.setMes(req.mes());
        presupuesto.setMontoLimite(limite);
    }

    private PresupuestoDto.Response aResponse(Presupuesto p) {
        YearMonth periodo = YearMonth.of(p.getAnio(), p.getMes());
        BigDecimal gastado = transaccionRepository.sumarPorCategoriaEnRango(p.getUsuario().getId(),
                p.getCategoria().getId(), TipoMovimiento.GASTO, periodo.atDay(1), periodo.atEndOfMonth());
        BigDecimal limite = p.getMontoLimite();
        return new PresupuestoDto.Response(p.getId(), p.getCategoria().getId(), p.getCategoria().getNombre(),
                p.getAnio(), p.getMes(), limite, gastado, limite.subtract(gastado),
                Validaciones.porcentaje(gastado, limite), calcularEstado(gastado, limite));
    }

    private Presupuesto buscar(Long usuarioId, Long id) {
        return presupuestoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El presupuesto no existe."));
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NoAutenticadoException("La sesión no es válida."));
    }
}
