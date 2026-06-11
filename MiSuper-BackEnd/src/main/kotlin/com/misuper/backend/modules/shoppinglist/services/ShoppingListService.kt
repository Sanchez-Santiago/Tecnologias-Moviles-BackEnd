package com.misuper.backend.modules.shoppinglist.services

import com.misuper.backend.database.tables.ProductsTable
import com.misuper.backend.database.tables.ShoppingListProductsTable
import com.misuper.backend.database.tables.ShoppingListsTable
import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.groups.repositories.GroupRepository
import com.misuper.backend.modules.shoppinglist.dto.*
import com.misuper.backend.modules.shoppinglist.repositories.ShoppingListRepository
import org.jetbrains.exposed.v1.core.ResultRow
import java.math.BigDecimal
import java.util.UUID

class ShoppingListService(
    private val shoppingListRepository: ShoppingListRepository,
    private val groupRepository: GroupRepository
) {
    fun getByGroup(groupId: UUID, userId: UUID): List<ShoppingListResponse> {
        val group = groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        return shoppingListRepository.findByGroupId(groupId).map { buildResponse(it) }
    }

    fun getById(listId: UUID, userId: UUID): ShoppingListResponse {
        val row = shoppingListRepository.findById(listId)
            ?: throw NotFoundException("Lista de compra no encontrada")

        val groupId = row[ShoppingListsTable.groupId].value
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        return buildResponse(row)
    }

    fun create(userId: UUID, request: CreateShoppingListRequest): ShoppingListResponse {
        val groupId = UUID.fromString(request.groupId)
        groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        val memberRole = groupRepository.getMemberRole(groupId, userId)
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
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        shoppingListRepository.update(id, request.name, request.description)
        return getById(id, userId)
    }

    fun delete(id: UUID, userId: UUID) {
        val row = shoppingListRepository.findById(id)
            ?: throw NotFoundException("Lista de compra no encontrada")

        val groupId = row[ShoppingListsTable.groupId].value
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        shoppingListRepository.delete(id)
    }

    fun addProduct(listId: UUID, userId: UUID, request: AddProductRequest): ShoppingListResponse {
        val row = shoppingListRepository.findById(listId)
            ?: throw NotFoundException("Lista de compra no encontrada")

        val groupId = row[ShoppingListsTable.groupId].value
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val productId = UUID.fromString(request.productId)
        shoppingListRepository.findProductById(productId)
            ?: throw NotFoundException("Producto no encontrado")

        shoppingListRepository.addProduct(
            shoppingListIdVal = listId,
            productIdVal = productId,
            quantityVal = request.quantity?.let { BigDecimal.valueOf(it) },
            notesVal = request.notes
        )

        return getById(listId, userId)
    }

    fun updateProduct(listId: UUID, productUuid: UUID, userId: UUID, request: UpdateProductRequest): ShoppingListResponse {
        val row = shoppingListRepository.findById(listId)
            ?: throw NotFoundException("Lista de compra no encontrada")

        val groupId = row[ShoppingListsTable.groupId].value
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val products = shoppingListRepository.getProducts(listId)
        val productRow = products.firstOrNull { it[ShoppingListProductsTable.id].value == productUuid }
            ?: throw NotFoundException("Producto no encontrado en la lista")

        shoppingListRepository.updateProduct(
            id = productUuid,
            checkedVal = request.checked,
            finalPriceVal = request.finalPrice?.let { BigDecimal.valueOf(it) },
            finalQuantityVal = request.finalQuantity?.let { BigDecimal.valueOf(it) },
            notesVal = request.notes
        )

        return getById(listId, userId)
    }

    fun deleteProduct(listId: UUID, productUuid: UUID, userId: UUID): ShoppingListResponse {
        val row = shoppingListRepository.findById(listId)
            ?: throw NotFoundException("Lista de compra no encontrada")

        val groupId = row[ShoppingListsTable.groupId].value
        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        shoppingListRepository.deleteProduct(productUuid)
        return getById(listId, userId)
    }

    private fun buildResponse(row: ResultRow): ShoppingListResponse {
        val listId = row[ShoppingListsTable.id].value
        val groupId = row[ShoppingListsTable.groupId].value
        val createdBy = row[ShoppingListsTable.createdBy]?.value

        val products = shoppingListRepository.getProducts(listId).map { prod ->
            val productId = prod[ShoppingListProductsTable.productId].value
            val productRow = shoppingListRepository.findProductById(productId)
            ShoppingListProductResponse(
                id = prod[ShoppingListProductsTable.id].value.toString(),
                productId = productId.toString(),
                productName = productRow?.get(ProductsTable.name) ?: "Producto",
                checked = prod[ShoppingListProductsTable.checked],
                finalPrice = prod[ShoppingListProductsTable.finalPrice]?.toDouble(),
                finalQuantity = prod[ShoppingListProductsTable.finalQuantity]?.toDouble(),
                notes = prod[ShoppingListProductsTable.notes]
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
