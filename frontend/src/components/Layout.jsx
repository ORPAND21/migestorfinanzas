import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useSesion } from '../context/SesionContext';

const MENU_USUARIO = [
  { ruta: '/inicio', icono: 'pi-home', texto: 'Inicio' },
  { ruta: '/movimientos', icono: 'pi-arrow-right-arrow-left', texto: 'Ingresos y gastos' },
  { ruta: '/presupuestos', icono: 'pi-wallet', texto: 'Presupuestos' },
  { ruta: '/metas', icono: 'pi-flag', texto: 'Metas de ahorro' },
  { ruta: '/reportes', icono: 'pi-chart-bar', texto: 'Reportes' },
  { ruta: '/perfil', icono: 'pi-user', texto: 'Mi perfil' }
];

const MENU_ADMIN = [
  { ruta: '/admin', icono: 'pi-users', texto: 'Usuarios' },
  { ruta: '/perfil', icono: 'pi-user', texto: 'Mi perfil' }
];

/** Estructura común: menú lateral con navegación y área de contenido. */
export default function Layout() {
  const { usuario, cerrarSesion } = useSesion();
  const navigate = useNavigate();
  const [abierto, setAbierto] = useState(false);
  const menu = usuario?.rol === 'ADMIN' ? MENU_ADMIN : MENU_USUARIO;

  const salir = async () => {
    await cerrarSesion();
    navigate('/login');
  };

  return (
    <div className="app">
      <aside className={`lateral no-imprimir ${abierto ? 'abierto' : ''}`}>
        <div className="marca">
          <img src="/logo.png" alt="" />
          <span>MiGestor<b>Finanzas</b></span>
        </div>
        <nav onClick={() => setAbierto(false)}>
          {menu.map((item) => (
            <NavLink key={item.ruta} to={item.ruta} end={item.ruta === '/admin'}>
              <i className={`pi ${item.icono}`} aria-hidden="true" />
              {item.texto}
            </NavLink>
          ))}
        </nav>
        <div className="lateral-pie">
          <div className="usuario-actual">
            <strong>{usuario?.nombre}</strong>
            <small>{usuario?.rol === 'ADMIN' ? 'Administrador' : usuario?.email}</small>
          </div>
          <button type="button" className="boton-salir" onClick={salir}>
            <i className="pi pi-sign-out" aria-hidden="true" /> Cerrar sesión
          </button>
        </div>
      </aside>

      <div className="contenido">
        <div className="barra-movil no-imprimir">
          <button type="button" aria-label="Abrir menú" onClick={() => setAbierto(!abierto)}>
            <i className="pi pi-bars" />
          </button>
          <img src="/logo.png" alt="" />
          <span>MiGestorFinanzas</span>
        </div>
        <main>
          <Outlet />
        </main>
      </div>
      {abierto && <div className="velo no-imprimir" onClick={() => setAbierto(false)} />}
    </div>
  );
}
