package com.misuper.backend.modules.docs

data class EndpointDoc(
    val method: String,
    val path: String,
    val auth: String,
    val description: String,
    val requestDto: String? = null,
    val requestExample: String? = null,
    val responseDto: String? = null,
    val responseExample: String? = null
)

data class ModuleDocs(
    val moduleName: String,
    val basePath: String,
    val endpoints: List<EndpointDoc>
)

fun allDocs(): List<ModuleDocs> = listOf(
    ModuleDocs("Salud", "", listOf(
        EndpointDoc("GET", "/", "Pública", "Estado del servidor", responseDto = "HealthResponse", responseExample = """
{
  "status": "ok",
  "server": { "port": 8080, "host": "localhost" },
  "database": { "connected": true, "activeConnections": 2, "idleConnections": 8 },
  "uptime": 3600,
  "timestamp": "2026-06-16T12:00:00Z"
}
        """.trimIndent()),
        EndpointDoc("GET", "/health", "Pública", "Estado del servidor (alias)", responseDto = "HealthResponse", responseExample = """
{
  "status": "ok",
  "server": { "port": 8080, "host": "localhost" },
  "database": { "connected": true, "activeConnections": 2, "idleConnections": 8 },
  "uptime": 3600,
  "timestamp": "2026-06-16T12:00:00Z"
}
        """.trimIndent()),
        EndpointDoc("GET", "/openapi.yaml", "Pública", "Especificación OpenAPI en YAML", responseDto = "text/yaml")
    )),
    ModuleDocs("Auth", "/api/auth", listOf(
        EndpointDoc("POST", "/api/auth/register", "Pública (rate-limit: 10/min)", "Registrar un nuevo usuario", "RegisterRequest", """
{
  "email": "usuario@ejemplo.com",
  "password": "MiPassword123",
  "fullName": "Juan Pérez"
}
        """.trimIndent(), "AuthResponse", """
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJl...",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "Juan Pérez",
      "email": "usuario@ejemplo.com",
      "role": "MEMBER",
      "verified": false,
      "createdAt": "2026-06-16T12:00:00"
    }
  }
}
        """.trimIndent()),
        EndpointDoc("POST", "/api/auth/login", "Pública (rate-limit: 10/min)", "Iniciar sesión", "LoginRequest", """
{
  "email": "usuario@ejemplo.com",
  "password": "MiPassword123"
}
        """.trimIndent(), "AuthResponse", """
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJl...",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "Juan Pérez",
      "email": "usuario@ejemplo.com",
      "role": "MEMBER",
      "verified": false,
      "createdAt": "2026-06-16T12:00:00"
    }
  }
}
        """.trimIndent()),
        EndpointDoc("POST", "/api/auth/refresh", "Pública", "Renovar token de acceso", "RefreshRequest", """
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJl..."
}
        """.trimIndent(), "AuthResponse", """
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "nuevo-refresh-token...",
    "user": { ... }
  }
}
        """.trimIndent())
    )),
    ModuleDocs("Usuarios", "/api/users", listOf(
        EndpointDoc("GET", "/api/users/me", "JWT", "Obtener perfil del usuario autenticado", responseDto = "UserProfileResponse", responseExample = """
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "Juan Pérez",
    "email": "usuario@ejemplo.com",
    "phone": null,
    "alternatePhone": null,
    "profileImageUrl": null,
    "role": "MEMBER",
    "verified": true,
    "createdAt": "2026-06-16T12:00:00"
  }
}
        """.trimIndent()),
        EndpointDoc("PUT", "/api/users/me", "JWT", "Actualizar perfil del usuario autenticado", "UpdateProfileRequest", """
{
  "fullName": "Juan Pérez",
  "email": "nuevo@ejemplo.com",
  "phone": "+5491123456789",
  "alternatePhone": null,
  "profileImageUrl": null
}
        """.trimIndent(), "UserProfileResponse"),
        EndpointDoc("PUT", "/api/users/me/password", "JWT", "Cambiar contraseña", "ChangePasswordRequest", """
{
  "currentPassword": "MiPassword123",
  "newPassword": "NuevaPassword456"
}
        """.trimIndent(), "ApiResponse", """
{
  "success": true,
  "data": null,
  "message": "Contraseña actualizada correctamente"
}
        """.trimIndent())
    )),
    ModuleDocs("Grupos", "/api/groups", listOf(
        EndpointDoc("GET", "/api/groups", "JWT", "Listar grupos del usuario autenticado", responseDto = "List<GroupResponse>", responseExample = """
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "name": "Mi Familia",
      "ownerId": "550e8400-e29b-41d4-a716-446655440000",
      "type": "FAMILY",
      "isPersonal": false,
      "memberCount": 4,
      "role": "ADMIN",
      "createdAt": "2026-01-01T00:00:00"
    }
  ]
}
        """.trimIndent()),
        EndpointDoc("GET", "/api/groups/{id}", "JWT", "Obtener detalle de un grupo", responseDto = "GroupDetailResponse", responseExample = """
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Mi Familia",
    "ownerId": "550e8400-e29b-41d4-a716-446655440000",
    "type": "FAMILY",
    "isPersonal": false,
    "members": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "fullName": "Juan Pérez",
        "email": "juan@ejemplo.com",
        "role": "ADMIN",
        "active": true,
        "createdAt": "2026-01-01T00:00:00"
      }
    ],
    "createdAt": "2026-01-01T00:00:00"
  }
}
        """.trimIndent()),
        EndpointDoc("POST", "/api/groups", "JWT", "Crear un grupo", "CreateGroupRequest", """
{
  "name": "Mi Familia",
  "type": "FAMILY"
}
        """.trimIndent(), "GroupDetailResponse"),
        EndpointDoc("PUT", "/api/groups/{id}", "JWT", "Actualizar un grupo", "UpdateGroupRequest", """
{
  "name": "Nuevo Nombre"
}
        """.trimIndent(), "GroupDetailResponse"),
        EndpointDoc("DELETE", "/api/groups/{id}", "JWT", "Eliminar un grupo", responseDto = "ApiResponse", responseExample = """
{
  "success": true,
  "data": null,
  "message": "Grupo eliminado"
}
        """.trimIndent()),
        EndpointDoc("POST", "/api/groups/{id}/members", "JWT", "Agregar miembro al grupo", "AddMemberRequest", """
{
  "userId": "550e8400-e29b-41d4-a716-446655440002"
}
        """.trimIndent(), "GroupDetailResponse"),
        EndpointDoc("DELETE", "/api/groups/{id}/members/{memberId}", "JWT", "Eliminar miembro del grupo", responseDto = "ApiResponse"),
        EndpointDoc("POST", "/api/groups/{id}/invitations", "JWT", "Invitar usuario al grupo", "InviteRequest", """
{
  "userId": "550e8400-e29b-41d4-a716-446655440002"
}
        """.trimIndent(), "InvitationResponse"),
        EndpointDoc("GET", "/api/groups/invitations", "JWT", "Listar invitaciones del usuario", responseDto = "List<InvitationResponse>"),
        EndpointDoc("POST", "/api/groups/invitations/{id}/accept", "JWT", "Aceptar invitación", responseDto = "InvitationResponse"),
        EndpointDoc("POST", "/api/groups/invitations/{id}/reject", "JWT", "Rechazar invitación", responseDto = "InvitationResponse")
    )),
    ModuleDocs("Periodos", "/api/groups/{groupId}/periods", listOf(
        EndpointDoc("GET", "/api/groups/{groupId}/periods/current", "JWT", "Obtener periodo actual del grupo", responseDto = "PeriodResponse"),
        EndpointDoc("GET", "/api/groups/{groupId}/periods", "JWT", "Listar periodos del grupo", responseDto = "List<PeriodResponse>"),
        EndpointDoc("GET", "/api/groups/{groupId}/periods/{id}", "JWT", "Obtener detalle de un periodo", responseDto = "PeriodResponse"),
        EndpointDoc("POST", "/api/groups/{groupId}/periods", "JWT", "Crear un periodo", "CreatePeriodRequest", """
{
  "name": "Junio 2026",
  "cycleType": "MONTHLY"
}
        """.trimIndent(), "PeriodResponse"),
        EndpointDoc("POST", "/api/groups/{groupId}/periods/{id}/close", "JWT", "Cerrar un periodo", "ClosePeriodRequest", """
{
  "finalBalance": 15000.50
}
        """.trimIndent(), "PeriodResponse")
    )),
    ModuleDocs("Tiendas", "/api/stores", listOf(
        EndpointDoc("GET", "/api/stores", "JWT", "Listar todas las tiendas", responseDto = "List<StoreResponse>", responseExample = """
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440010",
      "name": "Mercadona",
      "address": "Calle Principal 123"
    }
  ]
}
        """.trimIndent()),
        EndpointDoc("GET", "/api/stores/{id}", "JWT", "Obtener detalle de una tienda", responseDto = "StoreResponse"),
        EndpointDoc("POST", "/api/stores", "JWT + ADMIN", "Crear una tienda", "CreateStoreRequest", """
{
  "name": "Mercadona",
  "address": "Calle Principal 123"
}
        """.trimIndent(), "StoreResponse"),
        EndpointDoc("PUT", "/api/stores/{id}", "JWT + ADMIN", "Actualizar una tienda", "UpdateStoreRequest", """
{
  "name": "Mercadona Centro",
  "address": "Av. Libertador 456"
}
        """.trimIndent(), "StoreResponse"),
        EndpointDoc("DELETE", "/api/stores/{id}", "JWT + ADMIN", "Eliminar una tienda", responseDto = "ApiResponse")
    )),
    ModuleDocs("Productos", "/api/products", listOf(
        EndpointDoc("GET", "/api/products", "JWT", "Listar productos (opcional ?categoryId=)", responseDto = "List<ProductResponse>", responseExample = """
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440020",
      "name": "Leche Entera",
      "barcode": "7791234567890",
      "brand": "La Serenísima",
      "categoryId": "cat-001",
      "categoryName": "Lácteos",
      "unit": "L",
      "estimatedPrice": 1.20,
      "type": "ESSENTIAL",
      "active": true
    }
  ]
}
        """.trimIndent()),
        EndpointDoc("GET", "/api/products/{id}", "JWT", "Obtener detalle de un producto", responseDto = "ProductResponse"),
        EndpointDoc("POST", "/api/products", "JWT + ADMIN", "Crear un producto", "CreateProductRequest", """
{
  "name": "Leche Entera",
  "barcode": "7791234567890",
  "description": "Leche larga vida",
  "brand": "La Serenísima",
  "categoryId": "cat-001",
  "unit": "L",
  "estimatedPrice": 1.20,
  "type": "ESSENTIAL"
}
        """.trimIndent(), "ProductResponse"),
        EndpointDoc("PUT", "/api/products/{id}", "JWT + ADMIN", "Actualizar un producto", "UpdateProductRequest"),
        EndpointDoc("DELETE", "/api/products/{id}", "JWT + ADMIN", "Eliminar un producto", responseDto = "ApiResponse")
    )),
    ModuleDocs("Categorías", "/api/categories", listOf(
        EndpointDoc("GET", "/api/categories", "JWT", "Listar categorías", responseDto = "List<CategoryResponse>", responseExample = """
{
  "success": true,
  "data": [
    {
      "id": "cat-001",
      "name": "Lácteos",
      "description": "Productos lácteos",
      "icon": "🥛"
    }
  ]
}
        """.trimIndent()),
        EndpointDoc("GET", "/api/categories/{id}", "JWT", "Obtener detalle de categoría", responseDto = "CategoryResponse"),
        EndpointDoc("POST", "/api/categories", "JWT + ADMIN", "Crear una categoría", "CreateCategoryRequest", """
{
  "name": "Lácteos",
  "description": "Productos lácteos",
  "icon": "🥛"
}
        """.trimIndent(), "CategoryResponse"),
        EndpointDoc("PUT", "/api/categories/{id}", "JWT + ADMIN", "Actualizar una categoría", "CreateCategoryRequest"),
        EndpointDoc("DELETE", "/api/categories/{id}", "JWT + ADMIN", "Eliminar una categoría", responseDto = "ApiResponse")
    )),
    ModuleDocs("Compras", "/api/purchases", listOf(
        EndpointDoc("GET", "/api/purchases", "JWT", "Listar compras de un grupo (?groupId=)", responseDto = "List<PurchaseResponse>", responseExample = """
{
  "success": true,
  "data": [
    {
      "id": "pur-001",
      "groupId": "grp-001",
      "periodId": "per-001",
      "storeId": "store-001",
      "storeName": "Mercadona",
      "createdBy": "usr-001",
      "userName": "Juan Pérez",
      "total": 42.50,
      "status": "CREATED",
      "items": [
        {
          "id": "item-001",
          "productId": "prod-001",
          "productName": "Leche Entera",
          "quantity": 2,
          "price": 1.20
        }
      ],
      "purchaseDate": "2026-06-16T12:00:00",
      "createdAt": "2026-06-16T12:00:00"
    }
  ]
}
        """.trimIndent()),
        EndpointDoc("GET", "/api/purchases/period/{periodId}", "JWT", "Listar compras de un periodo (?groupId=)", responseDto = "List<PurchaseResponse>"),
        EndpointDoc("GET", "/api/purchases/{id}", "JWT", "Obtener detalle de una compra", responseDto = "PurchaseResponse"),
        EndpointDoc("POST", "/api/purchases", "JWT", "Crear una compra", "CreatePurchaseRequest", """
{
  "groupId": "grp-001",
  "storeId": "store-001",
  "items": [
    {
      "productId": "prod-001",
      "productName": "Leche Entera",
      "quantity": 2,
      "price": 1.20
    }
  ]
}
        """.trimIndent(), "PurchaseResponse"),
        EndpointDoc("PUT", "/api/purchases/{id}", "JWT", "Actualizar una compra", "UpdatePurchaseRequest", """
{
  "storeId": "store-002"
}
        """.trimIndent(), "PurchaseResponse"),
        EndpointDoc("POST", "/api/purchases/{id}/cancel", "JWT", "Cancelar una compra", responseDto = "ApiResponse", responseExample = """
{
  "success": true,
  "data": null,
  "message": "Compra cancelada"
}
        """.trimIndent())
    )),
    ModuleDocs("Tickets (fotos de compras)", "/api/tickets", listOf(
        EndpointDoc("GET", "/api/tickets/purchase/{purchaseId}", "JWT", "Obtener tickets de una compra", responseDto = "List<TicketResponse>", responseExample = """
{
  "success": true,
  "data": [
    {
      "id": "tkt-001",
      "purchaseId": "pur-001",
      "imageUrl": "data:image/jpeg;base64,/9j/4AAQ...",
      "ocrProcessed": true,
      "ocrText": "Mercadona",
      "confidence": 0.95,
      "createdAt": "2026-06-16T12:00:00",
      "analysis": {
        "id": "anl-001",
        "aiReport": "[{\"name\":\"Leche\",\"quantity\":2,\"unitPrice\":1.2,\"totalPrice\":2.4}]",
        "extractedStore": "Mercadona",
        "extractedTotal": 42.50,
        "createdAt": "2026-06-16T12:00:00"
      }
    }
  ]
}
        """.trimIndent()),
        EndpointDoc("GET", "/api/tickets/{id}", "JWT", "Obtener detalle de un ticket", responseDto = "TicketResponse"),
        EndpointDoc("POST", "/api/tickets/upload", "JWT", "Subir imagen de ticket (base64)", "UploadTicketRequest", """
{
  "purchaseId": "550e8400-e29b-41d4-a716-446655440030",
  "imageBase64": "/9j/4AAQSkZJRg...",
  "mimeType": "image/jpeg"
}
        """.trimIndent(), "TicketResponse"),
        EndpointDoc("POST", "/api/tickets/analyze", "JWT", "Analizar imagen con IA sin guardar", "AnalyzeTicketImageRequest", """
{
  "imageBase64": "/9j/4AAQSkZJRg...",
  "mimeType": "image/jpeg"
}
        """.trimIndent(), "AnalyzeTicketImageResponse", """
{
  "success": true,
  "data": {
    "storeName": "Mercadona",
    "purchaseDate": "2026-06-15",
    "total": 42.50,
    "products": [
      { "name": "Leche", "quantity": 2, "unitPrice": 1.20, "totalPrice": 2.40 }
    ]
  }
}
        """.trimIndent()),
        EndpointDoc("POST", "/api/tickets/{id}/analysis", "JWT", "Guardar resultado de análisis manual", "SaveAnalysisRequest", """
{
  "extractedStore": "Mercadona",
  "extractedTotal": 42.50,
  "aiReport": "Productos detectados correctamente"
}
        """.trimIndent(), "TicketResponse")
    )),
    ModuleDocs("Presupuestos", "/api/budgets", listOf(
        EndpointDoc("GET", "/api/budgets", "JWT", "Listar presupuestos de un grupo (?groupId=)", responseDto = "List<BudgetResponse>"),
        EndpointDoc("GET", "/api/budgets/{id}", "JWT", "Obtener detalle de un presupuesto", responseDto = "BudgetResponse"),
        EndpointDoc("POST", "/api/budgets", "JWT", "Crear un presupuesto", "CreateBudgetRequest", """
{
  "groupId": "grp-001",
  "name": "Presupuesto Junio",
  "totalAmount": 50000.00,
  "period": "MONTHLY",
  "startDate": "2026-06-01",
  "endDate": "2026-06-30",
  "items": [
    { "categoryId": "cat-001", "amount": 15000.00 }
  ]
}
        """.trimIndent(), "BudgetResponse"),
        EndpointDoc("PUT", "/api/budgets/{id}", "JWT", "Actualizar un presupuesto", "UpdateBudgetRequest"),
        EndpointDoc("PATCH", "/api/budgets/{id}/activate", "JWT", "Activar un presupuesto", responseDto = "BudgetResponse"),
        EndpointDoc("DELETE", "/api/budgets/{id}", "JWT", "Eliminar un presupuesto", responseDto = "ApiResponse")
    )),
    ModuleDocs("Listas de Compras", "/api/shopping-lists", listOf(
        EndpointDoc("GET", "/api/shopping-lists", "JWT", "Listar listas de un grupo (?groupId=)", responseDto = "List<ShoppingListResponse>"),
        EndpointDoc("GET", "/api/shopping-lists/{id}", "JWT", "Obtener detalle de una lista", responseDto = "ShoppingListResponse"),
        EndpointDoc("POST", "/api/shopping-lists", "JWT", "Crear una lista de compras", "CreateShoppingListRequest", """
{
  "groupId": "grp-001",
  "name": "Lista del super",
  "description": "Para la cena del sábado"
}
        """.trimIndent(), "ShoppingListResponse"),
        EndpointDoc("PUT", "/api/shopping-lists/{id}", "JWT", "Actualizar una lista", "UpdateShoppingListRequest"),
        EndpointDoc("DELETE", "/api/shopping-lists/{id}", "JWT", "Eliminar una lista", responseDto = "ApiResponse"),
        EndpointDoc("POST", "/api/shopping-lists/{id}/products", "JWT", "Agregar producto a la lista", "AddProductRequest", """
{
  "productId": "prod-001",
  "quantity": 2,
  "notes": "Marca La Serenísima"
}
        """.trimIndent(), "ShoppingListResponse"),
        EndpointDoc("PUT", "/api/shopping-lists/{id}/products/{productId}", "JWT", "Actualizar producto en lista", "UpdateProductRequest"),
        EndpointDoc("DELETE", "/api/shopping-lists/{id}/products/{productId}", "JWT", "Eliminar producto de la lista", responseDto = "ShoppingListResponse")
    )),
    ModuleDocs("Ofertas", "/api/offers", listOf(
        EndpointDoc("GET", "/api/offers", "JWT", "Listar todas las ofertas (?storeId=)", responseDto = "List<OfferResponse>"),
        EndpointDoc("GET", "/api/offers/active", "JWT", "Listar ofertas activas (?storeId=)", responseDto = "List<OfferResponse>"),
        EndpointDoc("GET", "/api/offers/{id}", "JWT", "Obtener detalle de una oferta", responseDto = "OfferResponse"),
        EndpointDoc("POST", "/api/offers", "JWT + ADMIN", "Crear una oferta", "CreateOfferRequest", """
{
  "storeId": "store-001",
  "title": "2x1 en Leche",
  "description": "Llevá 2 pagá 1",
  "discountType": "PERCENTAGE",
  "discountValue": 50.0,
  "startDate": "2026-06-01",
  "endDate": "2026-06-30"
}
        """.trimIndent(), "OfferResponse"),
        EndpointDoc("PUT", "/api/offers/{id}", "JWT + ADMIN", "Actualizar una oferta", "UpdateOfferRequest"),
        EndpointDoc("DELETE", "/api/offers/{id}", "JWT + ADMIN", "Eliminar una oferta", responseDto = "ApiResponse"),
        EndpointDoc("POST", "/api/offers/ai-suggest", "JWT", "Sugerir ofertas con IA", "AiOfferSuggestionRequest", """
{
  "productNames": ["Leche", "Pan", "Huevos"],
  "storeId": "store-001"
}
        """.trimIndent(), "AiOfferSuggestionResponse"),
        EndpointDoc("GET", "/api/offers/match", "JWT", "Buscar ofertas para productos (?productId[]=&storeId=)", responseDto = "List<MatchedOfferResponse>")
    )),
    ModuleDocs("Transacciones", "/api/transactions", listOf(
        EndpointDoc("GET", "/api/transactions", "JWT", "Listar transacciones (?groupId=)", responseDto = "List<FinancialTransactionResponse>"),
        EndpointDoc("GET", "/api/transactions/summary", "JWT", "Resumen financiero (?groupId=)", responseDto = "FinancialSummaryResponse", responseExample = """
{
  "success": true,
  "data": {
    "income": 50000.00,
    "expense": 35000.00,
    "balance": 15000.00
  }
}
        """.trimIndent()),
        EndpointDoc("POST", "/api/transactions", "JWT", "Crear una transacción", "CreateFinancialTransactionRequest", """
{
  "groupId": "grp-001",
  "type": "EXPENSE",
  "category": "FOOD",
  "amount": 2500.00,
  "description": "Compra semanal",
  "transactionDate": "2026-06-16"
}
        """.trimIndent(), "FinancialTransactionResponse"),
        EndpointDoc("DELETE", "/api/transactions/{id}", "JWT", "Eliminar una transacción", responseDto = "ApiResponse")
    )),
    ModuleDocs("Notificaciones", "/api/notifications", listOf(
        EndpointDoc("GET", "/api/notifications", "JWT", "Listar notificaciones del usuario", responseDto = "List<NotificationResponse>"),
        EndpointDoc("GET", "/api/notifications/unread/count", "JWT", "Contar notificaciones no leídas", responseDto = "ApiResponse", responseExample = """
{
  "success": true,
  "data": { "count": 3 }
}
        """.trimIndent()),
        EndpointDoc("PUT", "/api/notifications/{id}/read", "JWT", "Marcar notificación como leída", responseDto = "NotificationResponse"),
        EndpointDoc("PUT", "/api/notifications/read-all", "JWT", "Marcar todas como leídas", responseDto = "ApiResponse"),
        EndpointDoc("DELETE", "/api/notifications/{id}", "JWT", "Eliminar una notificación", responseDto = "ApiResponse")
    )),
    ModuleDocs("Estadísticas", "/api/statistics", listOf(
        EndpointDoc("GET", "/api/statistics/group/{groupId}/spending-by-category", "JWT", "Gastos por categoría", responseDto = "List<SpendingByCategory>"),
        EndpointDoc("GET", "/api/statistics/group/{groupId}/spending-by-importance", "JWT", "Gastos por importancia", responseDto = "List<SpendingByImportance>"),
        EndpointDoc("GET", "/api/statistics/group/{groupId}/spending-by-store", "JWT", "Gastos por tienda", responseDto = "List<SpendingByStore>"),
        EndpointDoc("GET", "/api/statistics/group/{groupId}/monthly-summary", "JWT", "Resumen mensual de gastos", responseDto = "List<MonthlySummary>"),
        EndpointDoc("GET", "/api/statistics/group/{groupId}/most-frequent-store", "JWT", "Tienda más frecuentada", responseDto = "StoreFrequency"),
        EndpointDoc("GET", "/api/statistics/group/{groupId}/budget-progress", "JWT", "Progreso de presupuestos", responseDto = "List<BudgetProgress>")
    ))
)

fun renderDocsHtml(port: Int, startTime: Long): String = """<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>MiSuper API - Documentación</title>
<style>
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f5f7fa; color: #1a1a2e; line-height: 1.6; }
  .header { background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%); color: #fff; padding: 2rem 0; text-align: center; }
  .header h1 { font-size: 2rem; margin-bottom: 0.5rem; }
  .header p { opacity: 0.85; font-size: 0.95rem; }
  .status-bar { display: flex; justify-content: center; gap: 2rem; margin-top: 1rem; font-size: 0.85rem; }
  .status-bar .badge { display: inline-block; padding: 0.2rem 0.6rem; border-radius: 999px; font-size: 0.75rem; font-weight: 600; }
  .badge.online { background: #22c55e; color: #fff; }
  .container { max-width: 1200px; margin: 0 auto; padding: 2rem 1rem; }
  .module { background: #fff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); margin-bottom: 1.5rem; overflow: hidden; }
  .module-header { background: #f8fafc; padding: 1rem 1.5rem; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center; }
  .module-header h2 { font-size: 1.1rem; color: #1a1a2e; }
  .module-header .base-path { font-family: monospace; font-size: 0.85rem; color: #64748b; }
  .endpoint { border-bottom: 1px solid #f1f5f9; padding: 1rem 1.5rem; }
  .endpoint:last-child { border-bottom: none; }
  .endpoint-row { display: flex; align-items: flex-start; gap: 1rem; flex-wrap: wrap; }
  .method { display: inline-block; padding: 0.15rem 0.5rem; border-radius: 4px; font-weight: 700; font-size: 0.75rem; font-family: monospace; text-transform: uppercase; letter-spacing: 0.5px; color: #fff; min-width: 4rem; text-align: center; }
  .method.get { background: #3b82f6; }
  .method.post { background: #22c55e; }
  .method.put { background: #eab308; color: #1a1a2e; }
  .method.patch { background: #a855f7; }
  .method.delete { background: #ef4444; }
  .path-text { font-family: monospace; font-size: 0.9rem; color: #1a1a2e; word-break: break-all; flex: 1; }
  .auth-badge { display: inline-block; padding: 0.15rem 0.5rem; border-radius: 4px; font-size: 0.7rem; font-weight: 600; white-space: nowrap; }
  .auth-jwt { background: #fef3c7; color: #92400e; }
  .auth-admin { background: #fce7f3; color: #9d174d; }
  .auth-public { background: #dbeafe; color: #1e40af; }
  .auth-rate { background: #ede9fe; color: #5b21b6; }
  .description { margin-top: 0.3rem; font-size: 0.85rem; color: #64748b; }
  .details { margin-top: 0.75rem; display: none; }
  .details.show { display: block; }
  .toggle-btn { background: none; border: 1px solid #e2e8f0; border-radius: 4px; padding: 0.25rem 0.75rem; font-size: 0.75rem; color: #64748b; cursor: pointer; }
  .toggle-btn:hover { background: #f1f5f9; }
  .dto-section { margin-top: 0.75rem; }
  .dto-label { font-size: 0.75rem; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 0.25rem; }
  pre { background: #1e293b; color: #e2e8f0; padding: 0.75rem 1rem; border-radius: 8px; overflow-x: auto; font-size: 0.8rem; line-height: 1.5; margin-top: 0.25rem; }
  code { font-family: 'JetBrains Mono', 'Fira Code', monospace; }
  .footer { text-align: center; padding: 2rem; color: #94a3b8; font-size: 0.8rem; }
  .endpoint-tools { display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; }
  @media (max-width: 768px) {
    .endpoint-row { flex-direction: column; gap: 0.3rem; }
    .header h1 { font-size: 1.5rem; }
  }
</style>
</head>
<body>
<div class="header">
  <h1>🏪 MiSuper API</h1>
  <p>Documentación interactiva de la API REST</p>
  <div class="status-bar">
    <span><span class="badge online">Online</span> Servidor activo</span>
    <span>Puerto: $port</span>
    <span>Uptime: ${startTime}s</span>
  </div>
</div>
<div class="container">
  <p style="margin-bottom:1.5rem;color:#64748b;font-size:0.85rem;text-align:center;">
    Formato: <strong>JSON</strong> &middot; Autenticación: <strong>Bearer JWT</strong> &middot; Base: <code style="background:#f1f5f9;padding:0.1rem 0.4rem;border-radius:4px;">/api/...</code>
  </p>
${allDocs().joinToString("\n") { module -> moduleHtml(module) }}
</div>
<div class="footer">
  MiSuper Backend &mdash; Generado dinámicamente
</div>
<script>
  document.querySelectorAll('.toggle-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const details = btn.nextElementSibling;
      details.classList.toggle('show');
      btn.textContent = details.classList.contains('show') ? 'Ocultar' : 'Ver más';
    });
  });
</script>
</body>
</html>"""

private fun moduleHtml(module: ModuleDocs): String = buildString {
    appendLine("""<div class="module">""")
    appendLine("""  <div class="module-header">""")
    appendLine("""    <h2>${module.moduleName}</h2>""")
    appendLine("""    <span class="base-path">${module.basePath}</span>""")
    appendLine("""  </div>""")
    for (ep in module.endpoints) {
        appendLine("""  <div class="endpoint">""")
        appendLine("""    <div class="endpoint-row">""")
        appendLine("""      <span class="method ${ep.method.lowercase()}">${ep.method}</span>""")
        appendLine("""      <span class="path-text">${ep.path}</span>""")
        appendLine("""      <div class="endpoint-tools">""")
        appendLine("""        <span class="auth-badge ${authClass(ep.auth)}">${ep.auth}</span>""")
        appendLine("""      </div>""")
        appendLine("""    </div>""")
        appendLine("""    <div class="description">${ep.description}</div>""")
        appendLine("""    <button class="toggle-btn">Ver más</button>""")
        appendLine("""    <div class="details">""")
        if (ep.requestDto != null) {
            appendLine("""      <div class="dto-section">""")
            appendLine("""        <div class="dto-label">Request — ${ep.requestDto}</div>""")
            if (ep.requestExample != null) {
                appendLine("""        <pre><code>${escapeHtml(ep.requestExample)}</code></pre>""")
            }
            appendLine("""      </div>""")
        }
        if (ep.responseDto != null) {
            appendLine("""      <div class="dto-section">""")
            appendLine("""        <div class="dto-label">Response — ${ep.responseDto}</div>""")
            if (ep.responseExample != null) {
                appendLine("""        <pre><code>${escapeHtml(ep.responseExample)}</code></pre>""")
            }
            appendLine("""      </div>""")
        }
        appendLine("""    </div>""")
        appendLine("""  </div>""")
    }
    appendLine("""</div>""")
}

private fun authClass(auth: String): String = when {
    auth.contains("ADMIN") -> "auth-admin"
    auth.contains("rate") || auth.contains("Pública") -> "auth-rate"
    auth == "JWT" -> "auth-jwt"
    auth.contains("Pública") -> "auth-public"
    else -> "auth-jwt"
}

private fun escapeHtml(text: String): String = text
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
    .replace("'", "&#39;")
