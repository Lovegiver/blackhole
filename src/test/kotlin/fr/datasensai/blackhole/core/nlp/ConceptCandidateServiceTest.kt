package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConceptCandidateServiceTest {

    private val openNlpConfig = TestConfigs.openNlpConfig()

    private val sentenceDetector = OpenNlpSentenceDetectorService(
        openNlpConfig = openNlpConfig
    )

    private val tokenizerService = TokenizerService(
        openNlpTokenizerService = OpenNlpTokenizerService(openNlpConfig)
    )

    private val posTaggingService = PosTaggingService(
        tokenizerService = tokenizerService,
        openNlpConfig = openNlpConfig
    )

    private val sentenceSplitterService = SentenceSplitterService(
        sentenceDetector = sentenceDetector,
        posTaggingService = posTaggingService
    )

    private val nounPhraseCandidateService = NounPhraseCandidateService(
        normalizer = TextNormalizerService()
    )

    private val linguisticResourceService = LinguisticResourceService(
        nlpConfig = TestConfigs.nlpConfig()
    )

    private val tokenFilterService = TokenFilterService(
        linguisticResourceService = linguisticResourceService
    )

    private val tokenQualificationService = TokenQualificationService(
        tokenFilterService = tokenFilterService
    )

    private val sentenceChunker = SentenceChunker(
        tokenQualificationService = tokenQualificationService
    )

    private val ngramService = NgramService(
        tokenQualificationService = tokenQualificationService
    )

    private val conceptCandidateMerger = ConceptCandidateMerger(tokenFilterService, TextNormalizerService())

    private val conceptCandidateScorer = ConceptCandidateScorer(
        scoringConfig = TestConfigs.conceptScoringConfig()
    )

    private val service = ConceptCandidateService(
        sentenceSplitterService = sentenceSplitterService,
        sentenceChunker = sentenceChunker,
        nounPhraseCandidateService = nounPhraseCandidateService,
        ngramService = ngramService,
        conceptCandidateMerger = conceptCandidateMerger,
        conceptCandidateScorer = conceptCandidateScorer,
        conceptExtractionConfig = TestConfigs.conceptExtractionConfig()
    )

    @Test
    fun `should extract meaningful concept candidates`() {
        val candidates = service.extract(
            "Proposition de loi de Mme Laure Miller visant à protéger durablement les jeunes majeurs"
        )

        val labels = candidates.map { it.text }

        assertTrue(labels.contains("proposition de loi"))
        assertTrue(labels.contains("laure miller"))
        assertTrue(labels.contains("jeunes majeurs"))
    }

    @Test
    fun `should include ngram candidates`() {
        val candidates = service.extract(
            "Budget 2026 pour la santé publique"
        )

        val labels = candidates.map { it.text.lowercase() }

        assertTrue(labels.contains("budget 2026"))
        assertTrue(labels.contains("santé publique"))
    }
}