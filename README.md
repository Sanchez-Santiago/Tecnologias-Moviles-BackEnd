# MiSuper / Super Ahorro - Backend

Backend del proyecto **Super Ahorro**, una aplicación para registrar compras de supermercado, controlar gastos y manejar información compartida dentro de grupos.

El proyecto está hecho con Kotlin y Ktor. Expone una API REST que consume la app cliente y se conecta a una base PostgreSQL.

## Autores

- Santiago Sanchez
- Giuliano Pucci

## Qué incluye

El backend implementa estos módulos principales:

- Autenticación de usuarios.
- Perfil y configuración de usuario.
- Productos y categorías.
- Supermercados o tiendas.
- Grupos con miembros y roles.
- Compras con productos asociados.
- Presupuestos.
- Estadísticas de gastos.
- Ofertas.
- Transacciones financieras.
- Notificaciones.
- Tickets de soporte.

También se agregó un endpoint para analizar imágenes de tickets con OpenAI. El endpoint recibe una imagen en Base64 y devuelve datos extraídos del ticket, como comercio, fecha, total y productos.

## Tecnologías usadas

| Tecnología | Versión | Uso |
|---|---:|---|
| Kotlin | 2.2.0 | Lenguaje principal |
| JDK | 21 | Runtime |
| Ktor | 3.4.2 | Servidor HTTP |
| Exposed ORM | 1.0.0 | Acceso a base de datos |
| PostgreSQL Driver | 42.7.5 | Conexión JDBC |
| HikariCP | 6.3.0 | Pool de conexiones |
| Flyway | 10.20.1 | Migraciones |
| kotlinx.serialization | 1.8.0 | JSON |
| BCrypt | 0.10.2 | Hash de contraseñas |
| Logback | 1.5.16 | Logs |
| Gradle | 9.3.0 | Build |

## Arquitectura

El proyecto está organizado por módulos. Cada módulo tiene, según corresponda:

- `routes`: define los endpoints.
- `services`: contiene la lógica de negocio.
- `repositories`: accede a la base de datos.
- `validators`: valida requests.
- `dto`: define objetos de entrada y salida.

La estructura general es:

```text
Request HTTP
    -> Ktor Routing
    -> JWT / Serialization / StatusPages
    -> Route
    -> Service
    -> Repository
    -> PostgreSQL
```

La idea es que las rutas no tengan lógica de negocio ni SQL directo. Las rutas reciben la request, llaman al service y devuelven una respuesta. El service valida reglas, permisos y casos de negocio. El repository se encarga de las consultas usando Exposed.

## Base de datos

Se usa PostgreSQL. La conexión se configura en `DatabaseFactory`, junto con:

- Migraciones con Flyway.
- Pool de conexiones con HikariCP.
- `autoCommit=false`.
- Nivel de aislamiento `TRANSACTION_REPEATABLE_READ`.
- Seeder opcional al iniciar.

El esquema actual tiene 19 tablas:

| Tabla | Uso |
|---|---|
| `users` | Usuarios del sistema |
| `password_history` | Historial de contraseñas |
| `refresh_tokens` | Tokens de refresco |
| `login_history` | Historial de login |
| `user_settings` | Preferencias del usuario |
| `categories` | Categorías de productos |
| `products` | Productos |
| `stores` | Supermercados o tiendas |
| `groups` | Grupos |
| `group_members` | Miembros de grupos |
| `purchases` | Compras |
| `purchase_products` | Productos dentro de una compra |
| `budgets` | Presupuestos |
| `budget_items` | Items de presupuesto |
| `tickets` | Tickets de soporte |
| `ticket_messages` | Mensajes de tickets |
| `notifications` | Notificaciones |
| `offers` | Ofertas |
| `financial_transactions` | Ingresos y egresos |

## Seguridad

La autenticación usa JWT.

Cuando un usuario se registra o inicia sesión, el backend genera:

- `accessToken`: JWT usado para acceder a endpoints protegidos.
- `refreshToken`: token guardado en base de datos para renovar la sesión.

Las contraseñas se guardan hasheadas con BCrypt. También se registran intentos de login y se contempla bloqueo de usuarios.

Los endpoints protegidos usan `authenticate("auth-jwt")`. Algunas operaciones requieren permisos extra:

- Crear, editar o eliminar productos, categorías, tiendas y ofertas requiere rol global `ADMIN`.
- Editar grupos o administrar miembros requiere rol `ADMIN` dentro del grupo.
- Consultar compras, estadísticas, presupuestos o tickets valida que el usuario pertenezca al grupo.

Los errores se manejan de forma centralizada con `StatusPages`, devolviendo respuestas JSON con `success`, `data`, `message` y `errorCode`.

## Endpoints

Hay 68 rutas registradas.

### Públicos

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/` | Health check |
| `GET` | `/openapi.yaml` | Archivo OpenAPI |
| `POST` | `/api/auth/register` | Registro |
| `POST` | `/api/auth/login` | Login |
| `POST` | `/api/auth/refresh` | Renovar token |

### Usuarios

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/users/me` | Perfil |
| `PUT` | `/api/users/me` | Actualizar perfil |
| `PUT` | `/api/users/me/password` | Cambiar contraseña |
| `GET` | `/api/users/me/settings` | Configuración |
| `PUT` | `/api/users/me/settings` | Actualizar configuración |

### Productos y categorías

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/products` | Listar productos |
| `GET` | `/api/products/{id}` | Obtener producto |
| `POST` | `/api/products` | Crear producto |
| `PUT` | `/api/products/{id}` | Actualizar producto |
| `DELETE` | `/api/products/{id}` | Eliminar producto |
| `GET` | `/api/categories` | Listar categorías |
| `GET` | `/api/categories/{id}` | Obtener categoría |
| `POST` | `/api/categories` | Crear categoría |
| `PUT` | `/api/categories/{id}` | Actualizar categoría |
| `DELETE` | `/api/categories/{id}` | Eliminar categoría |

### Tiendas

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/stores` | Listar tiendas |
| `GET` | `/api/stores/{id}` | Obtener tienda |
| `POST` | `/api/stores` | Crear tienda |
| `PUT` | `/api/stores/{id}` | Actualizar tienda |
| `DELETE` | `/api/stores/{id}` | Eliminar tienda |

### Grupos

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/groups` | Grupos del usuario |
| `GET` | `/api/groups/{id}` | Detalle del grupo |
| `POST` | `/api/groups` | Crear grupo |
| `PUT` | `/api/groups/{id}` | Actualizar grupo |
| `DELETE` | `/api/groups/{id}` | Eliminar grupo |
| `POST` | `/api/groups/{id}/members` | Agregar miembro |
| `DELETE` | `/api/groups/{id}/members/{memberId}` | Quitar miembro |

### Compras

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/purchases?groupId=` | Compras de un grupo |
| `GET` | `/api/purchases/{id}` | Detalle de compra |
| `GET` | `/api/purchases/{id}/share` | Texto para compartir |
| `POST` | `/api/purchases` | Registrar compra |

### Presupuestos

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/budgets?groupId=` | Presupuestos de un grupo |
| `GET` | `/api/budgets/{id}` | Obtener presupuesto |
| `POST` | `/api/budgets` | Crear presupuesto |
| `PUT` | `/api/budgets/{id}` | Actualizar presupuesto |
| `DELETE` | `/api/budgets/{id}` | Eliminar presupuesto |

### Tickets

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/tickets?groupId=` | Tickets de un grupo |
| `GET` | `/api/tickets/{id}` | Detalle del ticket |
| `POST` | `/api/tickets` | Crear ticket |
| `POST` | `/api/tickets/analyze-image` | Analizar imagen de ticket |
| `PUT` | `/api/tickets/{id}` | Actualizar ticket |
| `DELETE` | `/api/tickets/{id}` | Eliminar ticket |
| `POST` | `/api/tickets/{id}/messages` | Agregar mensaje |

### Notificaciones

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/notifications` | Listar notificaciones |
| `GET` | `/api/notifications/unread/count` | Contar no leídas |
| `PUT` | `/api/notifications/{id}/read` | Marcar como leída |
| `PUT` | `/api/notifications/read-all` | Marcar todas como leídas |
| `DELETE` | `/api/notifications/{id}` | Eliminar notificación |

### Ofertas

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/offers` | Listar ofertas |
| `GET` | `/api/offers/active` | Ofertas activas |
| `GET` | `/api/offers/match` | Buscar ofertas para productos |
| `GET` | `/api/offers/{id}` | Obtener oferta |
| `POST` | `/api/offers` | Crear oferta |
| `PUT` | `/api/offers/{id}` | Actualizar oferta |
| `DELETE` | `/api/offers/{id}` | Eliminar oferta |

### Estadísticas

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/statistics/group/{groupId}/spending-by-category` | Gasto por categoría |
| `GET` | `/api/statistics/group/{groupId}/spending-by-store` | Gasto por tienda |
| `GET` | `/api/statistics/group/{groupId}/monthly-summary` | Resumen mensual |
| `GET` | `/api/statistics/group/{groupId}/budget-progress` | Progreso de presupuesto |

### Transacciones financieras

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/transactions?groupId=` | Movimientos del grupo |
| `GET` | `/api/transactions/summary?groupId=` | Resumen financiero |
| `POST` | `/api/transactions` | Crear movimiento |
| `DELETE` | `/api/transactions/{id}` | Eliminar movimiento |

## Configuración

La configuración se lee desde `application.conf`, variables de entorno o un archivo `.env`.

Variables más importantes:

| Variable | Descripción |
|---|---|
| `PORT` / `HOST` | Puerto y host del servidor |
| `DATABASE_URL` | URL JDBC de PostgreSQL |
| `DATABASE_USER` | Usuario de la base |
| `DATABASE_PASSWORD` | Contraseña de la base |
| `DATABASE_MAX_POOL_SIZE` | Tamaño del pool |
| `DATABASE_MIGRATE_ON_START` | Ejecutar migraciones al iniciar |
| `DATABASE_SEED_ON_START` | Ejecutar seed al iniciar |
| `JWT_SECRET` | Secreto para firmar JWT |
| `JWT_ISSUER` | Issuer del token |
| `JWT_AUDIENCE` | Audience del token |
| `ACCESS_TOKEN_EXPIRY_MINUTES` | Duración del access token |
| `REFRESH_TOKEN_EXPIRY_DAYS` | Duración del refresh token |
| `PASSWORD_HASH_COST` | Costo de BCrypt |
| `PASSWORD_HISTORY_SIZE` | Cantidad de contraseñas en historial |
| `CORS_ALLOWED_HOSTS` | Hosts permitidos por CORS |
| `OPENAI_API_KEY` | API key para analizar tickets |
| `OPENAI_MODEL` | Modelo usado para analizar tickets |

## Cómo ejecutar

Desde la carpeta del backend:

```bash
cd MiSuper-BackEnd
./gradlew run
```

También hay archivos para Docker y despliegue:

- `Dockerfile`
- `docker-compose.yml`
- `render.yaml`
- `run.sh`

## Tests

Hay tests unitarios para seguridad:

- `JwtServiceTest`
- `PasswordHasherTest`

Para ejecutarlos:

```bash
cd MiSuper-BackEnd
./gradlew test
```

## Estado actual

- 12 módulos principales implementados.
- 68 rutas HTTP registradas.
- 19 tablas en PostgreSQL.
- Migraciones con Flyway.
- Autenticación JWT con access token y refresh token.
- Hashing de contraseñas con BCrypt.
- OpenAPI disponible en `/openapi.yaml`.
- Docker y configuración de despliegue incluidos.
- Rate limiting aplicado a autenticación.
- Tests unitarios básicos de seguridad.

## Pendiente o mejorable

- Agregar reparto detallado de gastos entre miembros.
- Agregar saldos o liquidaciones por usuario.
- Ampliar tests unitarios y sumar tests de integración.
- Agregar paginación en listados grandes.
- Agregar Swagger UI.
- Mejorar el flujo para guardar automáticamente productos detectados desde imágenes de tickets.
