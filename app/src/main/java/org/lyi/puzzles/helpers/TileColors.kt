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
package org.lyi.puzzles.helpers

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.graphics.ColorUtils
import androidx.preference.PreferenceManager
import com.google.android.material.color.MaterialColors
import org.lyi.puzzles.R

/**
 * Resolves the colours of the game tiles.
 *
 * Three palettes are available (preference `pref_color`):
 *  * `1` – **Material You**: generated at runtime from the current Material 3 theme
 *    (wallpaper based dynamic colours on Android 12+, the baseline scheme otherwise).
 *    Tiles cycle through tonal ramps of the primary, tertiary and secondary colour roles
 *    so they always harmonise with the rest of the UI – in light *and* dark mode.
 *  * `2` – **Original**: the well known 2048 palette.
 *  * `3` – **Classic blue**: the former default palette of the app.
 */
object TileColors {

    const val PREF_KEY = "pref_color"
    const val SCHEME_MATERIAL_YOU = "1"
    const val SCHEME_ORIGINAL = "2"
    const val SCHEME_CLASSIC_BLUE = "3"

    /** A pair of background / foreground colours for one tile value. */
    data class Tile(@ColorInt val background: Int, @ColorInt val text: Int)

    private enum class Role { PRIMARY, TERTIARY, SECONDARY }

    /** Colour role + lightness (light theme, dark theme) for every tile value, 2 .. 32768. */
    private data class Step(val role: Role, val light: Float, val dark: Float)

    private val STEPS = linkedMapOf(
        2 to Step(Role.PRIMARY, 0.90f, 0.30f),
        4 to Step(Role.PRIMARY, 0.82f, 0.38f),
        8 to Step(Role.PRIMARY, 0.70f, 0.46f),
        16 to Step(Role.PRIMARY, 0.58f, 0.54f),
        32 to Step(Role.PRIMARY, 0.46f, 0.63f),
        64 to Step(Role.PRIMARY, 0.35f, 0.74f),
        128 to Step(Role.TERTIARY, 0.85f, 0.32f),
        256 to Step(Role.TERTIARY, 0.72f, 0.42f),
        512 to Step(Role.TERTIARY, 0.58f, 0.52f),
        1024 to Step(Role.TERTIARY, 0.45f, 0.64f),
        2048 to Step(Role.TERTIARY, 0.33f, 0.78f),
        4096 to Step(Role.SECONDARY, 0.80f, 0.34f),
        8192 to Step(Role.SECONDARY, 0.65f, 0.46f),
        16384 to Step(Role.SECONDARY, 0.50f, 0.58f),
        32768 to Step(Role.SECONDARY, 0.36f, 0.72f),
    )

    private val STATIC_ATTRS = linkedMapOf(
        0 to (R.attr.buttonEmpty to R.attr.buttonEmptyText),
        2 to (R.attr.button2 to R.attr.button2Text),
        4 to (R.attr.button4 to R.attr.button4Text),
        8 to (R.attr.button8 to R.attr.button8Text),
        16 to (R.attr.button16 to R.attr.button16Text),
        32 to (R.attr.button32 to R.attr.button32Text),
        64 to (R.attr.button64 to R.attr.button64Text),
        128 to (R.attr.button128 to R.attr.button128Text),
        256 to (R.attr.button256 to R.attr.button256Text),
        512 to (R.attr.button512 to R.attr.button512Text),
        1024 to (R.attr.button1024 to R.attr.button1024Text),
        2048 to (R.attr.button2048 to R.attr.button2048Text),
        4096 to (R.attr.button4096 to R.attr.button4096Text),
        8192 to (R.attr.button8192 to R.attr.button8192Text),
        16384 to (R.attr.button16384 to R.attr.button16384Text),
        32768 to (R.attr.button32768 to R.attr.button32768Text),
    )

    // Small cache: palette generation is cheap, but drawItem() is called for every tile on
    // every move, so avoid re-resolving theme attributes all the time.
    private var cacheKey: String? = null
    private var cache: Map<Int, Tile> = emptyMap()

    @JvmStatic
    fun currentScheme(context: Context): String =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_KEY, SCHEME_MATERIAL_YOU) ?: SCHEME_MATERIAL_YOU

    /** `true` if the currently selected palette follows the app theme (Material You). */
    @JvmStatic
    fun isDynamic(context: Context): Boolean = currentScheme(context) == SCHEME_MATERIAL_YOU

    /** Colours of the tile showing [number] (0 = empty tile). */
    @JvmStatic
    fun forNumber(context: Context, number: Int): Tile {
        val palette = palette(context)
        return palette[number] ?: palette[32768] ?: palette.getValue(0)
    }

    /** Colour of the board behind the tiles. */
    @JvmStatic
    @ColorInt
    fun boardColor(context: Context): Int =
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceContainer, Color.LTGRAY)

    /** Full palette for the active colour scheme. */
    @JvmStatic
    fun palette(context: Context): Map<Int, Tile> {
        val scheme = currentScheme(context)
        val night = isNight(context)
        val primary = themeColor(context, com.google.android.material.R.attr.colorPrimary)
        val key = "$scheme|$night|$primary"
        if (key == cacheKey && cache.isNotEmpty()) return cache

        val palette = when (scheme) {
            SCHEME_ORIGINAL -> staticPalette(context, R.style.ThemeOverlay_YiPuzzles_Tiles_Original)
            SCHEME_CLASSIC_BLUE -> staticPalette(context, R.style.ThemeOverlay_YiPuzzles_Tiles_ClassicBlue)
            else -> materialYouPalette(context, night)
        }
        cacheKey = key
        cache = palette
        return palette
    }

    /** Drop the cached palette (e.g. after the user picked another scheme). */
    @JvmStatic
    fun invalidate() {
        cacheKey = null
        cache = emptyMap()
    }

    // ------------------------------------------------------------------ Material You palette

    private fun materialYouPalette(context: Context, night: Boolean): Map<Int, Tile> {
        val primary = themeColor(context, com.google.android.material.R.attr.colorPrimary)
        val secondary = themeColor(context, com.google.android.material.R.attr.colorSecondary)
        val tertiary = themeColor(context, com.google.android.material.R.attr.colorTertiary)
        val empty = themeColor(context, com.google.android.material.R.attr.colorSurfaceContainerHighest)

        val result = LinkedHashMap<Int, Tile>()
        result[0] = Tile(empty, empty)
        for ((number, step) in STEPS) {
            val base = when (step.role) {
                Role.PRIMARY -> primary
                Role.TERTIARY -> tertiary
                Role.SECONDARY -> secondary
            }
            val background = withLightness(base, if (night) step.dark else step.light)
            result[number] = Tile(background, textColorFor(background))
        }
        return result
    }

    /** Same hue/saturation as [color] but with the given HSL lightness (0..1). */
    @ColorInt
    private fun withLightness(@ColorInt color: Int, lightness: Float): Int {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color, hsl)
        // Dynamic secondary colours are intentionally muted – give tiles a little more life
        hsl[1] = hsl[1].coerceAtLeast(0.35f)
        hsl[2] = lightness.coerceIn(0f, 1f)
        return ColorUtils.HSLToColor(hsl)
    }

    /** Legible foreground colour for [background], tinted with the same hue. */
    @ColorInt
    private fun textColorFor(@ColorInt background: Int): Int {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(background, hsl)
        val dark = ColorUtils.calculateLuminance(background) > 0.30
        hsl[1] = (hsl[1] * 0.6f).coerceAtMost(0.5f)
        hsl[2] = if (dark) 0.12f else 0.97f
        return ColorUtils.HSLToColor(hsl)
    }

    // ------------------------------------------------------------------ static palettes

    private fun staticPalette(context: Context, @StyleRes overlay: Int): Map<Int, Tile> {
        val result = LinkedHashMap<Int, Tile>()
        for ((number, attrs) in STATIC_ATTRS) {
            result[number] = Tile(
                overlayColor(context, overlay, attrs.first),
                overlayColor(context, overlay, attrs.second)
            )
        }
        // Empty tiles always follow the surface so the board looks right in dark mode as well
        val empty = themeColor(context, com.google.android.material.R.attr.colorSurfaceContainerHighest)
        result[0] = Tile(empty, empty)
        return result
    }

    @ColorInt
    private fun overlayColor(context: Context, @StyleRes overlay: Int, @AttrRes attr: Int): Int {
        val ta = context.theme.obtainStyledAttributes(overlay, intArrayOf(attr))
        try {
            return ta.getColor(0, Color.MAGENTA)
        } finally {
            ta.recycle()
        }
    }

    // ------------------------------------------------------------------ helpers

    @ColorInt
    private fun themeColor(context: Context, @AttrRes attr: Int): Int =
        MaterialColors.getColor(context, attr, Color.GRAY)

    private fun isNight(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
}
