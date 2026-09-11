package fr.datasensai.blackhole.registry.api

import fr.datasensai.blackhole.registry.domain.InvalidContentHashException
import fr.datasensai.blackhole.registry.infrastructure.ContentRegistryUnavailableException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.jboss.logging.Logger

@Provider
class InvalidContentHashExceptionMapper : ExceptionMapper<InvalidContentHashException> {
    override fun toResponse(exception: InvalidContentHashException): Response =
        Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(ApiError("INVALID_CONTENT_HASH", exception.message!!))
            .build()
}

@Provider
class ContentRegistryUnavailableExceptionMapper : ExceptionMapper<ContentRegistryUnavailableException> {
    private val logger = Logger.getLogger(ContentRegistryUnavailableExceptionMapper::class.java)

    override fun toResponse(exception: ContentRegistryUnavailableException): Response {
        logger.error("Content registry database operation failed", exception)
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(
                ApiError(
                    code = "CONTENT_REGISTRY_UNAVAILABLE",
                    message = "Content registry is temporarily unavailable"
                )
            )
            .build()
    }
}
