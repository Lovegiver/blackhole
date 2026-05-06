package fr.datasensai.blackhole.core.nlp

import fr.datasensai.blackhole.core.config.ConceptScoringConfig
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ConceptCandidateScorer(
    private val scoringConfig: ConceptScoringConfig
) {

    fun score(candidate: ConceptCandidateDraft): ConceptCandidate {
        var score = 0.0

        score += when (candidate.source) {
            ConceptSource.NOUN_PHRASE -> scoringConfig.nounPhraseWeight()
            ConceptSource.NGRAM -> scoringConfig.ngramWeight()
            ConceptSource.NER -> scoringConfig.nerWeight()
        }

        score += candidate.occurrences * scoringConfig.occurrenceWeight()

        if (candidate.tokens.size == 1) {
            score -= scoringConfig.singleTokenPenalty()
        }

        return candidate.toConceptCandidate(score)
    }
}