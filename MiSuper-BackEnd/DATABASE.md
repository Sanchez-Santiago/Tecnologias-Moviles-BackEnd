# Base de datos

## Motor

El backend usa PostgreSQL. En desarrollo se puede levantar local con Docker Compose y en entrega/producción se puede apuntar a Supabase u otro PostgreSQL administrado.

## Configuración

Variables principales:

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/misuper
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres
DATABASE_MAX_POOL_SIZE=10
DATABASE_MIGRATE_ON_START=true
DATABASE_SEED_ON_START=false
```

Para desarrollo local con datos de ejemplo:

```env
DATABASE_SEED_ON_START=true
```

Para producción:

```env
DATABASE_MIGRATE_ON_START=true
DATABASE_SEED_ON_START=false
```

## Migraciones

Las migraciones están en:

```text
src/main/resources/db/migration
```

La migración inicial es:

```text
V1__initial_schema.sql
```

Flyway ejecuta las migraciones al iniciar la aplicación si `DATABASE_MIGRATE_ON_START=true`. Esto reemplaza el uso de `SchemaUtils.create`, que solo sirve para prototipos y puede dejar la base en estados difíciles de controlar.

## Seed

El seed está en:

```text
src/main/kotlin/com/misuper/backend/database/DatabaseSeeder.kt
```

Carga datos iniciales para probar la app móvil:

- Categorías.
- Productos.
- Supermercados.
- Ofertas activas.

El seed es idempotente: no duplica registros si ya existen por nombre/título.

## Tablas principales

- `users`
- `password_history`
- `refresh_tokens`
- `login_history`
- `user_settings`
- `categories`
- `products`
- `stores`
- `groups`
- `group_members`
- `purchases`
- `purchase_products`
- `budgets`
- `budget_items`
- `tickets`
- `ticket_messages`
- `notifications`
- `offers`
- `financial_transactions`

## Entrega prolija

Para probar local:

```bash
docker compose up --build
```

La API queda en:

```text
http://localhost:8080
```

La documentación OpenAPI queda en:

```text
http://localhost:8080/openapi.yaml
```

## Producción

Recomendaciones mínimas:

- Usar una base PostgreSQL administrada, por ejemplo Supabase.
- Usar `DATABASE_SEED_ON_START=false`.
- Usar un `JWT_SECRET` largo y privado.
- No usar `postgres/postgres` como usuario/contraseña.
- Hacer backup de la base antes de desplegar nuevas migraciones.
- No editar tablas manualmente si Flyway ya está administrando el esquema.
- Revisar la tabla `flyway_schema_history` para confirmar qué migraciones corrieron.
