package com.migestor.finanzas.seguridad;

import com.migestor.finanzas.excepcion.AccesoDenegadoException;
import com.migestor.finanzas.excepcion.NoAutenticadoException;
import com.migestor.finanzas.modelo.Rol;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Protege las rutas /api/**: exige un token valido y, para /api/admin/**, el rol ADMIN.
 * Deja el id del usuario en el atributo "usuarioId" de la peticion para los controladores.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATRIBUTO_USUARIO = "usuarioId";
    public static final String ATRIBUTO_TOKEN = "token";

    private final SesionService sesionService;

    public AuthInterceptor(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        String encabezado = request.getHeader("Authorization");
        String token = (encabezado != null && encabezado.startsWith("Bearer ")) ? encabezado.substring(7) : null;

        SesionService.Sesion sesion = sesionService.validar(token)
                .orElseThrow(() -> new NoAutenticadoException("Debe iniciar sesión para continuar."));

        if (request.getRequestURI().startsWith("/api/admin") && sesion.rol() != Rol.ADMIN) {
            throw new AccesoDenegadoException("Solo el administrador puede acceder a esta opción.");
        }
        request.setAttribute(ATRIBUTO_USUARIO, sesion.usuarioId());
        request.setAttribute(ATRIBUTO_TOKEN, token);
        return true;
    }
}
