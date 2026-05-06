package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Test

class ConceptExtractionExplorationTest {

    private val openNlpConfig = TestConfigs.openNlpConfig()

    private val tokenizerService = TokenizerService(
        openNlpTokenizerService = OpenNlpTokenizerService(openNlpConfig)
    )

    private val posTaggingService = PosTaggingService(
        tokenizerService = tokenizerService,
        openNlpConfig = openNlpConfig
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

    private val ngramService = NgramService(
        tokenQualificationService = tokenQualificationService
    )

    private val conceptCandidateMerger = ConceptCandidateMerger(tokenFilterService, TextNormalizerService())

    private val conceptCandidateScorer = ConceptCandidateScorer(
        scoringConfig = TestConfigs.conceptScoringConfig()
    )

    private val service = ConceptCandidateService(
        posTaggingService = posTaggingService,
        nounPhraseCandidateService = nounPhraseCandidateService,
        ngramService = ngramService,
        conceptCandidateMerger = conceptCandidateMerger,
        conceptCandidateScorer = conceptCandidateScorer,
        conceptExtractionConfig = TestConfigs.conceptExtractionConfig()
    )

    @Test
    fun `exploration - inspect concept extraction on long text`() {
        val text = """
            La proposition de loi vise à renforcer l’accompagnement des jeunes majeurs sortant de l’aide sociale à l’enfance.
            Elle prévoit un suivi personnalisé jusqu’à vingt-et-un ans, notamment pour l’accès au logement, à la formation et à l’emploi.
            Le texte présenté par Laure Miller entend réduire les ruptures de parcours et améliorer la coordination entre les départements, les associations et les services de l’État.
        """.trimIndent()

        val candidates = service.extract(text)

        println("\n===== CONCEPTS =====")

        candidates
            .take(50)
            .forEach {
                println("${it.score}\t${it.source}\t${it.text}")
            }
    }
}