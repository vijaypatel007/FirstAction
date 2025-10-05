package com.firstaction

import android.content.Context
import android.graphics.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object PatternBitmapGenerator {

    private const val ROWS = 6
    private const val COLS = 4

    // sizing (dp converted in initialize)
    private var CIRCLE_SIZE = 0
    private var SPACING = 0
    private var PADDING = 4

    // cache bitmaps for digits 0..9 for the final (static) state
    private val bitmapCache = mutableMapOf<Int, Bitmap>()

    // frames cache keyed by "digit_frameIndex" for transient animation frames
    // we don't aggressively cache frames; we generate and let GC free them.
    // But keep a small in-memory cache to avoid re-draw during a single animation run:
    private val transientCache = mutableMapOf<String, Bitmap>()

    // map PatternType -> pair(hourAngleDeg, minuteAngleDeg). Angles in degrees where 0deg=3 o'clock, increasing clockwise.
    // This mapping is subjective — tweak to taste.
    private val patternAngles = mapOf(
        PatternType.EMPTY to Pair(135f, 135f),        // between 7 and 8 (as user requested) -> approx -135 and -45
        PatternType.VERTICAL to Pair(-90f, 90f),        // vertical line: 12 and 6
        PatternType.HORIZONTAL to Pair(0f, 180f),      // horizontal line: 3 and 9
        PatternType.TOP_RIGHT to Pair(0f, -90f),       // corner top-right: hands at 3 and 12
        PatternType.BOTTOM_RIGHT to Pair(90f, 0f),     // corner bottom-right: hands at ~4:30 and 3
        PatternType.BOTTOM_LEFT to Pair(90f, 180f),   // corner bottom-left: ~7:30 and 9
        PatternType.TOP_LEFT to Pair(-90f, 180f),     // corner top-left: ~10:30 and 9
        PatternType.CROSS to Pair(-90f, 0f)            // cross: 12 and 3 (visual cross)
    )

    // number of animation frames when changing a digit (tweak; keep small to save battery)
    private const val N_FRAMES = 6
    const val FRAME_MS = 180L // spacing between frames in ms (N_FRAMES * FRAME_MS ~ total animation duration)

    private var density = 1f

    fun initialize(context: Context) {
        density = context.resources.displayMetrics.density
        CIRCLE_SIZE = dpToPx(30)
        SPACING = dpToPx(1)
        PADDING = dpToPx(2)

        // Pre-generate static bitmaps for digits 0..9
        for (digit in 0..9) {
            bitmapCache[digit] = generateDigitBitmap(context, digit)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * density).toInt()

    // Public: get current static bitmap for a digit
    fun getDigitBitmap(digit: Int): Bitmap {
        return bitmapCache[digit] ?: bitmapCache[0]!!
    }

    fun clearCache() {
        bitmapCache.values.forEach { if (!it.isRecycled) it.recycle() }
        bitmapCache.clear()
        transientCache.values.forEach { if (!it.isRecycled) it.recycle() }
        transientCache.clear()
    }

    // Build a digit by composing ROWS x COLS mini-clocks (each mini-clock depicts one PatternType)
    private fun generateDigitBitmap(context: Context, digit: Int): Bitmap {
        val width = COLS * CIRCLE_SIZE + (COLS - 1) * SPACING + PADDING * 2
        val height = ROWS * CIRCLE_SIZE + (ROWS - 1) * SPACING + PADDING * 2
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)

        val pattern = DigitPatterns.getPattern(digit)

        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                val pt = pattern[row][col]
                val left = PADDING + col * (CIRCLE_SIZE + SPACING)
                val top = PADDING + row * (CIRCLE_SIZE + SPACING)
                val cellBitmap = drawMiniClockBitmap(CIRCLE_SIZE, pt)
                canvas.drawBitmap(cellBitmap, left.toFloat(), top.toFloat(), null)
                cellBitmap.recycle()
            }
        }

        return bitmap
    }

    // Draw a single mini-clock bitmap representing a PatternType (final target geometry)
    private fun drawMiniClockBitmap(size: Int, patternType: PatternType, hourAngle: Float? = null, minuteAngle: Float? = null): Bitmap {
        val key = "${patternType.name}_${hourAngle ?: "T"}_${minuteAngle ?: "T"}"
        transientCache[key]?.let { return it.copy(it.config, true) }

        val b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        c.drawColor(Color.TRANSPARENT)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = size / 2f
        val cy = size / 2f
        val radius = min(size, size) / 2f - dpToPx(1)

        // face (fill + stroke)
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        c.drawCircle(cx, cy, radius, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = maxOf(0.5f, density * 0.5f)
        paint.color = Color.LTGRAY
        c.drawCircle(cx, cy, radius, paint)

        // ticks (4 major ticks)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = maxOf(1f, density * 0.8f)

        // use supplied angles or mapping
        val pair = patternAngles[patternType] ?: Pair(-90f, 90f) // default vertical
        val hAng = hourAngle ?: pair.first
        val mAng = minuteAngle ?: pair.second
        val increase = 0.9f

        // draw minute hand
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = maxOf((2f + increase), density * (1.4f + increase))
        paint.color = Color.BLACK
        drawHand(c, cx, cy, radius * 0.9f, mAng, paint)

        // draw hour hand (shorter)
        paint.strokeWidth = maxOf((2f + increase), density * (1.4f + increase))
        paint.color = Color.BLACK
        drawHand(c, cx, cy, radius * 0.9f, hAng, paint)

        // center disk
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        c.drawCircle(cx, cy, maxOf(1f, density * 1f), paint)

        transientCache[key] = b
        return b.copy(b.config, true)
    }

    private fun drawHand(canvas: Canvas, cx: Float, cy: Float, length: Float, angleDeg: Float, paint: Paint) {
        // angleDeg: 0deg => 3 o'clock; canvas coords: 0deg along +X; convert to radians
        val rad = Math.toRadians(angleDeg.toDouble())
        val x = cx + length * cos(rad)
        val y = cy + length * sin(rad)
        canvas.drawLine(cx, cy, x.toFloat(), y.toFloat(), paint)
    }

    // create interpolated frames between two PatternTypes for a single cell
    private fun generateFramesForCell(size: Int, from: PatternType, to: PatternType, frames: Int): List<Bitmap> {
        val fromPair = patternAngles[from] ?: Pair(-90f, 90f)
        val toPair = patternAngles[to] ?: Pair(-90f, 90f)

        val list = ArrayList<Bitmap>(frames)
        for (i in 1..frames) {
            val t = i.toFloat() / frames
            val ha = lerpAngle(fromPair.first, toPair.first, t)
            val ma = lerpAngle(fromPair.second, toPair.second, t)
            val b = drawMiniClockBitmap(size, to, ha, ma)
            list.add(b)
        }
        return list
    }

    private fun lerpAngle(a: Float, b: Float, t: Float): Float {
        // simple lerp; works even across wrap because angles here limited
        return a + (b - a) * t
    }

    // Public: animate transition for a full digit cell grid.
    // This will generate N_FRAMES frames for each changed cell and assemble each digit-frame bitmap,
    // then return the list of bitmaps representing each frame for the whole digit.
    // Caller must update widget with these bitmaps at FRAME_MS intervals.
    fun generateAnimatedDigitFrames(context: Context, fromDigit: Int, toDigit: Int): List<Bitmap> {
        val fromPattern = DigitPatterns.getPattern(fromDigit)
        val toPattern = DigitPatterns.getPattern(toDigit)

        // precompute per-cell frames
        val cellFrames = Array(ROWS) { Array(COLS) { emptyList<Bitmap>() } }
        for (r in 0 until ROWS) {
            for (c in 0 until COLS) {
                val fromPt = if (fromDigit in 0..9) fromPattern[r][c] else PatternType.EMPTY
                val toPt = toPattern[r][c]
                if (fromPt == toPt) {
                    // just static same frames repeated
                    val single = drawMiniClockBitmap(CIRCLE_SIZE, toPt)
                    cellFrames[r][c] = List(N_FRAMES) { single.copy(single.config, true) }
                    single.recycle()
                } else {
                    cellFrames[r][c] = generateFramesForCell(CIRCLE_SIZE, fromPt, toPt, N_FRAMES)
                }
            }
        }

        // now compose full-digit frames (N_FRAMES bitmaps)
        val width = COLS * CIRCLE_SIZE + (COLS - 1) * SPACING + PADDING * 2
        val height = ROWS * CIRCLE_SIZE + (ROWS - 1) * SPACING + PADDING * 2

        val framesList = ArrayList<Bitmap>(N_FRAMES)
        for (frameIdx in 0 until N_FRAMES) {
            val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm)
            canvas.drawColor(Color.TRANSPARENT)
            for (r in 0 until ROWS) {
                for (c in 0 until COLS) {
                    val left = PADDING + c * (CIRCLE_SIZE + SPACING)
                    val top = PADDING + r * (CIRCLE_SIZE + SPACING)
                    val cellBitmap = cellFrames[r][c][frameIdx]
                    canvas.drawBitmap(cellBitmap, left.toFloat(), top.toFloat(), null)
                    // recycle cellBitmap - they were copied during build so safe to recycle here
                    cellBitmap.recycle()
                }
            }
            framesList.add(bm)
        }

        return framesList
    }

    // Helper: assemble the four digit bitmaps into a full widget image (optional - we keep them separate normally)
    fun assembleDigitsIntoWidgetBitmap(hourTens: Bitmap, hourOnes: Bitmap, minTens: Bitmap, minOnes: Bitmap): Bitmap {
        // this function not used directly in widget, but handy if you want to produce 1 big bitmap.
        val gap = dpToPx(6)
        val w = maxOf(hourTens.width + hourOnes.width + minTens.width + minOnes.width + gap * 3, 1)
        val h = maxOf(hourTens.height, hourOnes.height, minTens.height, minOnes.height)
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        var x = 0
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        c.drawBitmap(hourTens, x.toFloat(), 0f, paint); x += hourTens.width + gap
        c.drawBitmap(hourOnes, x.toFloat(), 0f, paint); x += hourOnes.width + gap
        c.drawBitmap(minTens, x.toFloat(), 0f, paint); x += minTens.width + gap
        c.drawBitmap(minOnes, x.toFloat(), 0f, paint)
        return out
    }
}
