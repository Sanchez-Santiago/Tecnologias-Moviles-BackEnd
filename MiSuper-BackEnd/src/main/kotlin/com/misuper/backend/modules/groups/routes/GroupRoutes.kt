package com.misuper.backend.modules.groups.routes

import com.misuper.backend.modules.groups.dto.AcceptRejectRequest
import com.misuper.backend.modules.groups.dto.AddMemberRequest
import com.misuper.backend.modules.groups.dto.CreateGroupRequest
import com.misuper.backend.modules.groups.dto.InviteRequest
import com.misuper.backend.modules.groups.dto.UpdateGroupRequest
import com.misuper.backend.modules.groups.services.GroupInvitationService
import com.misuper.backend.modules.groups.services.GroupService
import com.misuper.backend.responses.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

class GroupRoutes(
    private val groupService: GroupService,
    private val groupInvitationService: GroupInvitationService
) {

    fun register(routing: Route) {
        routing.route("groups") {

            authenticate("auth-jwt") {
                get {
                    val userId = userId(call)
                    val groups = groupService.getMyGroups(userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(groups))
                }

                get("{id}") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["id"])
                    val group = groupService.getById(groupId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(group))
                }

                post {
                    val userId = userId(call)
                    val request = call.receive<CreateGroupRequest>()
                    val group = groupService.create(userId, request)
                    call.respond(HttpStatusCode.Created, ApiResponse.success(group))
                }

                put("{id}") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["id"])
                    val request = call.receive<UpdateGroupRequest>()
                    val group = groupService.update(groupId, userId, request)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(group))
                }

                delete("{id}") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["id"])
                    groupService.delete(groupId, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(mapOf("message" to "Grupo eliminado")))
                }

                post("{id}/members") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["id"])
                    val request = call.receive<AddMemberRequest>()
                    val group = groupService.addMember(groupId, userId, request)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(group))
                }

                delete("{id}/members/{memberId}") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["id"])
                    val memberId = UUID.fromString(call.parameters["memberId"])
                    groupService.removeMember(groupId, userId, memberId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(mapOf("message" to "Miembro eliminado del grupo")))
                }

                post("{id}/invitations") {
                    val userId = userId(call)
                    val groupId = UUID.fromString(call.parameters["id"])
                    val request = call.receive<InviteRequest>()
                    val invitation = groupInvitationService.invite(groupId, userId, request)
                    call.respond(HttpStatusCode.Created, ApiResponse.success(invitation))
                }

                get("invitations") {
                    val userId = userId(call)
                    val invitations = groupInvitationService.getMyInvitations(userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(invitations))
                }

                post("invitations/{token}/accept") {
                    val userId = userId(call)
                    val token = call.parameters["token"]!!
                    val invitation = groupInvitationService.accept(token, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(invitation))
                }

                post("invitations/{token}/reject") {
                    val userId = userId(call)
                    val token = call.parameters["token"]!!
                    val invitation = groupInvitationService.reject(token, userId)
                    call.respond(HttpStatusCode.OK, ApiResponse.success(invitation))
                }
            }
        }
    }

    private fun userId(call: ApplicationCall): UUID {
        val principal = call.principal<JWTPrincipal>()
        return principal?.payload?.subject?.let { UUID.fromString(it) }
            ?: throw IllegalArgumentException("Usuario no autenticado")
    }
}
