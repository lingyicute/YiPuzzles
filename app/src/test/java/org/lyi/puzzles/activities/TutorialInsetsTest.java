/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles.activities;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lyi.puzzles.R;
import org.lyi.puzzles.helpers.EdgeToEdgeHelper;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "w800dp-h360dp-land-mdpi")
public class TutorialInsetsTest {
    @Test public void tutorialAvoidsSideBarsAndCutoutsWithoutAccumulatingPadding() {
        checkTutorialInsets();
    }

    @Test @Config(qualifiers = "ldrtl-w800dp-h360dp-land-mdpi")
    public void tutorialUsesPhysicalInsetsInRtlLayouts() {
        checkTutorialInsets();
    }

    private void checkTutorialInsets() {
        try (ActivityController<TutorialActivity> controller =
                     Robolectric.buildActivity(TutorialActivity.class).setup()) {
            View content = controller.get().findViewById(R.id.main_content);
            for (int pass = 0; pass < 2; pass++) {
                WindowInsetsCompat result = ViewCompat.dispatchApplyWindowInsets(content, insets());
                assertPadding(content, 36, 24, 48, 20);
                assertEquals(Insets.of(0, 24, 48, 20),
                        result.getInsets(WindowInsetsCompat.Type.systemBars()));
            }
            ViewCompat.dispatchApplyWindowInsets(content, new WindowInsetsCompat.Builder()
                    .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.NONE)
                    .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.NONE).build());
            assertPadding(content, 0, 0, 0, 0);
        }
    }

    @Test public void bottomOnlyCallKeepsInsetsAlreadyHandledByTheParent() {
        View content = new View(RuntimeEnvironment.getApplication());
        content.setPadding(5, 6, 7, 8);
        EdgeToEdgeHelper.applyBottomSystemBarPadding(content);
        ViewCompat.dispatchApplyWindowInsets(content, insets());
        assertPadding(content, 5, 6, 7, 28);
    }

    private static WindowInsetsCompat insets() {
        return new WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, 24, 48, 20))
                .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(36, 0, 0, 0))
                .build();
    }

    private static void assertPadding(View view, int left, int top, int right, int bottom) {
        assertEquals("left safe area", left, view.getPaddingLeft());
        assertEquals("top safe area", top, view.getPaddingTop());
        assertEquals("right safe area", right, view.getPaddingRight());
        assertEquals("bottom safe area", bottom, view.getPaddingBottom());
    }
}
