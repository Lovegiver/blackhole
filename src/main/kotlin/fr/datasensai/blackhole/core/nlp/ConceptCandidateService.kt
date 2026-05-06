package fr.datasensai.blackhole.core.nlp

import fr.datasensai.blackhole.core.config.ConceptExtractionConfig
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ConceptCandidateService(
    private val posTaggingService: PosTaggingService,
    private val nounPhraseCandidateService: NounPhraseCandidateService,
    private val ngramService: NgramService,
    private val conceptCandidateMerger: ConceptCandidateMerger,
    private val conceptCandidateScorer: ConceptCandidateScorer,
    private val conceptExtractionConfig: ConceptExtractionConfig
) {

    fun extract(text: String): List<ConceptCandidate> {
        val posTokens = posTaggingService.tagText(text)
        val rawTokens = posTokens.map { it.token }

        val nounPhraseDrafts = nounPhraseCandidateService.extract(posTokens)
            .map { ConceptCandidateDraft.fromNounPhrase(it) }

        val ngramDrafts = ngramService.generate(
            tokens = rawTokens,
            minNgramSize = conceptExtractionConfig.minNgramSize(),
            maxNgramSize = conceptExtractionConfig.maxNgramSize()
        ).map {
            ConceptCandidateDraft.fromNgram(it)
        }

        val mergedCandidates = conceptCandidateMerger.merge(
            nounPhraseDrafts + ngramDrafts
        )

        return mergedCandidates
            .map { conceptCandidateScorer.score(it) }
            .sortedByDescending { it.score }
    }
}