// Mismas reglas que valida el backend, para avisar antes de enviar el formulario.
export const REGLA_PASSWORD = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;
export const REGLA_EMAIL = /^[\w.+-]+@[\w-]+(\.[\w-]+)*\.[A-Za-z]{2,}$/;

export const TEXTO_PASSWORD = 'Mínimo 8 caracteres con mayúscula, minúscula, número y símbolo.';

export function validarMonto(valor, campo = 'monto') {
  if (valor === null || valor === undefined || valor === '') return `Ingresa el ${campo}.`;
  if (Number(valor) <= 0) return `El ${campo} debe ser mayor que cero.`;
  return null;
}
