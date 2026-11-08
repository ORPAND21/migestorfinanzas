package com.migestor.finanzas.repositorio;

import com.migestor.finanzas.modelo.MetaAhorro;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MetaAhorroRepository extends JpaRepository<MetaAhorro, Long> {

    Optional<MetaAhorro> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<MetaAhorro> findByUsuarioIdOrderByFechaLimiteAsc(Long usuarioId);

    @Query("select coalesce(sum(m.montoAhorrado), 0) from MetaAhorro m where m.usuario.id = :usuarioId")
    BigDecimal sumarAhorrado(@Param("usuarioId") Long usuarioId);
}
