package fr.datasensai.blackhole.core.nlp

data class SectionConceptResult(
    val text: String,
    val normalizedKey: String,
    val score: Double,
    val section: TextSection
)
