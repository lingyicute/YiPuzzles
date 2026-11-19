/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles.activities;

import android.content.Intent;
import android.graphics.Rect;
import android.view.View;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class GameScoreLayoutTest {
    @Test @Config(qualifiers = "w640dp-h360dp-land-mdpi")
    public void landscapeScoresAndControlsRemainVisible() {
        checkScores(640, 360);
    }

    @Test @Config(qualifiers = "w320dp-h640dp-port-mdpi")
    public void narrowPortraitKeepsMaximumScoresOnOneLine() {
        checkScores(320, 640);
    }

    private void checkScores(int width, int height) {
        Intent intent = new Intent(RuntimeEnvironment.getApplication(), GameActivity.class)
                .putExtra("n", 4).putExtra("new", true);
        try (ActivityController<GameActivity> controller =
                     Robolectric.buildActivity(GameActivity.class, intent).setup()) {
            GameActivity activity = controller.get();
            View root = activity.getWindow().getDecorView();
            layout(root, width, height);
            String maximum = String.valueOf(Integer.MAX_VALUE);
            activity.textFieldPoints.setText(maximum);
            activity.textFieldRecord.setText(maximum);
            layout(root, width, height);
            for (TextView value : new TextView[]{activity.textFieldPoints, activity.textFieldRecord}) {
                assertNotNull("score layout missing at " + value.getWidth() + "x" + value.getHeight(), value.getLayout());
                assertEquals("scores must not wrap", 1, value.getLayout().getLineCount());
                assertEquals(0, value.getLayout().getEllipsisCount(0));
                assertEquals(maximum, value.getText().toString());
                int available = value.getWidth() - value.getCompoundPaddingLeft() - value.getCompoundPaddingRight();
                assertTrue("all digits must fit, not merely be clipped",
                        value.getPaint().measureText(maximum) <= available + 1);
            }
            Rect visible = new Rect();
            assertTrue(activity.restartButton.getGlobalVisibleRect(visible));
            assertEquals("restart must not be clipped", activity.restartButton.getHeight(), visible.height());
        }
    }

    private void layout(View root, int width, int height) {
        for (int i = 0; i < 4; i++) {
            root.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
            root.layout(0, 0, width, height);
        }
    }
}
