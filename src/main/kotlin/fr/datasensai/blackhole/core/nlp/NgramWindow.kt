package fr.datasensai.blackhole.core.nlp

class NgramWindow(
    private val tokens: List<QualifiedToken>
) {
    init {
        require(tokens.isNotEmpty()) { "tokens cannot be empty" }
    }

    fun isValid(): Boolean {
        if (containsPunctuation()) return false
        if (startsWithStopword()) return false
        if (endsWithStopword()) return false
        if (containsOnlyStopwords()) return false
        if (!containsMeaningfulToken()) return false
        if (endsWithOpenSyntax()) return false
        if (endsWithVerb()) return false

        return true
    }

    fun toCandidate(): NgramCandidate =
        NgramCandidate(
            text = tokens.joinToString(" ") { it.text }
        )

    private fun containsPunctuation(): Boolean =
        tokens.any { it.isPunctuation() }

    private fun startsWithStopword(): Boolean =
        tokens.first().stopword

    private fun endsWithStopword(): Boolean =
        tokens.last().stopword

    private fun containsOnlyStopwords(): Boolean =
        tokens.all { it.stopword }

    private fun containsMeaningfulToken(): Boolean =
        tokens.any { it.isMeaningful() }

    private fun endsWithOpenSyntax(): Boolean {
        val last = tokens.last()

        return last.isAdposition() ||
                last.isDeterminer() ||
                last.isConjunction()
    }

    private fun endsWithVerb(): Boolean =
        tokens.last().isVerb()
}