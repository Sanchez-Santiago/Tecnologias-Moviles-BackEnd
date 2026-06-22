package com.misuper.backend.modules.tickets

import com.misuper.backend.modules.tickets.dto.AnalyzeTicketImageRequest
import com.misuper.backend.modules.tickets.dto.AnalyzeTicketImageResponse
import com.misuper.backend.modules.tickets.dto.SaveAnalysisRequest
import com.misuper.backend.modules.tickets.dto.TicketProductDetection
import com.misuper.backend.modules.tickets.dto.UploadTicketRequest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TicketDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun serializeAndDeserializeUploadTicketRequest() {
        val original = UploadTicketRequest(
            purchaseId = "550e8400-e29b-41d4-a716-446655440000",
            imageBase64 = "dGVzdC1pbWFnZS1kYXRh",
            mimeType = "image/png"
        )
        val encoded = json.encodeToString(UploadTicketRequest.serializer(), original)
        val decoded = json.decodeFromString(UploadTicketRequest.serializer(), encoded)

        assertEquals(original.purchaseId, decoded.purchaseId)
        assertEquals(original.imageBase64, decoded.imageBase64)
        assertEquals(original.mimeType, decoded.mimeType)
    }

    @Test
    fun uploadTicketRequestDefaultsToJpeg() {
        val request = UploadTicketRequest(
            purchaseId = "id",
            imageBase64 = "data"
        )
        assertEquals("image/jpeg", request.mimeType)
    }

    @Test
    fun serializeAndDeserializeAnalyzeTicketImageRequest() {
        val original = AnalyzeTicketImageRequest(
            imageBase64 = "dGVzdC1pbWFnZS1kYXRh",
            mimeType = "image/webp"
        )
        val encoded = json.encodeToString(AnalyzeTicketImageRequest.serializer(), original)
        val decoded = json.decodeFromString(AnalyzeTicketImageRequest.serializer(), encoded)

        assertEquals(original.imageBase64, decoded.imageBase64)
        assertEquals(original.mimeType, decoded.mimeType)
    }

    @Test
    fun analyzeTicketImageRequestDefaultsToJpeg() {
        val request = AnalyzeTicketImageRequest(imageBase64 = "data")
        assertEquals("image/jpeg", request.mimeType)
    }

    @Test
    fun serializeAndDeserializeAnalyzeTicketImageResponse() {
        val original = AnalyzeTicketImageResponse(
            storeName = "Mercadona",
            purchaseDate = "2026-06-15",
            total = 42.50,
            products = listOf(
                TicketProductDetection("Leche", 2.0, 1.20, 2.40),
                TicketProductDetection("Pan", 1.0, 0.90, 0.90)
            )
        )
        val encoded = json.encodeToString(AnalyzeTicketImageResponse.serializer(), original)
        val decoded = json.decodeFromString(AnalyzeTicketImageResponse.serializer(), encoded)

        assertEquals(original.storeName, decoded.storeName)
        assertEquals(original.total, decoded.total)
        assertEquals(original.products.size, decoded.products.size)
        assertEquals("Leche", decoded.products[0].name)
        assertEquals(1.20, decoded.products[0].unitPrice)
    }

    @Test
    fun analyzeTicketImageResponseAllowsNullFields() {
        val original = AnalyzeTicketImageResponse(products = emptyList())
        val encoded = json.encodeToString(AnalyzeTicketImageResponse.serializer(), original)
        val decoded = json.decodeFromString(AnalyzeTicketImageResponse.serializer(), encoded)

        assertNull(decoded.storeName)
        assertNull(decoded.purchaseDate)
        assertNull(decoded.total)
        assertEquals(0, decoded.products.size)
    }

    @Test
    fun serializeAndDeserializeSaveAnalysisRequest() {
        val original = SaveAnalysisRequest(
            extractedStore = "Carrefour",
            extractedTotal = 35.99,
            aiReport = "Productos detectados correctamente"
        )
        val encoded = json.encodeToString(SaveAnalysisRequest.serializer(), original)
        val decoded = json.decodeFromString(SaveAnalysisRequest.serializer(), encoded)

        assertEquals(original.extractedStore, decoded.extractedStore)
        assertEquals(original.extractedTotal, decoded.extractedTotal)
        assertEquals(original.aiReport, decoded.aiReport)
    }

    @Test
    fun saveAnalysisRequestAllowsNullFields() {
        val original = SaveAnalysisRequest()
        val encoded = json.encodeToString(SaveAnalysisRequest.serializer(), original)
        val decoded = json.decodeFromString(SaveAnalysisRequest.serializer(), encoded)

        assertNull(decoded.extractedStore)
        assertNull(decoded.extractedTotal)
        assertNull(decoded.aiReport)
    }
}
