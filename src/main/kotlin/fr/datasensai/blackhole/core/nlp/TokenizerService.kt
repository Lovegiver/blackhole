package fr.datasensai.blackhole.core.nlp

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TokenizerService(
    private val openNlpTokenizerService: OpenNlpTokenizerService
) {
    fun tokenize(text: String): List<String> =
        openNlpTokenizerService.tokenize(text)
}