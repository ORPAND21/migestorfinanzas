import cliente from '../api/cliente';

export const presupuestoService = {
  listar: (anio, mes) => cliente.get('/presupuestos', { params: { anio, mes } }).then((r) => r.data),
  crear: (datos) => cliente.post('/presupuestos', datos).then((r) => r.data),
  actualizar: (id, datos) => cliente.put(`/presupuestos/${id}`, datos).then((r) => r.data),
  eliminar: (id) => cliente.delete(`/presupuestos/${id}`).then((r) => r.data)
};
