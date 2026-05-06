package fr.datasensai.blackhole.core.nlp

data class Sentence(
    val rawText: String,
    val tokens: List<PosToken>
) {
    init {
        require(rawText.isNotBlank()) { "rawText cannot be blank" }
        require(tokens.isNotEmpty()) { "tokens cannot be empty" }
    }

    fun text(): String =
        rawText

    fun containsVerb(): Boolean =
        tokens.any { it.pos == "VERB" }

    fun containsNounLike(): Boolean =
        tokens.any { it.pos == "NOUN" || it.pos == "PROPN" }
}
