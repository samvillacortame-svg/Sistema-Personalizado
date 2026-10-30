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

MySQL también queda disponible para Workbench en `127.0.0.1:3307`, base `gestion_comercial`, usando las credenciales de `.env`. La base de datos se guarda en un volumen Docker local. Ese volumen no se comparte mediante Git; cada PC tendrá su propia copia de los datos.

El archivo `database/schema.sql` se carga automáticamente la primera vez que se crea el volumen. Si posteriormente cambias el esquema, aplica la migración o importa el SQL manualmente en Workbench. Borrar el volumen también elimina los datos guardados.

## Ejecutar sin contenedores
1. En MySQL Workbench, abre y ejecuta `database/schema.sql` para crear la base y las tablas iniciales.
2. En PowerShell, entra a `backend` y ejecuta `Copy-Item .env.example .env`.
3. Edita `backend/.env` con la cuenta MySQL local. La URL de conexión es `jdbc:mysql://localhost:3306/gestion_comercial`.
4. En una terminal, dentro de `backend`, ejecuta `./run-local.ps1`.
5. En otra terminal, dentro de `frontend`, ejecuta `pnpm install` y luego `pnpm dev`.

Vite reenvía solicitudes /api a http://localhost:8080. El endpoint público /api/estado ejecuta SELECT 1 para revisar la conexión MySQL.

## Roles del prototipo
La API usa HTTP Basic en desarrollo. Configura usuarios en el archivo local:
- APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD
- APP_VENTAS_USERNAME / APP_VENTAS_PASSWORD
- APP_ALMACEN_USERNAME / APP_ALMACEN_PASSWORD

Las rutas /api/admin/**, /api/ventas/** y /api/almacen/** exigen el rol correspondiente. Sin usuarios configurados, el endpoint de estado permanece público y las demás rutas quedan protegidas. En Docker Compose, las variables se definen en el `.env` de la raíz.

## Alcance inicial
CRM básico, pedidos, inventario, movimientos y reportes. Los requisitos se validarán con la empresa. Integraciones automáticas con WhatsApp, Facebook, pagos y facturación quedan fuera hasta confirmar APIs, permisos y viabilidad.
