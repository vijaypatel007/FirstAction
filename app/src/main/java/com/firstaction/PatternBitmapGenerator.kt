package com.firstaction

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

object PatternBitmapGenerator {

    private const val ROWS = 6
    private const val COLS = 4

    private val drawableCache = mutableMapOf<PatternType, Drawable?>()
    private val bitmapCache = mutableMapOf<Int, Bitmap>()

    private var CIRCLE_SIZE = 0
    private var SPACING = 0

    fun initialize(context: Context) {
        CIRCLE_SIZE = dpToPx(context, 30)
        SPACING = dpToPx(context, 6)

        // Load all drawables once
        drawableCache[PatternType.EMPTY] = ContextCompat.getDrawable(context, R.drawable.pattern_empty)
        drawableCache[PatternType.VERTICAL] = ContextCompat.getDrawable(context, R.drawable.pattern_vertical)
        drawableCache[PatternType.HORIZONTAL] = ContextCompat.getDrawable(context, R.drawable.pattern_horizontal)
        drawableCache[PatternType.BOTTOM_LEFT] = ContextCompat.getDrawable(context, R.drawable.pattern_bottom_left)
        drawableCache[PatternType.BOTTOM_RIGHT] = ContextCompat.getDrawable(context, R.drawable.pattern_bottom_right)
        drawableCache[PatternType.TOP_LEFT] = ContextCompat.getDrawable(context, R.drawable.pattern_top_left)
        drawableCache[PatternType.TOP_RIGHT] = ContextCompat.getDrawable(context, R.drawable.pattern_top_right)
        drawableCache[PatternType.CROSS] = ContextCompat.getDrawable(context, R.drawable.pattern_cross)

        // Pre-generate all digits (0-9)
        for (digit in 0..9) {
            bitmapCache[digit] = generateDigitBitmap(digit)
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    fun getDigitBitmap(digit: Int): Bitmap {
        return bitmapCache[digit] ?: generateDigitBitmap(digit)
    }

    private fun generateDigitBitmap(digit: Int): Bitmap {
        val width = COLS * CIRCLE_SIZE + (COLS - 1) * SPACING
        val height = ROWS * CIRCLE_SIZE + (ROWS - 1) * SPACING

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val pattern = DigitPatterns.getPattern(digit)

        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                val patternType = pattern[row][col]
                val drawable = drawableCache[patternType] ?: continue

                val left = col * (CIRCLE_SIZE + SPACING)
                val top = row * (CIRCLE_SIZE + SPACING)
                val right = left + CIRCLE_SIZE
                val bottom = top + CIRCLE_SIZE

                drawable.setBounds(left, top, right, bottom)
                drawable.draw(canvas)
            }
        }

        return bitmap
    }

    fun clearCache() {
        bitmapCache.values.forEach { it.recycle() }
        bitmapCache.clear()
    }
}