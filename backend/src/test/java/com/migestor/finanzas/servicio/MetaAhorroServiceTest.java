package com.migestor.finanzas.servicio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.migestor.finanzas.dto.MetaDto;
import com.migestor.finanzas.dto.TransaccionDto;
import com.migestor.finanzas.excepcion.ReglaNegocioException;
import com.migestor.finanzas.modelo.AporteMeta;
import com.migestor.finanzas.modelo.MetaAhorro;
import com.migestor.finanzas.repositorio.AporteMetaRepository;
import com.migestor.finanzas.repositorio.MetaAhorroRepository;
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

/** Pruebas de los aportes a metas de ahorro (RF005.3). */
@ExtendWith(MockitoExtension.class)
class MetaAhorroServiceTest {

    @Mock private MetaAhorroRepository metaRepository;
    @Mock private AporteMetaRepository aporteRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TransaccionService transaccionService;
    @InjectMocks private MetaAhorroService metaService;

    private MetaAhorro meta;

    @BeforeEach
    void prepararMeta() {
        meta = new MetaAhorro();
        meta.setId(1L);
        meta.setNombre("Fondo de emergencia");
        meta.setMontoObjetivo(new BigDecimal("1000000"));
        meta.setMontoAhorrado(new BigDecimal("200000"));
        meta.setFechaLimite(LocalDate.now().plusMonths(6));
    }

    @Test
    @DisplayName("Un aporte válido suma al monto ahorrado")
    void aporteValido() {
        // Arrange
        when(metaRepository.findByIdAndUsuarioId(1L, 2L)).thenReturn(Optional.of(meta));
        when(transaccionService.resumen(2L)).thenReturn(saldo("500000"));
        when(metaRepository.save(any(MetaAhorro.class))).thenAnswer(inv -> inv.getArgument(0));
        // Act
        MetaDto.Response respuesta = metaService.aportar(2L, 1L, new MetaDto.AporteRequest(new BigDecimal("100000")));
        // Assert
        assertEquals(0, new BigDecimal("300000").compareTo(respuesta.montoAhorrado()));
        assertEquals(30, respuesta.porcentaje());
        assertEquals("EN_PROGRESO", respuesta.estado());
        verify(aporteRepository).save(any(AporteMeta.class));
    }

    @Test
    @DisplayName("No permite aportar más de lo que falta para la meta")
    void aporteSuperaRestante() {
        when(metaRepository.findByIdAndUsuarioId(1L, 2L)).thenReturn(Optional.of(meta));

        assertThrows(ReglaNegocioException.class,
                () -> metaService.aportar(2L, 1L, new MetaDto.AporteRequest(new BigDecimal("900000"))));
        verify(aporteRepository, never()).save(any(AporteMeta.class));
    }

    @Test
    @DisplayName("No permite aportar más del saldo disponible")
    void aporteSinSaldo() {
        when(metaRepository.findByIdAndUsuarioId(1L, 2L)).thenReturn(Optional.of(meta));
        when(transaccionService.resumen(2L)).thenReturn(saldo("50000"));

        assertThrows(ReglaNegocioException.class,
                () -> metaService.aportar(2L, 1L, new MetaDto.AporteRequest(new BigDecimal("100000"))));
        verify(aporteRepository, never()).save(any(AporteMeta.class));
    }

    @Test
    @DisplayName("El estado es COMPLETADA, VENCIDA o EN_PROGRESO según el avance y la fecha")
    void estados() {
        BigDecimal objetivo = new BigDecimal("100");
        assertEquals("COMPLETADA", MetaAhorroService.calcularEstado(objetivo, objetivo, LocalDate.now().minusDays(1)));
        assertEquals("VENCIDA", MetaAhorroService.calcularEstado(BigDecimal.TEN, objetivo, LocalDate.now().minusDays(1)));
        assertEquals("EN_PROGRESO", MetaAhorroService.calcularEstado(BigDecimal.TEN, objetivo, LocalDate.now().plusDays(1)));
    }

    private TransaccionDto.Resumen saldo(String saldo) {
        return new TransaccionDto.Resumen(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal(saldo));
    }
}
