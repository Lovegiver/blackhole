package fr.datasensai.blackhole.registry.api

import com.fasterxml.jackson.annotation.JsonProperty
import fr.datasensai.blackhole.registry.application.ContentRegistryService
import fr.datasensai.blackhole.registry.domain.ContentRegistration
import fr.datasensai.blackhole.registry.domain.RegistrationResult
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.OffsetDateTime
import java.util.UUID

/** Bounded HTTP boundary for Black Hole's durable hash registry. */
@Path("/v1/content-registry")
@Produces(MediaType.APPLICATION_JSON)
class ContentRegistryResource(
    private val service: ContentRegistryService
) {

    @PUT
    @Path("/{content_hash}")
    fun register(@PathParam("content_hash") contentHash: String): Response {
        val registration = service.register(contentHash)
        val status = when (registration.result) {
            RegistrationResult.NEW -> Response.Status.CREATED
            RegistrationResult.KNOWN -> Response.Status.OK
        }
        return Response.status(status)
            .entity(ContentRegistryResponse.from(registration))
            .build()
    }
}

data class ContentRegistryResponse(
    @field:JsonProperty("content_id")
    val contentId: UUID,
    @field:JsonProperty("content_hash")
    val contentHash: String,
    val result: RegistrationResult,
    @field:JsonProperty("registered_at")
    val registeredAt: OffsetDateTime
) {
    companion object {
        fun from(registration: ContentRegistration) = ContentRegistryResponse(
            contentId = registration.content.contentId,
            contentHash = registration.content.contentHash.value,
            result = registration.result,
            registeredAt = registration.content.registeredAt
        )
    }
}

data class ApiError(
    val code: String,
    val message: String
)
