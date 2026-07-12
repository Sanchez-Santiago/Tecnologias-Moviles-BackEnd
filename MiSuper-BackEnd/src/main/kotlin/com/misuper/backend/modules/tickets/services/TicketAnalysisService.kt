package com.misuper.backend.modules.tickets.services

import com.misuper.backend.exceptions.ValidationException
import com.misuper.backend.modules.tickets.dto.AnalyzeTicketImageResponse
import com.misuper.backend.modules.tickets.dto.TicketProductDetection
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class TicketAnalysisService(
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

    fun analyze(imageBase64: String, mimeType: String): AnalyzeTicketImageResponse {
        val key = apiKey?.takeIf { it.isNotBlank() }
            ?: throw ValidationException("Falta configurar GEMINI_API_KEY")

        val requestBody = buildRequestBody(imageBase64, mimeType)
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
            throw ValidationException("No se pudo analizar la imagen: ${errorMsg.replace('\n', ' ').replace('\r', ' ')}")
        }

        val responseJson = json.parseToJsonElement(response.body())
        val outputText = extractOutputText(responseJson)
            ?: throw ValidationException("La IA no devolvió una respuesta legible")
        val payload = extractJsonPayload(outputText)

        return parseResponse(payload)
    }

    private fun buildRequestBody(imageBase64: String, mimeType: String): String {
        val instructions = """
            Sos un asistente que analiza tickets o facturas de supermercado a partir de una imagen.
            Extraé la siguiente información en formato JSON exacto (sin markdown):
            {
              "storeName": "nombre del supermercado o tienda",
              "purchaseDate": "fecha de la compra en formato yyyy-MM-dd",
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
            - storeName: puede estar en el encabezado del ticket.
            - purchaseDate: si no se ve claramente, omitila (null).
            - total: el monto total de la compra. Si no se ve, omitilo (null).
            - products: lista de productos. Si no se distinguen claramente, devolvé lista vacía.
            - quantity: cantidad de unidades del producto (1 si no se especifica).
            - unitPrice: precio unitario del producto.
            - totalPrice: precio total del producto (cantidad * unitPrice si no viene explícito).
            Devolvé SOLO el JSON, sin explicaciones ni markdown.
        """.trimIndent()

        val body = buildJsonObject {
            put("contents", buildJsonArray {
                add(buildJsonObject {
                    put("role", "user")
                    put("parts", buildJsonArray {
                        add(buildJsonObject {
                            put("inlineData", buildJsonObject {
                                put("mimeType", mimeType)
                                put("data", imageBase64)
                            })
                        })
                        add(buildJsonObject {
                            put("text", instructions)
                        })
                    })
                })
            })
            put("generationConfig", buildJsonObject {
                put("temperature", 0.1)
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

    private fun parseResponse(payload: String): AnalyzeTicketImageResponse {
        return try {
            val root = json.parseToJsonElement(payload) as? JsonObject ?: return AnalyzeTicketImageResponse()

            val storeName = root["storeName"]?.jsonPrimitive?.contentOrNull
            val purchaseDate = root["purchaseDate"]?.jsonPrimitive?.contentOrNull
            val total = root["total"]?.jsonPrimitive?.doubleOrNull
            val products = (root["products"] as? JsonArray)?.mapNotNull { item ->
                val obj = item as? JsonObject ?: return@mapNotNull null
                val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                TicketProductDetection(
                    name = name,
                    quantity = obj["quantity"]?.jsonPrimitive?.doubleOrNull,
                    unitPrice = obj["unitPrice"]?.jsonPrimitive?.doubleOrNull,
                    totalPrice = obj["totalPrice"]?.jsonPrimitive?.doubleOrNull
                )
            } ?: emptyList()

            AnalyzeTicketImageResponse(
                storeName = storeName,
                purchaseDate = purchaseDate,
                total = total,
                products = products
            )
        } catch (_: Exception) {
            AnalyzeTicketImageResponse()
        }
    }
}
