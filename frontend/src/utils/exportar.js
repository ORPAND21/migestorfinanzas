/** Descarga filas como archivo CSV que Excel abre con tildes correctas. */
export function descargarCsv(nombreArchivo, encabezados, filas) {
  const escapar = (valor) => {
    const texto = valor === null || valor === undefined ? '' : String(valor);
    return /[";\n]/.test(texto) ? `"${texto.replace(/"/g, '""')}"` : texto;
  };
  const lineas = [encabezados, ...filas].map((fila) => fila.map(escapar).join(';'));
  const blob = new Blob(['\uFEFF' + lineas.join('\n')], { type: 'text/csv;charset=utf-8' });
  const enlace = document.createElement('a');
  enlace.href = URL.createObjectURL(blob);
  enlace.download = nombreArchivo;
  enlace.click();
  URL.revokeObjectURL(enlace.href);
}

/** Abre el diálogo de impresión; el usuario elige "Guardar como PDF". */
export function exportarPdf() {
  window.print();
}
