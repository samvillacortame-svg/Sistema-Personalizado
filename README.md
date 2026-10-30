# Sistema personalizado de gestión comercial

Prototipo académico para centralizar clientes, pedidos e inventario en una empresa importadora de dermocosméticos.

## Componentes
- frontend: React + Vite.
- backend: API REST con Spring Boot, Java 17 y Maven Wrapper.
- database: esquema inicial MySQL.
- MySQL Workbench puede conectarse al servidor local o al MySQL del contenedor.

## Requisitos locales
- Docker Desktop con WSL 2 habilitado.
- Para desarrollo sin contenedores: JDK 17+, Node.js 20.19+, pnpm y MySQL Server 8.

En el instalador de Windows, elige **Per-user installation** y deja **WSL 2** seleccionado. El proyecto usa contenedores Linux; Windows Containers no hace falta.

## Ejecutar con Docker Compose
1. Copia `.env.example` a `.env` en la raíz del repositorio. Los valores incluidos son solo para desarrollo local.
2. Desde la raíz del repositorio, ejecuta `docker compose up --build -d`.
3. Abre `http://localhost:8081`. El panel consultará la API y la base de datos.
4. Para revisar los servicios, ejecuta `docker compose ps`; para ver sus registros, `docker compose logs -f`.
5. Para detenerlos sin borrar la base de datos, ejecuta `docker compose down`.

MySQL también queda disponible para Workbench en `127.0.0.1:3308`, base `gestion_comercial`, usando las credenciales de `.env`. La base de datos se guarda en un volumen Docker local. Ese volumen no se comparte mediante Git; cada PC tendrá su propia copia de los datos.

El archivo `database/schema.sql` crea el esquema inicial la primera vez que se crea el volumen. El API aplica automáticamente las migraciones pendientes desde `backend/src/main/resources/db/migration`, tanto en bases nuevas como en volúmenes existentes. No borres el volumen para actualizar el esquema: los datos se conservan. Borrar el volumen sí elimina toda la base local.

## Ejecutar sin contenedores
1. En MySQL Workbench, abre y ejecuta `database/schema.sql` para crear la base y las tablas iniciales.
2. En PowerShell, dentro de `backend`, ejecuta `Copy-Item .env.example .env`.
3. Edita `backend/.env` con la cuenta MySQL local. La URL de conexión es `jdbc:mysql://localhost:3306/gestion_comercial`.
4. En una terminal, dentro de `backend`, ejecuta `./run-local.ps1`.
5. En otra terminal, dentro de `frontend`, ejecuta `pnpm install` y luego `pnpm dev`.

Vite reenvía solicitudes /api a http://localhost:8080. El endpoint público /api/estado ejecuta SELECT 1 para revisar la conexión MySQL.

## Roles del prototipo
La API usa HTTP Basic en desarrollo. Configura usuarios en el archivo local:
- APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD
- APP_VENTAS_USERNAME / APP_VENTAS_PASSWORD
- APP_ALMACEN_USERNAME / APP_ALMACEN_PASSWORD

Las rutas /api/admin/**, /api/ventas/** y /api/almacen/** exigen el rol correspondiente. En Docker Compose, las variables se definen en el `.env` de la raíz. El `.env.example` incluye credenciales ficticias solo para la demostración académica; reemplázalas fuera del entorno local.

En la demostración local, ingresa con `ventas_demo` y la contraseña `ventas_local_demo`. Las credenciales son ficticias para el entorno académico; los cambios en Docker requieren recrear la API con `docker compose up -d --force-recreate api`.

## Alcance inicial
CRM básico, pedidos, inventario, movimientos y reportes. Los requisitos se validarán con la empresa. Integraciones automáticas con WhatsApp, Facebook, pagos y facturación quedan fuera hasta confirmar APIs, permisos y viabilidad.

## Sprint 3: pedidos e inventario
Ventas puede registrar pedidos vinculados con clientes activos y productos del catálogo. Al confirmar un pedido, el sistema valida disponibilidad y reserva el stock dentro de una transacción. Almacén registra productos y entradas o ajustes, inicia la preparación y despacha pedidos; el despacho descuenta las unidades del inventario. Los movimientos conservan el motivo y la referencia al pedido.

Para la demostración, entra como `almacen_demo` / `almacen_local_demo`, registra un producto con stock inicial y cierra sesión. Luego entra como `ventas_demo` / `ventas_local_demo`, registra un cliente si aún no existe y crea un pedido. Finalmente vuelve a entrar como almacén para preparar y despachar. El inventario muestra stock físico, reservado y disponible.

## Sprint 4: reportes de gestión
El panel de reportes permite filtrar por fechas y consultar ventas de pedidos despachados, pedidos por estado, ticket promedio, ventas por canal y día, productos más vendidos y alertas de stock bajo. Los roles de ventas y almacén pueden consultar el panel.
