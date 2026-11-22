/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles.activities;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lyi.puzzles.R;
import org.lyi.puzzles.activities.helper.GameState;
import org.lyi.puzzles.activities.helper.GameStorage;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {21, 34})
public class GameAccessibilityTest {
    private Context context;

    @Before public void prepare() {
        context = RuntimeEnvironment.getApplication();
        clear();
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean("pref_animationActivated", false).commit();
    }

    @After public void clear() {
        for (int size = 4; size <= 7; size++) {
            GameStorage.deleteState(context.getFilesDir(), size);
            GameStorage.deleteStatistics(context.getFilesDir(), size);
        }
    }

    @Test public void accessibilityActionsMergeInAllFourDirections() {
        int[] labels = {R.string.swipe_up, R.string.swipe_right, R.string.swipe_down, R.string.swipe_left};
        for (int size : new int[]{4, 7}) for (int direction = 0; direction < 4; direction++) {
            GameState state = new GameState(size);
            boolean vertical = direction % 2 == 0;
            state.numbers[vertical ? size : 1] = 2;
            state.numbers[vertical ? size * 2 : 2] = 2;
            GameStorage.writeState(context.getFilesDir(), size, state);
            try (ActivityController<GameActivity> controller = open(size)) {
                GameActivity activity = controller.get();
                int action = action(activity.touch_field, context.getString(labels[direction]));
                assertNotNull(activity.touch_field.getContentDescription());
                assertEquals(View.IMPORTANT_FOR_ACCESSIBILITY_YES,
                        activity.touch_field.getImportantForAccessibility());
                assertTrue(activity.touch_field.performAccessibilityAction(action, null));
                int y = direction == 2 ? size - 1 : 0;
                int x = direction == 1 ? size - 1 : 0;
                assertEquals(4, activity.elements[y][x].number);
                assertEquals("4", activity.textFieldPoints.getText().toString());
                assertEquals(View.VISIBLE, activity.undoButton.getVisibility());
            }
        }
    }

    @Test public void noOpDoesNotSpawnAndRecreationKeepsExactlyOneActionPerDirection() {
        GameState state = new GameState(4);
        state.numbers[0] = 2;
        GameStorage.writeState(context.getFilesDir(), 4, state);
        try (ActivityController<GameActivity> controller = open(4)) {
            GameActivity activity = controller.get();
            assertFalse(activity.touch_field.performAccessibilityAction(
                    action(activity.touch_field, context.getString(R.string.swipe_left)), null));
            assertEquals(View.INVISIBLE, activity.undoButton.getVisibility());
            int occupied = 0;
            for (Element[] row : activity.elements) for (Element tile : row) if (tile.number != 0) occupied++;
            assertEquals(1, occupied);
            controller.recreate();
            layout(controller.get());
            controller.get().initialize();
            for (int label : new int[]{R.string.swipe_up, R.string.swipe_right, R.string.swipe_down, R.string.swipe_left}) {
                action(controller.get().touch_field, context.getString(label));
            }
        }
    }

    private ActivityController<GameActivity> open(int size) {
        Intent intent = new Intent(context, GameActivity.class).putExtra("n", size).putExtra("new", false);
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent).setup();
        layout(controller.get());
        return controller;
    }

    private static void layout(GameActivity activity) {
        View root = activity.getWindow().getDecorView();
        for (int pass = 0; pass < 3; pass++) {
            root.measure(View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY));
            root.layout(0, 0, 600, 1000);
        }
    }

    private static int action(View board, String label) {
        int count = 0, id = 0;
        AccessibilityNodeInfoCompat node = AccessibilityNodeInfoCompat.wrap(board.createAccessibilityNodeInfo());
        for (AccessibilityNodeInfoCompat.AccessibilityActionCompat action : node.getActionList()) {
            if (label.contentEquals(action.getLabel() == null ? "" : action.getLabel())) {
                count++;
                id = action.getId();
            }
        }
        assertEquals("Expected one accessible move action: " + label, 1, count);
        return id;
    }
}
