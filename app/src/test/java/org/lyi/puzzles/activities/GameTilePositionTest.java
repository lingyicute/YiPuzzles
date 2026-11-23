/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.view.KeyEvent;

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

import java.time.Duration;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, qualifiers = "w360dp-h640dp-port-mdpi")
public class GameTilePositionTest {
    @Test public void tilesStayInTheirCellsWhenTwoMovesPrecedeALayoutPass() {
        Context context = RuntimeEnvironment.getApplication();
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean("pref_animationActivated", false).commit();
        GameState state = new GameState(4);
        state.numbers[0] = 2;
        GameStorage.writeState(context.getFilesDir(), 4, state);
        Intent intent = new Intent(context, GameActivity.class).putExtra("n", 4).putExtra("new", false);
        try (ActivityController<GameActivity> controller = Robolectric.buildActivity(GameActivity.class, intent).setup()) {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(300));
            GameActivity activity = controller.get();
            for (int key : new int[]{KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_LEFT}) {
                activity.onKeyDown(key, new KeyEvent(KeyEvent.ACTION_DOWN, key));
            }
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(300));
            for (Element[] row : activity.elements) {
                for (Element tile : row) {
                    assertEquals(tile.dPosX, tile.getX(), 0f);
                    assertEquals(tile.dPosY, tile.getY(), 0f);
                }
            }
        } finally {
            GameStorage.deleteState(context.getFilesDir(), 4);
            GameStorage.deleteStatistics(context.getFilesDir(), 4);
        }
    }
}
