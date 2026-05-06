package fr.datasensai.blackhole.core.nlp

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class TokenQualificationService(
    private val tokenFilterService: TokenFilterService
) {

    fun qualify(tokens: List<PosToken>): List<QualifiedToken> {
        return tokens
            .filter { it.token.isNotBlank() }
            .map {
                QualifiedToken(
                    text = it.token,
                    pos = it.pos,
                    stopword = tokenFilterService.isStopword(it.token),
                    weak = tokenFilterService.isWeakToken(it.token)
                )
            }
    }
}