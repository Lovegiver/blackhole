package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Test

class ActionPhraseExtractionExplorationTest {

    private val openNlpConfig = TestConfigs.openNlpConfig()

    private val tokenizerService = TokenizerService(
        openNlpTokenizerService = OpenNlpTokenizerService(openNlpConfig)
    )

    private val posTaggingService = PosTaggingService(
        tokenizerService = tokenizerService,
        openNlpConfig = openNlpConfig
    )

    private val linguisticResourceService = LinguisticResourceService(
        nlpConfig = TestConfigs.nlpConfig()
    )

    private val tokenFilterService = TokenFilterService(
        linguisticResourceService = linguisticResourceService
    )

    private val service = ActionPhraseCandidateService(
        tokenFilterService = tokenFilterService,
        textNormalizerService = TextNormalizerService()
    )

    @Test
    fun `exploration - inspect action phrases on long text`() {
        val text = """
            La proposition de loi vise à renforcer l’accompagnement des jeunes majeurs sortant de l’aide sociale à l’enfance.
            Elle prévoit un suivi personnalisé jusqu’à vingt-et-un ans, notamment pour l’accès au logement, à la formation et à l’emploi.
            Le texte présenté par Laure Miller entend réduire les ruptures de parcours et améliorer la coordination entre les départements, les associations et les services de l’État.
        """.trimIndent()

        val posTokens = posTaggingService.tagText(text)

        println("\n===== POS TOKENS =====")
        posTokens.forEach {
            println("${it.token}\t${it.pos}")
        }

        val candidates = service.extract(posTokens)

        println("\n===== ACTION PHRASES =====")
        candidates.forEach {
            println(it.text)
        }
    }
}