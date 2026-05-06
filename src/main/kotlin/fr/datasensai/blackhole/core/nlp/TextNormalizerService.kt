package fr.datasensai.blackhole.core.nlp

import jakarta.enterprise.context.ApplicationScoped
import java.text.Normalizer

@ApplicationScoped
class TextNormalizerService {

    fun normalizeConceptLabel(value: String): String =
        value
            .lowercase()
            .replace("’", "'")
            .replace(Regex("\\s+"), " ")
            .trim()

    fun normalizeKey(value: String): String =
        Normalizer.normalize(normalizeConceptLabel(value), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
}