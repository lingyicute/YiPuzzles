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
@file:JvmName("EdgeToEdgeHelper")

package org.lyi.puzzles.helpers

import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.EdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Enables Material 3 edge-to-edge drawing: the content is laid out behind the transparent
 * system bars and the status / navigation bar icons automatically switch between light and
 * dark depending on the current theme.
 */
fun ComponentActivity.enableEdgeToEdgeDisplay() {
    EdgeToEdge.enable(this)
}

/**
 * Adds the bottom system bar inset (navigation bar / gesture area) as padding to [view] so
 * that scrollable or bottom aligned content is never covered.
 */
fun applyBottomSystemBarPadding(view: View?) = applySystemBarPadding(view, top = false, bottom = true)

/**
 * Applies the system bar insets of the requested edges as padding to [view]. Unlike
 * `android:fitsSystemWindows` the insets are *not* consumed, so sibling views still receive them.
 */
fun applySystemBarPadding(view: View?, top: Boolean, bottom: Boolean) {
    if (view == null) return
    val initialTop = view.paddingTop
    val initialBottom = view.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        v.updatePadding(
            top = if (top) initialTop + bars.top else v.paddingTop,
            bottom = if (bottom) initialBottom + bars.bottom else v.paddingBottom
        )
        insets
    }
    ViewCompat.requestApplyInsets(view)
}
