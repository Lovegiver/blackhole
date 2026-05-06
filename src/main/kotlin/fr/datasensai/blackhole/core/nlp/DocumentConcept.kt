package fr.datasensai.blackhole.core.nlp

data class DocumentConcept(
    val documentId: String,
    val text: String,
    val normalizedKey: String,
    val totalScore: Double,
    val sections: Set<TextSection>
)
