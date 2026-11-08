package com.migestor.finanzas.config;

import com.migestor.finanzas.modelo.Categoria;
import com.migestor.finanzas.modelo.Rol;
import com.migestor.finanzas.modelo.TipoMovimiento;
import com.migestor.finanzas.modelo.Usuario;
import com.migestor.finanzas.repositorio.CategoriaRepository;
import com.migestor.finanzas.repositorio.UsuarioRepository;
import com.migestor.finanzas.seguridad.HashService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Al arrancar, crea las categorías predefinidas y el usuario administrador si todavía no existen.
 * Así la aplicación funciona aunque no se haya ejecutado el script SQL.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);
    public static final String ADMIN_EMAIL = "admin@migestor.com";
    public static final String ADMIN_PASSWORD = "Admin123*";

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HashService hashService;

    public DatosIniciales(CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository,
                          HashService hashService) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.hashService = hashService;
    }

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() == 0) {
            List<String> ingresos = List.of("Salario", "Bono", "Regalo", "Ventas", "Otros ingresos");
            List<String> gastos = List.of("Alimentación", "Transporte", "Vivienda", "Servicios públicos",
                    "Salud", "Educación", "Ocio", "Otros gastos");
            ingresos.forEach(n -> categoriaRepository.save(new Categoria(n, TipoMovimiento.INGRESO)));
            gastos.forEach(n -> categoriaRepository.save(new Categoria(n, TipoMovimiento.GASTO)));
            log.info("Categorías predefinidas creadas.");
        }
        if (!usuarioRepository.existsByEmailIgnoreCase(ADMIN_EMAIL)) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setEmail(ADMIN_EMAIL);
            admin.setPasswordHash(hashService.cifrar(ADMIN_PASSWORD));
            admin.setRol(Rol.ADMIN);
            usuarioRepository.save(admin);
            log.info("Usuario administrador creado: {}", ADMIN_EMAIL);
        }
    }
}
