import { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { InputNumber } from 'primereact/inputnumber';
import { ProgressBar } from 'primereact/progressbar';
import { Tag } from 'primereact/tag';
import { confirmDialog } from 'primereact/confirmdialog';
import Encabezado from '../components/Encabezado';
import { presupuestoService } from '../services/presupuestoService';
import { categoriaService } from '../services/categoriaService';
import { mensajeError } from '../api/cliente';
import { useAviso } from '../context/AvisoContext';
import { nombreMes, pesos } from '../utils/formato';
import { validarMonto } from '../utils/validacion';

const ESTADOS = {
  NORMAL: { texto: 'Dentro del límite', severidad: 'success' },
  ADVERTENCIA: { texto: 'Cerca del límite', severidad: 'warning' },
  EXCEDIDO: { texto: 'Superado', severidad: 'danger' }
};

/** RF006: presupuestos mensuales por categoría con seguimiento del gasto. */
export default function Presupuestos() {
  const aviso = useAviso();
  const [periodo, setPeriodo] = useState(new Date());
  const [presupuestos, setPresupuestos] = useState([]);
  const [categorias, setCategorias] = useState([]);
  const [formulario, setFormulario] = useState(null);
  const [errores, setErrores] = useState({});
  const [guardando, setGuardando] = useState(false);

  const anio = periodo.getFullYear();
  const mes = periodo.getMonth() + 1;

  const cargar = async () => {
    try {
      setPresupuestos(await presupuestoService.listar(anio, mes));
    } catch (e) {
      aviso.error(mensajeError(e));
    }
  };

  useEffect(() => {
    categoriaService.listar('GASTO').then(setCategorias).catch((e) => aviso.error(mensajeError(e)));
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => { cargar(); }, [anio, mes]); // eslint-disable-line react-hooks/exhaustive-deps

  const totalLimite = presupuestos.reduce((s, p) => s + Number(p.montoLimite), 0);
  const totalGastado = presupuestos.reduce((s, p) => s + Number(p.gastado), 0);

  const abrir = (p) => {
    setErrores({});
    setFormulario(p ? { id: p.id, categoriaId: p.categoriaId, montoLimite: Number(p.montoLimite) }
                    : { id: null, categoriaId: null, montoLimite: null });
  };

  const guardar = async () => {
    const e = {};
    if (!formulario.categoriaId) e.categoriaId = 'Elige una categoría de gasto.';
    const errMonto = validarMonto(formulario.montoLimite, 'monto límite');
    if (errMonto) e.montoLimite = errMonto;
    setErrores(e);
    if (Object.keys(e).length) return;
    setGuardando(true);
    const datos = { categoriaId: formulario.categoriaId, anio, mes, montoLimite: formulario.montoLimite };
    try {
      if (formulario.id) await presupuestoService.actualizar(formulario.id, datos);
      else await presupuestoService.crear(datos);
      aviso.exito(formulario.id ? 'Presupuesto actualizado.' : 'Presupuesto creado.');
      setFormulario(null);
      cargar();
    } catch (err) {
      aviso.error(mensajeError(err));
    } finally {
      setGuardando(false);
    }
  };

  const eliminar = (p) => confirmDialog({
    header: 'Eliminar presupuesto',
    message: `¿Eliminar el presupuesto de ${p.categoriaNombre} para ${nombreMes(p.mes, p.anio)}?`,
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: 'Eliminar',
    rejectLabel: 'Cancelar',
    acceptClassName: 'p-button-danger',
    accept: async () => {
      try {
        await presupuestoService.eliminar(p.id);
        aviso.exito('Presupuesto eliminado.');
        cargar();
      } catch (err) {
        aviso.error(mensajeError(err));
      }
    }
  });

  return (
    <>
      <Encabezado titulo="Presupuestos" descripcion="Ponle un límite mensual a cada categoría de gasto.">
        <Calendar value={periodo} onChange={(e) => e.value && setPeriodo(e.value)} view="month" dateFormat="MM 'de' yy"
                  showIcon aria-label="Mes del presupuesto" className="selector-mes" />
        <Button label="Nuevo presupuesto" icon="pi pi-plus" onClick={() => abrir(null)} />
      </Encabezado>

      {presupuestos.length > 0 && (
        <p className="totales-filtro">
          En {nombreMes(mes, anio)} llevas <b>{pesos(totalGastado)}</b> de <b>{pesos(totalLimite)}</b> presupuestados.
        </p>
      )}

      {presupuestos.length === 0 ? (
        <div className="vacio">
          <i className="pi pi-wallet" aria-hidden="true" />
          <p>No tienes presupuestos para {nombreMes(mes, anio)}.</p>
          <Button label="Crear el primero" icon="pi pi-plus" onClick={() => abrir(null)} />
        </div>
      ) : (
        <ul className="lista-presupuestos">
          {presupuestos.map((p) => (
            <li key={p.id} className={`presupuesto estado-${p.estado.toLowerCase()}`}>
              <div className="presupuesto-cabeza">
                <h2>{p.categoriaNombre}</h2>
                <Tag value={ESTADOS[p.estado].texto} severity={ESTADOS[p.estado].severidad} />
              </div>
              <ProgressBar value={Math.min(p.porcentaje, 100)} showValue={false} aria-label={`${p.porcentaje} % usado`} />
              <div className="presupuesto-cifras">
                <span>Gastado <b>{pesos(p.gastado)}</b> de {pesos(p.montoLimite)} ({p.porcentaje} %)</span>
                <span className={Number(p.disponible) < 0 ? 'texto-gasto' : ''}>
                  {Number(p.disponible) < 0 ? `Excedido por ${pesos(-p.disponible)}` : `Disponible ${pesos(p.disponible)}`}
                </span>
              </div>
              <div className="acciones-fila">
                <Button label="Editar" icon="pi pi-pencil" text size="small" onClick={() => abrir(p)} />
                <Button label="Eliminar" icon="pi pi-trash" text size="small" severity="danger" onClick={() => eliminar(p)} />
              </div>
            </li>
          ))}
        </ul>
      )}

      <Dialog header={formulario?.id ? 'Editar presupuesto' : `Nuevo presupuesto para ${nombreMes(mes, anio)}`}
              visible={!!formulario} onHide={() => setFormulario(null)} style={{ width: '28rem' }} breakpoints={{ '640px': '95vw' }}
              footer={(
                <>
                  <Button label="Cancelar" text onClick={() => setFormulario(null)} />
                  <Button label="Guardar" icon="pi pi-check" loading={guardando} onClick={guardar} />
                </>
              )}>
        {formulario && (
          <div className="formulario">
            <label htmlFor="p-cat">Categoría de gasto</label>
            <Dropdown inputId="p-cat" value={formulario.categoriaId} options={categorias} optionLabel="nombre" optionValue="id"
                      placeholder="Elige una categoría" invalid={!!errores.categoriaId}
                      onChange={(e) => setFormulario({ ...formulario, categoriaId: e.value })} />
            {errores.categoriaId && <small className="campo-error">{errores.categoriaId}</small>}
            <label htmlFor="p-monto">Límite del mes</label>
            <InputNumber inputId="p-monto" value={formulario.montoLimite} mode="currency" currency="COP" locale="es-CO"
                         minFractionDigits={0} invalid={!!errores.montoLimite}
                         onValueChange={(e) => setFormulario({ ...formulario, montoLimite: e.value })} />
            {errores.montoLimite && <small className="campo-error">{errores.montoLimite}</small>}
            <small className="campo-ayuda">Te avisaremos cuando llegues al 80 % y cuando superes el límite.</small>
          </div>
        )}
      </Dialog>
    </>
  );
}
