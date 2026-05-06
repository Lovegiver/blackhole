package fr.datasensai.blackhole.core.nlp

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class SentenceChunker(
    private val tokenQualificationService: TokenQualificationService
) {

    fun chunk(sentence: Sentence): List<SentenceChunk> {

        val qualified = tokenQualificationService.qualify(sentence.tokens)

        val chunks = mutableListOf<SentenceChunk>()
        val current = mutableListOf<QualifiedToken>()

        for (token in qualified) {

            if (shouldSplitBefore(token) && current.isNotEmpty()) {
                chunks.add(SentenceChunk(current.toList()))
                current.clear()
            }

            current.add(token)

            if (shouldSplitAfter(token)) {
                chunks.add(SentenceChunk(current.toList()))
                current.clear()
            }
        }

        if (current.isNotEmpty()) {
            chunks.add(SentenceChunk(current.toList()))
        }

        return chunks
            .map { trimChunk(it) }
            .filter { it.tokens.isNotEmpty() }
    }

    private fun shouldSplitBefore(token: QualifiedToken): Boolean {
        return token.isConjunction()
    }

    private fun shouldSplitAfter(token: QualifiedToken): Boolean {
        return token.isPunctuation()
    }

    private fun trimChunk(chunk: SentenceChunk): SentenceChunk {

        val trimmed = chunk.tokens
            .dropWhile { it.isPunctuation() }
            .dropLastWhile { it.isPunctuation() }

        return SentenceChunk(trimmed)
    }
}