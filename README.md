# Inventory Backend Microservices

Backend reactivo para inventario de productos con arquitectura de microservicios en Java 21 + Spring Boot:

- `discovery-server` `:8761`
- `api-gateway` `:8080`
- `auth-service` `:8081`
- `catalog-service` `:8082`
- `inventory-service` `:8083`
- `report-service` `:8084`
- PostgreSQL con bases de datos independientes
- Docker Compose para despliegue del backend

## Funcionalidad

- Gestion de usuarios con roles `ADMIN` y `USER`
- El primer usuario registrado se crea como `ADMIN`
- Login con JWT y refresh token
- Gestion de categorias
- Gestion de productos con stock inicial
- Registro de entradas y salidas con fecha y hora
- Reportes de stock y movimientos
- Restriccion para que solo `ADMIN` cree categorias y productos

## Arquitectura

- `auth-service`: usuarios, autenticacion, JWT, refresh token
- `catalog-service`: categorias y productos
- `inventory-service`: movimientos de inventario y ajuste de stock
- `report-service`: consolidacion de reportes y auditoria de reportes generados
- `api-gateway`: entrada unica, validacion JWT y propagacion de headers internos
- `discovery-server`: registro Eureka

Cada microservicio usa su propia base en PostgreSQL:

- `auth_db`
- `catalog_db`
- `inventory_db`
- `report_db`

## Ejecutar con Docker

```bash
docker compose up --build
```

Accesos:

- API Gateway: `http://localhost:8080`
- Eureka Dashboard: `http://localhost:8761`

## Endpoints principales

Base URL: `http://localhost:8080`

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/auth/me`

### Usuarios

- `GET /api/users`
- `POST /api/users`
- `PUT /api/users/{id}/role`
- `PUT /api/users/{id}/status`

### Categorias

- `GET /api/categories`
- `GET /api/categories/{id}`
- `POST /api/categories`
- `PUT /api/categories/{id}`
- `DELETE /api/categories/{id}`

### Productos

- `GET /api/products`
- `GET /api/products/{id}`
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`

### Inventario

- `GET /api/inventory/movements`
- `GET /api/inventory/movements/{id}`
- `POST /api/inventory/entries`
- `POST /api/inventory/exits`

### Reportes

- `GET /api/reports/stock`
- `GET /api/reports/movements`
- `GET /api/reports/audits`

## Ejemplos JSON

Registrar usuario:

```json
{
  "name": "Ana Admin",
  "email": "ana@inventario.com",
  "password": "Secreto123"
}
```

Crear usuario desde administrador:

```json
{
  "name": "Operador Uno",
  "email": "operador@inventario.com",
  "password": "Secreto123",
  "role": "USER"
}
```

Crear categoria:

```json
{
  "name": "Bebidas",
  "description": "Productos liquidos"
}
```

Crear producto:

```json
{
  "name": "Agua 600ml",
  "sku": "AGUA-600",
  "description": "Botella de agua",
  "categoryId": "11111111-1111-1111-1111-111111111111",
  "price": 2500,
  "stock": 20,
  "active": true
}
```

Registrar entrada o salida:

```json
{
  "productId": "22222222-2222-2222-2222-222222222222",
  "quantity": 5,
  "occurredAt": "2026-03-19T10:30:00Z",
  "reference": "FACT-001",
  "notes": "Ingreso de proveedor"
}
```
