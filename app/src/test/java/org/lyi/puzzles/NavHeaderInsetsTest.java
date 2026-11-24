/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles;

import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lyi.puzzles.helpers.EdgeToEdgeHelper;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class NavHeaderInsetsTest {
    @Test public void liveInsetsReplaceTheFallbackEvenWhenTheStatusBarDisappears() {
        View root = new View(RuntimeEnvironment.getApplication());
        View header = new View(RuntimeEnvironment.getApplication());
        header.setPadding(3, 7, 11, 13);
        EdgeToEdgeHelper.applyNavHeaderTopInset(root, Arrays.asList(null, header));
        for (int top : new int[]{80, 80, 10, 0, 24, 0}) {
            ViewCompat.dispatchApplyWindowInsets(root, new WindowInsetsCompat.Builder()
                    .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, top, 0, 0))
                    .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.NONE).build());
            assertEquals("authoritative top inset " + top, 7 + top, header.getPaddingTop());
            assertEquals(3, header.getPaddingLeft());
            assertEquals(11, header.getPaddingRight());
            assertEquals(13, header.getPaddingBottom());
        }
        ViewCompat.dispatchApplyWindowInsets(root, new WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, 24, 0, 0))
                .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(0, 48, 0, 0)).build());
        assertEquals(55, header.getPaddingTop());
    }
}
