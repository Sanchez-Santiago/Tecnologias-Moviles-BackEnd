package com.misuper.backend.modules.offers.services

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.offers.dto.AiOfferSuggestion
import com.misuper.backend.modules.offers.dto.AiOfferSuggestionResponse
import com.misuper.backend.modules.offers.dto.OfferResponse
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

class OfferSuggestionService(
    private val apiKey: String? = System.getProperty("GEMINI_API_KEY") ?: System.getenv("GEMINI_API_KEY"),
    private val model: String = System.getProperty("GEMINI_MODEL") ?: System.getenv("GEMINI_MODEL") ?: "gemini-2.0-flash",
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun suggest(productNames: List<String>, offers: List<OfferResponse>): AiOfferSuggestionResponse {
        val key = apiKey?.takeIf { it.isNotBlank() }
            ?: throw ValidationException("Falta configurar GEMINI_API_KEY")

        if (productNames.isEmpty() || offers.isEmpty()) {
            return AiOfferSuggestionResponse(emptyList())
        }

        val requestBody = buildRequestBody(productNames, offers)
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$key"))
            .timeout(Duration.ofSeconds(60))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            val errorBody = response.body()
            val errorMsg = runCatching {
                json.parseToJsonElement(errorBody)
                    .let { it as? JsonObject }?.get("error")
                    ?.let { it as? JsonObject }?.get("message")?.jsonPrimitive?.contentOrNull
            }.getOrNull() ?: "Error ${response.statusCode()}"
            throw ValidationException("No se pudieron generar sugerencias: ${errorMsg.replace('\n', ' ').replace('\r', ' ')}")
        }

        val responseJson = json.parseToJsonElement(response.body())
        val outputText = extractOutputText(responseJson)
            ?: throw ValidationException("La IA no devolvió una respuesta legible")
        val payload = extractJsonPayload(outputText)

        return try {
            json.decodeFromString<AiOfferSuggestionResponse>(payload)
        } catch (_: Exception) {
            AiOfferSuggestionResponse(emptyList())
        }
    }

    private fun buildRequestBody(productNames: List<String>, offers: List<OfferResponse>): String {
        val productsStr = productNames.joinToString("\n") { "- $it" }
        val offersStr = offers.joinToString("\n---\n") { offer ->
            buildString {
                appendLine("ID: ${offer.id}")
                appendLine("Título: ${offer.title}")
                offer.description?.let { appendLine("Descripción: $it") }
                offer.storeName?.let { appendLine("Tienda: $it") }
                appendLine("Descuento: ${offer.discountValue} (${offer.discountType})")
                offer.termsConditions?.let { appendLine("Términos: $it") }
            }
        }

        val instructions = """
            Sos un asistente de supermercado que ayuda a encontrar ofertas relevantes para productos específicos.

            Productos del usuario:
            $productsStr

            Ofertas activas disponibles:
            $offersStr

            Para cada producto, identificá cuáles de las ofertas disponibles son relevantes.
            Considerá coincidencias directas de nombre, productos similares, o categorías relacionadas.
            Devolvé SOLO JSON válido con este formato exacto (sin markdown):
            {
              "suggestions": [
                {
                  "productName": "nombre exacto del producto",
                  "offerId": "ID de la oferta",
                  "relevance": "ALTA o MEDIA o BAJA",
                  "explanation": "explicación breve de por qué esta oferta es relevante para este producto"
                }
              ]
            }
            Si un producto no tiene ninguna oferta relevante, simplemente no lo incluyas.
        """.trimIndent()

        val body = buildJsonObject {
            put("contents", buildJsonArray {
                add(buildJsonObject {
                    put("role", "user")
                    put("parts", buildJsonArray {
                        add(buildJsonObject {
                            put("text", instructions)
                        })
                    })
                })
            })
            put("generationConfig", buildJsonObject {
                put("temperature", 0.3)
                put("maxOutputTokens", 4096)
            })
        }

        return json.encodeToString(JsonObject.serializer(), body)
    }

    private fun extractOutputText(element: JsonElement): String? {
        if (element is JsonObject) {
            val candidates = element["candidates"] as? JsonArray
            if (candidates != null) {
                val first = candidates.firstOrNull() as? JsonObject
                val content = first?.get("content") as? JsonObject
                val parts = content?.get("parts") as? JsonArray
                val text = parts?.firstOrNull()
                    ?.let { (it as? JsonObject)?.get("text")?.jsonPrimitive?.contentOrNull }
                if (text != null) return text
            }
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
