package fr.datasensai.blackhole.registry.application

import fr.datasensai.blackhole.registry.domain.ContentHash
import fr.datasensai.blackhole.registry.domain.ContentRegistration
import fr.datasensai.blackhole.registry.domain.IdempotencyKey
import fr.datasensai.blackhole.registry.infrastructure.ContentRegistryRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

/** Owns the short transaction that atomically registers or returns a hash. */
@ApplicationScoped
class ContentRegistryService(
    private val repository: ContentRegistryRepository
) {

    @Transactional(Transactional.TxType.REQUIRED)
    fun register(rawContentHash: String, rawIdempotencyKey: String?): ContentRegistration =
        repository.registerOrFind(
            ContentHash.parse(rawContentHash),
            IdempotencyKey.parse(rawIdempotencyKey)
        )
}
