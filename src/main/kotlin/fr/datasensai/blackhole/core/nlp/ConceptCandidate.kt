package fr.datasensai.blackhole.core.nlp

data class ConceptCandidate(
    val text: String,
    val source: String,   // "ngram" ou "nlp"
    val score: Double
) {
    init {
        require(text.isNotBlank()) { "text cannot be blank" }
        require(source in listOf("ngram", "nlp", "ner")) {
            "source must be 'ngram', 'nlp' or 'ner'"
        }
        require(score >= 0.0) { "score cannot be negative" }
    }
}
