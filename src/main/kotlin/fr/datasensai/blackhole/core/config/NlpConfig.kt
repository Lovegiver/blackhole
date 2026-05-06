package fr.datasensai.blackhole.core.config

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "blackhole.nlp")
interface NlpConfig {
    fun language(): String
    fun resources(): ResourcesConfig

    interface ResourcesConfig {
        fun mode(): String
        fun basePath(): String
    }
}