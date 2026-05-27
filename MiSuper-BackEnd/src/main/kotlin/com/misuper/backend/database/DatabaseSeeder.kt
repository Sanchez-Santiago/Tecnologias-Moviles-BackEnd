package com.misuper.backend.database

import com.misuper.backend.database.tables.CategoriesTable
import com.misuper.backend.database.tables.OffersTable
import com.misuper.backend.database.tables.ProductsTable
import com.misuper.backend.database.tables.StoresTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

object DatabaseSeeder {
    fun seed(db: org.jetbrains.exposed.v1.jdbc.Database) = transaction(db) {
        val supermercado = category("Supermercado", "Productos generales de supermercado", "shopping-cart")
        val limpieza = category("Limpieza", "Artículos de limpieza del hogar", "sparkles")
        val lacteos = category("Lácteos", "Leche, yogures y quesos", "milk")

        val disco = store("Disco", "Av. Colón 1200, Córdoba", "-")
        val carrefour = store("Carrefour", "Av. Rafael Núñez 3900, Córdoba", "-")
        val vea = store("Vea", "Bv. San Juan 450, Córdoba", "-")

        product("Leche larga vida 1L", BigDecimal("1250.00"), lacteos, "ESENCIAL", "Leche entera larga vida")
        product("Arroz largo fino 1kg", BigDecimal("1800.00"), supermercado, "ESENCIAL", "Arroz para comidas diarias")
        product("Fideos spaghetti 500g", BigDecimal("950.00"), supermercado, "PRIMARIO", "Pasta seca")
        product("Detergente 750ml", BigDecimal("1450.00"), limpieza, "PRIMARIO", "Detergente concentrado")
        product("Galletitas dulces", BigDecimal("1100.00"), supermercado, "SECUNDARIO", "Producto secundario")

        offer(
            carrefour,
            "15% en leche y lácteos",
            "Promoción activa en leche, yogures y quesos seleccionados",
            "PERCENTAGE",
            BigDecimal("15.00")
        )
        offer(
            disco,
            "2x1 en limpieza",
            "Oferta en detergente y productos de limpieza",
            "PERCENTAGE",
            BigDecimal("50.00")
        )
        offer(
            vea,
            "Ahorro semanal supermercado",
            "Descuentos generales en productos de almacén",
            "FIXED",
            BigDecimal("500.00")
        )
    }

    private fun category(name: String, description: String, icon: String): UUID {
        CategoriesTable.selectAll().where { CategoriesTable.name eq name }.singleOrNull()?.let {
            return it[CategoriesTable.id].value
        }
        return CategoriesTable.insert {
            it[CategoriesTable.name] = name
            it[CategoriesTable.description] = description
            it[CategoriesTable.icon] = icon
        }[CategoriesTable.id].value
    }

    private fun store(name: String, address: String, phone: String): UUID {
        StoresTable.selectAll().where { StoresTable.name eq name }.singleOrNull()?.let {
            return it[StoresTable.id].value
        }
        return StoresTable.insert {
            it[StoresTable.name] = name
            it[StoresTable.address] = address
            it[StoresTable.phone] = phone
        }[StoresTable.id].value
    }

    private fun product(name: String, price: BigDecimal, categoryId: UUID, priority: String, description: String) {
        if (ProductsTable.selectAll().where { ProductsTable.name eq name }.singleOrNull() != null) return
        ProductsTable.insert {
            it[ProductsTable.name] = name
            it[ProductsTable.price] = price
            it[ProductsTable.categoryId] = EntityID(categoryId, CategoriesTable)
            it[ProductsTable.priority] = priority
            it[ProductsTable.description] = description
        }
    }

    private fun offer(storeId: UUID, title: String, description: String, type: String, value: BigDecimal) {
        if (OffersTable.selectAll().where { OffersTable.title eq title }.singleOrNull() != null) return
        OffersTable.insert {
            it[OffersTable.storeId] = EntityID(storeId, StoresTable)
            it[OffersTable.title] = title
            it[OffersTable.description] = description
            it[OffersTable.discountType] = type
            it[OffersTable.discountValue] = value
            it[OffersTable.startDate] = LocalDateTime.now().minusDays(7)
            it[OffersTable.endDate] = LocalDateTime.now().plusDays(30)
        }
    }
}
