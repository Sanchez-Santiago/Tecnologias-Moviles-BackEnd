package com.misuper.backend.modules.tickets

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.tickets.dto.AddMessageRequest
import com.misuper.backend.modules.tickets.dto.CreateTicketRequest
import com.misuper.backend.modules.tickets.validators.TicketValidator
import kotlin.test.Test
import kotlin.test.assertFailsWith

class TicketValidatorTest {

    @Test
    fun acceptsValidCreateRequest() {
        val request = CreateTicketRequest(
            groupId = "550e8400-e29b-41d4-a716-446655440000",
            title = "Issue with tickets",
            description = "Can't upload",
            priority = "HIGH"
        )
        TicketValidator.validateCreate(request)
    }

    @Test
    fun rejectsCreateBlankTitle() {
        val request = CreateTicketRequest(
            groupId = "550e8400-e29b-41d4-a716-446655440000",
            title = " ",
            description = "Description"
        )
        assertFailsWith<ValidationException> {
            TicketValidator.validateCreate(request)
        }
    }

    @Test
    fun acceptsValidMessage() {
        val request = AddMessageRequest(message = "Hello")
        TicketValidator.validateMessage(request)
    }

    @Test
    fun rejectsBlankMessage() {
        val request = AddMessageRequest(message = "   ")
        assertFailsWith<ValidationException> {
            TicketValidator.validateMessage(request)
        }
    }
}
