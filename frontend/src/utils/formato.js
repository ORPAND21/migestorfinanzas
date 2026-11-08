export const MESES = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto',
  'septiembre', 'octubre', 'noviembre', 'diciembre'];

const moneda = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

/** 1250000 -> "$ 1.250.000" */
export const pesos = (valor) => moneda.format(Number(valor || 0));

/** "2026-11-08" -> "8 nov 2026" */
export function fechaCorta(iso) {
  if (!iso) return '';
  const [a, m, d] = iso.slice(0, 10).split('-').map(Number);
  return `${d} ${MESES[m - 1].slice(0, 3)} ${a}`;
}

/** Date -> "2026-11-08" usando la fecha local (sin desfase por zona horaria). */
export function aIso(fecha) {
  if (!fecha) return null;
  const m = String(fecha.getMonth() + 1).padStart(2, '0');
  const d = String(fecha.getDate()).padStart(2, '0');
  return `${fecha.getFullYear()}-${m}-${d}`;
}

/** "2026-11-08" -> Date local */
export function deIso(iso) {
  if (!iso) return null;
  const [a, m, d] = iso.slice(0, 10).split('-').map(Number);
  return new Date(a, m - 1, d);
}

export const nombreMes = (mes, anio) => `${MESES[mes - 1]} de ${anio}`;
