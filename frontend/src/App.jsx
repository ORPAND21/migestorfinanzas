import { Navigate, Route, Routes } from 'react-router-dom';
import { ConfirmDialog } from 'primereact/confirmdialog';
import { SesionProvider, useSesion } from './context/SesionContext';
import { AvisoProvider } from './context/AvisoContext';
import Layout from './components/Layout';
import RutaProtegida from './components/RutaProtegida';
import Login from './pages/Login';
import Registro from './pages/Registro';
import Inicio from './pages/Inicio';
import Movimientos from './pages/Movimientos';
import Presupuestos from './pages/Presupuestos';
import Metas from './pages/Metas';
import Reportes from './pages/Reportes';
import Perfil from './pages/Perfil';
import Usuarios from './pages/admin/Usuarios';
import NoEncontrado from './pages/NoEncontrado';

function RutaInicial() {
  const { usuario } = useSesion();
  if (!usuario) return <Navigate to="/login" replace />;
  return <Navigate to={usuario.rol === 'ADMIN' ? '/admin' : '/inicio'} replace />;
}

export default function App() {
  return (
    <AvisoProvider>
      <SesionProvider>
        <ConfirmDialog />
        <Routes>
          <Route path="/" element={<RutaInicial />} />
          <Route path="/login" element={<Login />} />
          <Route path="/registro" element={<Registro />} />
          <Route element={<RutaProtegida><Layout /></RutaProtegida>}>
            <Route path="/inicio" element={<RutaProtegida rol="USUARIO"><Inicio /></RutaProtegida>} />
            <Route path="/movimientos" element={<RutaProtegida rol="USUARIO"><Movimientos /></RutaProtegida>} />
            <Route path="/presupuestos" element={<RutaProtegida rol="USUARIO"><Presupuestos /></RutaProtegida>} />
            <Route path="/metas" element={<RutaProtegida rol="USUARIO"><Metas /></RutaProtegida>} />
            <Route path="/reportes" element={<RutaProtegida rol="USUARIO"><Reportes /></RutaProtegida>} />
            <Route path="/perfil" element={<Perfil />} />
            <Route path="/admin" element={<RutaProtegida rol="ADMIN"><Usuarios /></RutaProtegida>} />
          </Route>
          <Route path="*" element={<NoEncontrado />} />
        </Routes>
      </SesionProvider>
    </AvisoProvider>
  );
}
