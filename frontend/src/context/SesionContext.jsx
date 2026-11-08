import { createContext, useContext, useState } from 'react';
import { authService } from '../services/authService';
import { borrarSesion, guardarSesion, leerSesion } from '../api/cliente';

const SesionContext = createContext(null);

/** Guarda el usuario que inició sesión y su token. */
export function SesionProvider({ children }) {
  const [sesion, setSesion] = useState(() => leerSesion());

  const iniciarSesion = async (email, password) => {
    const datos = await authService.login({ email, password });
    guardarSesion(datos);
    setSesion(datos);
    return datos.usuario;
  };

  const cerrarSesion = async () => {
    try {
      await authService.logout();
    } catch {
      // Si el backend no responde, igual se borra la sesión local
    }
    borrarSesion();
    setSesion(null);
  };

  const actualizarUsuario = (usuario) => {
    const nueva = { ...sesion, usuario };
    guardarSesion(nueva);
    setSesion(nueva);
  };

  return (
    <SesionContext.Provider value={{ usuario: sesion?.usuario, iniciarSesion, cerrarSesion, actualizarUsuario }}>
      {children}
    </SesionContext.Provider>
  );
}

export const useSesion = () => useContext(SesionContext);
