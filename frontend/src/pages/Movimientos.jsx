import { useCallback, useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { Dropdown } from 'primereact/dropdown';
import { Calendar } from 'primereact/calendar';
import { InputNumber } from 'primereact/inputnumber';
import { InputText } from 'primereact/inputtext';
import { SelectButton } from 'primereact/selectbutton';
import { Tag } from 'primereact/tag';
import { confirmDialog } from 'primereact/confirmdialog';
import Encabezado from '../components/Encabezado';
import { transaccionService } from '../services/transaccionService';
import { categoriaService } from '../services/categoriaService';
import { mensajeError } from '../api/cliente';
import { useAviso } from '../context/AvisoContext';
import { aIso, deIso, fechaCorta, pesos } from '../utils/formato';
import { validarMonto } from '../utils/validacion';

const TIPOS = [{ label: 'Ingreso', value: 'INGRESO' }, { label: 'Gasto', value: 'GASTO' }];
const FILTROS_VACIOS = { tipo: null, categoriaId: null, desde: null, hasta: null };

/** RF002 y RF003: registrar, consultar, editar y eliminar ingresos y gastos. */
export default function Movimientos() {
  const aviso = useAviso();
  const [params, setParams] = useSearchParams();
  const [movimientos, setMovimientos] = useState([]);
  const [categorias, setCategorias] = useState([]);
  const [filtros, setFiltros] = useState(FILTROS_VACIOS);
  const [cargando, setCargando] = useState(true);
  const [formulario, setFormulario] = useState(null); // null = cerrado
  const [errores, setErrores] = useState({});
  const [guardando, setGuardando] = useState(false);

  const cargar = useCallback(async (f = filtros) => {
    setCargando(true);
    try {
      const datos = await transaccionService.listar({
        tipo: f.tipo || undefined,
        categoriaId: f.categoriaId || undefined,
        desde: aIso(f.desde) || undefined,
        hasta: aIso(f.hasta) || undefined
      });
      setMovimientos(datos);
    } catch (e) {
      aviso.error(mensajeError(e));
    } finally {
      setCargando(false);
    }
  }, [filtros]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    categoriaService.listar().then(setCategorias).catch((e) => aviso.error(mensajeError(e)));
    cargar(FILTROS_VACIOS);
    const nuevo = params.get('nuevo');
    if (nuevo === 'INGRESO' || nuevo === 'GASTO') {
      abrirNuevo(nuevo);
      setParams({}, { replace: true });
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const categoriasDe = (tipo) => categorias.filter((c) => !tipo || c.tipo === tipo);
  const totales = useMemo(() => movimientos.reduce((t, m) => {
    t[m.tipo] += Number(m.monto);
    return t;
  }, { INGRESO: 0, GASTO: 0 }), [movimientos]);

  function abrirNuevo(tipo = 'GASTO') {
    setErrores({});
    setFormulario({ id: null, tipo, categoriaId: null, monto: null, fecha: new Date(), descripcion: '' });
  }

  const abrirEdicion = (m) => {
    setErrores({});
    setFormulario({ id: m.id, tipo: m.tipo, categoriaId: m.categoriaId, monto: Number(m.monto),
      fecha: deIso(m.fecha), descripcion: m.descripcion || '' });
  };

  const validar = () => {
    const e = {};
    if (!formulario.tipo) e.tipo = 'Elige si es ingreso o gasto.';
    if (!formulario.categoriaId) e.categoriaId = 'Elige una categoría.';
    const errMonto = validarMonto(formulario.monto);
    if (errMonto) e.monto = errMonto;
    if (!formulario.fecha) e.fecha = 'Elige la fecha.';
    else if (formulario.fecha > new Date()) e.fecha = 'La fecha no puede ser posterior a hoy.';
    setErrores(e);
    return Object.keys(e).length === 0;
  };

  const guardar = async () => {
    if (!validar()) return;
    setGuardando(true);
    const datos = {
      tipo: formulario.tipo,
      categoriaId: formulario.categoriaId,
      monto: formulario.monto,
      fecha: aIso(formulario.fecha),
      descripcion: formulario.descripcion.trim() || null
    };
    try {
      const r = formulario.id
        ? await transaccionService.actualizar(formulario.id, datos)
        : await transaccionService.crear(datos);
      aviso.exito(formulario.id ? 'Movimiento actualizado.' : `${formulario.tipo === 'INGRESO' ? 'Ingreso' : 'Gasto'} registrado.`);
      if (r.alerta) {
        r.alerta.nivel === 'EXCEDIDO' ? aviso.peligro(r.alerta.mensaje) : aviso.advertencia(r.alerta.mensaje);
      }
      setFormulario(null);
      cargar();
    } catch (e) {
      aviso.error(mensajeError(e));
    } finally {
      setGuardando(false);
    }
  };

  const eliminar = (m) => {
    confirmDialog({
      header: 'Eliminar movimiento',
      message: `¿Eliminar "${m.descripcion || m.categoriaNombre}" por ${pesos(m.monto)}? Esta acción no se puede deshacer.`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Eliminar',
      rejectLabel: 'Cancelar',
      acceptClassName: 'p-button-danger',
      accept: async () => {
        try {
          await transaccionService.eliminar(m.id);
          aviso.exito('Movimiento eliminado.');
          cargar();
        } catch (e) {
          aviso.error(mensajeError(e));
        }
      }
    });
  };

  const aplicarFiltros = () => {
    if (filtros.desde && filtros.hasta && filtros.desde > filtros.hasta) {
      aviso.error('La fecha inicial no puede ser posterior a la fecha final.');
      return;
    }
    cargar(filtros);
  };

  const limpiarFiltros = () => {
    setFiltros(FILTROS_VACIOS);
    cargar(FILTROS_VACIOS);
  };

  return (
    <>
      <Encabezado titulo="Ingresos y gastos" descripcion="Registra cada movimiento con su categoría y fecha.">
        <Button label="Nuevo ingreso" icon="pi pi-plus" severity="success" onClick={() => abrirNuevo('INGRESO')} />
        <Button label="Nuevo gasto" icon="pi pi-minus" severity="danger" onClick={() => abrirNuevo('GASTO')} />
      </Encabezado>

      <section className="filtros">
        <div className="campo">
          <label htmlFor="f-tipo">Tipo</label>
          <Dropdown inputId="f-tipo" value={filtros.tipo} options={TIPOS} showClear placeholder="Todos"
                    onChange={(e) => setFiltros({ ...filtros, tipo: e.value, categoriaId: null })} />
        </div>
        <div className="campo">
          <label htmlFor="f-cat">Categoría</label>
          <Dropdown inputId="f-cat" value={filtros.categoriaId} options={categoriasDe(filtros.tipo)} optionLabel="nombre"
                    optionValue="id" showClear placeholder="Todas" onChange={(e) => setFiltros({ ...filtros, categoriaId: e.value })} />
        </div>
        <div className="campo">
          <label htmlFor="f-desde">Desde</label>
          <Calendar inputId="f-desde" value={filtros.desde} dateFormat="dd/mm/yy" showIcon showButtonBar
                    onChange={(e) => setFiltros({ ...filtros, desde: e.value })} />
        </div>
        <div className="campo">
          <label htmlFor="f-hasta">Hasta</label>
          <Calendar inputId="f-hasta" value={filtros.hasta} dateFormat="dd/mm/yy" showIcon showButtonBar
                    onChange={(e) => setFiltros({ ...filtros, hasta: e.value })} />
        </div>
        <div className="filtros-acciones">
          <Button label="Filtrar" icon="pi pi-filter" onClick={aplicarFiltros} />
          <Button label="Limpiar" icon="pi pi-times" text onClick={limpiarFiltros} />
        </div>
      </section>

      <p className="totales-filtro">
        {movimientos.length} movimientos. Ingresos <b className="texto-ingreso">{pesos(totales.INGRESO)}</b>,
        gastos <b className="texto-gasto">{pesos(totales.GASTO)}</b>.
      </p>

      <DataTable value={movimientos} loading={cargando} paginator rows={10} rowsPerPageOptions={[10, 20, 50]}
                 emptyMessage="No hay movimientos con estos filtros." stripedRows size="small" className="tabla">
        <Column field="fecha" header="Fecha" sortable body={(m) => fechaCorta(m.fecha)} />
        <Column field="tipo" header="Tipo" body={(m) => (
          <Tag value={m.tipo === 'INGRESO' ? 'Ingreso' : 'Gasto'} severity={m.tipo === 'INGRESO' ? 'success' : 'danger'} />
        )} />
        <Column field="categoriaNombre" header="Categoría" sortable />
        <Column field="descripcion" header="Descripción" body={(m) => m.descripcion || <span className="tenue">Sin descripción</span>} />
        <Column field="monto" header="Monto" sortable align="right" body={(m) => (
          <span className={m.tipo === 'INGRESO' ? 'texto-ingreso' : 'texto-gasto'}>{pesos(m.monto)}</span>
        )} />
        <Column header="Acciones" align="center" body={(m) => (
          <div className="acciones-fila">
            <Button icon="pi pi-pencil" rounded text aria-label="Editar" tooltip="Editar" onClick={() => abrirEdicion(m)} />
            <Button icon="pi pi-trash" rounded text severity="danger" aria-label="Eliminar" tooltip="Eliminar" onClick={() => eliminar(m)} />
          </div>
        )} />
      </DataTable>

      <Dialog header={formulario?.id ? 'Editar movimiento' : 'Nuevo movimiento'} visible={!!formulario}
              onHide={() => setFormulario(null)} style={{ width: '30rem' }} breakpoints={{ '640px': '95vw' }}
              footer={(
                <>
                  <Button label="Cancelar" text onClick={() => setFormulario(null)} />
                  <Button label="Guardar" icon="pi pi-check" loading={guardando} onClick={guardar} />
                </>
              )}>
        {formulario && (
          <div className="formulario">
            <label>Tipo de movimiento</label>
            <SelectButton value={formulario.tipo} options={TIPOS} allowEmpty={false}
                          onChange={(e) => setFormulario({ ...formulario, tipo: e.value, categoriaId: null })} />
            {errores.tipo && <small className="campo-error">{errores.tipo}</small>}

            <label htmlFor="m-cat">Categoría</label>
            <Dropdown inputId="m-cat" value={formulario.categoriaId} options={categoriasDe(formulario.tipo)}
                      optionLabel="nombre" optionValue="id" placeholder="Elige una categoría" invalid={!!errores.categoriaId}
                      onChange={(e) => setFormulario({ ...formulario, categoriaId: e.value })} />
            {errores.categoriaId && <small className="campo-error">{errores.categoriaId}</small>}

            <label htmlFor="m-monto">Monto</label>
            <InputNumber inputId="m-monto" value={formulario.monto} mode="currency" currency="COP" locale="es-CO"
                         minFractionDigits={0} maxFractionDigits={2} invalid={!!errores.monto}
                         onValueChange={(e) => setFormulario({ ...formulario, monto: e.value })} />
            {errores.monto && <small className="campo-error">{errores.monto}</small>}

            <label htmlFor="m-fecha">Fecha</label>
            <Calendar inputId="m-fecha" value={formulario.fecha} dateFormat="dd/mm/yy" maxDate={new Date()} showIcon
                      invalid={!!errores.fecha} onChange={(e) => setFormulario({ ...formulario, fecha: e.value })} />
            {errores.fecha && <small className="campo-error">{errores.fecha}</small>}

            <label htmlFor="m-desc">Descripción (opcional)</label>
            <InputText id="m-desc" value={formulario.descripcion} maxLength={150}
                       onChange={(e) => setFormulario({ ...formulario, descripcion: e.target.value })}
                       placeholder={formulario.tipo === 'INGRESO' ? 'Ej: Salario de noviembre' : 'Ej: Mercado de la semana'} />
          </div>
        )}
      </Dialog>
    </>
  );
}
