# MiGestorFinanzas

Sistema web de gestión de finanzas personales. Permite registrar ingresos y gastos por categoría, definir presupuestos mensuales con alertas, crear metas de ahorro con aportes y consultar reportes mensuales y anuales exportables. Incluye un rol de administrador para supervisar las cuentas.

Proyecto final de la Tecnología en Análisis y Desarrollo de Software (ADSO), SENA.

## Tecnologías

| Capa | Herramientas |
|---|---|
| Frontend | React 18, Vite 5, PrimeReact 10, PrimeIcons, React Router 6, Axios, Chart.js |
| Backend | Java 21, Spring Boot 3.5.6 (Web, Data JPA), Maven |
| Base de datos | MySQL 8 / MariaDB (XAMPP) |
| Pruebas | JUnit 5 y Mockito |

## Estructura

```
MiGestorFinanzas/
├── backend/                     API REST en Spring Boot
│   └── src/main/java/com/migestor/finanzas/
│       ├── controlador/         Endpoints REST (/api/...)
│       ├── servicio/            Reglas de negocio y validaciones
│       ├── repositorio/         Acceso a datos (Spring Data JPA)
│       ├── modelo/              Entidades JPA
│       ├── dto/                 Objetos de entrada y salida
│       ├── seguridad/           Cifrado de contraseñas, sesiones e interceptor
│       ├── excepcion/           Excepciones y manejador de errores
│       └── config/              CORS y datos iniciales
├── frontend/                    Aplicación React
│   └── src/
│       ├── pages/               Pantallas (una por módulo)
│       ├── components/          Menú lateral, rutas protegidas, encabezado
│       ├── services/            Llamadas a la API por módulo
│       ├── context/             Sesión y mensajes emergentes
│       ├── api/                 Cliente HTTP (Axios)
│       └── utils/               Formatos, validaciones y exportación
└── database/
    ├── 01_esquema.sql           Creación de la base de datos y tablas
    └── 02_datos_prueba.sql      Datos de demostración
```

## Requisitos

- JDK 21
- Apache NetBeans (o cualquier IDE con Maven)
- XAMPP con MySQL en el puerto 3306, usuario `root` sin contraseña
- Node.js 18 o superior

## Instalación y ejecución

### 1. Base de datos

1. Inicia **MySQL** en el panel de XAMPP.
2. Abre phpMyAdmin (`http://localhost/phpmyadmin`), entra a la pestaña **SQL**.
3. Ejecuta el contenido de `database/01_esquema.sql`.
4. Ejecuta el contenido de `database/02_datos_prueba.sql` (opcional, para tener datos de demostración).

Si te saltas este paso, el backend crea la base, las tablas, las categorías y el administrador al arrancar.

### 2. Backend

1. En NetBeans: **Archivo > Abrir proyecto** y elige la carpeta `backend`.
2. Clic derecho en el proyecto > **Run**. Espera el mensaje `Tomcat started on port 8080`.

Desde la terminal también puedes usar `mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).

Si tu MySQL tiene contraseña, cámbiala en `backend/src/main/resources/application.properties`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`.

## Usuarios de prueba

| Rol | Correo | Contraseña | Nota |
|---|---|---|---|
| Administrador | admin@migestor.com | Admin123* | Gestiona usuarios |
| Usuario | laura@demo.com | Demo123* | Tiene movimientos, presupuestos y metas |
| Usuario | carlos@demo.com | Demo123* | Cuenta desactivada: no puede iniciar sesión |

## Módulos

| Módulo | Funciones | Requisitos |
|---|---|---|
| Usuarios y acceso | Registro, inicio y cierre de sesión, perfil, cambio de contraseña | RF001, CU01, CU02, CU08 |
| Ingresos y gastos | Registrar, consultar con filtros, editar y eliminar movimientos | RF002, RF003, CU03, CU04, CU06 |
| Presupuestos | Límite mensual por categoría, seguimiento y alertas al 80 % y al superar | RF006 |
| Metas de ahorro | Crear, editar, eliminar, aportar y ver el historial de aportes | RF005, CU05 |
| Reportes | Gráfico de ingresos frente a gastos, gastos por categoría, reporte anual, exportar CSV y PDF | RF004, CU07 |
| Administración | Listar usuarios, activar o desactivar cuentas, estadísticas del sistema | CU09, CU10 |

## API REST

Todas las rutas, excepto registro e inicio de sesión, requieren el encabezado `Authorization: Bearer {token}`. Los errores responden `{"mensaje": "..."}` con código 400, 401, 403 o 404.

| Método | Ruta | Descripción |
|---|---|---|
| POST | /api/auth/registro | Crear cuenta |
| POST | /api/auth/login | Iniciar sesión (devuelve el token) |
| POST | /api/auth/logout | Cerrar sesión |
| GET, PUT | /api/perfil | Consultar y editar el perfil |
| PUT | /api/perfil/password | Cambiar contraseña |
| GET | /api/categorias?tipo= | Categorías de ingreso o gasto |
| GET, POST | /api/transacciones | Listar (filtros tipo, categoriaId, desde, hasta) y registrar |
| PUT, DELETE | /api/transacciones/{id} | Editar y eliminar |
| GET | /api/transacciones/resumen | Totales y saldo disponible |
| GET, POST | /api/presupuestos | Listar por anio y mes, crear |
| PUT, DELETE | /api/presupuestos/{id} | Editar y eliminar |
| GET, POST | /api/metas | Listar y crear metas |
| PUT, DELETE | /api/metas/{id} | Editar y eliminar |
| GET, POST | /api/metas/{id}/aportes | Historial de aportes y nuevo aporte |
| GET | /api/reportes/panel | Datos del panel de inicio |
| GET | /api/reportes/mensual?anio=&mes= | Reporte mensual |
| GET | /api/reportes/anual?anio= | Reporte anual |
| GET | /api/admin/usuarios | Listar usuarios (solo ADMIN) |
| PUT | /api/admin/usuarios/{id}/estado | Activar o desactivar (solo ADMIN) |
| GET | /api/admin/estadisticas | Estadísticas del sistema (solo ADMIN) |

## Reglas de negocio principales

- El correo es único y la contraseña exige mínimo 8 caracteres con mayúscula, minúscula, número y símbolo. Se guarda cifrada con PBKDF2.
- Cada usuario solo ve y modifica sus propios datos.
- Los montos deben ser mayores que cero y tener máximo dos decimales.
- La fecha de un movimiento no puede ser futura y su categoría debe coincidir con el tipo (ingreso o gasto).
- Solo hay un presupuesto por categoría de gasto y mes. Se alerta al llegar al 80 % y al superar el límite, si el usuario tiene las alertas activadas.
- Saldo disponible = ingresos − gastos − dinero apartado en metas.
- Un aporte no puede superar el saldo disponible ni lo que falta para completar la meta. Al eliminar una meta, lo ahorrado vuelve al saldo.
- Una cuenta desactivada no puede iniciar sesión, y el administrador no puede desactivarse a sí mismo.

## Pruebas unitarias

```bash
cd backend
mvnw test
```

En NetBeans: clic derecho en el proyecto > **Test**. Las pruebas están en `backend/src/test/java` y cubren validaciones, cifrado de contraseñas, estados de presupuestos y metas, aportes, registro de movimientos, saldo e inicio de sesión.

## Problemas comunes

| Síntoma | Solución |
|---|---|
| "No hay conexión con el servidor" en la app | El backend no está corriendo o no está en el puerto 8080 |
| `Communications link failure` al iniciar el backend | MySQL no está iniciado en XAMPP |
| `Access denied for user 'root'` | Tu MySQL tiene contraseña: ponla en `application.properties` |
| Error de CORS en el navegador | Abre el frontend en `http://localhost:5173`, no en otro puerto |
| Te devuelve al inicio de sesión | La sesión venció (8 horas) o se reinició el backend: vuelve a entrar |
