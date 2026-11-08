import { Navigate } from 'react-router-dom';
import { useSesion } from '../context/SesionContext';

/** Solo deja pasar a usuarios con sesión y, si se indica, con el rol requerido. */
export default function RutaProtegida({ rol, children }) {
  const { usuario } = useSesion();
  if (!usuario) {
    return <Navigate to="/login" replace />;
  }
  if (rol && usuario.rol !== rol) {
    return <Navigate to={usuario.rol === 'ADMIN' ? '/admin' : '/inicio'} replace />;
  }
  return children;
}
