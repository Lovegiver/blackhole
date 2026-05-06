package fr.datasensai.blackhole.core.config

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "blackhole.nlp.extraction")
interface ConceptExtractionConfig {
    fun minNgramSize(): Int
    fun maxNgramSize(): Int
}