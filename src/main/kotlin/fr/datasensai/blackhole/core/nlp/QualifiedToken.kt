package fr.datasensai.blackhole.core.nlp

data class QualifiedToken(
    val text: String,
    val pos: String,
    val stopword: Boolean,
    val weak: Boolean
) {
    init {
        require(text.isNotBlank()) { "text cannot be blank" }
    }

    fun isVerb(): Boolean =
        pos == "VERB"

    fun isNounLike(): Boolean =
        pos == "NOUN" || pos == "PROPN"

    fun isAdposition(): Boolean =
        pos == "ADP" || pos == "ADP+DET"

    fun isDeterminer(): Boolean =
        pos == "DET"

    fun isConjunction(): Boolean =
        pos == "CCONJ" || pos == "SCONJ"

    fun isPunctuation(): Boolean =
        pos == "PUNCT" || text.all { !it.isLetterOrDigit() }

    fun isMeaningful(): Boolean =
        !stopword && !weak && !isPunctuation()
}
