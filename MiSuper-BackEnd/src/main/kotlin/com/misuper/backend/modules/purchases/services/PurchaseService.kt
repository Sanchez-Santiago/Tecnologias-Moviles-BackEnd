package com.misuper.backend.modules.purchases.services

import com.misuper.backend.database.tables.*
import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.groups.repositories.GroupRepository
import com.misuper.backend.modules.products.repositories.ProductRepository
import com.misuper.backend.modules.purchases.dto.CreatePurchaseRequest
import com.misuper.backend.modules.purchases.dto.PurchaseProductResponse
import com.misuper.backend.modules.purchases.dto.PurchaseResponse
import com.misuper.backend.modules.purchases.repositories.PurchaseRepository
import com.misuper.backend.modules.purchases.validators.PurchaseValidator
import com.misuper.backend.modules.stores.repositories.StoreRepository
import org.jetbrains.exposed.v1.core.ResultRow
import java.math.BigDecimal
import java.util.UUID

class PurchaseService(
    private val purchaseRepository: PurchaseRepository,
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository,
    private val groupRepository: GroupRepository
) {
    fun getByGroup(groupId: UUID, userId: UUID): List<PurchaseResponse> {
        val group = groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        return purchaseRepository.findByGroupId(groupId).map { row ->
            buildResponse(row)
        }
    }

    fun getById(purchaseId: UUID, userId: UUID): PurchaseResponse {
        val row = purchaseRepository.findById(purchaseId)
            ?: throw NotFoundException("Compra no encontrada")

        val groupId = row[PurchasesTable.groupId].value

        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        return buildResponse(row)
    }

    fun create(userId: UUID, request: CreatePurchaseRequest): PurchaseResponse {
        PurchaseValidator.validateCreate(request)

        val groupId = UUID.fromString(request.groupId)
        groupRepository.findById(groupId)
            ?: throw NotFoundException("Grupo no encontrado")

        val memberRole = groupRepository.getMemberRole(groupId, userId)
            ?: throw ForbiddenException("No eres miembro de este grupo")

        val storeId = request.storeId?.let { UUID.fromString(it) }
        storeId?.let { sid ->
            storeRepository.findById(sid)
                ?: throw NotFoundException("Tienda no encontrada")
        }

        var total = BigDecimal.ZERO
        val items = request.items.map { item ->
            val productId = UUID.fromString(item.productId)
            val productRow = productRepository.findById(productId)
                ?: throw NotFoundException("Producto no encontrado: ${item.productId}")

            val unitPrice = productRow[ProductsTable.price]
            val quantity = BigDecimal.valueOf(item.quantity.toLong())
            val subtotal = unitPrice * quantity
            total += subtotal

            PurchaseItemData(
                productId = productId,
                productName = productRow[ProductsTable.name],
                quantity = item.quantity,
                unitPrice = unitPrice,
                subtotal = subtotal
            )
        }

        val purchaseId = purchaseRepository.create(
            groupIdVal = groupId,
            storeIdVal = storeId,
            userIdVal = userId,
            totalVal = total,
            notesVal = request.notes
        )

        items.forEach { item ->
            purchaseRepository.addItem(
                purchaseIdVal = purchaseId,
                productIdVal = item.productId,
                productNameVal = item.productName,
                quantityVal = item.quantity,
                unitPriceVal = item.unitPrice,
                subtotalVal = item.subtotal
            )
        }

        return getById(purchaseId, userId)
    }

    private fun buildResponse(row: ResultRow): PurchaseResponse {
        val purchaseId = row[PurchasesTable.id].value
        val groupId = row[PurchasesTable.groupId].value
        val userId = row[PurchasesTable.userId].value

        val storeName = row[PurchasesTable.storeId]?.let { sid ->
            storeRepository.findById(sid.value)?.get(StoresTable.name)
        }

        val userRow = groupRepository.findUserById(userId)
        val userName = userRow?.get(UsersTable.fullName) ?: "Desconocido"

        val items = purchaseRepository.getItems(purchaseId).map { item ->
            PurchaseProductResponse(
                id = item[PurchaseProductsTable.id].value.toString(),
                productId = item[PurchaseProductsTable.productId].value.toString(),
                productName = item[PurchaseProductsTable.productName],
                quantity = item[PurchaseProductsTable.quantity],
                unitPrice = item[PurchaseProductsTable.unitPrice],
                subtotal = item[PurchaseProductsTable.subtotal]
            )
        }

        return PurchaseResponse(
            id = purchaseId.toString(),
            groupId = groupId.toString(),
            storeId = row[PurchasesTable.storeId]?.value?.toString(),
            storeName = storeName,
            userId = userId.toString(),
            userName = userName,
            total = row[PurchasesTable.total],
            notes = row[PurchasesTable.notes],
            items = items,
            purchaseDate = row[PurchasesTable.purchaseDate],
            createdAt = row[PurchasesTable.createdAt]
        )
    }

    private data class PurchaseItemData(
        val productId: UUID,
        val productName: String,
        val quantity: Int,
        val unitPrice: BigDecimal,
        val subtotal: BigDecimal
    )
}
