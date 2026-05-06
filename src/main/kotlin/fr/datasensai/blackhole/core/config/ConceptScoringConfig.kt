package fr.datasensai.blackhole.core.config

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "blackhole.nlp.scoring")
interface ConceptScoringConfig {
    fun nounPhraseWeight(): Double
    fun ngramWeight(): Double
    fun nerWeight(): Double
    fun occurrenceWeight(): Double
    fun singleTokenPenalty(): Double
}