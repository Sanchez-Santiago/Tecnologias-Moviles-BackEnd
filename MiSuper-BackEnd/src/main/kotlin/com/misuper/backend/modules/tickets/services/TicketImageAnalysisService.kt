package com.misuper.backend.modules.tickets.services

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.tickets.dto.AnalyzeTicketImageResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.Base64

class TicketImageAnalysisService(
    private val apiKey: String? = System.getProperty("OPENAI_API_KEY") ?: System.getenv("OPENAI_API_KEY"),
    private val model: String = System.getProperty("OPENAI_MODEL") ?: System.getenv("OPENAI_MODEL") ?: "gpt-4.1-mini",
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun analyze(imageBase64: String, mimeType: String): AnalyzeTicketImageResponse {
        val key = apiKey?.takeIf { it.isNotBlank() }
            ?: throw ValidationException("Falta configurar OPENAI_API_KEY")

        val cleanBase64 = normalizeBase64(imageBase64)
        validateImage(cleanBase64, mimeType)

        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.openai.com/v1/responses"))
            .timeout(Duration.ofSeconds(60))
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(cleanBase64, mimeType)))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            throw ValidationException("No se pudo analizar la imagen del ticket")
        }

        val responseJson = json.parseToJsonElement(response.body())
        val outputText = extractOutputText(responseJson)
            ?: throw ValidationException("La IA no devolvió una respuesta legible")
        val payload = extractJsonPayload(outputText)

        return json.decodeFromString<AnalyzeTicketImageResponse>(payload)
    }

    private fun buildRequestBody(imageBase64: String, mimeType: String): String {
        val instructions = """
            Leé esta imagen de un ticket de compra de supermercado.
            Devolvé solamente JSON válido con esta forma exacta:
            {
              "storeName": "nombre del comercio o null",
              "purchaseDate": "fecha visible o null",
              "total": 123.45,
              "products": [
                {
                  "name": "nombre del producto",
                  "quantity": 1,
                  "unitPrice": 123.45,
                  "totalPrice": 123.45
                }
              ]
            }
            Incluí únicamente productos comprados. No incluyas subtotal, total, impuestos,
            medios de pago, descuentos globales ni líneas administrativas como productos.
            Si una cantidad o precio no se ve con certeza, usá null.
            Usá punto decimal para los importes.
        """.trimIndent()

        val body = buildJsonObject {
            put("model", model)
            put("input", buildJsonArray {
                add(buildJsonObject {
                    put("role", "user")
                    put("content", buildJsonArray {
                        add(buildJsonObject {
                            put("type", "input_text")
                            put("text", instructions)
                        })
                        add(buildJsonObject {
                            put("type", "input_image")
                            put("image_url", "data:$mimeType;base64,$imageBase64")
                            put("detail", "high")
                        })
                    })
                })
            })
        }

        return json.encodeToString(JsonObject.serializer(), body)
    }

    private fun normalizeBase64(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.startsWith("data:", ignoreCase = true)) {
            trimmed.substringAfter("base64,", missingDelimiterValue = "")
        } else {
            trimmed
        }
    }

    private fun validateImage(imageBase64: String, mimeType: String) {
        val allowedMimeTypes = setOf("image/jpeg", "image/png", "image/webp", "image/gif")
        if (mimeType !in allowedMimeTypes) {
            throw ValidationException("Formato de imagen no soportado")
        }

        val decodedSize = runCatching { Base64.getDecoder().decode(imageBase64).size }.getOrElse {
            throw ValidationException("La imagen no tiene un Base64 válido")
        }

        val maxBytes = 10 * 1024 * 1024
        if (decodedSize > maxBytes) {
            throw ValidationException("La imagen no puede superar 10 MB")
        }
    }

    private fun extractOutputText(element: JsonElement): String? {
        if (element is JsonObject) {
            val directText = element["text"]?.jsonPrimitive?.contentOrNull
            if (directText != null) return directText

            val outputText = element["output_text"]?.jsonPrimitive?.contentOrNull
            if (outputText != null) return outputText
        }

        return when (element) {
            is JsonObject -> element.values.firstNotNullOfOrNull { extractOutputText(it) }
            is JsonArray -> element.firstNotNullOfOrNull { extractOutputText(it) }
            else -> null
        }
    }

    private fun extractJsonPayload(text: String): String {
        val withoutFence = text
            .replace("```json", "")
            .replace("```", "")
            .trim()

        val start = withoutFence.indexOf('{')
        val end = withoutFence.lastIndexOf('}')
        if (start < 0 || end <= start) {
            throw ValidationException("La IA no devolvió JSON válido")
        }

        return withoutFence.substring(start, end + 1)
    }
}
