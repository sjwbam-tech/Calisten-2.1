package com.example.domain.ai.tokenizer

/**
 * Tokenizer abstraction for local offline model input/output processing.
 */
interface Tokenizer {
    /**
     * Converts raw text into token IDs.
     */
    fun encode(text: String): List<Int>

    /**
     * Converts token IDs back into readable string.
     */
    fun decode(tokens: List<Int>): String

    /**
     * Estimates the token count for prompt window budgeting.
     */
    fun countTokens(text: String): Int

    /**
     * Checks if a token ID is a special sequence delimiter.
     */
    fun isSpecialToken(tokenId: Int): Boolean
}

/**
 * High-performance, fully offline rule-based tokenizer for English and Persian text.
 * Requires zero network calls and works completely in-memory with deterministic ID mapping.
 */
class OfflineRuleBasedTokenizer(
    private val vocabOffset: Int = 1000
) : Tokenizer {

    companion object {
        const val PAD_TOKEN = 0
        const val UNK_TOKEN = 1
        const val BOS_TOKEN = 2
        const val EOS_TOKEN = 3
        const val IM_START_TOKEN = 4
        const val IM_END_TOKEN = 5
        const val JSON_START_TOKEN = 6
        const val JSON_END_TOKEN = 7

        private val SPECIAL_TOKENS = setOf(
            PAD_TOKEN, UNK_TOKEN, BOS_TOKEN, EOS_TOKEN,
            IM_START_TOKEN, IM_END_TOKEN, JSON_START_TOKEN, JSON_END_TOKEN
        )
    }

    private val wordToIdCache = mutableMapOf<String, Int>()
    private val idToWordCache = mutableMapOf<Int, String>()
    private var nextDynamicId = vocabOffset

    init {
        // Pre-populate core control tokens
        idToWordCache[PAD_TOKEN] = "<pad>"
        idToWordCache[UNK_TOKEN] = "<unk>"
        idToWordCache[BOS_TOKEN] = "<s>"
        idToWordCache[EOS_TOKEN] = "</s>"
        idToWordCache[IM_START_TOKEN] = "<|im_start|>"
        idToWordCache[IM_END_TOKEN] = "<|im_end|>"
        idToWordCache[JSON_START_TOKEN] = "```json"
        idToWordCache[JSON_END_TOKEN] = "```"

        idToWordCache.forEach { (id, str) ->
            wordToIdCache[str] = id
        }
    }

    override fun encode(text: String): List<Int> {
        if (text.isBlank()) return emptyList()

        val tokens = mutableListOf<Int>()
        tokens.add(BOS_TOKEN)

        // Split preserving words, Persian characters, numbers, and JSON delimiters
        val regex = Regex("<\\|[a-z_]+\\|>|```json|```|[\\p{L}\\p{M}\\p{N}_]+|[^\\s\\p{L}\\p{M}\\p{N}_]")
        val matches = regex.findAll(text)

        for (match in matches) {
            val part = match.value
            val id = synchronized(this) {
                wordToIdCache.getOrPut(part) {
                    val newId = nextDynamicId++
                    idToWordCache[newId] = part
                    newId
                }
            }
            tokens.add(id)
        }

        tokens.add(EOS_TOKEN)
        return tokens
    }

    override fun decode(tokens: List<Int>): String {
        if (tokens.isEmpty()) return ""

        val sb = StringBuilder()
        for (token in tokens) {
            if (token == PAD_TOKEN || token == BOS_TOKEN || token == EOS_TOKEN) continue
            val word = idToWordCache[token] ?: "<unk>"
            if (sb.isNotEmpty() && !word.startsWith("```") && !word.all { !it.isLetterOrDigit() }) {
                sb.append(" ")
            }
            sb.append(word)
        }
        return sb.toString().trim()
    }

    override fun countTokens(text: String): Int {
        if (text.isBlank()) return 0
        // Approximation: count word/symbol tokens plus BOS/EOS
        val regex = Regex("[\\p{L}\\p{M}\\p{N}_]+|[^\\s\\p{L}\\p{M}\\p{N}_]")
        return regex.findAll(text).count() + 2
    }

    override fun isSpecialToken(tokenId: Int): Boolean {
        return tokenId in SPECIAL_TOKENS
    }
}
