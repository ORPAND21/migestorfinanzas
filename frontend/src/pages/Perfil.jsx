import { useState } from 'react';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { InputSwitch } from 'primereact/inputswitch';
import { Password } from 'primereact/password';
import Encabezado from '../components/Encabezado';
import { perfilService } from '../services/perfilService';
import { mensajeError } from '../api/cliente';
import { useAviso } from '../context/AvisoContext';
import { useSesion } from '../context/SesionContext';
import { REGLA_PASSWORD, TEXTO_PASSWORD } from '../utils/validacion';

/** CU08 Editar perfil y RF006.3.1 activar o desactivar las alertas. */
export default function Perfil() {
  const aviso = useAviso();
  const { usuario, actualizarUsuario } = useSesion();
  const [nombre, setNombre] = useState(usuario.nombre);
  const [notificaciones, setNotificaciones] = useState(usuario.notificaciones);
  const [claves, setClaves] = useState({ actual: '', nueva: '', confirmacion: '' });
  const [errores, setErrores] = useState({});
  const [guardando, setGuardando] = useState(false);

  const guardarDatos = async (e) => {
    e.preventDefault();
    if (!nombre.trim()) { setErrores({ nombre: 'Ingresa tu nombre.' }); return; }
    setErrores({});
    setGuardando(true);
    try {
      actualizarUsuario(await perfilService.actualizar({ nombre: nombre.trim(), notificaciones }));
      aviso.exito('Perfil actualizado.');
    } catch (err) {
      aviso.error(mensajeError(err));
    } finally {
      setGuardando(false);
    }
  };

  const cambiarClave = async (e) => {
    e.preventDefault();
    const er = {};
    if (!claves.actual) er.actual = 'Ingresa tu contraseña actual.';
    if (!REGLA_PASSWORD.test(claves.nueva)) er.nueva = TEXTO_PASSWORD;
    if (claves.confirmacion !== claves.nueva) er.confirmacion = 'Las contraseñas no coinciden.';
    setErrores(er);
    if (Object.keys(er).length) return;
    try {
      const r = await perfilService.cambiarPassword({ actual: claves.actual, nueva: claves.nueva });
      aviso.exito(r.mensaje);
      setClaves({ actual: '', nueva: '', confirmacion: '' });
    } catch (err) {
      aviso.error(mensajeError(err));
    }
  };

  return (
    <>
      <Encabezado titulo="Mi perfil" descripcion="Actualiza tus datos, tus alertas y tu contraseña." />
      <div className="rejilla-dos">
        <form className="bloque formulario" onSubmit={guardarDatos} noValidate>
          <h2>Datos personales</h2>
          <label htmlFor="p-nombre">Nombre</label>
          <InputText id="p-nombre" value={nombre} maxLength={80} invalid={!!errores.nombre} onChange={(e) => setNombre(e.target.value)} />
          {errores.nombre && <small className="campo-error">{errores.nombre}</small>}
          <label htmlFor="p-email">Correo electrónico</label>
          <InputText id="p-email" value={usuario.email} disabled />
          <small className="campo-ayuda">El correo identifica tu cuenta y no se puede cambiar.</small>
          {usuario.rol === 'USUARIO' && (
            <div className="interruptor">
              <InputSwitch inputId="p-notif" checked={notificaciones} onChange={(e) => setNotificaciones(e.value)} />
              <label htmlFor="p-notif">Avisarme cuando me acerque o supere un presupuesto</label>
            </div>
          )}
          <Button type="submit" label="Guardar cambios" icon="pi pi-check" loading={guardando} />
        </form>

        <form className="bloque formulario" onSubmit={cambiarClave} noValidate>
          <h2>Cambiar contraseña</h2>
          <label htmlFor="c-actual">Contraseña actual</label>
          <Password inputId="c-actual" value={claves.actual} feedback={false} toggleMask invalid={!!errores.actual}
                    onChange={(e) => setClaves({ ...claves, actual: e.target.value })} />
          {errores.actual && <small className="campo-error">{errores.actual}</small>}
          <label htmlFor="c-nueva">Nueva contraseña</label>
          <Password inputId="c-nueva" value={claves.nueva} feedback={false} toggleMask invalid={!!errores.nueva}
                    onChange={(e) => setClaves({ ...claves, nueva: e.target.value })} />
          <small className={errores.nueva ? 'campo-error' : 'campo-ayuda'}>{errores.nueva || TEXTO_PASSWORD}</small>
          <label htmlFor="c-conf">Confirma la nueva contraseña</label>
          <Password inputId="c-conf" value={claves.confirmacion} feedback={false} toggleMask invalid={!!errores.confirmacion}
                    onChange={(e) => setClaves({ ...claves, confirmacion: e.target.value })} />
          {errores.confirmacion && <small className="campo-error">{errores.confirmacion}</small>}
          <Button type="submit" label="Cambiar contraseña" icon="pi pi-lock" outlined />
        </form>
      </div>
    </>
  );
}
