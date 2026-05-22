package com.misuper.backend.modules.stores.services

import com.misuper.backend.database.tables.StoresTable
import com.misuper.backend.exceptions.NotFoundException
import com.misuper.backend.modules.stores.dto.CreateStoreRequest
import com.misuper.backend.modules.stores.dto.StoreResponse
import com.misuper.backend.modules.stores.dto.UpdateStoreRequest
import com.misuper.backend.modules.stores.repositories.StoreRepository
import com.misuper.backend.modules.stores.validators.StoreValidator
import java.util.UUID

class StoreService(private val storeRepository: StoreRepository) {

    fun getAll(): List<StoreResponse> {
        return storeRepository.findAll().map { row ->
            StoreResponse(
                id = row[StoresTable.id].value.toString(),
                name = row[StoresTable.name],
                address = row[StoresTable.address],
                phone = row[StoresTable.phone],
                latitude = row[StoresTable.latitude],
                longitude = row[StoresTable.longitude],
                active = row[StoresTable.active]
            )
        }
    }

    fun getById(id: UUID): StoreResponse {
        val row = storeRepository.findById(id)
            ?: throw NotFoundException("Tienda no encontrada")
        return StoreResponse(
            id = row[StoresTable.id].value.toString(),
            name = row[StoresTable.name],
            address = row[StoresTable.address],
            phone = row[StoresTable.phone],
            latitude = row[StoresTable.latitude],
            longitude = row[StoresTable.longitude],
            active = row[StoresTable.active]
        )
    }

    fun create(request: CreateStoreRequest): StoreResponse {
        StoreValidator.validateCreate(request)

        val id = storeRepository.create(
            nameVal = request.name,
            addressVal = request.address,
            phoneVal = request.phone,
            latitudeVal = request.latitude,
            longitudeVal = request.longitude
        )
        return getById(id)
    }

    fun update(id: UUID, request: UpdateStoreRequest): StoreResponse {
        storeRepository.findById(id)
            ?: throw NotFoundException("Tienda no encontrada")

        storeRepository.update(
            id = id,
            nameVal = request.name,
            addressVal = request.address,
            phoneVal = request.phone,
            latitudeVal = request.latitude,
            longitudeVal = request.longitude
        )
        return getById(id)
    }

    fun delete(id: UUID) {
        storeRepository.findById(id)
            ?: throw NotFoundException("Tienda no encontrada")
        storeRepository.softDelete(id)
    }
}
