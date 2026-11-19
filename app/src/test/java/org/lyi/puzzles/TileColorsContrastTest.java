/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles;

import android.content.Context;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.graphics.ColorUtils;
import androidx.preference.PreferenceManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lyi.puzzles.helpers.TileColors;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import java.util.Map;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class TileColorsContrastTest {
    @Test @Config(qualifiers = "notnight")
    public void lightPaletteHasReadableLabels() { assertReadablePalette(); }

    @Test @Config(qualifiers = "night")
    public void darkPaletteHasReadableLabels() { assertReadablePalette(); }

    private void assertReadablePalette() {
        Context context = new ContextThemeWrapper(RuntimeEnvironment.getApplication(), R.style.Theme_YiPuzzles);
        for (String scheme : new String[] {TileColors.SCHEME_MATERIAL_YOU, TileColors.SCHEME_ORIGINAL, TileColors.SCHEME_CLASSIC_BLUE}) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(TileColors.PREF_KEY, scheme).commit();
            TileColors.invalidate();
            for (Map.Entry<Integer, TileColors.Tile> entry : TileColors.palette(context).entrySet()) {
                if (entry.getKey() == 0) continue;
                TileColors.Tile tile = entry.getValue();
                double contrast = ColorUtils.calculateContrast(tile.getText(), tile.getBackground());
                assertTrue("Scheme " + scheme + " tile " + entry.getKey() + " contrast: " + contrast, contrast >= 4.5);
            }
        }
    }
}
