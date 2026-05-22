package com.misuper.backend.modules.products.services

import com.misuper.backend.database.tables.CategoriesTable
import com.misuper.backend.database.tables.ProductsTable
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.products.dto.CreateProductRequest
import com.misuper.backend.modules.products.dto.ProductResponse
import com.misuper.backend.modules.products.dto.UpdateProductRequest
import com.misuper.backend.modules.products.repositories.CategoryRepository
import com.misuper.backend.modules.products.repositories.ProductRepository
import com.misuper.backend.modules.products.validators.ProductValidator
import java.math.BigDecimal
import java.util.UUID

class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) {
    fun getAll(categoryId: UUID? = null): List<ProductResponse> {
        return productRepository.findAll(categoryId).map { row ->
val categoryRow = categoryRepository.findById(row[ProductsTable.categoryId].value)
            ProductResponse(
                id = row[ProductsTable.id].value.toString(),
                name = row[ProductsTable.name],
                price = row[ProductsTable.price],
                categoryId = row[ProductsTable.categoryId].value.toString(),
                categoryName = categoryRow?.get(CategoriesTable.name) ?: "Sin categoría",
                description = row[ProductsTable.description],
                imageUrl = row[ProductsTable.imageUrl],
                barcode = row[ProductsTable.barcode],
                active = row[ProductsTable.active]
            )
        }
    }

    fun getById(id: UUID): ProductResponse {
        val row = productRepository.findById(id)
            ?: throw NotFoundException("Producto no encontrado")
        val categoryRow = categoryRepository.findById(row[ProductsTable.categoryId].value)
        return ProductResponse(
            id = row[ProductsTable.id].value.toString(),
            name = row[ProductsTable.name],
            price = row[ProductsTable.price],
            categoryId = row[ProductsTable.categoryId].value.toString(),
            categoryName = categoryRow?.get(CategoriesTable.name) ?: "Sin categoría",
            description = row[ProductsTable.description],
            imageUrl = row[ProductsTable.imageUrl],
            barcode = row[ProductsTable.barcode],
            active = row[ProductsTable.active]
        )
    }

    fun create(request: CreateProductRequest): ProductResponse {
        ProductValidator.validateCreateProduct(request)

        val categoryId = UUID.fromString(request.categoryId)
        categoryRepository.findById(categoryId)
            ?: throw NotFoundException("Categoría no encontrada")

        val id = productRepository.create(
            nameVal = request.name,
            priceVal = BigDecimal.valueOf(request.price),
            categoryIdVal = categoryId,
            descriptionVal = request.description,
            imageUrlVal = request.imageUrl,
            barcodeVal = request.barcode
        )
        return getById(id)
    }

    fun update(id: UUID, request: UpdateProductRequest): ProductResponse {
        val existing = productRepository.findById(id)
            ?: throw NotFoundException("Producto no encontrado")

        ProductValidator.validateUpdateProduct(request)

        val categoryId = request.categoryId?.let { UUID.fromString(it) }
        categoryId?.let { cid ->
            categoryRepository.findById(cid)
                ?: throw NotFoundException("Categoría no encontrada")
        }

        productRepository.update(
            id = id,
            nameVal = request.name,
            priceVal = request.price?.let { BigDecimal.valueOf(it) },
            categoryIdVal = categoryId,
            descriptionVal = request.description,
            imageUrlVal = request.imageUrl,
            barcodeVal = request.barcode
        )
        return getById(id)
    }

    fun delete(id: UUID) {
        productRepository.findById(id)
            ?: throw NotFoundException("Producto no encontrado")
        productRepository.softDelete(id)
    }
}
