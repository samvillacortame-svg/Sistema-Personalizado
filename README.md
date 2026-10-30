# Sistema personalizado de gestión comercial

Prototipo académico para centralizar clientes, pedidos e inventario en una empresa importadora de dermocosméticos.

## Componentes
- frontend: React + Vite.
- backend: API REST con Spring Boot, Java 17 y Maven Wrapper.
- database: esquema inicial MySQL.
- MySQL Workbench administra el servidor MySQL; MySQL Server debe estar activo.

## Requisitos locales
- JDK 17 o superior.
- Node.js 20.19+ y pnpm.
- MySQL Server 8 en ejecución.

## Preparación
1. En MySQL Workbench, abre y ejecuta `database/schema.sql` para crear la base y las tablas iniciales.
2. En PowerShell, entra a `backend` y ejecuta `Copy-Item .env.example .env`.
3. Edita `backend/.env` con el usuario y contraseña locales de MySQL. La URL de conexión es `jdbc:mysql://localhost:3306/gestion_comercial`.
4. Para habilitar las rutas de demostración, agrega contraseñas locales para APP_ADMIN, APP_VENTAS y APP_ALMACEN.
5. No subas `backend/.env`; está excluido por `.gitignore`. Comparte las credenciales con el equipo por un medio privado.

## Ejecutar en desarrollo
En una terminal de PowerShell:
- Entra a backend y ejecuta .\run-local.ps1

En otra terminal:
- Entra a frontend
- Ejecuta pnpm install
- Ejecuta pnpm dev

Vite reenvía solicitudes /api a http://localhost:8080. El endpoint público /api/estado ejecuta SELECT 1 para revisar la conexión MySQL.

## Roles del prototipo
La API usa HTTP Basic en desarrollo. Configura usuarios en el archivo local:
- APP_ADMIN_USERNAME / APP_ADMIN_PASSWORD
- APP_VENTAS_USERNAME / APP_VENTAS_PASSWORD
- APP_ALMACEN_USERNAME / APP_ALMACEN_PASSWORD

Las rutas /api/admin/**, /api/ventas/** y /api/almacen/** exigen el rol correspondiente. Sin usuarios configurados, el endpoint de estado permanece público y las demás rutas quedan protegidas.

## Alcance inicial
CRM básico, pedidos, inventario, movimientos y reportes. Los requisitos se validarán con la empresa. Integraciones automáticas con WhatsApp, Facebook, pagos y facturación quedan fuera hasta confirmar APIs, permisos y viabilidad.
