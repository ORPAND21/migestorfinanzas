import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from 'primereact/button';
import { Message } from 'primereact/message';
import { ProgressSpinner } from 'primereact/progressspinner';
import Encabezado from '../components/Encabezado';
import { reporteService } from '../services/reporteService';
import { mensajeError } from '../api/cliente';
import { useSesion } from '../context/SesionContext';
import { fechaCorta, MESES, pesos } from '../utils/formato';

/** Panel de inicio: saldo disponible, mes actual, alertas y últimos movimientos. */
export default function Inicio() {
  const { usuario } = useSesion();
  const navigate = useNavigate();
  const [panel, setPanel] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    reporteService.panel().then(setPanel).catch((e) => setError(mensajeError(e)));
  }, []);

  if (error) return <Message severity="error" text={error} />;
  if (!panel) return <div className="cargando"><ProgressSpinner /></div>;

  const { resumen } = panel;
  const ingresos = Number(resumen.totalIngresos);
  // Reparto de todo lo que ha entrado: gastado, apartado en metas y disponible
  const parte = (valor) => (ingresos > 0 ? Math.max(0, (Number(valor) / ingresos) * 100) : 0);
  const hoy = new Date();

  return (
    <>
      <Encabezado titulo={`Hola, ${usuario.nombre.split(' ')[0]}`} descripcion="Así están tus finanzas hoy.">
        <Button label="Registrar ingreso" icon="pi pi-plus" severity="success" outlined
                onClick={() => navigate('/movimientos?nuevo=INGRESO')} />
        <Button label="Registrar gasto" icon="pi pi-minus" severity="danger" outlined
                onClick={() => navigate('/movimientos?nuevo=GASTO')} />
      </Encabezado>

      <section className="saldo">
        <span className="saldo-etiqueta">Saldo disponible</span>
        <strong className={`saldo-valor ${Number(resumen.saldoDisponible) < 0 ? 'negativo' : ''}`}>
          {pesos(resumen.saldoDisponible)}
        </strong>
        <div className="reparto" role="img"
             aria-label={`De ${pesos(ingresos)} recibidos: ${pesos(resumen.totalGastos)} gastados, ${pesos(resumen.totalAhorrado)} en metas`}>
          <span className="reparto-gasto" style={{ width: `${parte(resumen.totalGastos)}%` }} />
          <span className="reparto-ahorro" style={{ width: `${parte(resumen.totalAhorrado)}%` }} />
          <span className="reparto-libre" style={{ width: `${parte(resumen.saldoDisponible)}%` }} />
        </div>
        <dl className="reparto-leyenda">
          <div><dt><i className="punto gasto" />Gastado</dt><dd>{pesos(resumen.totalGastos)}</dd></div>
          <div><dt><i className="punto ahorro" />En metas de ahorro</dt><dd>{pesos(resumen.totalAhorrado)}</dd></div>
          <div><dt><i className="punto libre" />Disponible</dt><dd>{pesos(resumen.saldoDisponible)}</dd></div>
          <div><dt>Total recibido</dt><dd>{pesos(resumen.totalIngresos)}</dd></div>
        </dl>
      </section>

      <div className="rejilla-dos">
        <section className="bloque">
          <h2>Este mes: {MESES[hoy.getMonth()]} de {hoy.getFullYear()}</h2>
          <div className="mes-cifras">
            <div><span>Ingresos</span><strong className="texto-ingreso">{pesos(panel.ingresosMes)}</strong></div>
            <div><span>Gastos</span><strong className="texto-gasto">{pesos(panel.gastosMes)}</strong></div>
            <div><span>Balance</span><strong>{pesos(Number(panel.ingresosMes) - Number(panel.gastosMes))}</strong></div>
            <div><span>Metas en progreso</span><strong>{panel.metasEnProgreso}</strong></div>
          </div>
          <h3>Alertas de presupuesto</h3>
          {panel.alertas.length === 0
            ? <p className="vacio-texto">Tus gastos de este mes están dentro de los presupuestos.</p>
            : panel.alertas.map((a) => (
                <Message key={a.mensaje} severity={a.nivel === 'EXCEDIDO' ? 'error' : 'warn'} text={a.mensaje} className="alerta" />
              ))}
        </section>

        <section className="bloque">
          <div className="bloque-titulo">
            <h2>Últimos movimientos</h2>
            <Button label="Ver todos" link onClick={() => navigate('/movimientos')} />
          </div>
          {panel.ultimosMovimientos.length === 0 ? (
            <p className="vacio-texto">Todavía no registras movimientos. Empieza con tu salario o tu primer gasto.</p>
          ) : (
            <ul className="lista-movimientos">
              {panel.ultimosMovimientos.map((m) => (
                <li key={m.id}>
                  <div>
                    <strong>{m.descripcion || m.categoriaNombre}</strong>
                    <small>{m.categoriaNombre}, {fechaCorta(m.fecha)}</small>
                  </div>
                  <span className={m.tipo === 'INGRESO' ? 'texto-ingreso' : 'texto-gasto'}>
                    {m.tipo === 'INGRESO' ? '+' : '−'}{pesos(m.monto)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </>
  );
}
