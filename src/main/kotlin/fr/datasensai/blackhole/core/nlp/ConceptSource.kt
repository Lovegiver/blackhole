package fr.datasensai.blackhole.core.nlp

enum class ConceptSource(val value: String) {
    NOUN_PHRASE("nlp"),
    NGRAM("ngram"),
    NER("ner")
}