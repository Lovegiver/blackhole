package fr.datasensai.blackhole.core.nlp

data class SentenceChunk(
    val tokens: List<QualifiedToken>
) {

    init {
        require(tokens.isNotEmpty()) {
            "tokens cannot be empty"
        }
    }

    fun text(): String =
        tokens.joinToString(" ") { it.text }

    fun containsVerb(): Boolean =
        tokens.any { it.isVerb() }

    fun containsNounLike(): Boolean =
        tokens.any { it.isNounLike() }

    fun size(): Int =
        tokens.size

    fun toPosTokens(): List<PosToken> =
        tokens.map {
            PosToken(
                token = it.text,
                pos = it.pos
            )
        }
}
