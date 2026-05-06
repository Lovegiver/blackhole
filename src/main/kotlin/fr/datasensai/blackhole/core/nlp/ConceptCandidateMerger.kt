package fr.datasensai.blackhole.core.nlp

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ConceptCandidateMerger(
    private val tokenFilterService: TokenFilterService,
    private val textNormalizerService: TextNormalizerService
) {

    fun merge(candidates: List<ConceptCandidateDraft>): List<ConceptCandidateDraft> {
        val mergedByKey = linkedMapOf<String, ConceptCandidateDraft>()

        candidates.forEach { candidate ->
            val cleanedText = stripLeadingStopwords(candidate.text)
            if (cleanedText.isBlank()) {
                return@forEach
            }

            val cleanedCandidate = candidate.copy(
                text = cleanedText,
                normalizedText = textNormalizerService.normalizeKey(cleanedText)
            )

            val key = cleanedCandidate.normalizedText

            val existing = mergedByKey[key]

            if (existing == null) {
                mergedByKey[key] = cleanedCandidate
            } else {
                mergedByKey[key] = mergeCandidates(existing, cleanedCandidate)
            }
        }

        return mergedByKey.values.toList()
    }

    private fun mergeCandidates(
        existing: ConceptCandidateDraft,
        incoming: ConceptCandidateDraft
    ): ConceptCandidateDraft {
        val best = chooseBestCandidate(existing, incoming)

        return best.copy(
            occurrences = existing.occurrences + incoming.occurrences
        )
    }

    private fun chooseBestCandidate(
        first: ConceptCandidateDraft,
        second: ConceptCandidateDraft
    ): ConceptCandidateDraft {
        val firstPriority = sourcePriority(first.source)
        val secondPriority = sourcePriority(second.source)

        if (secondPriority > firstPriority) {
            return second
        }

        if (firstPriority > secondPriority) {
            return first
        }

        return if (second.text.length > first.text.length) second else first
    }

    private fun sourcePriority(source: ConceptSource): Int {
        return when (source) {
            ConceptSource.NOUN_PHRASE -> 3
            ConceptSource.NER -> 2
            ConceptSource.NGRAM -> 1
        }
    }

    private fun stripLeadingStopwords(text: String): String {
        val tokens = text
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        val cleanedTokens = tokens.dropWhile { token ->
            tokenFilterService.isStopword(token)
        }

        return cleanedTokens.joinToString(" ")
    }
}