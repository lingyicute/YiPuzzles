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

import android.os.Build
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.color.DynamicColors
import org.lyi.puzzles.PF2048

/**
 * Enables Material 3 edge-to-edge drawing: the content is laid out behind the transparent
 * system bars and the status / navigation bar icons automatically switch between light and
 * dark depending on the current theme.
 */
fun ComponentActivity.enableEdgeToEdgeDisplay() {
    enableEdgeToEdge()
}

/**
 * Re-applies the Material You dynamic colour overlay at a point in the activity
 * lifecycle where the effective night mode is already settled, i.e. right after
 * [ComponentActivity.onCreate] (which lets AppCompat apply the app's
 * dark / light / system preference).
 *
 * [DynamicColors.applyToActivitiesIfAvailable] (registered in [PF2048.onCreate])
 * applies the overlay in `onActivityPreCreated`, which runs *before*
 * `super.onCreate()`. If the configuration is still settling at that moment
 * (system auto dark mode switch, process restored from a stale state, ...),
 * the light/dark overlay variant can be inconsistent with the theme the
 * content is actually inflated with, so different colour roles resolve to
 * light *and* dark values on the same screen. Re-applying (with force=true
 * inside the library) after `super.onCreate()` guarantees that the overlay
 * and the theme agree before the first layout pass; when nothing changed it
 * is a cheap no-op.
 */
fun ComponentActivity.applyDynamicColorsSettled() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && PF2048.isDynamicColorEnabled(this)) {
        DynamicColors.applyToActivityIfAvailable(this)
    }
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

/**
 * Keeps NavigationView header view(s) below the status bar.
 *
 * A listener set directly on the header does NOT work here: DrawerLayout (with
 * `fitsSystemWindows="true"`) consumes the system window insets in its own
 * `OnApplyWindowInsetsListener` and only re-dispatches them during `onMeasure`,
 * and NavigationView consumes them again internally — so the header never reliably
 * receives real insets.
 *
 * Instead this listens on [root] (the activity content view, which always receives
 * the real insets before any consumer) and forwards the top inset to [headers].
 * As a safety net the system `status_bar_height` is applied immediately, so the
 * header is correct even before the first inset dispatch (or if dispatch fails).
 * The insets are *not* consumed, so DrawerLayout / NavigationView keep working as before.
 */
fun applyNavHeaderTopInset(root: View?, headers: List<View?>) {
    val targets = headers.filterNotNull()
    if (root == null || targets.isEmpty()) return
    val initialTops = targets.map { it.paddingTop }

    // Immediate fallback from framework resources (no dispatch chain involved).
    val res = root.resources
    val statusBarId = res.getIdentifier("status_bar_height", "dimen", "android")
    val statusBarHeight = if (statusBarId > 0) res.getDimensionPixelSize(statusBarId) else 0
    targets.forEachIndexed { index, header ->
        header.updatePadding(top = initialTops[index] + statusBarHeight)
    }

    // Refine with live insets (cutout, rotation, foldables, ...).
    ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val top = maxOf(bars.top, statusBarHeight)
        targets.forEachIndexed { index, header ->
            header.updatePadding(top = initialTops[index] + top)
        }
        insets
    }
    ViewCompat.requestApplyInsets(root)
}
