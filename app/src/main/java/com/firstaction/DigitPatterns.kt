// PatternType.kt
package com.firstaction

enum class PatternType {
    EMPTY,           // No pattern (blank circle)
    VERTICAL,        // |
    HORIZONTAL,      // —
    TOP_LEFT,        // ┐ (corner top-left)
    TOP_RIGHT,       // ┌ (corner top-right)
    BOTTOM_LEFT,     // ┘ (corner bottom-left)
    BOTTOM_RIGHT,    // └ (corner bottom-right)
    CROSS            // + (cross)
}

object DigitPatterns {
    // Each digit is represented as 4x6 grid (4 columns, 6 rows)
    // Total 24 positions per digit

    private val patterns = mapOf(
        0 to arrayOf(
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.VERTICAL, PatternType.BOTTOM_RIGHT, PatternType.BOTTOM_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.VERTICAL, PatternType.VERTICAL, PatternType.VERTICAL, PatternType.VERTICAL),
            arrayOf(PatternType.VERTICAL, PatternType.VERTICAL, PatternType.VERTICAL, PatternType.VERTICAL),
            arrayOf(PatternType.VERTICAL, PatternType.TOP_RIGHT, PatternType.TOP_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.TOP_LEFT)
        ),
        1 to arrayOf(
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT, PatternType.EMPTY),
            arrayOf(PatternType.TOP_RIGHT, PatternType.BOTTOM_LEFT, PatternType.VERTICAL, PatternType.EMPTY),
            arrayOf(PatternType.EMPTY, PatternType.VERTICAL, PatternType.VERTICAL, PatternType.EMPTY),
            arrayOf(PatternType.EMPTY, PatternType.VERTICAL, PatternType.VERTICAL, PatternType.EMPTY),
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.TOP_LEFT, PatternType.TOP_RIGHT, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.TOP_LEFT)
        ),
        2 to arrayOf(
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.TOP_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.VERTICAL, PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.TOP_LEFT),
            arrayOf(PatternType.VERTICAL, PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.TOP_LEFT)
        ),
        3 to arrayOf(
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.EMPTY, PatternType.BOTTOM_RIGHT, PatternType.TOP_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.EMPTY, PatternType.TOP_RIGHT, PatternType.BOTTOM_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.TOP_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.TOP_LEFT)
        ),
        4 to arrayOf(
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.BOTTOM_LEFT, PatternType.BOTTOM_RIGHT, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.VERTICAL, PatternType.VERTICAL, PatternType.VERTICAL, PatternType.VERTICAL),
            arrayOf(PatternType.VERTICAL, PatternType.TOP_RIGHT, PatternType.TOP_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.EMPTY, PatternType.EMPTY, PatternType.VERTICAL, PatternType.VERTICAL),
            arrayOf(PatternType.EMPTY, PatternType.EMPTY, PatternType.TOP_RIGHT, PatternType.TOP_LEFT)
        ),
        5 to arrayOf(
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.VERTICAL, PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.TOP_LEFT),
            arrayOf(PatternType.VERTICAL, PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.TOP_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.TOP_LEFT)
        ),
        6 to arrayOf(
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.VERTICAL, PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.TOP_LEFT),
            arrayOf(PatternType.VERTICAL, PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.VERTICAL, PatternType.BOTTOM_RIGHT, PatternType.BOTTOM_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.VERTICAL, PatternType.TOP_RIGHT, PatternType.TOP_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.TOP_LEFT)
        ),
        9 to arrayOf(
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.VERTICAL, PatternType.BOTTOM_RIGHT, PatternType.BOTTOM_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.VERTICAL, PatternType.TOP_RIGHT, PatternType.TOP_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.EMPTY, PatternType.EMPTY, PatternType.VERTICAL, PatternType.VERTICAL),
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.TOP_LEFT, PatternType.TOP_RIGHT, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.TOP_LEFT)
        ),
        7 to arrayOf(
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.EMPTY, PatternType.EMPTY, PatternType.VERTICAL, PatternType.VERTICAL),
            arrayOf(PatternType.EMPTY, PatternType.EMPTY, PatternType.VERTICAL, PatternType.VERTICAL),
            arrayOf(PatternType.EMPTY, PatternType.EMPTY, PatternType.VERTICAL, PatternType.VERTICAL),
            arrayOf(PatternType.EMPTY, PatternType.EMPTY, PatternType.TOP_RIGHT, PatternType.TOP_LEFT)
        ),
        8 to arrayOf(
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.VERTICAL, PatternType.BOTTOM_RIGHT, PatternType.BOTTOM_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.VERTICAL, PatternType.TOP_RIGHT, PatternType.TOP_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.VERTICAL, PatternType.BOTTOM_RIGHT, PatternType.BOTTOM_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.VERTICAL, PatternType.TOP_RIGHT, PatternType.TOP_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.TOP_LEFT)
        ),
        9 to arrayOf(
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT),
            arrayOf(PatternType.VERTICAL, PatternType.BOTTOM_RIGHT, PatternType.BOTTOM_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.VERTICAL, PatternType.TOP_RIGHT, PatternType.TOP_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.BOTTOM_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.BOTTOM_RIGHT, PatternType.HORIZONTAL, PatternType.TOP_LEFT, PatternType.VERTICAL),
            arrayOf(PatternType.TOP_RIGHT, PatternType.HORIZONTAL, PatternType.HORIZONTAL, PatternType.TOP_LEFT)
        )
    )

    fun getPattern(digit: Int): Array<Array<PatternType>> {
        return patterns[digit] ?: patterns[0]!!
    }
}