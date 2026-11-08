package com.migestor.finanzas.servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.migestor.finanzas.dto.TransaccionDto;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Pruebas del registro de movimientos y del cálculo del saldo (RF002 y RF003). */
@ExtendWith(MockitoExtension.class)
class TransaccionServiceTest {

    @Mock private TransaccionRepository transaccionRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private MetaAhorroRepository metaAhorroRepository;
    @Mock private PresupuestoService presupuestoService;
    @InjectMocks private TransaccionService transaccionService;

    private Usuario usuario;

    @BeforeEach
    void prepararUsuario() {
        usuario = new Usuario();
        usuario.setId(2L);
        usuario.setNombre("Laura");
    }

    @Test
    @DisplayName("Registra un gasto válido y devuelve la alerta del presupuesto")
    void registrarGasto() {
        Categoria alimentacion = categoria(6L, "Alimentación", TipoMovimiento.GASTO);
        TransaccionDto.Alerta alerta = new TransaccionDto.Alerta("ADVERTENCIA", "Ya usaste el 85 %");
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(categoriaRepository.findById(6L)).thenReturn(Optional.of(alimentacion));
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(inv -> {
            Transaccion t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });
        when(presupuestoService.alertaPara(usuario, alimentacion, LocalDate.now())).thenReturn(alerta);

        TransaccionDto.Guardada guardada = transaccionService.crear(2L, new TransaccionDto.Request(
                TipoMovimiento.GASTO, 6L, new BigDecimal("45000"), LocalDate.now(), "Mercado"));

        assertEquals(10L, guardada.transaccion().id().longValue());
        assertEquals("Alimentación", guardada.transaccion().categoriaNombre());
        assertEquals("ADVERTENCIA", guardada.alerta().nivel());
    }

    @Test
    @DisplayName("Rechaza un gasto clasificado con una categoría de ingreso")
    void categoriaNoCoincide() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria(1L, "Salario", TipoMovimiento.INGRESO)));

        assertThrows(ReglaNegocioException.class, () -> transaccionService.crear(2L, new TransaccionDto.Request(
                TipoMovimiento.GASTO, 1L, new BigDecimal("45000"), LocalDate.now(), null)));
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    @DisplayName("Saldo disponible = ingresos - gastos - ahorrado en metas")
    void calcularSaldo() {
        when(transaccionRepository.sumarPorTipo(2L, TipoMovimiento.INGRESO)).thenReturn(new BigDecimal("1000000"));
        when(transaccionRepository.sumarPorTipo(2L, TipoMovimiento.GASTO)).thenReturn(new BigDecimal("300000"));
        when(metaAhorroRepository.sumarAhorrado(2L)).thenReturn(new BigDecimal("200000"));

        TransaccionDto.Resumen resumen = transaccionService.resumen(2L);

        assertEquals(0, new BigDecimal("500000").compareTo(resumen.saldoDisponible()));
    }

    private Categoria categoria(Long id, String nombre, TipoMovimiento tipo) {
        Categoria c = new Categoria(nombre, tipo);
        c.setId(id);
        return c;
    }
}
