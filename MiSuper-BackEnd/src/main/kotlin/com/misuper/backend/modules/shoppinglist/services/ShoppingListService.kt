package com.misuper.backend.modules.shoppinglist.services

import com.misuper.backend.database.tables.ProductsTable
import com.misuper.backend.database.tables.ShoppingListItemsTable
import com.misuper.backend.database.tables.ShoppingListsTable
import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.groups.repositories.GroupRepository
import com.misuper.backend.modules.shoppinglist.dto.*
import com.misuper.backend.modules.shoppinglist.repositories.ShoppingListRepository
import org.jetbrains.exposed.v1.core.ResultRow
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class ShoppingListService(
    private val shoppingListRepository: ShoppingListRepository,
    private val groupRepository: GroupRepository
) {
    fun getByGroup(groupId: UUID, userId: UUID, from: LocalDate? = null, to: LocalDate? = null): List<ShoppingListResponse> {
        groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val lists = if (from != null || to != null) {
            shoppingListRepository.findByGroupIdAndDateRange(groupId, from, to)
        } else {
            shoppingListRepository.findByGroupId(groupId)
        }
        return lists.map { buildResponse(it) }
    }

    fun getById(listId: UUID, userId: UUID): ShoppingListResponse {
        val row = shoppingListRepository.findById(listId)
            ?: throw NotFoundException("Lista de compra no encontrada")

        val groupId = row[ShoppingListsTable.groupId].value
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        return buildResponse(row)
    }

    fun create(userId: UUID, request: CreateShoppingListRequest): ShoppingListResponse {
        val groupId = UUID.fromString(request.groupId)
        groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val listId = shoppingListRepository.create(
            groupIdVal = groupId,
            createdByVal = userId,
            nameVal = request.name,
            descriptionVal = request.description
        )

        return getById(listId, userId)
    }

    fun update(id: UUID, userId: UUID, request: UpdateShoppingListRequest): ShoppingListResponse {
        val row = shoppingListRepository.findById(id)
            ?: throw NotFoundException("Lista de compra no encontrada")

        val groupId = row[ShoppingListsTable.groupId].value
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        shoppingListRepository.update(id, request.name, request.description)
        return getById(id, userId)
    }

    fun delete(id: UUID, userId: UUID) {
        val row = shoppingListRepository.findById(id)
            ?: throw NotFoundException("Lista de compra no encontrada")

        val groupId = row[ShoppingListsTable.groupId].value
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        shoppingListRepository.delete(id)
    }

    fun addProduct(listId: UUID, userId: UUID, request: AddProductRequest): ShoppingListResponse {
        val row = shoppingListRepository.findById(listId)
            ?: throw NotFoundException("Lista de compra no encontrada")

        val groupId = row[ShoppingListsTable.groupId].value
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val productId = request.productId?.let { UUID.fromString(it) }

        if (productId != null) {
            shoppingListRepository.findProductById(productId)
                ?: throw NotFoundException("Producto no encontrado")
        } else if (request.customProductName.isNullOrBlank()) {
            throw NotFoundException("Debe proporcionar productId o customProductName")
        }

        shoppingListRepository.addProduct(
            shoppingListIdVal = listId,
            productIdVal = productId,
            customProductNameVal = request.customProductName,
            estimatedPriceVal = request.estimatedPrice?.let { BigDecimal.valueOf(it) },
            estimatedQuantityVal = request.estimatedQuantity?.let { BigDecimal.valueOf(it) },
            estimatedBrandVal = request.estimatedBrand,
            unitVal = request.unit,
            priorityVal = request.priority,
            notesVal = request.notes
        )

        return getById(listId, userId)
    }

    fun updateProduct(listId: UUID, productUuid: UUID, userId: UUID, request: UpdateProductRequest): ShoppingListResponse {
        val row = shoppingListRepository.findById(listId)
            ?: throw NotFoundException("Lista de compra no encontrada")

        val groupId = row[ShoppingListsTable.groupId].value
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val products = shoppingListRepository.getProducts(listId)
        val productRow = products.firstOrNull { it[ShoppingListItemsTable.id].value == productUuid }
            ?: throw NotFoundException("Producto no encontrado en la lista")

        shoppingListRepository.updateProduct(
            id = productUuid,
            checkedVal = request.checked,
            notesVal = request.notes
        )

        return getById(listId, userId)
    }

    fun deleteProduct(listId: UUID, productUuid: UUID, userId: UUID): ShoppingListResponse {
        val row = shoppingListRepository.findById(listId)
            ?: throw NotFoundException("Lista de compra no encontrada")

        val groupId = row[ShoppingListsTable.groupId].value
        groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        shoppingListRepository.deleteProduct(productUuid)
        return getById(listId, userId)
    }

    private fun buildResponse(row: ResultRow): ShoppingListResponse {
        val listId = row[ShoppingListsTable.id].value
        val groupId = row[ShoppingListsTable.groupId].value
        val createdBy = row[ShoppingListsTable.createdBy]?.value

        val products = shoppingListRepository.getProducts(listId).map { prod ->
            val productId = prod[ShoppingListItemsTable.productId]?.value
            val productRow = productId?.let { shoppingListRepository.findProductById(it) }
            ShoppingListProductResponse(
                id = prod[ShoppingListItemsTable.id].value.toString(),
                productId = productId?.toString(),
                productName = productRow?.get(ProductsTable.name)
                    ?: prod[ShoppingListItemsTable.customProductName] ?: "Producto",
                customProductName = prod[ShoppingListItemsTable.customProductName],
                estimatedPrice = prod[ShoppingListItemsTable.estimatedPrice]?.toDouble(),
                estimatedQuantity = prod[ShoppingListItemsTable.estimatedQuantity].toDouble(),
                estimatedBrand = prod[ShoppingListItemsTable.estimatedBrand],
                unit = prod[ShoppingListItemsTable.unit],
                priority = prod[ShoppingListItemsTable.priority],
                checked = prod[ShoppingListItemsTable.checked],
                notes = prod[ShoppingListItemsTable.notes],
                lastCheckedAt = prod[ShoppingListItemsTable.lastCheckedAt]
            )
        }

        return ShoppingListResponse(
            id = listId.toString(),
            groupId = groupId.toString(),
            createdBy = createdBy?.toString(),
            name = row[ShoppingListsTable.name],
            description = row[ShoppingListsTable.description],
            products = products,
            createdAt = row[ShoppingListsTable.createdAt]
        )
    }
}
