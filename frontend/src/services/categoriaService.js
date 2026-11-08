import cliente from '../api/cliente';

export const categoriaService = {
  listar: (tipo) => cliente.get('/categorias', { params: { tipo } }).then((r) => r.data)
};
