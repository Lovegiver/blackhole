package fr.datasensai.blackhole.core.nlp

data class NgramCandidate(
    val text: String
) {
    init {
        require(text.isNotBlank()) { "text cannot be blank" }
    }
}
