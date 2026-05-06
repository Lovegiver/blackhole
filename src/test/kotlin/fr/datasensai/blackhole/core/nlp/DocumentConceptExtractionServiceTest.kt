package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DocumentConceptExtractionServiceTest {

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

    private val textNormalizerService = TextNormalizerService()

    private val nounPhraseCandidateService = NounPhraseCandidateService(
        normalizer = textNormalizerService
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

    private val conceptCandidateMerger = ConceptCandidateMerger(
        tokenFilterService = tokenFilterService,
        textNormalizerService = textNormalizerService
    )

    private val conceptCandidateScorer = ConceptCandidateScorer(
        scoringConfig = TestConfigs.conceptScoringConfig()
    )

    private val conceptCandidateService = ConceptCandidateService(
        sentenceSplitterService = sentenceSplitterService,
        sentenceChunker = sentenceChunker,
        nounPhraseCandidateService = nounPhraseCandidateService,
        ngramService = ngramService,
        conceptCandidateMerger = conceptCandidateMerger,
        conceptCandidateScorer = conceptCandidateScorer,
        conceptExtractionConfig = TestConfigs.conceptExtractionConfig()
    )

    private val service = DocumentConceptExtractionService(
        conceptCandidateService = conceptCandidateService,
        textNormalizerService = textNormalizerService
    )

    @Test
    fun `should boost title concepts`() {
        val input = DocumentAnalysisInput(
            documentId = "doc-1",
            title = "Proposition de loi pour les jeunes majeurs",
            summary = null,
            content = "Cette proposition vise à renforcer l’accompagnement des jeunes majeurs"
        )

        val result = service.extract(input)

        val jeunesMajeurs = result.find {
            it.normalizedKey == "jeunes majeurs"
        }

        assertNotNull(jeunesMajeurs)
        assertTrue(jeunesMajeurs!!.totalScore > 0)
        assertTrue(jeunesMajeurs.sections.contains(TextSection.TITLE))
        assertTrue(jeunesMajeurs.sections.contains(TextSection.CONTENT))
    }

    @Test
    fun `should rank title concepts higher than content only concepts`() {
        val input = DocumentAnalysisInput(
            documentId = "doc-1",
            title = "Jeunes majeurs et aide sociale à l’enfance",
            summary = null,
            content = "Le texte évoque également la coordination entre les départements et les associations."
        )

        val result = service.extract(input)

        val jeunesMajeurs = result.find {
            it.normalizedKey == "jeunes majeurs"
        }

        val coordination = result.find {
            it.normalizedKey == "coordination"
        }

        assertNotNull(jeunesMajeurs)

        if (coordination != null) {
            assertTrue(jeunesMajeurs!!.totalScore > coordination.totalScore)
        }
    }

    @Test
    fun `should merge same concept across title summary and content`() {
        val input = DocumentAnalysisInput(
            documentId = "doc-1",
            title = "Jeunes majeurs",
            summary = "Accompagnement des jeunes majeurs",
            content = "La proposition de loi vise les jeunes majeurs sortant de l’aide sociale à l’enfance."
        )

        val result = service.extract(input)

        val jeunesMajeurs = result.find {
            it.normalizedKey == "jeunes majeurs"
        }

        assertNotNull(jeunesMajeurs)
        assertTrue(jeunesMajeurs!!.sections.contains(TextSection.TITLE))
        assertTrue(jeunesMajeurs.sections.contains(TextSection.SUMMARY))
        assertTrue(jeunesMajeurs.sections.contains(TextSection.CONTENT))
    }

    @Test
    fun `exploration - inspect document concept extraction`() {
        val input = DocumentAnalysisInput(
            documentId = "doc-1",
            title = "Proposition de loi pour renforcer l’accompagnement des jeunes majeurs",
            summary = "Le texte prévoit un suivi personnalisé pour l’accès au logement, à la formation et à l’emploi.",
            content = """
            Le texte présenté par Laure Miller entend réduire les ruptures de parcours 
            et améliorer la coordination entre les départements, les associations et les services de l’État.
        """.trimIndent()
        )

        val result = service.extract(input)

        println("\n===== DOCUMENT CONCEPTS =====")
        result.take(50).forEach {
            println("${it.totalScore}\t${it.sections}\t${it.text}\t${it.normalizedKey}")
        }
    }

}