package com.misuper.backend.modules.offers.routes

import com.misuper.backend.exceptions.ForbiddenException
import com.misuper.backend.modules.offers.dto.AiOfferSuggestionRequest
import com.misuper.backend.modules.offers.dto.CreateOfferRequest
import com.misuper.backend.modules.offers.dto.UpdateOfferRequest
import com.misuper.backend.modules.offers.services.OfferService
import com.misuper.backend.modules.offers.services.OfferSuggestionService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

class OfferRoutes(
    private val offerService: OfferService,
    private val offerSuggestionService: OfferSuggestionService
) {

    fun register(routing: Route) {
        routing.route("offers") {

            authenticate("auth-jwt") {
                get {
                    val storeId = call.request.queryParameters["storeId"]
                    val offers = offerService.getAll(storeId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(offers))
                }

                get("active") {
                    val storeId = call.request.queryParameters["storeId"]
                    val offers = offerService.getActive(storeId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(offers))
                }

                post("ai-suggest") {
                    val request = call.receive<AiOfferSuggestionRequest>()
                    val storeId = request.storeId
                    val activeOffers = offerService.getActive(storeId)
                    val result = offerSuggestionService.suggest(request.productNames, activeOffers)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(result))
                }

                get("match") {
                    val productIds = call.request.queryParameters.getAll("productId").orEmpty()
                    if (productIds.isEmpty()) {
                        throw IllegalArgumentException("Debe indicar al menos un productId")
                    }
                    val storeId = call.request.queryParameters["storeId"]
                    val matches = offerService.matchProducts(productIds, storeId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(matches))
                }

                get("{id}") {
                    val offerId = UUID.fromString(call.parameters["id"])
                    val offer = offerService.getById(offerId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(offer))
                }

                post {
                    requireAdminRole(call)
                    val request = call.receive<CreateOfferRequest>()
                    val offer = offerService.create(request)
                    call.respond(HttpStatusCode.Created, ApiResponse.success(offer))
                }

                put("{id}") {
                    requireAdminRole(call)
                    val offerId = UUID.fromString(call.parameters["id"])
                    val request = call.receive<UpdateOfferRequest>()
                    val offer = offerService.update(offerId, request)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(offer))
                }

                delete("{id}") {
                    requireAdminRole(call)
                    val offerId = UUID.fromString(call.parameters["id"])
                    offerService.softDelete(offerId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success("Oferta eliminada"))
                }
            }
        }
    }

    private fun requireAdminRole(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.payload?.getClaim("role")?.asString()
        if (role != "ADMIN") {
            throw ForbiddenException("Se requiere rol de administrador")
        }
    }
}
