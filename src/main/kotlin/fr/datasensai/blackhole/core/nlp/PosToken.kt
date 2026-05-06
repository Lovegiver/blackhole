package fr.datasensai.blackhole.core.nlp

data class PosToken(
    val token: String,
    val pos: String
) {
    override fun toString(): String = "$token/$pos"
}
