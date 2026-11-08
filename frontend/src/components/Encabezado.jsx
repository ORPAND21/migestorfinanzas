/** Título de cada pantalla con su descripción y acciones a la derecha. */
export default function Encabezado({ titulo, descripcion, children }) {
  return (
    <header className="encabezado">
      <div>
        <h1>{titulo}</h1>
        {descripcion && <p>{descripcion}</p>}
      </div>
      {children && <div className="encabezado-acciones no-imprimir">{children}</div>}
    </header>
  );
}
