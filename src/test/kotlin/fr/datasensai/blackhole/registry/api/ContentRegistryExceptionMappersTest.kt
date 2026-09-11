package fr.datasensai.blackhole.registry.api

import fr.datasensai.blackhole.registry.infrastructure.ContentRegistryUnavailableException
import jakarta.ws.rs.core.MediaType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.sql.SQLException

class ContentRegistryExceptionMappersTest {

    @Test
    fun `database failure response contains no internal detail`() {
        val internalDetail = "internal-host:5432 private-role"

        val response = ContentRegistryUnavailableExceptionMapper().toResponse(
            ContentRegistryUnavailableException(SQLException(internalDetail))
        )
        val error = response.entity as ApiError

        assertEquals(503, response.status)
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.mediaType)
        assertEquals("CONTENT_REGISTRY_UNAVAILABLE", error.code)
        assertEquals("Content registry is temporarily unavailable", error.message)
        assertFalse(error.message.contains(internalDetail))
    }
}
