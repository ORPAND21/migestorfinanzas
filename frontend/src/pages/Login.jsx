import { useState } from 'react';
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { Button } from 'primereact/button';
import { Message } from 'primereact/message';
import { useSesion } from '../context/SesionContext';
import { mensajeError } from '../api/cliente';
import PanelMarca from './PanelMarca';

/** CU02 Iniciar sesión */
export default function Login() {
  const { usuario, iniciarSesion } = useSesion();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(false);

  if (usuario) {
    return <Navigate to={usuario.rol === 'ADMIN' ? '/admin' : '/inicio'} replace />;
  }

  const enviar = async (e) => {
    e.preventDefault();
    if (!email.trim() || !password) {
      setError('Ingresa tu correo y tu contraseña.');
      return;
    }
    setCargando(true);
    setError('');
    try {
      const u = await iniciarSesion(email.trim(), password);
      navigate(u.rol === 'ADMIN' ? '/admin' : '/inicio');
    } catch (err) {
      setError(mensajeError(err));
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="acceso">
      <PanelMarca />
      <form className="acceso-formulario" onSubmit={enviar} noValidate>
        <h1>Inicia sesión</h1>
        <p className="acceso-ayuda">Entra con el correo con el que te registraste.</p>
        {params.get('expirada') && !error && (
          <Message severity="info" text="Tu sesión terminó. Vuelve a iniciar sesión." />
        )}
        {error && <Message severity="error" text={error} />}
        <label htmlFor="email">Correo electrónico</label>
        <InputText id="email" type="email" value={email} autoComplete="email"
                   onChange={(e) => setEmail(e.target.value)} placeholder="nombre@correo.com" />
        <label htmlFor="password">Contraseña</label>
        <Password inputId="password" value={password} feedback={false} toggleMask
                  onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" />
        <Button type="submit" label="Iniciar sesión" icon="pi pi-sign-in" loading={cargando} />
        <p className="acceso-cambio">¿Aún no tienes cuenta? <Link to="/registro">Crea una aquí</Link></p>
      </form>
    </div>
  );
}
