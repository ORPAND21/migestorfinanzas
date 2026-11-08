import cliente from '../api/cliente';

export const reporteService = {
  panel: () => cliente.get('/reportes/panel').then((r) => r.data),
  mensual: (anio, mes) => cliente.get('/reportes/mensual', { params: { anio, mes } }).then((r) => r.data),
  anual: (anio) => cliente.get('/reportes/anual', { params: { anio } }).then((r) => r.data)
};
