import cliente from '../api/cliente';

export const metaService = {
  listar: () => cliente.get('/metas').then((r) => r.data),
  crear: (datos) => cliente.post('/metas', datos).then((r) => r.data),
  actualizar: (id, datos) => cliente.put(`/metas/${id}`, datos).then((r) => r.data),
  eliminar: (id) => cliente.delete(`/metas/${id}`).then((r) => r.data),
  aportar: (id, monto) => cliente.post(`/metas/${id}/aportes`, { monto }).then((r) => r.data),
  aportes: (id) => cliente.get(`/metas/${id}/aportes`).then((r) => r.data)
};
