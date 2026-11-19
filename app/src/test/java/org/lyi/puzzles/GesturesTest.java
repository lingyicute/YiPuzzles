/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles;

import android.os.SystemClock;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lyi.puzzles.activities.helper.Gestures;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "mdpi")
public class GesturesTest {
    private final View view = new View(RuntimeEnvironment.getApplication());
    private final long downTime = SystemClock.uptimeMillis();

    @Test public void slowSwipesWorkInEveryDirectionWithoutAFling() {
        float[][] ends = {{100, 10}, {190, 100}, {100, 190}, {10, 100}};
        for (int direction = 0; direction < ends.length; direction++) {
            RecordingGestures gestures = new RecordingGestures();
            touch(gestures, MotionEvent.ACTION_DOWN, 0, 100, 100);
            touch(gestures, MotionEvent.ACTION_MOVE, 100, ends[direction][0], ends[direction][1]);
            touch(gestures, MotionEvent.ACTION_MOVE, 700, ends[direction][0], ends[direction][1]);
            touch(gestures, MotionEvent.ACTION_UP, 800, ends[direction][0], ends[direction][1]);
            assertEquals("slow swipe direction " + direction, direction, gestures.direction);
            assertEquals(1, gestures.moves);
        }
    }

    @Test public void cancellationDoesNotMoveAndTheNextGestureStillWorks() {
        RecordingGestures gestures = new RecordingGestures();
        touch(gestures, MotionEvent.ACTION_DOWN, 0, 100, 100);
        touch(gestures, MotionEvent.ACTION_CANCEL, 50, 190, 100);
        touch(gestures, MotionEvent.ACTION_UP, 100, 190, 100);
        assertEquals(0, gestures.moves);
        touch(gestures, MotionEvent.ACTION_DOWN, 200, 100, 100);
        touch(gestures, MotionEvent.ACTION_MOVE, 250, 10, 100);
        touch(gestures, MotionEvent.ACTION_UP, 300, 10, 100);
        assertEquals(3, gestures.direction);
        assertEquals(1, gestures.moves);
    }

    @Test public void aSecondFingerCancelsTheSwipe() {
        RecordingGestures gestures = new RecordingGestures();
        touch(gestures, MotionEvent.ACTION_DOWN, 0, 100, 100);
        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[2];
        MotionEvent.PointerCoords[] coords = new MotionEvent.PointerCoords[2];
        for (int i = 0; i < 2; i++) {
            properties[i] = new MotionEvent.PointerProperties();
            properties[i].id = i;
            properties[i].toolType = MotionEvent.TOOL_TYPE_FINGER;
            coords[i] = new MotionEvent.PointerCoords();
            coords[i].x = 100 + i * 10;
            coords[i].y = 100;
        }
        for (int action : new int[]{MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_POINTER_UP}) {
            MotionEvent event = MotionEvent.obtain(downTime, downTime + 20,
                    action | (1 << MotionEvent.ACTION_POINTER_INDEX_SHIFT), 2, properties, coords,
                    0, 0, 1, 1, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
            try { gestures.onTouch(view, event); }
            finally { event.recycle(); }
        }
        touch(gestures, MotionEvent.ACTION_MOVE, 50, 190, 100);
        touch(gestures, MotionEvent.ACTION_UP, 100, 190, 100);
        assertEquals(0, gestures.moves);
    }

    @Test @Config(qualifiers = "xxxhdpi")
    public void shortPhysicalDragsDoNotBecomeSwipesOnDenseScreens() {
        RecordingGestures gestures = new RecordingGestures();
        touch(gestures, MotionEvent.ACTION_DOWN, 0, 100, 100);
        touch(gestures, MotionEvent.ACTION_MOVE, 50, 160, 100);
        touch(gestures, MotionEvent.ACTION_UP, 100, 160, 100);
        assertEquals(0, gestures.moves);
    }

    private void touch(Gestures gestures, int action, int elapsed, float x, float y) {
        MotionEvent event = MotionEvent.obtain(downTime, downTime + elapsed, action, x, y, 0);
        try { gestures.onTouch(view, event); }
        finally { event.recycle(); }
    }

    private static class RecordingGestures extends Gestures {
        int direction = -1;
        int moves;
        RecordingGestures() { super(RuntimeEnvironment.getApplication()); }
        private boolean record(int value) { direction = value; moves++; return true; }
        @Override public boolean onSwipeTop() { return record(0); }
        @Override public boolean onSwipeRight() { return record(1); }
        @Override public boolean onSwipeBottom() { return record(2); }
        @Override public boolean onSwipeLeft() { return record(3); }
    }
}
