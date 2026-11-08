package com.migestor.finanzas.seguridad;

import com.migestor.finanzas.modelo.Rol;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Administra las sesiones activas. Al iniciar sesion se entrega un token aleatorio
 * que el frontend envia en el encabezado Authorization: Bearer {token}.
 */
@Component
public class SesionService {

    /** Datos de una sesion activa. */
    public record Sesion(Long usuarioId, Rol rol, LocalDateTime expira) { }

    private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final long horas;

    public SesionService(@Value("${app.sesion.horas:8}") long horas) {
        this.horas = horas;
    }

    public String crear(Long usuarioId, Rol rol) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sesiones.put(token, new Sesion(usuarioId, rol, LocalDateTime.now().plusHours(horas)));
        return token;
    }

    public Optional<Sesion> validar(String token) {
        if (token == null) {
            return Optional.empty();
        }
        Sesion sesion = sesiones.get(token);
        if (sesion == null) {
            return Optional.empty();
        }
        if (sesion.expira().isBefore(LocalDateTime.now())) {
            sesiones.remove(token);
            return Optional.empty();
        }
        return Optional.of(sesion);
    }

    public void cerrar(String token) {
        if (token != null) {
            sesiones.remove(token);
        }
    }

    /** Cierra todas las sesiones de un usuario (por ejemplo, al ser desactivado). */
    public void cerrarTodas(Long usuarioId) {
        sesiones.values().removeIf(s -> s.usuarioId().equals(usuarioId));
    }
}
