import cliente from '../api/cliente';

export const adminService = {
  usuarios: () => cliente.get('/admin/usuarios').then((r) => r.data),
  cambiarEstado: (id, activo) => cliente.put(`/admin/usuarios/${id}/estado`, { activo }).then((r) => r.data),
  estadisticas: () => cliente.get('/admin/estadisticas').then((r) => r.data)
};
