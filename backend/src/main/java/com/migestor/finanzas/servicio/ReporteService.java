package com.migestor.finanzas.servicio;

import com.migestor.finanzas.dto.ReporteDto;
import com.migestor.finanzas.dto.TransaccionDto;
import com.migestor.finanzas.excepcion.ReglaNegocioException;
import com.migestor.finanzas.modelo.TipoMovimiento;
import com.migestor.finanzas.modelo.Transaccion;
import com.migestor.finanzas.repositorio.MetaAhorroRepository;
import com.migestor.finanzas.repositorio.TransaccionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Panel de inicio y reportes mensual y anual (RF004). */
@Service
public class ReporteService {

    private final TransaccionRepository transaccionRepository;
    private final MetaAhorroRepository metaAhorroRepository;
    private final TransaccionService transaccionService;
    private final PresupuestoService presupuestoService;

    public ReporteService(TransaccionRepository transaccionRepository, MetaAhorroRepository metaAhorroRepository,
                          TransaccionService transaccionService, PresupuestoService presupuestoService) {
        this.transaccionRepository = transaccionRepository;
        this.metaAhorroRepository = metaAhorroRepository;
        this.transaccionService = transaccionService;
        this.presupuestoService = presupuestoService;
    }

    @Transactional(readOnly = true)
    public ReporteDto.Panel panel(Long usuarioId) {
        YearMonth actual = YearMonth.now();
        List<Transaccion> delMes = transaccionRepository.findByUsuarioIdAndFechaBetweenOrderByFechaDescIdDesc(
                usuarioId, actual.atDay(1), actual.atEndOfMonth());
        List<TransaccionDto.Response> ultimos = transaccionRepository.findByUsuarioIdOrderByFechaDescIdDesc(usuarioId)
                .stream().limit(5).map(TransaccionService::aResponse).toList();
        long metasEnProgreso = metaAhorroRepository.findByUsuarioIdOrderByFechaLimiteAsc(usuarioId).stream()
                .filter(m -> "EN_PROGRESO".equals(MetaAhorroService.calcularEstado(
                        m.getMontoAhorrado(), m.getMontoObjetivo(), m.getFechaLimite())))
                .count();
        return new ReporteDto.Panel(transaccionService.resumen(usuarioId),
                sumar(delMes, TipoMovimiento.INGRESO), sumar(delMes, TipoMovimiento.GASTO),
                ultimos, presupuestoService.alertasMesActual(usuarioId), metasEnProgreso);
    }

    @Transactional(readOnly = true)
    public ReporteDto.Mensual mensual(Long usuarioId, Integer anio, Integer mes) {
        Validaciones.periodo(anio, mes);
        YearMonth periodo = YearMonth.of(anio, mes);
        List<Transaccion> movimientos = transaccionRepository.findByUsuarioIdAndFechaBetweenOrderByFechaDescIdDesc(
                usuarioId, periodo.atDay(1), periodo.atEndOfMonth());
        BigDecimal ingresos = sumar(movimientos, TipoMovimiento.INGRESO);
        BigDecimal gastos = sumar(movimientos, TipoMovimiento.GASTO);
        return new ReporteDto.Mensual(anio, mes, ingresos, gastos, ingresos.subtract(gastos),
                porCategoria(movimientos, TipoMovimiento.INGRESO, ingresos),
                porCategoria(movimientos, TipoMovimiento.GASTO, gastos),
                movimientos.stream().map(TransaccionService::aResponse).toList());
    }

    @Transactional(readOnly = true)
    public ReporteDto.Anual anual(Long usuarioId, Integer anio) {
        if (anio == null || anio < 2000 || anio > 2100) {
            throw new ReglaNegocioException("El año no es válido.");
        }
        List<Transaccion> movimientos = transaccionRepository.findByUsuarioIdAndFechaBetweenOrderByFechaDescIdDesc(
                usuarioId, LocalDate.of(anio, 1, 1), LocalDate.of(anio, 12, 31));
        List<ReporteDto.Mes> meses = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            final int numero = mes;
            List<Transaccion> delMes = movimientos.stream().filter(t -> t.getFecha().getMonthValue() == numero).toList();
            BigDecimal ingresos = sumar(delMes, TipoMovimiento.INGRESO);
            BigDecimal gastos = sumar(delMes, TipoMovimiento.GASTO);
            meses.add(new ReporteDto.Mes(mes, ingresos, gastos, ingresos.subtract(gastos)));
        }
        BigDecimal ingresos = sumar(movimientos, TipoMovimiento.INGRESO);
        BigDecimal gastos = sumar(movimientos, TipoMovimiento.GASTO);
        return new ReporteDto.Anual(anio, meses, ingresos, gastos, ingresos.subtract(gastos));
    }

    static BigDecimal sumar(List<Transaccion> movimientos, TipoMovimiento tipo) {
        return movimientos.stream()
                .filter(t -> t.getTipo() == tipo)
                .map(Transaccion::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    static List<ReporteDto.TotalCategoria> porCategoria(List<Transaccion> movimientos, TipoMovimiento tipo,
                                                        BigDecimal total) {
        Map<String, BigDecimal> totales = new LinkedHashMap<>();
        for (Transaccion t : movimientos) {
            if (t.getTipo() == tipo) {
                totales.merge(t.getCategoria().getNombre(), t.getMonto(), BigDecimal::add);
            }
        }
        return totales.entrySet().stream()
                .map(e -> new ReporteDto.TotalCategoria(e.getKey(), e.getValue(), Validaciones.porcentaje(e.getValue(), total)))
                .sorted(Comparator.comparing(ReporteDto.TotalCategoria::total).reversed())
                .toList();
    }
}
