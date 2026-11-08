import axios from 'axios';

const CLAVE_SESION = 'mgf_sesion';

/** Cliente HTTP hacia el backend Spring Boot. */
const cliente = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' }
});

// Envía el token de la sesión en cada petición
cliente.interceptors.request.use((config) => {
  const sesion = leerSesion();
  if (sesion?.token) {
    config.headers.Authorization = `Bearer ${sesion.token}`;
  }
  return config;
});

// Si la sesión venció, vuelve al inicio de sesión
cliente.interceptors.response.use(
  (respuesta) => respuesta,
  (error) => {
    const esLogin = error.config?.url?.includes('/auth/login');
    if (error.response?.status === 401 && !esLogin) {
      borrarSesion();
      window.location.href = '/login?expirada=1';
    }
    return Promise.reject(error);
  }
);

export function leerSesion() {
  try {
    return JSON.parse(localStorage.getItem(CLAVE_SESION));
  } catch {
    return null;
  }
}

export function guardarSesion(sesion) {
  localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
}

export function borrarSesion() {
  localStorage.removeItem(CLAVE_SESION);
}

/** Texto del error para mostrar al usuario. */
export function mensajeError(error) {
  if (error?.response?.data?.mensaje) {
    return error.response.data.mensaje;
  }
  if (!error?.response) {
    return 'No hay conexión con el servidor. Verifica que el backend esté en ejecución.';
  }
  return 'No se pudo completar la operación.';
}

export default cliente;
