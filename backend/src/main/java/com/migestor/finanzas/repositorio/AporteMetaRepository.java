package com.migestor.finanzas.repositorio;

import com.migestor.finanzas.modelo.AporteMeta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AporteMetaRepository extends JpaRepository<AporteMeta, Long> {

    List<AporteMeta> findByMetaIdOrderByFechaDescIdDesc(Long metaId);

    void deleteByMetaId(Long metaId);
}
