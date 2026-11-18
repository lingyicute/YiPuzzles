/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles.activities;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import androidx.preference.PreferenceManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lyi.puzzles.activities.helper.GameState;
import org.lyi.puzzles.activities.helper.GameStorage;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import java.util.Arrays;
import java.util.Random;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class GameActivityMovesTest {
    @Test public void movesAndUndoMatchReferenceAcrossSizesAndDirections() {
        Context context = RuntimeEnvironment.getApplication();
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean("pref_animationActivated", false).commit();
        Random random = new Random(2048);
        int[] keys = {KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT};
        for (int size = 4; size <= 7; size++) {
            GameState state = new GameState(size);
            state.numbers[0] = state.numbers[1] = 2;
            GameStorage.writeState(context.getFilesDir(), size, state);
            Intent intent = new Intent(context, GameActivity.class).putExtra("n", size).putExtra("new", false);
            try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent).setup()) {
                GameActivity activity = controller.get();
                View root = activity.getWindow().getDecorView();
                for (int pass = 0; pass < 3; pass++) {
                    root.measure(View.MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY));
                    root.layout(0, 0, 600, 1000);
                }
                for (int direction = 0; direction < 4; direction++) {
                    for (int sample = 0; sample < 32; sample++) {
                        int[][] input = new int[size][size];
                        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
                            // Leave a row empty so random spawns cannot end the test round.
                            input[y][x] = y == size - 1 || random.nextInt(4) == 0 ? 0 : 2 << random.nextInt(3);
                            activity.elements[y][x].setNumber(input[y][x]);
                            activity.elements[y][x].drawItem();
                        }
                        activity.updateGameState();
                        int before = Integer.parseInt(activity.textFieldPoints.getText().toString());
                        int[][] expected = new int[size][size];
                        int gain = merge(input, expected, direction);
                        boolean moved = !Arrays.deepEquals(input, expected);
                        activity.onKeyDown(keys[direction], new KeyEvent(KeyEvent.ACTION_DOWN, keys[direction]));
                        String label = "size=" + size + " direction=" + direction + " sample=" + sample;
                        int spawns = 0;
                        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
                            int actual = activity.elements[y][x].number;
                            if (expected[y][x] == 0 && actual != 0) {
                                assertTrue(label, actual == 2 || actual == 4);
                                spawns++;
                            } else assertEquals(label, expected[y][x], actual);
                        }
                        assertEquals(label, moved ? 1 : 0, spawns);
                        assertEquals(label, before + gain, Integer.parseInt(activity.textFieldPoints.getText().toString()));
                        if (moved) {
                            activity.undoButton.performClick();
                            for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
                                assertEquals(label, input[y][x], activity.elements[y][x].number);
                            }
                            assertEquals(label, before, Integer.parseInt(activity.textFieldPoints.getText().toString()));
                        }
                    }
                }
            } finally {
                GameStorage.deleteState(context.getFilesDir(), size);
                GameStorage.deleteStatistics(context.getFilesDir(), size);
            }
        }
    }

    private static int merge(int[][] input, int[][] output, int direction) {
        int size = input.length, score = 0;
        for (int line = 0; line < size; line++) {
            int[] values = new int[size];
            int count = 0;
            for (int offset = 0; offset < size; offset++) {
                int value = input[y(direction, line, offset, size)][x(direction, line, offset, size)];
                if (value != 0) values[count++] = value;
            }
            int target = 0;
            for (int index = 0; index < count; index++) {
                int value = values[index];
                if (index + 1 < count && value == values[index + 1]) {
                    value *= 2;
                    score += value;
                    index++;
                }
                output[y(direction, line, target, size)][x(direction, line, target, size)] = value;
                target++;
            }
        }
        return score;
    }

    private static int x(int direction, int line, int offset, int size) {
        return direction == 1 ? size - 1 - offset : direction == 3 ? offset : line;
    }

    private static int y(int direction, int line, int offset, int size) {
        return direction == 2 ? size - 1 - offset : direction == 0 ? offset : line;
    }
}
