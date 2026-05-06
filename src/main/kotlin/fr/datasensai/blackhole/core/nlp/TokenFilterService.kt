package fr.datasensai.blackhole.core.nlp

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TokenFilterService(
    private val linguisticResourceService: LinguisticResourceService
) {

    private val weakTokens: Set<String> by lazy {
        linguisticResourceService.loadWordSet("weak-tokens-fr.txt")
    }

    private val stopwords: Set<String> by lazy {
        linguisticResourceService.loadWordSet("stopwords-fr.txt")
    }

    fun isWeakToken(token: String): Boolean {
        return token.lowercase() in weakTokens
    }

    fun isStopword(token: String): Boolean {
        return token.lowercase() in stopwords
    }

    fun isSignificantToken(token: String): Boolean {
        return token.isNotBlank() && !isWeakToken(token)
    }

    fun isAllStopwords(tokens: List<String>): Boolean {
        return tokens.isNotEmpty() && tokens.all { isStopword(it) }
    }

    fun containsAtLeastOneSignificantToken(tokens: List<String>): Boolean {
        return tokens.any { isSignificantToken(it) }
    }

}