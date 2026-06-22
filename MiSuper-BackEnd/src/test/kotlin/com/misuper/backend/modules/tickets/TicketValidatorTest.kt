package com.misuper.backend.modules.tickets

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.tickets.dto.UploadTicketRequest
import com.misuper.backend.modules.tickets.validators.TicketValidator
import kotlin.test.Test
import kotlin.test.assertFailsWith

class TicketValidatorTest {

    @Test
    fun acceptsValidRequest() {
        val request = UploadTicketRequest(
            purchaseId = "550e8400-e29b-41d4-a716-446655440000",
            imageBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
            mimeType = "image/jpeg"
        )
        TicketValidator.validateUpload(request)
    }

    @Test
    fun rejectsBlankPurchaseId() {
        val request = UploadTicketRequest(
            purchaseId = "  ",
            imageBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
        )
        assertFailsWith<ValidationException> {
            TicketValidator.validateUpload(request)
        }
    }

    @Test
    fun rejectsBlankImage() {
        val request = UploadTicketRequest(
            purchaseId = "550e8400-e29b-41d4-a716-446655440000",
            imageBase64 = ""
        )
        assertFailsWith<ValidationException> {
            TicketValidator.validateUpload(request)
        }
    }

    @Test
    fun rejectsUnsupportedMimeType() {
        val request = UploadTicketRequest(
            purchaseId = "550e8400-e29b-41d4-a716-446655440000",
            imageBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
            mimeType = "image/gif"
        )
        assertFailsWith<ValidationException> {
            TicketValidator.validateUpload(request)
        }
    }

    @Test
    fun acceptsAllSupportedMimeTypes() {
        val base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
        for (mime in listOf("image/jpeg", "image/png", "image/webp")) {
            val request = UploadTicketRequest(
                purchaseId = "550e8400-e29b-41d4-a716-446655440000",
                imageBase64 = base64,
                mimeType = mime
            )
            TicketValidator.validateUpload(request)
        }
    }
}
