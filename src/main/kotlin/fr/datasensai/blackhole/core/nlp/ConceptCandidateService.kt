package fr.datasensai.blackhole.core.nlp

import fr.datasensai.blackhole.core.config.ConceptExtractionConfig
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ConceptCandidateService(
    private val sentenceSplitterService: SentenceSplitterService,
    private val nounPhraseCandidateService: NounPhraseCandidateService,
    private val ngramService: NgramService,
    private val conceptCandidateMerger: ConceptCandidateMerger,
    private val conceptCandidateScorer: ConceptCandidateScorer,
    private val conceptExtractionConfig: ConceptExtractionConfig
) {

    fun extract(text: String): List<ConceptCandidate> {
        if (text.isBlank()) {
            return emptyList()
        }

        val drafts = sentenceSplitterService
            .split(text)
            .flatMap { sentence ->
                extractFromSentence(sentence)
            }

        val mergedCandidates = conceptCandidateMerger.merge(drafts)

        return mergedCandidates
            .map { conceptCandidateScorer.score(it) }
            .sortedByDescending { it.score }
    }

    private fun extractFromSentence(sentence: Sentence): List<ConceptCandidateDraft> {
        val nounPhraseDrafts = nounPhraseCandidateService
            .extract(sentence.tokens)
            .map { ConceptCandidateDraft.fromNounPhrase(it) }

        val ngramDrafts = ngramService
            .generate(
                tokens = sentence.tokens,
                minNgramSize = conceptExtractionConfig.minNgramSize(),
                maxNgramSize = conceptExtractionConfig.maxNgramSize()
            )
            .map { ConceptCandidateDraft.fromNgram(it) }

        return nounPhraseDrafts + ngramDrafts
    }
}