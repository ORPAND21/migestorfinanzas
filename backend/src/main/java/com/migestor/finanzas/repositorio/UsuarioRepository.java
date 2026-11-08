package com.migestor.finanzas.repositorio;

import com.migestor.finanzas.modelo.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByActivoTrue();

    List<Usuario> findAllByOrderByIdAsc();
}
