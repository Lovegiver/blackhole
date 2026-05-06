package fr.datasensai.blackhole.core.nlp

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class NounPhraseCandidateService(
    private val normalizer: TextNormalizerService
) {

    fun extract(posTokens: List<PosToken>): List<NounPhraseCandidate> {
        val results = mutableListOf<NounPhraseCandidate>()

        var i = 0
        while (i < posTokens.size) {

            // Fenêtre max de 5 tokens
            for (size in 2..5) {
                if (i + size > posTokens.size) continue

                val slice = posTokens.subList(i, i + size)

                if (isNounPhrase(slice)) {
                    results.add(
                        NounPhraseCandidate(
                            text = normalize(slice),
                            tokens = slice
                        )
                    )
                }
            }

            i++
        }

        return results.distinctBy { it.text }
    }

    private val titleTokens = setOf("m", "m.", "mme", "mr", "monsieur", "madame")

    private fun isNounPhrase(tokens: List<PosToken>): Boolean {
        val normalizedTokens = tokens.map {
            normalizer.normalizeConceptLabel(it.token)
        }

        // Une civilité ne doit jamais apparaître au milieu ou à la fin d'un groupe nominal générique.
        if (normalizedTokens.drop(1).any { it in titleTokens }) {
            return false
        }

        val tags = tokens.map { it.pos }

        return when (tags) {
            listOf("NOUN", "ADP", "NOUN") -> true
            listOf("PROPN", "PROPN") -> true
            listOf("NOUN", "ADJ") -> true
            listOf("ADJ", "NOUN") -> true
            listOf("ADJ", "ADJ") -> true
            listOf("DET", "NOUN", "ADP", "NOUN") -> true
            else -> false
        }
    }

    private fun normalize(tokens: List<PosToken>): String {
        val text = tokens.joinToString(" ") { it.token }
        return normalizer.normalizeConceptLabel(text)
    }
}