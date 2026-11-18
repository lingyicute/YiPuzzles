/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles.activities;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lyi.puzzles.activities.helper.GameState;
import org.lyi.puzzles.activities.helper.GameStorage;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowSystemClock;

import java.io.File;
import java.time.Duration;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class GameActivityLifecycleTest {
    private Context context;
    private File files;

    @Before public void cleanBefore() {
        context = RuntimeEnvironment.getApplication();
        files = context.getFilesDir();
        clear();
    }

    @After public void clear() {
        for (int size = 4; size <= 7; size++) {
            GameStorage.deleteState(files, size);
            GameStorage.deleteStatistics(files, size);
        }
    }

    @Test public void windowFocusDoesNotRebuildTilesOrLoseMoves() {
        GameState state = new GameState(4);
        state.numbers[0] = state.numbers[1] = 2;
        GameStorage.writeState(files, 4, state);

        try (ActivityController<GameActivity> controller = open(4, false)) {
            GameActivity activity = controller.get();
            press(activity, KeyEvent.KEYCODE_DPAD_LEFT);
            View tile = activity.elements[0][0];
            assertEquals(4, activity.elements[0][0].number);
            activity.onWindowFocusChanged(false);
            activity.onWindowFocusChanged(true);
            assertSame(tile, activity.elements[0][0]);
            assertEquals("4", activity.textFieldPoints.getText().toString());
            controller.pause();
            assertEquals(1, GameStorage.readStatistics(files, 4).getMoves());
        }
    }

    @Test public void freshGameSurvivesActivityRecreation() {
        try (ActivityController<GameActivity> controller = open(7, true)) {
            GameActivity activity = controller.get();
            for (int key : new int[]{KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN}) {
                press(activity, key);
                if (activity.undoButton.getVisibility() == View.VISIBLE) break;
            }
            assertEquals(View.VISIBLE, activity.undoButton.getVisibility());
            int[] before = numbers(activity);
            controller.recreate();
            layout(controller.get());
            assertArrayEquals(before, numbers(controller.get()));
            assertEquals(View.VISIBLE, controller.get().undoButton.getVisibility());
        }
    }

    @Test public void excludesPausedTimeAndNormalizesUnsupportedSizes() {
        try (ActivityController<GameActivity> controller = open(8, true)) {
            assertEquals(4, controller.get().elements.length);
            ShadowSystemClock.advanceBy(Duration.ofSeconds(2));
            controller.pause();
            long first = GameStorage.readStatistics(files, 4).getTimePlayed();
            assertTrue(first >= 2_000 && first < 4_000);

            ShadowSystemClock.advanceBy(Duration.ofMinutes(10));
            controller.resume();
            ShadowSystemClock.advanceBy(Duration.ofSeconds(3));
            controller.pause();
            long second = GameStorage.readStatistics(files, 4).getTimePlayed() - first;
            assertTrue(second >= 3_000 && second < 5_000);
        }
    }

    private ActivityController<GameActivity> open(int size, boolean fresh) {
        Intent intent = new Intent(context, GameActivity.class).putExtra("n", size).putExtra("new", fresh);
        ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent).setup();
        layout(controller.get());
        return controller;
    }

    private static void layout(GameActivity activity) {
        View root = activity.getWindow().getDecorView();
        int width = View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY);
        int height = View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY);
        for (int i = 0; i < 2; i++) {
            root.measure(width, height);
            root.layout(0, 0, 600, 1000);
        }
        assertNotNull(activity.elements);
    }

    private static int[] numbers(GameActivity activity) {
        int size = activity.elements.length;
        int[] result = new int[size * size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) result[y * size + x] = activity.elements[y][x].number;
        }
        return result;
    }

    private static void press(GameActivity activity, int key) {
        activity.onKeyDown(key, new KeyEvent(KeyEvent.ACTION_DOWN, key));
    }
}
