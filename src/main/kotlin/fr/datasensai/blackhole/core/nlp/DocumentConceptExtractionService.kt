package fr.datasensai.blackhole.core.nlp

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class DocumentConceptExtractionService(
    private val conceptCandidateService: ConceptCandidateService,
    private val textNormalizerService: TextNormalizerService
) {

    fun extract(input: DocumentAnalysisInput): List<DocumentConcept> {

        val sectionResults = mutableListOf<SectionConceptResult>()

        extractSection(input.title, TextSection.TITLE, sectionResults)

        input.summary?.let {
            extractSection(it, TextSection.SUMMARY, sectionResults)
        }

        input.content?.let {
            extractSection(it, TextSection.CONTENT, sectionResults)
        }

        return merge(sectionResults, input.documentId)
    }

    private fun extractSection(
        text: String,
        section: TextSection,
        results: MutableList<SectionConceptResult>
    ) {
        val candidates = conceptCandidateService.extract(text)

        val weight = sectionWeight(section)

        candidates.forEach {
            results.add(
                SectionConceptResult(
                    text = it.text,
                    normalizedKey = textNormalizerService.normalizeKey(it.text),
                    score = it.score * weight,
                    section = section
                )
            )
        }
    }

    private fun merge(
        results: List<SectionConceptResult>,
        documentId: String
    ): List<DocumentConcept> {

        val map = mutableMapOf<String, DocumentConcept>()

        results.forEach { result ->

            val existing = map[result.normalizedKey]

            if (existing == null) {
                map[result.normalizedKey] = DocumentConcept(
                    documentId = documentId,
                    text = result.text,
                    normalizedKey = result.normalizedKey,
                    totalScore = result.score,
                    sections = setOf(result.section)
                )
            } else {
                map[result.normalizedKey] = existing.copy(
                    totalScore = existing.totalScore + result.score,
                    sections = existing.sections + result.section
                )
            }
        }

        return map.values.sortedByDescending { it.totalScore }
    }

    private fun sectionWeight(section: TextSection): Double {
        return when (section) {
            TextSection.TITLE -> 3.0
            TextSection.SUMMARY -> 2.0
            TextSection.CONTENT -> 1.0
        }
    }

}