import { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Dialog } from 'primereact/dialog';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { ProgressBar } from 'primereact/progressbar';
import { Tag } from 'primereact/tag';
import { confirmDialog } from 'primereact/confirmdialog';
import Encabezado from '../components/Encabezado';
import { metaService } from '../services/metaService';
import { transaccionService } from '../services/transaccionService';
import { mensajeError } from '../api/cliente';
import { useAviso } from '../context/AvisoContext';
import { aIso, deIso, fechaCorta, pesos } from '../utils/formato';
import { validarMonto } from '../utils/validacion';

const ESTADOS = {
  EN_PROGRESO: { texto: 'En progreso', severidad: 'info' },
  COMPLETADA: { texto: 'Completada', severidad: 'success' },
  VENCIDA: { texto: 'Vencida', severidad: 'warning' }
};

const manana = () => {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return d;
};

/** RF005: metas de ahorro con aportes y seguimiento del progreso. */
export default function Metas() {
  const aviso = useAviso();
  const [metas, setMetas] = useState([]);
  const [saldo, setSaldo] = useState(0);
  const [formulario, setFormulario] = useState(null);
  const [aporte, setAporte] = useState(null);       // { meta, monto }
  const [historial, setHistorial] = useState(null); // { meta, aportes }
  const [errores, setErrores] = useState({});
  const [guardando, setGuardando] = useState(false);

  const cargar = async () => {
    try {
      const [lista, resumen] = await Promise.all([metaService.listar(), transaccionService.resumen()]);
      setMetas(lista);
      setSaldo(Number(resumen.saldoDisponible));
    } catch (e) {
      aviso.error(mensajeError(e));
    }
  };

  useEffect(() => { cargar(); }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const abrir = (m) => {
    setErrores({});
    setFormulario(m ? { id: m.id, nombre: m.nombre, montoObjetivo: Number(m.montoObjetivo), fechaLimite: deIso(m.fechaLimite) }
                    : { id: null, nombre: '', montoObjetivo: null, fechaLimite: null });
  };

  const guardar = async () => {
    const e = {};
    if (!formulario.nombre.trim()) e.nombre = 'Ponle un nombre a la meta.';
    const errMonto = validarMonto(formulario.montoObjetivo, 'monto objetivo');
    if (errMonto) e.montoObjetivo = errMonto;
    if (!formulario.fechaLimite) e.fechaLimite = 'Elige la fecha límite.';
    else if (formulario.fechaLimite <= new Date()) e.fechaLimite = 'La fecha límite debe ser posterior a hoy.';
    setErrores(e);
    if (Object.keys(e).length) return;
    setGuardando(true);
    const datos = { nombre: formulario.nombre.trim(), montoObjetivo: formulario.montoObjetivo, fechaLimite: aIso(formulario.fechaLimite) };
    try {
      if (formulario.id) await metaService.actualizar(formulario.id, datos);
      else await metaService.crear(datos);
      aviso.exito(formulario.id ? 'Meta actualizada.' : 'Meta creada.');
      setFormulario(null);
      cargar();
    } catch (err) {
      aviso.error(mensajeError(err));
    } finally {
      setGuardando(false);
    }
  };

  const aportar = async () => {
    const err = validarMonto(aporte.monto, 'aporte');
    if (err) { setErrores({ aporte: err }); return; }
    if (aporte.monto > saldo) { setErrores({ aporte: `Tu saldo disponible es ${pesos(saldo)}.` }); return; }
    setGuardando(true);
    try {
      const meta = await metaService.aportar(aporte.meta.id, aporte.monto);
      aviso.exito(meta.estado === 'COMPLETADA' ? `¡Completaste la meta ${meta.nombre}!` : `Aporte de ${pesos(aporte.monto)} registrado.`);
      setAporte(null);
      cargar();
    } catch (e) {
      aviso.error(mensajeError(e));
    } finally {
      setGuardando(false);
    }
  };

  const verHistorial = async (meta) => {
    try {
      setHistorial({ meta, aportes: await metaService.aportes(meta.id) });
    } catch (e) {
      aviso.error(mensajeError(e));
    }
  };

  const eliminar = (m) => confirmDialog({
    header: 'Eliminar meta',
    message: `¿Eliminar la meta "${m.nombre}"? Lo ahorrado (${pesos(m.montoAhorrado)}) vuelve a tu saldo disponible.`,
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Eliminar',
    rejectLabel: 'Cancelar',
    acceptClassName: 'p-button-danger',
    accept: async () => {
      try {
        const r = await metaService.eliminar(m.id);
        aviso.exito(r.mensaje);
        cargar();
      } catch (e) {
        aviso.error(mensajeError(e));
      }
    }
  });

  return (
    <>
      <Encabezado titulo="Metas de ahorro" descripcion={`Aparta dinero para lo que quieres. Saldo disponible: ${pesos(saldo)}.`}>
        <Button label="Nueva meta" icon="pi pi-plus" onClick={() => abrir(null)} />
      </Encabezado>

      {metas.length === 0 ? (
        <div className="vacio">
          <i className="pi pi-flag" aria-hidden="true" />
          <p>Aún no tienes metas. Crea una, por ejemplo un fondo de emergencia.</p>
          <Button label="Crear meta" icon="pi pi-plus" onClick={() => abrir(null)} />
        </div>
      ) : (
        <ul className="lista-metas">
          {metas.map((m) => (
            <li key={m.id} className="meta">
              <div className="meta-cabeza">
                <h2>{m.nombre}</h2>
                <Tag value={ESTADOS[m.estado].texto} severity={ESTADOS[m.estado].severidad} />
              </div>
              <div className="meta-avance">
                <strong>{m.porcentaje} %</strong>
                <span>{pesos(m.montoAhorrado)} de {pesos(m.montoObjetivo)}</span>
              </div>
              <ProgressBar value={m.porcentaje} showValue={false} aria-label={`${m.porcentaje} % ahorrado`} />
              <p className="meta-detalle">
                {m.estado === 'COMPLETADA' ? 'Meta alcanzada.' : `Te faltan ${pesos(m.restante)}.`} Fecha límite: {fechaCorta(m.fechaLimite)}.
              </p>
              <div className="acciones-fila">
                <Button label="Aportar" icon="pi pi-plus-circle" size="small" disabled={m.estado === 'COMPLETADA'}
                        onClick={() => { setErrores({}); setAporte({ meta: m, monto: null }); }} />
                <Button label="Aportes" icon="pi pi-history" text size="small" onClick={() => verHistorial(m)} />
                <Button label="Editar" icon="pi pi-pencil" text size="small" onClick={() => abrir(m)} />
                <Button label="Eliminar" icon="pi pi-trash" text size="small" severity="danger" onClick={() => eliminar(m)} />
              </div>
            </li>
          ))}
        </ul>
      )}

      <Dialog header={formulario?.id ? 'Editar meta' : 'Nueva meta de ahorro'} visible={!!formulario}
              onHide={() => setFormulario(null)} style={{ width: '28rem' }} breakpoints={{ '640px': '95vw' }}
              footer={(
                <>
                  <Button label="Cancelar" text onClick={() => setFormulario(null)} />
                  <Button label="Guardar" icon="pi pi-check" loading={guardando} onClick={guardar} />
                </>
              )}>
        {formulario && (
          <div className="formulario">
            <label htmlFor="g-nombre">Nombre de la meta</label>
            <InputText id="g-nombre" value={formulario.nombre} maxLength={80} placeholder="Ej: Fondo de emergencia"
                       invalid={!!errores.nombre} onChange={(e) => setFormulario({ ...formulario, nombre: e.target.value })} />
            {errores.nombre && <small className="campo-error">{errores.nombre}</small>}
            <label htmlFor="g-monto">Monto objetivo</label>
            <InputNumber inputId="g-monto" value={formulario.montoObjetivo} mode="currency" currency="COP" locale="es-CO"
                         minFractionDigits={0} invalid={!!errores.montoObjetivo}
                         onValueChange={(e) => setFormulario({ ...formulario, montoObjetivo: e.value })} />
            {errores.montoObjetivo && <small className="campo-error">{errores.montoObjetivo}</small>}
            <label htmlFor="g-fecha">Fecha límite</label>
            <Calendar inputId="g-fecha" value={formulario.fechaLimite} minDate={manana()} dateFormat="dd/mm/yy" showIcon
                      invalid={!!errores.fechaLimite} onChange={(e) => setFormulario({ ...formulario, fechaLimite: e.value })} />
            {errores.fechaLimite && <small className="campo-error">{errores.fechaLimite}</small>}
          </div>
        )}
      </Dialog>

      <Dialog header={aporte ? `Aportar a ${aporte.meta.nombre}` : ''} visible={!!aporte} onHide={() => setAporte(null)}
              style={{ width: '26rem' }} breakpoints={{ '640px': '95vw' }}
              footer={(
                <>
                  <Button label="Cancelar" text onClick={() => setAporte(null)} />
                  <Button label="Aportar" icon="pi pi-check" loading={guardando} onClick={aportar} />
                </>
              )}>
        {aporte && (
          <div className="formulario">
            <p className="campo-ayuda">Faltan {pesos(aporte.meta.restante)} para la meta. Tu saldo disponible es {pesos(saldo)}.</p>
            <label htmlFor="a-monto">Monto del aporte</label>
            <InputNumber inputId="a-monto" value={aporte.monto} mode="currency" currency="COP" locale="es-CO" minFractionDigits={0}
                         invalid={!!errores.aporte} onValueChange={(e) => setAporte({ ...aporte, monto: e.value })} />
            {errores.aporte && <small className="campo-error">{errores.aporte}</small>}
          </div>
        )}
      </Dialog>

      <Dialog header={historial ? `Aportes a ${historial.meta.nombre}` : ''} visible={!!historial} onHide={() => setHistorial(null)}
              style={{ width: '26rem' }} breakpoints={{ '640px': '95vw' }}>
        {historial && (historial.aportes.length === 0
          ? <p className="vacio-texto">Esta meta todavía no tiene aportes.</p>
          : (
            <ul className="lista-movimientos">
              {historial.aportes.map((a) => (
                <li key={a.id}><span>{fechaCorta(a.fecha)}</span><strong className="texto-ahorro">{pesos(a.monto)}</strong></li>
              ))}
            </ul>
          ))}
      </Dialog>
    </>
  );
}
