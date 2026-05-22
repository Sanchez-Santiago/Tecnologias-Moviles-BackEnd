# 🛒 MiSuper / Super Ahorro — Backend

## 👥 Autores
- **Santiago Sanchez**
- **Giuliano Pucci**

---

## 📋 Descripción del Proyecto

Backend monolítico modular para **SUPER AHORRO**, una aplicación móvil integral de control de gastos de supermercado que va más allá de un simple registro de compras. El sistema está diseñado para brindar una experiencia completa de finanzas personales y grupales, con las siguientes capacidades:

### 🛒 Gestión de Compras Inteligente
Registro detallado de compras con productos asociados, incluyendo fecha, supermercado, total y detalle por ítem. Cada producto se clasifica en **tres categorías de prioridad**:
- **Esencial** — productos de primera necesidad
- **Primario** — productos importantes pero no críticos
- **Secundario** — productos complementarios o prescindibles

Esto permite al usuario priorizar su lista de compras y optimizar su gasto.

### 🏪 Ofertas Inteligentes
Sistema que cruza automáticamente los productos de la lista de compra del usuario con las **ofertas activas de supermercados cercanos**, mostrando dónde conviene comprar cada producto y calculando el ahorro potencial.

### 👨‍👩‍👧‍👦 Reparto de Gastos (Individual / Familiar / Empresarial)
Sistema de grupos flexible que permite:
- **Individual** — control de gastos personal
- **Familiar** — compras compartidas entre miembros de la familia con reparto proporcional de gastos
- **Empresarial** — escalable a pequeños negocios o equipos de trabajo, con roles jerárquicos y reportes

Cada grupo tiene membresías con roles (ADMIN/MEMBER), permitiendo agregar/quitar miembros y gestionar permisos.

### 💰 Gestión Financiera Completa (Ingresos y Egresos)
Además de las compras, el sistema registra y categoriza **ingresos** (sueldos, transferencias, otros) y **egresos** (compras, servicios, suscripciones), ofreciendo una visión completa de las finanzas del usuario o grupo.

### 📊 Estadísticas y Presupuestos
- Gasto por categoría y supermercado con porcentajes
- Resumen mensual de gastos
- Progreso de presupuestos vs gasto real
- Reportes visuales para toma de decisiones

### 🎫 Tickets de Soporte
Sistema de tickets con mensajes, estados (OPEN, IN_PROGRESS, RESOLVED, CLOSED) y prioridades (LOW, MEDIUM, HIGH, URGENT), permitiendo gestionar reclamos o incidencias dentro de cada grupo.

### 🔔 Notificaciones
Sistema centralizado de notificaciones por usuario, con tipos personalizados, marcado de lectura y conteo de no leídas.

---

## 🛠 Stack Tecnológico

| Tecnología | Versión | Propósito | Detalle Técnico |
|---|---|---|---|
| **Kotlin** | 2.2.0 | Lenguaje principal | Null-safety, corrutinas nativas (Dispatchers.IO, async/await), interoperabilidad total con Android, Type-safe builders para DSL |
| **Ktor** | 3.4.2 | Framework HTTP | Servidor asíncrono basado en corrutinas, pipeline de procesamiento de requests, Content Negotiation, Autenticación por plugins, Netty engine |
| **Exposed ORM** | 1.0.0 | Mapeo objeto-relacional | DSL type-safe que genera SQL en tiempo de compilación, evita inyección SQL, soporta UUIDTable, joins, transacciones, SchemaUtils para migraciones |
| **PostgreSQL** | 16 | Base de datos | Tipos nativos UUID (gen_random_uuid()), DECIMAL para precisión financiera, JSONB para datos flexibles, índices únicos, FK constraints, esquemas separados |
| **HikariCP** | — | Pool de conexiones | Pool liviano con alto rendimiento, auto-commit desactivado, tiempo de espera configurable, monitoreo de conexiones activas/inactivas |
| **kotlinx.serialization** | 1.11.0 | Serialización JSON | 100% Kotlin, sin reflection, serializadores contextuales (@Contextual) para tipos complejos (BigDecimal, LocalDateTime, UUID), formatos JSON/BIN/CBOR |
| **JWT (auth-jwt)** | — | Autenticación stateless | Access token 15 min + Refresh token 7 días, claims personalizados (userId, email, role, groupIds), verificación HMAC-SHA256 |
| **BCrypt** | — | Hashing de contraseñas | Salt automático, costo computacional configurable (factor 12), resistencia a ataques de fuerza bruta |
| **Logback** | — | Logging estructurado | Appender consola con patrones personalizados, niveles DEBUG/INFO para diferentes paquetes, formato de fecha y hilo |
| **Gradle** | 9.3 | Sistema de build | Kotlin DSL para scripts de build, resolución paralela de dependencias, caché de compilación, daemon persistente |
| **JDK** | 21 LTS | Runtime | Recolección de basura ZGC, Virtual Threads (Project Loom), records pattern matching, mejora de rendimiento sostenido |

---

## 🏗 Arquitectura del Sistema

### Patrón: MVC + Service Layer + Repository Pattern

```
┌──────────────────────────────────────────────────────────────────────┐
│                      Android (Jetpack Compose)                       │
│                         ┌─────────────────┐                          │
│                         │  Retrofit/HTTP  │                          │
│                         └────────┬────────┘                          │
└──────────────────────────────────┼───────────────────────────────────┘
                                   │ HTTPS / JSON
                                   ▼
┌──────────────────────────────────────────────────────────────────────┐
│                         KTOR SERVER (Netty)                          │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                        PIPELINE DE KTOR                        │  │
│  │  ┌───────┐     ┌────────┐     ┌───────────┐     ┌───────────┐  │  │
│  │  │Routing│ ──▶ │Auth JWT│ ──▶ │  Content  │ ──▶ │StatusPages│  │  │
│  │  │       │     │ Plugin │     │Negotiation│     │Exceptions │  │  │
│  │  └───────┘     └────────┘     └───────────┘     └───────────┘  │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                      ROUTES / CONTROLLERS                      │  │
│  │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐         │  │
│  │  │ Auth │ │Users │ │ Prod │ │Stores│ │Groups│ │Purch │  ...    │  │
│  │  └──┬───┘ └──┬───┘ └──┬───┘ └──┬───┘ └──┬───┘ └──┬───┘         │  │
│  └─────┼────────┼────────┼────────┼────────┼────────┼─────────────────────┘  │
│        ▼        ▼        ▼        ▼        ▼        ▼                │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                         SERVICE LAYER                          │  │
│  │  (Reglas de negocio, validaciones, permisos, coordinación)     │  │
│  └──────────┬──────────┬──────────┬──────────┬────────────────────┘  │
│             ▼          ▼          ▼          ▼                       │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                        REPOSITORY LAYER                        │  │
│  │  (Acceso a DB, Queries Exposed DSL, Transacciones)             │  │
│  └──────────────────────────┬─────────────────────────────────────┘  │
│                             │                                        │
│  ┌──────────────────────────▼─────────────────────────────────────┐  │
│  │                        DATABASE FACTORY                        │  │
│  │  (HikariCP Pool, SchemaUtils.create, Conexión Supabase)        │  │
│  └──────────────────────────┬─────────────────────────────────────┘  │
└─────────────────────────────┼────────────────────────────────────────┘
                              │ JDBC
                    ┌─────────▼─────────┐
                    │     SUPABASE      │
                    │   PostgreSQL 16   │
                    │     18 tablas     │
                    └───────────────────┘
```

### 🔹 Flujo de una Request

```
1. Request HTTP → Ktor Routing
2. └─ authenticate("auth-jwt") verifica JWT
3.    └─ Route handler recibe request
4.       └─ call.receive<DTO>() deserializa JSON
5.          └─ Service.validate() + Service.process()
6.             └─ Repository.query() ejecuta SQL via Exposed
7.                └─ Database responde
8.             └─ Repository retorna ResultRow
9.          └─ Service construye Response DTO
10.      └─ Route responde JSON con ApiResponse.success()
```

### 🔹 Capas y Responsabilidades

| Capa | Responsabilidad | NO debe hacer |
|------|----------------|---------------|
| **Routes** | Recibir request, parsear JSON, llamar service, responder | Lógica de negocio, SQL, JWT manual |
| **Services** | Reglas de negocio, validaciones, permisos, cálculos, orquestación | SQL, serialización, HTTP |
| **Repositories** | Acceso a DB, queries, joins, transacciones | Reglas de negocio, JWT, cálculos |
| **Validators** | Validar formato de entrada (emails, rangos, obligatorios) | Lógica de negocio compleja |
| **DTOs** | Objetos de transferencia Request/Response | Lógica, herencia de entidades |

---

## 🗄 Esquema de Base de Datos (18 tablas)

### Módulo Auth (5 tablas)
| Tabla | Descripción |
|---|---|
| `users` | Usuarios del sistema (id, email, password, fullName, role, verified, active, timestamps) |
| `password_history` | Historial de contraseñas para evitar reuso |
| `refresh_tokens` | Tokens de refresco JWT con expiración |
| `login_history` | Historial de inicios de sesión (fecha, IP, dispositivo) |
| `user_settings` | Preferencias de usuario (idioma, moneda, notificaciones) |

### Módulo Products (2 tablas)
| Tabla | Descripción |
|---|---|
| `categories` | Categorías de productos (nombre, descripción, icono) |
| `products` | Productos (nombre, precio, categoría, código de barras, imagen, prioridad) |

### Módulo Stores (1 tabla)
| Tabla | Descripción |
|---|---|
| `stores` | Supermercados (nombre, dirección, teléfono, coordenadas) |

### Módulo Groups (2 tablas)
| Tabla | Descripción |
|---|---|
| `groups` | Grupos familiar/empresarial (nombre, descripción, creador) |
| `group_members` | Membresías con roles ADMIN/MEMBER |

### Módulo Purchases (2 tablas)
| Tabla | Descripción |
|---|---|
| `purchases` | Compras (grupo, tienda, usuario, total, notas, fecha, soft delete) |
| `purchase_products` | Productos de cada compra (cantidad, precio unitario, subtotal) |

### Módulo Budgets (2 tablas)
| Tabla | Descripción |
|---|---|
| `budgets` | Presupuestos (grupo, nombre, monto total, período, fechas) |
| `budget_items` | Partidas del presupuesto por categoría |

### Módulo Tickets (2 tablas)
| Tabla | Descripción |
|---|---|
| `tickets` | Tickets de soporte (grupo, creador, título, descripción, estado, prioridad, asignado) |
| `ticket_messages` | Mensajes del ticket (usuario, contenido, timestamp) |

### Módulo Notifications (1 tabla)
| Tabla | Descripción |
|---|---|
| `notifications` | Notificaciones por usuario (tipo, título, mensaje, datos extra, leído) |

### Módulo Offers (1 tabla)
| Tabla | Descripción |
|---|---|
| `offers` | Ofertas y promociones (tienda, título, tipo descuento, valor, fechas, condiciones) |

---

## 📡 Endpoints de la API (59 totales)

### Públicos (sin autenticación)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/` | Health check (server, DB, uptime) |
| `POST` | `/api/auth/register` | Registro de usuario |
| `POST` | `/api/auth/login` | Inicio de sesión |
| `POST` | `/api/auth/refresh` | Refrescar token |

### Usuarios (JWT)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/users/me` | Obtener perfil |
| `PUT` | `/api/users/me` | Actualizar perfil |
| `PUT` | `/api/users/me/password` | Cambiar contraseña |
| `GET` | `/api/users/me/settings` | Obtener configuración |
| `PUT` | `/api/users/me/settings` | Actualizar configuración |

### Productos y Categorías (JWT, algunas ADMIN)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/products` | Listar productos |
| `GET` | `/api/products/{id}` | Obtener producto |
| `POST` | `/api/products` | Crear producto (ADMIN) |
| `PUT` | `/api/products/{id}` | Actualizar producto (ADMIN) |
| `DELETE` | `/api/products/{id}` | Eliminar producto (ADMIN) |
| `GET` | `/api/categories` | Listar categorías |
| `GET` | `/api/categories/{id}` | Obtener categoría |
| `POST` | `/api/categories` | Crear categoría (ADMIN) |
| `PUT` | `/api/categories/{id}` | Actualizar categoría (ADMIN) |
| `DELETE` | `/api/categories/{id}` | Eliminar categoría (ADMIN) |

### Tiendas (JWT, algunas ADMIN)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/stores` | Listar tiendas |
| `GET` | `/api/stores/{id}` | Obtener tienda |
| `POST` | `/api/stores` | Crear tienda (ADMIN) |
| `PUT` | `/api/stores/{id}` | Actualizar tienda (ADMIN) |
| `DELETE` | `/api/stores/{id}` | Eliminar tienda (ADMIN) |

### Grupos (JWT)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/groups` | Listar grupos del usuario |
| `GET` | `/api/groups/{id}` | Detalle del grupo con miembros |
| `POST` | `/api/groups` | Crear grupo |
| `PUT` | `/api/groups/{id}` | Actualizar grupo |
| `DELETE` | `/api/groups/{id}` | Eliminar grupo |
| `POST` | `/api/groups/{id}/members` | Agregar miembro por email |
| `DELETE` | `/api/groups/{id}/members/{memberId}` | Quitar miembro |

### Compras (JWT)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/purchases?groupId=` | Listar compras del grupo |
| `GET` | `/api/purchases/{id}` | Detalle de compra con productos |
| `POST` | `/api/purchases` | Registrar compra con productos |

### Presupuestos (JWT)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/budgets?groupId=` | Listar presupuestos |
| `GET` | `/api/budgets/{id}` | Detalle con partidas |
| `POST` | `/api/budgets` | Crear presupuesto |
| `PUT` | `/api/budgets/{id}` | Actualizar presupuesto |
| `DELETE` | `/api/budgets/{id}` | Eliminar presupuesto |

### Tickets (JWT)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/tickets?groupId=` | Listar tickets |
| `GET` | `/api/tickets/{id}` | Detalle del ticket |
| `POST` | `/api/tickets` | Crear ticket |
| `PUT` | `/api/tickets/{id}` | Actualizar ticket |
| `DELETE` | `/api/tickets/{id}` | Eliminar ticket |
| `POST` | `/api/tickets/{id}/messages` | Agregar mensaje |

### Notificaciones (JWT)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/notifications` | Listar notificaciones |
| `GET` | `/api/notifications/unread/count` | Contar no leídas |
| `PUT` | `/api/notifications/{id}/read` | Marcar como leída |
| `PUT` | `/api/notifications/read-all` | Marcar todas leídas |
| `DELETE` | `/api/notifications/{id}` | Eliminar notificación |

### Ofertas (JWT, algunas ADMIN)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/offers` | Listar ofertas |
| `GET` | `/api/offers/{id}` | Obtener oferta |
| `POST` | `/api/offers` | Crear oferta (ADMIN) |
| `PUT` | `/api/offers/{id}` | Actualizar oferta (ADMIN) |
| `DELETE` | `/api/offers/{id}` | Eliminar oferta (ADMIN) |

### Estadísticas (JWT)
| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/statistics/group/{groupId}/spending-by-category` | Gasto por categoría |
| `GET` | `/api/statistics/group/{groupId}/spending-by-store` | Gasto por tienda |
| `GET` | `/api/statistics/group/{groupId}/monthly-summary` | Resumen mensual |
| `GET` | `/api/statistics/group/{groupId}/budget-progress` | Progreso presupuesto |

---

## 🔐 Seguridad

- **Autenticación**: JWT stateless (HMAC-SHA256)
  - Access Token: 15 minutos de validez
  - Refresh Token: 7 días, almacenado en DB
- **Claims**: `sub` (userId), `email`, `role`, `iss`, `aud`, `iat`, `exp`
- **Roles**: `ADMIN` (global, verificado por claim) y roles por grupo (`ADMIN`/`MEMBER`)
- **Passwords**: BCrypt con factor de costo 12, historial de últimas 5 contraseñas
- **Soft Delete**: Todas las entidades tienen columna `active` (no se eliminan físicamente)
- **Validación**: 5 excepciones custom centralizadas via Ktor StatusPages
- **CORS**: Configurado para desarrollo local

---

## ✅ Estado Actual

- **12 módulos implementados** y funcionales
- **59 endpoints** operativos
- **18 tablas** en PostgreSQL (Supabase)
- **Servidor**: Ktor 3.4.2 en JDK 21, pool HikariCP
- **Compilación**: Gradle 9.3 con Kotlin 2.2.0

---

## 🚀 Próximas Actualizaciones Planeadas

1. **Prioridad de productos** — columna `priority` (ESENCIAL/PRIMARIO/SECUNDARIO) en products
2. **Ofertas inteligentes** — endpoint que matchea productos de lista vs ofertas activas
3. **Reparto de gastos** — splits entre miembros del grupo con liquidaciones
4. **Gestión financiera** — módulo completo de ingresos/egresos (transactions + categories)
5. **Upload de imagen de ticket** — endpoint + integración Cloudinary
6. **Swagger / OpenAPI** — documentación automática
7. **Docker** — containerización
8. **Testing automatizado** — unit + integración
9. **Rate Limiting** — protección contra abusos
