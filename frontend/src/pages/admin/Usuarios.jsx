import { useEffect, useState } from 'react';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { confirmDialog } from 'primereact/confirmdialog';
import Encabezado from '../../components/Encabezado';
import { adminService } from '../../services/adminService';
import { mensajeError } from '../../api/cliente';
import { useAviso } from '../../context/AvisoContext';
import { useSesion } from '../../context/SesionContext';
import { fechaCorta } from '../../utils/formato';

/** CU09 Gestionar usuarios y CU10 Reportes del sistema (solo administrador). */
export default function Usuarios() {
  const aviso = useAviso();
  const { usuario } = useSesion();
  const [usuarios, setUsuarios] = useState([]);
  const [estadisticas, setEstadisticas] = useState(null);
  const [busqueda, setBusqueda] = useState('');
  const [cargando, setCargando] = useState(true);

  const cargar = async () => {
    setCargando(true);
    try {
      const [lista, stats] = await Promise.all([adminService.usuarios(), adminService.estadisticas()]);
      setUsuarios(lista);
      setEstadisticas(stats);
    } catch (e) {
      aviso.error(mensajeError(e));
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => { cargar(); }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const cambiarEstado = (u) => confirmDialog({
    header: u.activo ? 'Desactivar cuenta' : 'Activar cuenta',
    message: u.activo
      ? `${u.nombre} no podrá iniciar sesión hasta que vuelvas a activar su cuenta. ¿Continuar?`
      : `${u.nombre} podrá volver a iniciar sesión. ¿Continuar?`,
    icon: 'pi pi-exclamation-triangle',
    acceptLabel: u.activo ? 'Desactivar' : 'Activar',
    rejectLabel: 'Cancelar',
    acceptClassName: u.activo ? 'p-button-danger' : 'p-button-success',
    accept: async () => {
      try {
        await adminService.cambiarEstado(u.id, !u.activo);
        aviso.exito(u.activo ? 'Cuenta desactivada.' : 'Cuenta activada.');
        cargar();
      } catch (e) {
        aviso.error(mensajeError(e));
      }
    }
  });

  return (
    <>
      <Encabezado titulo="Usuarios" descripcion="Supervisa las cuentas y el uso del sistema." />
      {estadisticas && (
        <div className="cifras">
          <div><span>Usuarios registrados</span><strong>{estadisticas.totalUsuarios}</strong></div>
          <div><span>Activos</span><strong className="texto-ingreso">{estadisticas.usuariosActivos}</strong></div>
          <div><span>Inactivos</span><strong className="texto-gasto">{estadisticas.usuariosInactivos}</strong></div>
          <div><span>Movimientos registrados</span><strong>{estadisticas.totalTransacciones}</strong></div>
          <div><span>Metas de ahorro</span><strong>{estadisticas.totalMetas}</strong></div>
          <div><span>Presupuestos</span><strong>{estadisticas.totalPresupuestos}</strong></div>
        </div>
      )}
      <div className="filtros">
        <div className="campo">
          <label htmlFor="buscar">Buscar por nombre o correo</label>
          <InputText id="buscar" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} placeholder="Ej: laura" />
        </div>
      </div>
      <DataTable value={usuarios} loading={cargando} paginator rows={10} globalFilter={busqueda}
                 globalFilterFields={['nombre', 'email']} emptyMessage="No hay usuarios con esa búsqueda." size="small" className="tabla">
        <Column field="id" header="ID" sortable />
        <Column field="nombre" header="Nombre" sortable />
        <Column field="email" header="Correo" sortable />
        <Column field="rol" header="Rol" body={(u) => (u.rol === 'ADMIN' ? 'Administrador' : 'Usuario')} />
        <Column field="fechaRegistro" header="Registro" sortable body={(u) => fechaCorta(u.fechaRegistro)} />
        <Column field="activo" header="Estado" body={(u) => <Tag value={u.activo ? 'Activa' : 'Inactiva'} severity={u.activo ? 'success' : 'danger'} />} />
        <Column header="Acciones" body={(u) => (
          <Button label={u.activo ? 'Desactivar' : 'Activar'} size="small" text severity={u.activo ? 'danger' : 'success'}
                  disabled={u.id === usuario.id} onClick={() => cambiarEstado(u)} />
        )} />
      </DataTable>
    </>
  );
}
