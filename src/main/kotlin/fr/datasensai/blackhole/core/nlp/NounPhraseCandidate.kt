package fr.datasensai.blackhole.core.nlp

data class NounPhraseCandidate(
    val text: String,
    val tokens: List<PosToken>
) {
    override fun toString(): String {
        return "NounPhraseCandidate(text='$text', tokens=${tokens.size} tokens)"
    }
}