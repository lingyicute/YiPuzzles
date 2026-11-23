/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lyi.puzzles.activities.helper.GameState;
import org.lyi.puzzles.activities.helper.GameStorage;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "w360dp-h640dp-port-mdpi")
public class GameMultiTouchTest {
    private static final float TRAVEL = 120;

    @Test public void aSecondFingerOnAnotherTileCancelsTheSwipe() {
        Context context = RuntimeEnvironment.getApplication();
        GameState state = new GameState(4);
        state.numbers[4] = 2;
        state.numbers[5] = 4;
        GameStorage.writeState(context.getFilesDir(), 4, state);
        Intent intent = new Intent(context, GameActivity.class).putExtra("n", 4).putExtra("new", false);
        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent).setup()) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(300));
            GameActivity activity = controller.get();
            View root = activity.getWindow().getDecorView();
            float[] first = center(activity.elements[1][0]);
            float[] second = center(activity.elements[1][1]);
            float[] firstEnd = {first[0] + TRAVEL, first[1]};
            float[] secondEnd = {second[0] + TRAVEL, second[1]};
            int secondPointer = 1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT;
            long down = SystemClock.uptimeMillis();
            dispatch(root, down, 0, MotionEvent.ACTION_DOWN, first);
            dispatch(root, down, 20, MotionEvent.ACTION_POINTER_DOWN | secondPointer, first, second);
            dispatch(root, down, 60, MotionEvent.ACTION_MOVE, firstEnd, secondEnd);
            dispatch(root, down, 80, MotionEvent.ACTION_POINTER_UP | secondPointer, firstEnd, secondEnd);
            dispatch(root, down, 100, MotionEvent.ACTION_UP, firstEnd);
            assertEquals(2, activity.elements[1][0].number);
            assertEquals(4, activity.elements[1][1].number);

            down += 200;
            dispatch(root, down, 0, MotionEvent.ACTION_DOWN, first);
            dispatch(root, down, 60, MotionEvent.ACTION_MOVE, firstEnd);
            dispatch(root, down, 100, MotionEvent.ACTION_UP, firstEnd);
            assertEquals("the next single-finger swipe still works", 2, activity.elements[1][2].number);
            assertEquals(4, activity.elements[1][3].number);
        } finally {
            GameStorage.deleteState(context.getFilesDir(), 4);
            GameStorage.deleteStatistics(context.getFilesDir(), 4);
        }
    }

    private static float[] center(View view) {
        float x = view.getWidth() / 2f, y = view.getHeight() / 2f;
        for (View v = view; v.getParent() instanceof View; v = (View) v.getParent()) {
            x += v.getLeft();
            y += v.getTop();
        }
        return new float[]{x, y};
    }

    private static void dispatch(View root, long down, int elapsed, int action, float[]... fingers) {
        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[fingers.length];
        MotionEvent.PointerCoords[] coords = new MotionEvent.PointerCoords[fingers.length];
        for (int i = 0; i < fingers.length; i++) {
            properties[i] = new MotionEvent.PointerProperties();
            properties[i].id = i;
            properties[i].toolType = MotionEvent.TOOL_TYPE_FINGER;
            coords[i] = new MotionEvent.PointerCoords();
            coords[i].x = fingers[i][0];
            coords[i].y = fingers[i][1];
        }
        MotionEvent event = MotionEvent.obtain(down, down + elapsed, action, fingers.length, properties, coords,
                0, 0, 1, 1, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
        try { root.dispatchTouchEvent(event); }
        finally { event.recycle(); }
    }
}
