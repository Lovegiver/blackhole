package fr.datasensai.blackhole.core.nlp

data class DocumentAnalysisInput(
    val documentId: String,
    val title: String,
    val summary: String?,
    val content: String?
)
