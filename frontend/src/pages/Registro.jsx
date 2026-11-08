import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { Button } from 'primereact/button';
import { Message } from 'primereact/message';
import { authService } from '../services/authService';
import { mensajeError } from '../api/cliente';
import { useAviso } from '../context/AvisoContext';
import { REGLA_EMAIL, REGLA_PASSWORD, TEXTO_PASSWORD } from '../utils/validacion';
import PanelMarca from './PanelMarca';

const VACIO = { nombre: '', email: '', password: '', confirmacion: '' };

/** CU01 Registrarse */
export default function Registro() {
  const navigate = useNavigate();
  const aviso = useAviso();
  const [datos, setDatos] = useState(VACIO);
  const [errores, setErrores] = useState({});
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(false);

  const cambiar = (campo) => (e) => setDatos({ ...datos, [campo]: e.target.value });

  const validar = () => {
    const e = {};
    if (!datos.nombre.trim()) e.nombre = 'Ingresa tu nombre.';
    if (!REGLA_EMAIL.test(datos.email.trim())) e.email = 'Ingresa un correo válido.';
    if (!REGLA_PASSWORD.test(datos.password)) e.password = TEXTO_PASSWORD;
    if (datos.confirmacion !== datos.password) e.confirmacion = 'Las contraseñas no coinciden.';
    setErrores(e);
    return Object.keys(e).length === 0;
  };

  const enviar = async (e) => {
    e.preventDefault();
    setError('');
    if (!validar()) return;
    setCargando(true);
    try {
      const r = await authService.registrar({ nombre: datos.nombre.trim(), email: datos.email.trim(), password: datos.password });
      aviso.exito(r.mensaje);
      navigate('/login');
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
        <h1>Crea tu cuenta</h1>
        <p className="acceso-ayuda">Solo necesitas tu nombre, un correo y una contraseña segura.</p>
        {error && <Message severity="error" text={error} />}

        <label htmlFor="nombre">Nombre completo</label>
        <InputText id="nombre" value={datos.nombre} onChange={cambiar('nombre')} invalid={!!errores.nombre} maxLength={80} />
        {errores.nombre && <small className="campo-error">{errores.nombre}</small>}

        <label htmlFor="email">Correo electrónico</label>
        <InputText id="email" type="email" value={datos.email} onChange={cambiar('email')} invalid={!!errores.email} />
        {errores.email && <small className="campo-error">{errores.email}</small>}

        <label htmlFor="password">Contraseña</label>
        <Password inputId="password" value={datos.password} onChange={cambiar('password')} toggleMask
                  invalid={!!errores.password} promptLabel="Escribe una contraseña" weakLabel="Débil"
                  mediumLabel="Media" strongLabel="Fuerte" />
        <small className={errores.password ? 'campo-error' : 'campo-ayuda'}>{errores.password || TEXTO_PASSWORD}</small>

        <label htmlFor="confirmacion">Confirma la contraseña</label>
        <Password inputId="confirmacion" value={datos.confirmacion} onChange={cambiar('confirmacion')}
                  feedback={false} toggleMask invalid={!!errores.confirmacion} />
        {errores.confirmacion && <small className="campo-error">{errores.confirmacion}</small>}

        <Button type="submit" label="Crear cuenta" icon="pi pi-user-plus" loading={cargando} />
        <p className="acceso-cambio">¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link></p>
      </form>
    </div>
  );
}
