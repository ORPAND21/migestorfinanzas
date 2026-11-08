import cliente from '../api/cliente';

export const transaccionService = {
  listar: (filtros) => cliente.get('/transacciones', { params: filtros }).then((r) => r.data),
  resumen: () => cliente.get('/transacciones/resumen').then((r) => r.data),
  crear: (datos) => cliente.post('/transacciones', datos).then((r) => r.data),
  actualizar: (id, datos) => cliente.put(`/transacciones/${id}`, datos).then((r) => r.data),
  eliminar: (id) => cliente.delete(`/transacciones/${id}`).then((r) => r.data)
};
