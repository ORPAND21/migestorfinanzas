package com.migestor.finanzas.repositorio;

import com.migestor.finanzas.modelo.TipoMovimiento;
import com.migestor.finanzas.modelo.Transaccion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    /** Busca una transaccion solo si pertenece al usuario (los usuarios no ven datos ajenos). */
    Optional<Transaccion> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Transaccion> findByUsuarioIdOrderByFechaDescIdDesc(Long usuarioId);

    List<Transaccion> findByUsuarioIdAndFechaBetweenOrderByFechaDescIdDesc(Long usuarioId, LocalDate desde, LocalDate hasta);

    boolean existsByCategoriaId(Long categoriaId);

    @Query("select coalesce(sum(t.monto), 0) from Transaccion t "
         + "where t.usuario.id = :usuarioId and t.tipo = :tipo")
    BigDecimal sumarPorTipo(@Param("usuarioId") Long usuarioId, @Param("tipo") TipoMovimiento tipo);

    @Query("select coalesce(sum(t.monto), 0) from Transaccion t "
         + "where t.usuario.id = :usuarioId and t.categoria.id = :categoriaId and t.tipo = :tipo "
         + "and t.fecha between :desde and :hasta")
    BigDecimal sumarPorCategoriaEnRango(@Param("usuarioId") Long usuarioId,
                                        @Param("categoriaId") Long categoriaId,
                                        @Param("tipo") TipoMovimiento tipo,
                                        @Param("desde") LocalDate desde,
                                        @Param("hasta") LocalDate hasta);
}
