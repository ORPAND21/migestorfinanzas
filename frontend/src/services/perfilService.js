import cliente from '../api/cliente';

export const perfilService = {
  obtener: () => cliente.get('/perfil').then((r) => r.data),
  actualizar: (datos) => cliente.put('/perfil', datos).then((r) => r.data),
  cambiarPassword: (datos) => cliente.put('/perfil/password', datos).then((r) => r.data)
};
