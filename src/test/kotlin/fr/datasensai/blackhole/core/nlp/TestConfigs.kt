package fr.datasensai.blackhole.core.nlp

import fr.datasensai.blackhole.core.config.ConceptExtractionConfig
import fr.datasensai.blackhole.core.config.ConceptScoringConfig
import fr.datasensai.blackhole.core.config.NlpConfig
import fr.datasensai.blackhole.core.config.OpenNlpConfig

object TestConfigs {

    fun openNlpConfig(): OpenNlpConfig =
        object : OpenNlpConfig {
            override fun tokenizerModelPath(): String =
                "models/opennlp-fr-ud-gsd-tokens-1.3-2.5.4.bin"

            override fun posModelPath(): String =
                "models/opennlp-fr-ud-gsd-pos-1.3-2.5.4.bin"
        }

    fun nlpConfig(): NlpConfig =
        object : NlpConfig {
            override fun language(): String = "fr"

            override fun resources(): NlpConfig.ResourcesConfig =
                object : NlpConfig.ResourcesConfig {
                    override fun mode(): String = "classpath"
                    override fun basePath(): String = "nlp"
                }
        }

    fun conceptScoringConfig(): ConceptScoringConfig =
        object : ConceptScoringConfig {
            override fun nounPhraseWeight(): Double = 5.0
            override fun ngramWeight(): Double = 3.0
            override fun nerWeight(): Double = 6.0
            override fun occurrenceWeight(): Double = 1.0
            override fun singleTokenPenalty(): Double = 2.0
        }

    fun conceptExtractionConfig(): ConceptExtractionConfig =
        object : ConceptExtractionConfig {
            override fun minNgramSize(): Int = 2
            override fun maxNgramSize(): Int = 3
        }

}