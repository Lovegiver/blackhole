package fr.datasensai.blackhole.core.nlp

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class NgramService(
    private val tokenQualificationService: TokenQualificationService
) {

    fun generate(
        tokens: List<PosToken>,
        minNgramSize: Int,
        maxNgramSize: Int
    ): List<NgramCandidate> {
        require(minNgramSize > 0) { "minNgramSize must be greater than 0" }
        require(maxNgramSize >= minNgramSize) {
            "maxNgramSize must be greater than or equal to minNgramSize"
        }

        val qualifiedTokens = tokenQualificationService.qualify(tokens)

        if (qualifiedTokens.isEmpty()) {
            return emptyList()
        }

        val results = mutableListOf<NgramCandidate>()

        for (size in minNgramSize..maxNgramSize) {
            if (size > qualifiedTokens.size) continue

            for (start in 0..qualifiedTokens.size - size) {
                val slice = qualifiedTokens.subList(start, start + size)
                val window = NgramWindow(slice)

                if (window.isValid()) {
                    results.add(window.toCandidate())
                }
            }
        }

        return results.distinctBy { it.text.lowercase() }
    }
}