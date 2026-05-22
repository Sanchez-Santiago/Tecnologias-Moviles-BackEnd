package com.misuper.backend.modules.tickets.validators

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.tickets.dto.AddMessageRequest
import com.misuper.backend.modules.tickets.dto.CreateTicketRequest
import com.misuper.backend.modules.tickets.dto.UpdateTicketRequest

object TicketValidator {

    fun validateCreate(request: CreateTicketRequest) {
        if (request.groupId.isBlank()) throw ValidationException("El grupo es obligatorio")
        if (request.title.isBlank()) throw ValidationException("El título es obligatorio")
        if (request.description.isBlank()) throw ValidationException("La descripción es obligatoria")
        val validPriorities = setOf("LOW", "MEDIUM", "HIGH", "URGENT")
        if (request.priority != null && request.priority.uppercase() !in validPriorities) {
            throw ValidationException("Prioridad inválida. Use: LOW, MEDIUM, HIGH o URGENT")
        }
    }

    fun validateUpdate(request: UpdateTicketRequest) {
        if (request.title != null && request.title.isBlank()) {
            throw ValidationException("El título no puede estar vacío")
        }
        if (request.status != null) {
            val validStatuses = setOf("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED")
            if (request.status.uppercase() !in validStatuses) {
                throw ValidationException("Estado inválido. Use: OPEN, IN_PROGRESS, RESOLVED o CLOSED")
            }
        }
        if (request.priority != null) {
            val validPriorities = setOf("LOW", "MEDIUM", "HIGH", "URGENT")
            if (request.priority.uppercase() !in validPriorities) {
                throw ValidationException("Prioridad inválida. Use: LOW, MEDIUM, HIGH o URGENT")
            }
        }
    }

    fun validateMessage(request: AddMessageRequest) {
        if (request.message.isBlank()) throw ValidationException("El mensaje no puede estar vacío")
    }
}
