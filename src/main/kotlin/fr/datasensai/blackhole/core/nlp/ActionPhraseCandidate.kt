package fr.datasensai.blackhole.core.nlp

data class ActionPhraseCandidate(
    val text: String
) {
    init {
        require(text.isNotBlank()) { "text cannot be blank" }
    }
}