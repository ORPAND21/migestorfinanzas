import { createContext, useContext, useRef } from 'react';
import { Toast } from 'primereact/toast';

const AvisoContext = createContext(null);

/** Mensajes emergentes de confirmación, advertencia y error. */
export function AvisoProvider({ children }) {
  const toast = useRef(null);
  const mostrar = (severity, summary, detail, life = 4000) =>
    toast.current?.show({ severity, summary, detail, life });

  const aviso = {
    exito: (detalle) => mostrar('success', 'Listo', detalle),
    error: (detalle) => mostrar('error', 'No se pudo completar', detalle, 6000),
    advertencia: (detalle) => mostrar('warn', 'Atención', detalle, 8000),
    peligro: (detalle) => mostrar('error', 'Presupuesto superado', detalle, 9000)
  };

  return (
    <AvisoContext.Provider value={aviso}>
      <Toast ref={toast} position="top-right" />
      {children}
    </AvisoContext.Provider>
  );
}

export const useAviso = () => useContext(AvisoContext);
