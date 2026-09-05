/*
 This file is part of YiPuzzles.

 YiPuzzles is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 YiPuzzles is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YiPuzzles. If not, see <http://www.gnu.org/licenses/>.
 */
package org.lyi.puzzles.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import org.lyi.puzzles.R
import org.lyi.puzzles.helpers.TileColors
import kotlin.math.min

/**
 * Draws a miniature, themed preview of a game board (used on the home screen to choose the
 * board size and in the statistics). Colours are taken from [TileColors] so the preview always
 * matches the palette the user actually plays with – including Material You dynamic colours.
 */
class BoardPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var boardSize: Int = 4
        set(value) {
            field = value.coerceIn(2, 12)
            invalidate()
        }

    private val boardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val rect = RectF()

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.BoardPreviewView, defStyleAttr, 0).apply {
            try {
                boardSize = getInt(R.styleable.BoardPreviewView_boardSize, 4)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Always square, fit into the available space
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val size = when {
            wMode == MeasureSpec.UNSPECIFIED && hMode == MeasureSpec.UNSPECIFIED -> dp(200f).toInt()
            wMode == MeasureSpec.UNSPECIFIED -> h
            hMode == MeasureSpec.UNSPECIFIED -> w
            else -> min(w, h)
        }
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val n = boardSize
        val size = min(width, height).toFloat()
        val left = (width - size) / 2f
        val top = (height - size) / 2f

        val outerRadius = size * 0.08f
        boardPaint.color = TileColors.boardColor(context)
        rect.set(left, top, left + size, top + size)
        canvas.drawRoundRect(rect, outerRadius, outerRadius, boardPaint)

        val gap = size / (n * 7f + 1f)
        val cell = (size - gap * (n + 1)) / n
        val radius = cell * 0.18f

        for (row in 0 until n) {
            for (col in 0 until n) {
                val number = sampleNumber(row, col, n)
                val tile = TileColors.forNumber(context, number)
                val x = left + gap + col * (cell + gap)
                val y = top + gap + row * (cell + gap)
                rect.set(x, y, x + cell, y + cell)
                tilePaint.color = tile.background
                canvas.drawRoundRect(rect, radius, radius, tilePaint)

                if (number > 0) {
                    val label = number.toString()
                    textPaint.color = tile.text
                    textPaint.textSize = cell * (if (label.length <= 2) 0.46f else 0.46f * 2.4f / label.length)
                    val baseline = y + cell / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                    canvas.drawText(label, x + cell / 2f, baseline, textPaint)
                }
            }
        }
    }

    /**
     * Deterministic "in progress" looking board: higher numbers collect in one corner,
     * some cells stay empty.
     */
    private fun sampleNumber(row: Int, col: Int, n: Int): Int {
        val distance = row + col                      // 0 .. 2n-2, corner first
        val maxExp = n + 3                              // 4x4 -> up to 128
        val exp = maxExp - distance - ((row * 7 + col * 3) % 2)
        if (exp < 1) return 0
        // sprinkle a few empty cells
        if ((row * 5 + col * 11 + n) % 6 == 0 && distance > 1) return 0
        return 1 shl exp
    }

    private fun dp(value: Float) = value * resources.displayMetrics.density
}
