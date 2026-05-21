Perfecto.
Entonces vamos a diseñar el backend de **SUPER AHORRO** como una aplicación monolítica moderna, modular y bien organizada, SIN microservicios, pero preparada para crecer sin romperse.

La idea es construir un backend:

✔ limpio
✔ escalable
✔ mantenible
✔ fácil de debuggear
✔ coherente con Android/Kotlin
✔ con arquitectura profesional
✔ preparado para producción real

---

# 🚀 STACK FINAL RECOMENDADO

Estas versiones son compatibles entre sí y estables actualmente.

---

# ✅ Kotlin

```plaintext id="j8x9mq"
Kotlin 2.3.0
```

---

# ✅ Ktor

```plaintext id="p3mvla"
Ktor 3.4.2
```

Porque:

* es estable
* corrige muchos bugs de 3.4.0
* mantiene compatibilidad
* tiene mejoras de performance
* evita problemas recientes de releases mayores ([Ktor Framework][1])

---

# ✅ Exposed ORM

```plaintext id="4l7cwb"
Exposed 1.0.0
```

Muy importante:
👉 Exposed 1.0 estabilizó MUCHAS APIs. ([Reddit][2])

---

# ✅ PostgreSQL

```plaintext id="7s3wdf"
PostgreSQL 16
```

---

# ✅ JWT

```plaintext id="d1xmnp"
ktor-server-auth-jwt 3.4.2
```

Compatible oficialmente con Ktor 3.4.x. ([klibs.io][3])

---

# ✅ Serialización

```plaintext id="m2bqfz"
kotlinx.serialization 1.11.0
```

Compatible con Kotlin 2.3.x. ([GitHub][4])

---

# ✅ Gradle

```plaintext id="y9wnvr"
Gradle 9.3
```

---

# ✅ JDK

```plaintext id="r4clxs"
JDK 21 LTS
```

NO usar Java 24/25 todavía.

---

# 🚀 FILOSOFÍA DEL BACKEND

---

# ❌ Qué NO queremos

NO queremos:

* lógica mezclada
* SQL en routes
* rutas gigantes
* clases mágicas
* acoplamiento
* controllers inteligentes
* services desordenados

---

# ✅ Qué SÍ queremos

✔ separación clara
✔ arquitectura modular
✔ reglas de negocio centralizadas
✔ manejo global de errores
✔ DTOs
✔ validaciones limpias
✔ repositories desacoplados
✔ JWT seguro
✔ corrutinas reales

---

# 🚀 ARQUITECTURA FINAL

---

# ✅ Arquitectura elegida

## MVC + Service Layer + Repository Pattern

---

# Flujo real

```plaintext id="q6ntzb"
Request HTTP
↓
Routes / Controllers
↓
Services
↓
Repositories
↓
Database
```

---

# 🧠 Qué hace cada capa

---

# ✅ CONTROLLERS / ROUTES

Responsabilidad:

✔ recibir requests
✔ parsear JSON
✔ validar input básico
✔ llamar services
✔ devolver respuestas

---

# ❌ NO deben hacer

* lógica financiera
* reglas de negocio
* SQL
* JWT manual
* cálculos

---

# Ejemplo mental

```plaintext id="w5cpln"
POST /purchases
↓
PurchaseRoutes
↓
PurchaseService.createPurchase()
```

---

# ✅ SERVICES

La capa MÁS importante.

---

# Responsabilidades

✔ reglas de negocio
✔ validaciones complejas
✔ seguridad lógica
✔ permisos
✔ cálculos
✔ estadísticas
✔ automatizaciones
✔ coordinación del sistema

---

# Ejemplos reales

---

## PurchaseService

Debe:

* validar presupuesto
* crear compra
* registrar productos
* generar historial
* disparar notificación
* actualizar estadísticas

---

## AuthService

Debe:

* validar contraseña
* generar JWT
* verificar bloqueo
* manejar refresh token
* guardar login history

---

# 🧠 Los services son el cerebro.

---

# ✅ REPOSITORIES

Responsabilidad:

✔ acceso a DB
✔ consultas SQL
✔ joins
✔ persistencia

---

# ❌ NO deben contener

* reglas de negocio
* JWT
* cálculos
* lógica financiera

---

# Ejemplo

```plaintext id="7plfkg"
PurchaseRepository.findByUserId()
```

---

# 🚀 ESTRUCTURA PROFESIONAL

---

```plaintext id="k4yvcz"
backend/
│
├── src/
│
├── auth/
│   ├── routes/
│   ├── services/
│   ├── repositories/
│   ├── dto/
│   ├── models/
│   └── validators/
│
├── users/
├── groups/
├── products/
├── purchases/
├── budgets/
├── tickets/
├── offers/
├── notifications/
├── statistics/
├── stores/
│
├── database/
│   ├── tables/
│   ├── migrations/
│   ├── DatabaseFactory.kt
│   └── Transaction.kt
│
├── security/
│   ├── JwtService.kt
│   ├── PasswordHasher.kt
│   ├── Authorization.kt
│   └── TokenConfig.kt
│
├── plugins/
│   ├── Routing.kt
│   ├── Serialization.kt
│   ├── Security.kt
│   ├── Monitoring.kt
│   └── StatusPages.kt
│
├── exceptions/
│
├── responses/
│
├── utils/
│
├── config/
│
└── Application.kt
```

---

# 🚀 DTOs

MUY importante.

---

# ❌ Nunca devolver entidades DB directamente

Porque:

* exponés datos internos
* acoplás frontend y DB
* rompés seguridad

---

# ✅ Usar DTOs

---

# Request DTO

```plaintext id="u7nfrw"
CreatePurchaseRequest
```

---

# Response DTO

```plaintext id="3tqlbz"
PurchaseResponse
```

---

# 🚀 VALIDADORES

MUY recomendado.

---

# Responsabilidad

Validar:

✔ email
✔ password
✔ formatos
✔ tamaños
✔ fechas

---

# Beneficio

Los services quedan mucho más limpios.

---

# 🚀 JWT

---

# Access Token

```plaintext id="q7lvra"
15 minutos
```

---

# Refresh Token

```plaintext id="t2pfme"
7 días
```

---

# Claims importantes

```plaintext id="7tzy4m"
userId
email
role
groupIds
```

---

# 🚀 Seguridad

---

# Passwords

Usar:

## BCrypt

---

# Nunca guardar

❌ passwords planas
❌ JWT en DB
❌ secretos hardcodeados

---

# 🚀 Manejo profesional de errores

MUY importante.

---

# ❌ NO hacer

```plaintext id="r7nxdm"
try/catch por todos lados
```

---

# ✅ Crear exceptions custom

---

```plaintext id="v8tqws"
ValidationException
AuthException
ForbiddenException
NotFoundException
ConflictException
```

---

# 🚀 Centralizar errores

Usar:

## StatusPages

de Ktor.

---

# Entonces:

```plaintext id="p6ynfa"
throw ValidationException()
```

↓

Ktor responde automáticamente:

```json id="e9wqzs"
{
  "success": false,
  "message": "Email inválido"
}
```

---

# 🚀 Beneficios

✔ menos código repetido
✔ respuestas consistentes
✔ debugging simple
✔ API profesional

---

# 🚀 RESPUESTAS ESTÁNDAR

MUY importante.

---

# Todas las respuestas deberían tener estructura uniforme

---

# Success

```json id="r7cqlk"
{
  "success": true,
  "data": {}
}
```

---

# Error

```json id="d6pztm"
{
  "success": false,
  "message": "Compra no encontrada",
  "errorCode": "PURCHASE_NOT_FOUND"
}
```

---

# 🚀 BASE DE DATOS

---

# Recomendación importante

NO meter toda la lógica en PostgreSQL.

---

# PostgreSQL debe:

✔ persistir
✔ indexar
✔ relacionar

---

# Services deben:

✔ decidir
✔ calcular
✔ validar

---

# 🚀 TABLAS IMPORTANTES

---

# USERS

---

# PASSWORD_HISTORY

Para historial de contraseñas.

---

# GROUPS

Familiares / individuales / empresariales.

---

# PURCHASES

Compras principales.

---

# PURCHASE_PRODUCTS

Detalle de productos.

---

# BUDGETS

Presupuestos grupales.

---

# OFFERS

Promociones.

---

# TICKETS

Tickets OCR futuros.

---

# NOTIFICATIONS

Sistema completo de notificaciones.

---

# 🚀 NOTIFICACIONES

Tu diseño ya quedó MUY avanzado.

---

# Recomendación importante

Crear:

## NotificationService

Centralizado.

---

# Entonces:

```plaintext id="z4mvpt"
PurchaseService
↓
NotificationService.create()
```

---

# 🚀 Beneficio

✔ desacoplamiento
✔ reutilización
✔ limpieza

---

# 🚀 LOGGING

MUY importante.

---

# Agregar:

## CallLogging

---

# Y logs estructurados

Ejemplo:

```plaintext id="q3ztfa"
[INFO]
Purchase created
userId=123
total=12000
```

---

# 🚀 CONFIGURACIÓN

---

# Variables de entorno

MUY importante.

---

# Nunca hardcodear:

❌ JWT_SECRET
❌ DB_PASSWORD
❌ CLOUDINARY_SECRET

---

# Usar:

```plaintext id="u5xtpw"
.env
```

---

# 🚀 CLOUDINARY

Más adelante.

---

# Backend debe:

✔ subir imagen
✔ recibir URL
✔ guardar URL DB

---

# NO guardar imágenes binarias en PostgreSQL

---

# 🚀 Qué más agregaría

---

# ✅ Swagger / OpenAPI

MUY útil.

Documentación automática.

---

# ✅ Rate Limiting

Para evitar spam/login brute force.

---

# ✅ Soft Delete

```plaintext id="t6pmyq"
deleted
deleted_at
```

---

# ✅ Auditoría

```plaintext id="b4cxtn"
created_by
updated_by
updated_at
```

---

# ✅ Caching

Más adelante.

Para:

* estadísticas
* ofertas
* historial

---

# ✅ Docker

MUY recomendado desde el inicio.

---

# 🚀 Testing

---

# Unit Tests

Services.

---

# Integration Tests

Routes + DB.

---

# 🚀 Hosting recomendado

---

# Railway

Porque:

✔ fácil
✔ PostgreSQL incluido
✔ Kotlin funciona perfecto
✔ deploy simple

---

# 🚀 Flujo completo real

---

```plaintext id="f8wnrq"
Android Compose
↓
Ktor API
↓
Service Layer
↓
Repositories
↓
PostgreSQL
```

---

# 🚀 Resultado final

Vas a tener:

✔ backend profesional
✔ arquitectura limpia
✔ fácil de mantener
✔ coherente con Android
✔ preparado para crecer
✔ muy diferencial para el TP

