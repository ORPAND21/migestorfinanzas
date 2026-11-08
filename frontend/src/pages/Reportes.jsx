import { useEffect, useState } from 'react';
import { Button } from 'primereact/button';
import { Calendar } from 'primereact/calendar';
import { Chart } from 'primereact/chart';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { TabView, TabPanel } from 'primereact/tabview';
import Encabezado from '../components/Encabezado';
import { reporteService } from '../services/reporteService';
import { mensajeError } from '../api/cliente';
import { useAviso } from '../context/AvisoContext';
import { useSesion } from '../context/SesionContext';
import { descargarCsv, exportarPdf } from '../utils/exportar';
import { fechaCorta, MESES, nombreMes, pesos } from '../utils/formato';

const VERDE = '#2E8B57';
const ROJO = '#C8423B';
const PALETA = ['#1D5FA8', '#2E8B57', '#E0A100', '#C8423B', '#6C5BB5', '#1F9AA8', '#B5651D', '#7A8794'];

const opcionesBarras = {
  maintainAspectRatio: false,
  plugins: { legend: { position: 'bottom' }, tooltip: { callbacks: { label: (c) => `${c.dataset.label}: ${pesos(c.raw)}` } } },
  scales: { y: { beginAtZero: true, ticks: { callback: (v) => pesos(v) } } }
};

/** RF004: gráficos de flujo de dinero y reportes mensual y anual exportables. */
export default function Reportes() {
  const aviso = useAviso();
  const { usuario } = useSesion();
  const [periodo, setPeriodo] = useState(new Date());
  const [anioAnual, setAnioAnual] = useState(new Date());
  const [mensual, setMensual] = useState(null);
  const [anual, setAnual] = useState(null);
  const [pestana, setPestana] = useState(0);

  const anio = periodo.getFullYear();
  const mes = periodo.getMonth() + 1;
  const anioSel = anioAnual.getFullYear();

  useEffect(() => {
    reporteService.mensual(anio, mes).then(setMensual).catch((e) => aviso.error(mensajeError(e)));
  }, [anio, mes]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    reporteService.anual(anioSel).then(setAnual).catch((e) => aviso.error(mensajeError(e)));
  }, [anioSel]); // eslint-disable-line react-hooks/exhaustive-deps

  const exportarMensualCsv = () => descargarCsv(`reporte-${anio}-${String(mes).padStart(2, '0')}.csv`,
    ['Fecha', 'Tipo', 'Categoría', 'Descripción', 'Monto'],
    mensual.movimientos.map((m) => [m.fecha, m.tipo === 'INGRESO' ? 'Ingreso' : 'Gasto', m.categoriaNombre, m.descripcion || '', m.monto]));

  const exportarAnualCsv = () => descargarCsv(`reporte-anual-${anioSel}.csv`,
    ['Mes', 'Ingresos', 'Gastos', 'Balance'],
    [...anual.meses.map((m) => [MESES[m.mes - 1], m.ingresos, m.gastos, m.balance]),
      ['Total', anual.totalIngresos, anual.totalGastos, anual.balance]]);

  const graficoMes = mensual && {
    labels: [nombreMes(mes, anio)],
    datasets: [
      { label: 'Ingresos', data: [Number(mensual.totalIngresos)], backgroundColor: VERDE },
      { label: 'Gastos', data: [Number(mensual.totalGastos)], backgroundColor: ROJO }
    ]
  };
  const graficoCategorias = mensual && {
    labels: mensual.gastosPorCategoria.map((c) => c.categoria),
    datasets: [{ data: mensual.gastosPorCategoria.map((c) => Number(c.total)), backgroundColor: PALETA }]
  };
  const graficoAnual = anual && {
    labels: MESES.map((m) => m.slice(0, 3)),
    datasets: [
      { label: 'Ingresos', data: anual.meses.map((m) => Number(m.ingresos)), backgroundColor: VERDE },
      { label: 'Gastos', data: anual.meses.map((m) => Number(m.gastos)), backgroundColor: ROJO }
    ]
  };

  return (
    <>
      <Encabezado titulo="Reportes" descripcion="Compara lo que entra con lo que sale y descarga el reporte.">
        {pestana === 0 && mensual && (
          <>
            <Button label="Exportar CSV" icon="pi pi-file-excel" outlined onClick={exportarMensualCsv} />
            <Button label="Exportar PDF" icon="pi pi-file-pdf" outlined onClick={exportarPdf} />
          </>
        )}
        {pestana === 1 && anual && (
          <>
            <Button label="Exportar CSV" icon="pi pi-file-excel" outlined onClick={exportarAnualCsv} />
            <Button label="Exportar PDF" icon="pi pi-file-pdf" outlined onClick={exportarPdf} />
          </>
        )}
      </Encabezado>

      <p className="solo-imprimir">MiGestorFinanzas. Reporte de {usuario?.nombre}, generado el {fechaCorta(new Date().toISOString())}.</p>

      <TabView activeIndex={pestana} onTabChange={(e) => setPestana(e.index)}>
        <TabPanel header="Reporte mensual">
          <div className="barra-periodo no-imprimir">
            <label htmlFor="r-mes">Mes</label>
            <Calendar inputId="r-mes" value={periodo} onChange={(e) => e.value && setPeriodo(e.value)} view="month"
                      dateFormat="MM 'de' yy" showIcon className="selector-mes" />
          </div>
          {mensual && (
            <>
              <h2 className="titulo-reporte">Reporte de {nombreMes(mes, anio)}</h2>
              <div className="cifras">
                <div><span>Ingresos</span><strong className="texto-ingreso">{pesos(mensual.totalIngresos)}</strong></div>
                <div><span>Gastos</span><strong className="texto-gasto">{pesos(mensual.totalGastos)}</strong></div>
                <div><span>Ahorro neto del mes</span>
                  <strong className={Number(mensual.ahorroNeto) < 0 ? 'texto-gasto' : ''}>{pesos(mensual.ahorroNeto)}</strong></div>
              </div>
              {mensual.movimientos.length === 0 ? (
                <p className="vacio-texto">No hay movimientos en {nombreMes(mes, anio)}.</p>
              ) : (
                <>
                  <div className="rejilla-dos">
                    <section className="bloque">
                      <h3>Ingresos frente a gastos</h3>
                      <div className="grafico"><Chart type="bar" data={graficoMes} options={opcionesBarras} style={{ height: '100%' }} /></div>
                    </section>
                    <section className="bloque">
                      <h3>Gastos por categoría</h3>
                      {mensual.gastosPorCategoria.length === 0
                        ? <p className="vacio-texto">Sin gastos este mes.</p>
                        : <div className="grafico"><Chart type="doughnut" data={graficoCategorias}
                                 options={{ maintainAspectRatio: false, plugins: { legend: { position: 'right' } } }} style={{ height: '100%' }} /></div>}
                    </section>
                  </div>
                  <section className="bloque">
                    <h3>Detalle de gastos por categoría</h3>
                    <DataTable value={mensual.gastosPorCategoria} size="small" className="tabla">
                      <Column field="categoria" header="Categoría" />
                      <Column field="total" header="Total" align="right" body={(c) => pesos(c.total)} />
                      <Column field="porcentaje" header="% del gasto" align="right" body={(c) => `${c.porcentaje} %`} />
                    </DataTable>
                  </section>
                </>
              )}
            </>
          )}
        </TabPanel>

        <TabPanel header="Reporte anual">
          <div className="barra-periodo no-imprimir">
            <label htmlFor="r-anio">Año</label>
            <Calendar inputId="r-anio" value={anioAnual} onChange={(e) => e.value && setAnioAnual(e.value)} view="year"
                      dateFormat="yy" showIcon className="selector-mes" />
          </div>
          {anual && (
            <>
              <h2 className="titulo-reporte">Reporte anual {anioSel}</h2>
              <div className="cifras">
                <div><span>Ingresos del año</span><strong className="texto-ingreso">{pesos(anual.totalIngresos)}</strong></div>
                <div><span>Gastos del año</span><strong className="texto-gasto">{pesos(anual.totalGastos)}</strong></div>
                <div><span>Balance</span><strong className={Number(anual.balance) < 0 ? 'texto-gasto' : ''}>{pesos(anual.balance)}</strong></div>
              </div>
              <section className="bloque">
                <h3>Flujo de dinero por mes</h3>
                <div className="grafico grafico-ancho"><Chart type="bar" data={graficoAnual} options={opcionesBarras} style={{ height: '100%' }} /></div>
              </section>
              <section className="bloque">
                <DataTable value={anual.meses} size="small" className="tabla">
                  <Column header="Mes" body={(m) => MESES[m.mes - 1]} />
                  <Column header="Ingresos" align="right" body={(m) => pesos(m.ingresos)} />
                  <Column header="Gastos" align="right" body={(m) => pesos(m.gastos)} />
                  <Column header="Balance" align="right" body={(m) => (
                    <span className={Number(m.balance) < 0 ? 'texto-gasto' : ''}>{pesos(m.balance)}</span>
                  )} />
                </DataTable>
              </section>
            </>
          )}
        </TabPanel>
      </TabView>
    </>
  );
}
