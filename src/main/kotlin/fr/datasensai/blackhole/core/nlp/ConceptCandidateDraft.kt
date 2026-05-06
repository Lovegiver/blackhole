package fr.datasensai.blackhole.core.nlp

data class ConceptCandidateDraft(
    val text: String,
    val normalizedText: String,
    val source: ConceptSource,
    val tokens: List<PosToken> = emptyList(),
    var occurrences: Int = 1
) {
    fun toConceptCandidate(score: Double): ConceptCandidate {
        return ConceptCandidate(
            text = text,
            source = source.value,
            score = score
        )
    }

    companion object {
        fun fromNounPhrase(np: NounPhraseCandidate): ConceptCandidateDraft {
            val text = np.text

            return ConceptCandidateDraft(
                text = text,
                normalizedText = text.lowercase(),
                source = ConceptSource.NOUN_PHRASE,
                tokens = np.tokens
            )
        }

        fun fromNgram(ng: NgramCandidate): ConceptCandidateDraft {
            return ConceptCandidateDraft(
                text = ng.text,
                normalizedText = ng.text.lowercase(),
                source = ConceptSource.NGRAM,
                tokens = emptyList()
            )
        }
    }
}
