package fr.datasensai.blackhole.core.nlp

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class NgramService(
    private val tokenFilterService: TokenFilterService
) {

    fun generate(
        tokens: List<String>,
        minNgramSize: Int,
        maxNgramSize: Int
    ): List<NgramCandidate> {
        require(minNgramSize > 0) { "minNgramSize must be greater than 0" }
        require(maxNgramSize >= minNgramSize) {
            "maxNgramSize must be greater than or equal to minNgramSize"
        }

        val normalizedTokens = tokens
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (normalizedTokens.isEmpty()) {
            return emptyList()
        }

        val results = mutableListOf<NgramCandidate>()

        for (size in minNgramSize..maxNgramSize) {
            if (size > normalizedTokens.size) continue

            for (start in 0..normalizedTokens.size - size) {
                val slice = normalizedTokens.subList(start, start + size)

                if (isValidNgram(slice)) {
                    results.add(
                        NgramCandidate(
                            text = slice.joinToString(" ")
                        )
                    )
                }
            }
        }

        return results.distinctBy { it.text.lowercase() }
    }

    private fun isValidNgram(tokens: List<String>): Boolean {
        if (tokens.isEmpty()) {
            return false
        }

        if (tokens.any { isPunctuationOnly(it) }) {
            return false
        }

        if (!tokenFilterService.containsAtLeastOneSignificantToken(tokens)) {
            return false
        }

        if (tokenFilterService.isAllStopwords(tokens)) {
            return false
        }

        if (startsWithStopword(tokens)) {
            return false
        }

        if (endsWithStopword(tokens)) {
            return false
        }

        return true
    }

    private fun startsWithStopword(tokens: List<String>): Boolean {
        return tokenFilterService.isStopword(tokens.first())
    }

    private fun endsWithStopword(tokens: List<String>): Boolean {
        return tokenFilterService.isStopword(tokens.last())
    }

    private fun isPunctuationOnly(token: String): Boolean {
        return token.all { !it.isLetterOrDigit() }
    }
}