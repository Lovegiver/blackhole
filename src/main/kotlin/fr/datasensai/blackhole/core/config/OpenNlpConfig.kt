package fr.datasensai.blackhole.core.config

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "blackhole.nlp.opennlp")
interface OpenNlpConfig {
    fun tokenizerModelPath(): String
    fun posModelPath(): String
    fun sentenceModelPath(): String
}