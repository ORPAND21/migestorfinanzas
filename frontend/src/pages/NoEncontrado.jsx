import { Link } from 'react-router-dom';

export default function NoEncontrado() {
  return (
    <div className="no-encontrado">
      <img src="/logo.png" alt="" />
      <h1>Esta página no existe</h1>
      <p>Revisa la dirección o vuelve al inicio.</p>
      <Link to="/">Ir al inicio</Link>
    </div>
  );
}
