package com.migestor.finanzas.servicio;

import com.migestor.finanzas.dto.TransaccionDto;
import com.migestor.finanzas.excepcion.NoAutenticadoException;
import com.migestor.finanzas.excepcion.RecursoNoEncontradoException;
import com.migestor.finanzas.excepcion.ReglaNegocioException;
import com.migestor.finanzas.modelo.Categoria;
import com.migestor.finanzas.modelo.TipoMovimiento;
import com.migestor.finanzas.modelo.Transaccion;
import com.migestor.finanzas.modelo.Usuario;
import com.migestor.finanzas.repositorio.CategoriaRepository;
import com.migestor.finanzas.repositorio.MetaAhorroRepository;
import com.migestor.finanzas.repositorio.TransaccionRepository;
import com.migestor.finanzas.repositorio.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registro, consulta, edición y eliminación de ingresos y gastos (RF002 y RF003). */
@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MetaAhorroRepository metaAhorroRepository;
    private final PresupuestoService presupuestoService;

    public TransaccionService(TransaccionRepository transaccionRepository, CategoriaRepository categoriaRepository,
                              UsuarioRepository usuarioRepository, MetaAhorroRepository metaAhorroRepository,
                              PresupuestoService presupuestoService) {
        this.transaccionRepository = transaccionRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.metaAhorroRepository = metaAhorroRepository;
        this.presupuestoService = presupuestoService;
    }

    @Transactional(readOnly = true)
    public List<TransaccionDto.Response> listar(Long usuarioId, TipoMovimiento tipo, Long categoriaId,
                                                LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        return transaccionRepository.findByUsuarioIdOrderByFechaDescIdDesc(usuarioId).stream()
                .filter(t -> tipo == null || t.getTipo() == tipo)
                .filter(t -> categoriaId == null || t.getCategoria().getId().equals(categoriaId))
                .filter(t -> desde == null || !t.getFecha().isBefore(desde))
                .filter(t -> hasta == null || !t.getFecha().isAfter(hasta))
                .map(TransaccionService::aResponse)
                .toList();
    }

    @Transactional
    public TransaccionDto.Guardada crear(Long usuarioId, TransaccionDto.Request req) {
        Usuario usuario = buscarUsuario(usuarioId);
        Transaccion transaccion = new Transaccion();
        transaccion.setUsuario(usuario);
        aplicar(transaccion, req);
        Transaccion guardada = transaccionRepository.save(transaccion);
        return new TransaccionDto.Guardada(aResponse(guardada),
                presupuestoService.alertaPara(usuario, guardada.getCategoria(), guardada.getFecha()));
    }

    @Transactional
    public TransaccionDto.Guardada actualizar(Long usuarioId, Long id, TransaccionDto.Request req) {
        Transaccion transaccion = buscar(usuarioId, id);
        aplicar(transaccion, req);
        Transaccion guardada = transaccionRepository.save(transaccion);
        return new TransaccionDto.Guardada(aResponse(guardada),
                presupuestoService.alertaPara(guardada.getUsuario(), guardada.getCategoria(), guardada.getFecha()));
    }

    @Transactional
    public void eliminar(Long usuarioId, Long id) {
        transaccionRepository.delete(buscar(usuarioId, id));
    }

    /** Saldo disponible = ingresos - gastos - dinero apartado en metas de ahorro. */
    @Transactional(readOnly = true)
    public TransaccionDto.Resumen resumen(Long usuarioId) {
        BigDecimal ingresos = transaccionRepository.sumarPorTipo(usuarioId, TipoMovimiento.INGRESO);
        BigDecimal gastos = transaccionRepository.sumarPorTipo(usuarioId, TipoMovimiento.GASTO);
        BigDecimal ahorrado = metaAhorroRepository.sumarAhorrado(usuarioId);
        return new TransaccionDto.Resumen(ingresos, gastos, ahorrado, ingresos.subtract(gastos).subtract(ahorrado));
    }

    public static TransaccionDto.Response aResponse(Transaccion t) {
        return new TransaccionDto.Response(t.getId(), t.getTipo(), t.getCategoria().getId(),
                t.getCategoria().getNombre(), t.getMonto(), t.getFecha(), t.getDescripcion());
    }

    private void aplicar(Transaccion transaccion, TransaccionDto.Request req) {
        if (req == null) {
            throw new ReglaNegocioException("Debe enviar los datos del movimiento.");
        }
        if (req.tipo() == null) {
            throw new ReglaNegocioException("Debe indicar si el movimiento es ingreso o gasto.");
        }
        if (req.categoriaId() == null) {
            throw new ReglaNegocioException("Debe seleccionar una categoría.");
        }
        BigDecimal monto = Validaciones.monto(req.monto(), "monto");
        LocalDate fecha = Validaciones.fechaNoFutura(req.fecha(), "fecha");
        String descripcion = Validaciones.textoOpcional(req.descripcion(), "descripción", 150);
        Categoria categoria = categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new ReglaNegocioException("La categoría seleccionada no existe."));
        if (categoria.getTipo() != req.tipo()) {
            throw new ReglaNegocioException("La categoría " + categoria.getNombre()
                    + " no corresponde al tipo de movimiento seleccionado.");
        }
        transaccion.setTipo(req.tipo());
        transaccion.setCategoria(categoria);
        transaccion.setMonto(monto);
        transaccion.setFecha(fecha);
        transaccion.setDescripcion(descripcion);
    }

    private Transaccion buscar(Long usuarioId, Long id) {
        return transaccionRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El movimiento no existe."));
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NoAutenticadoException("La sesión no es válida."));
    }
}
