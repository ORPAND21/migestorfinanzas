import cliente from '../api/cliente';

export const authService = {
  registrar: (datos) => cliente.post('/auth/registro', datos).then((r) => r.data),
  login: (datos) => cliente.post('/auth/login', datos).then((r) => r.data),
  logout: () => cliente.post('/auth/logout').then((r) => r.data)
};
