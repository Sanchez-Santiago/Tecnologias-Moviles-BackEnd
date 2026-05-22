package com.misuper.backend.modules.products.services

import com.misuper.backend.database.tables.CategoriesTable
import com.misuper.backend.exceptions.ConflictException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.products.dto.CategoryResponse
import com.misuper.backend.modules.products.dto.CreateCategoryRequest
import com.misuper.backend.modules.products.repositories.CategoryRepository
import com.misuper.backend.modules.products.validators.ProductValidator
import java.util.UUID

class CategoryService(private val categoryRepository: CategoryRepository) {

    fun getAll(): List<CategoryResponse> {
        return categoryRepository.findAll().map { row ->
            CategoryResponse(
                id = row[CategoriesTable.id].value.toString(),
                name = row[CategoriesTable.name],
                description = row[CategoriesTable.description],
                icon = row[CategoriesTable.icon],
                active = row[CategoriesTable.active]
            )
        }
    }

    fun getById(id: UUID): CategoryResponse {
        val row = categoryRepository.findById(id)
            ?: throw NotFoundException("Categoría no encontrada")
        return CategoryResponse(
            id = row[CategoriesTable.id].value.toString(),
            name = row[CategoriesTable.name],
            description = row[CategoriesTable.description],
            icon = row[CategoriesTable.icon],
            active = row[CategoriesTable.active]
        )
    }

    fun create(request: CreateCategoryRequest): CategoryResponse {
        ProductValidator.validateCreateCategory(request)

        val id = categoryRepository.create(
            nameVal = request.name,
            descriptionVal = request.description,
            iconVal = request.icon
        )
        return getById(id)
    }

    fun update(id: UUID, request: CreateCategoryRequest): CategoryResponse {
        val existing = categoryRepository.findById(id)
            ?: throw NotFoundException("Categoría no encontrada")

        categoryRepository.update(
            id = id,
            nameVal = request.name,
            descriptionVal = request.description,
            iconVal = request.icon
        )
        return getById(id)
    }

    fun delete(id: UUID) {
        val existing = categoryRepository.findById(id)
            ?: throw NotFoundException("Categoría no encontrada")
        categoryRepository.softDelete(id)
    }
}
