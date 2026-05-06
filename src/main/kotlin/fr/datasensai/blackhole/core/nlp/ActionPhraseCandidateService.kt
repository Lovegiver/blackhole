package fr.datasensai.blackhole.core.nlp

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ActionPhraseCandidateService(
    private val tokenFilterService: TokenFilterService,
    private val textNormalizerService: TextNormalizerService
) {

    fun extract(
        posTokens: List<PosToken>,
        minWindowSize: Int = 2,
        maxWindowSize: Int = 7
    ): List<ActionPhraseCandidate> {
        require(minWindowSize > 0) { "minWindowSize must be greater than 0" }
        require(maxWindowSize >= minWindowSize) {
            "maxWindowSize must be greater than or equal to minWindowSize"
        }

        if (posTokens.isEmpty()) {
            return emptyList()
        }

        val results = mutableListOf<ActionPhraseCandidate>()

        for (size in minWindowSize..maxWindowSize) {
            if (size > posTokens.size) continue

            for (start in 0..posTokens.size - size) {
                val window = posTokens.subList(start, start + size)
                val candidate = extractFromWindow(window)

                if (candidate != null) {
                    results.add(candidate)
                }
            }
        }

        return results
            .distinctBy { textNormalizerService.normalizeKey(it.text) }
    }

    private fun extractFromWindow(window: List<PosToken>): ActionPhraseCandidate? {
        if (window.any { isPunctuationOnly(it.token) }) {
            return null
        }

        val lastVerbIndex = window.indexOfLast { isVerb(it) }

        if (lastVerbIndex < 0) {
            return null
        }

        val candidateTokens = window.drop(lastVerbIndex)

        if (candidateTokens.size < 2) {
            return null
        }

        if (!containsNounAfterFirstToken(candidateTokens)) {
            return null
        }

        if (tokenFilterService.isStopword(candidateTokens.last().token)) {
            return null
        }

        val text = candidateTokens
            .map { it.token }
            .joinToString(" ")
            .trim()

        return if (text.isBlank()) {
            null
        } else {
            ActionPhraseCandidate(text)
        }
    }

    private fun containsNounAfterFirstToken(tokens: List<PosToken>): Boolean {
        return tokens
            .drop(1)
            .any { isNounLike(it) }
    }

    private fun isVerb(posToken: PosToken): Boolean {
        return posToken.pos == "VERB"
    }

    private fun isNounLike(posToken: PosToken): Boolean {
        return posToken.pos == "NOUN" || posToken.pos == "PROPN"
    }

    private fun isPunctuationOnly(token: String): Boolean {
        return token.all { !it.isLetterOrDigit() }
    }
}