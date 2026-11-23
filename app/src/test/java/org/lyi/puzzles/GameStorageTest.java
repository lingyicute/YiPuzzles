/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles;

import android.util.AtomicFile;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.lyi.puzzles.activities.helper.GameState;
import org.lyi.puzzles.activities.helper.GameStatistics;
import org.lyi.puzzles.activities.helper.GameStorage;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class GameStorageTest {
    @Rule public TemporaryFolder files = new TemporaryFolder();

    @Test public void opensLegacySaveAndKeepsValidUndo() throws IOException {
        GameState state = new GameState(5);
        state.numbers[0] = 8;
        state.points = 8;
        state.last_numbers = new int[25];
        state.last_numbers[0] = 4;
        state.last_numbers[1] = 4;
        state.undo = true;
        File legacy = new File(files.getRoot(), "state5.txt");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(legacy))) {
            out.writeObject(state);
        }

        GameState restored = GameStorage.readState(files.getRoot(), 5);
        assertNotNull(restored);
        assertEquals(8, restored.getNumber(0, 0));
        assertTrue(restored.undo);
        assertEquals(4, restored.getLastNumber(0, 1));
        assertNull(GameStorage.readState(files.getRoot(), 4));
    }

    @Test public void rejectsMalformedBoardButOnlyDiscardsBadUndo() throws IOException {
        File dir = files.getRoot();
        assertNull(GameStorage.readState(dir, 7));
        Files.write(new File(dir, "state7.txt").toPath(), new byte[] {1, 2, 3});
        assertNull(GameStorage.readState(dir, 7));

        GameState state = new GameState(7);
        state.numbers[0] = 3;
        GameStorage.writeState(dir, 7, state);
        assertNull(GameStorage.readState(dir, 7));

        state.numbers[0] = 2;
        state.undo = true;
        state.last_numbers = new int[49];
        GameStorage.writeState(dir, 7, state);
        assertFalse(GameStorage.readState(dir, 7).undo);

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 7; x++) {
                state.last_numbers[y * 7 + x] = (x + y) % 2 == 0 ? 2 : 4;
            }
        }
        GameStorage.writeState(dir, 7, state);
        assertFalse(GameStorage.readState(dir, 7).undo);
    }

    @Test public void treatsUncheckedDeserializationFailuresAsUnreadable() throws IOException {
        File dir = files.getRoot();
        for (String name : new String[] {"state6.txt", "statistics6.txt"}) {
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(dir, name)))) {
                out.writeObject(new Exploding());
            }
        }
        assertNull(GameStorage.readState(dir, 6));
        assertEquals(0, GameStorage.readStatistics(dir, 6).getRecord());
    }

    @Test public void interruptedWriteLeavesOldSaveAndResetOnlyDeletesSelectedData() throws IOException {
        File dir = files.getRoot();
        GameState state = new GameState(4);
        state.numbers[0] = 64;
        GameStorage.writeState(dir, 4, state);
        GameStatistics stats = new GameStatistics(5);
        stats.setRecord(128);
        GameStorage.writeStatistics(dir, 5, stats);

        AtomicFile atomic = new AtomicFile(new File(dir, "state4.txt"));
        FileOutputStream interrupted = atomic.startWrite();
        try {
            interrupted.write(new byte[] {1, 2, 3});
        } finally {
            atomic.failWrite(interrupted);
        }
        assertTrue(Arrays.equals(state.numbers, GameStorage.readState(dir, 4).numbers));
        GameStorage.deleteState(dir, 4);
        assertNull(GameStorage.readState(dir, 4));
        assertEquals(128, GameStorage.readStatistics(dir, 5).getRecord());
        GameStorage.deleteStatistics(dir, 5);
        assertEquals(0, GameStorage.readStatistics(dir, 5).getRecord());
    }

    @Test public void clampsCorruptNegativeStatisticsFields() throws Exception {
        File dir = files.getRoot();
        GameStatistics stats = new GameStatistics(4);
        for (String name : new String[]{"moves", "timePlayed", "record"}) {
            java.lang.reflect.Field field = GameStatistics.class.getDeclaredField(name);
            field.setAccessible(true);
            field.setLong(stats, -10L);
        }
        java.lang.reflect.Field highest = GameStatistics.class.getDeclaredField("highestNumber");
        highest.setAccessible(true);
        highest.setLong(stats, 3L);
        java.lang.reflect.Field undo = GameStatistics.class.getDeclaredField("undo");
        undo.setAccessible(true);
        undo.setInt(stats, -2);
        GameStorage.writeStatistics(dir, 4, stats);

        GameStatistics restored = GameStorage.readStatistics(dir, 4);
        assertEquals(0L, restored.getMoves());
        assertEquals(0L, restored.getTimePlayed());
        assertEquals(0L, restored.getRecord());
        assertEquals(2L, restored.getHighestNumber());
        assertEquals(0, restored.getUndo());
    }

    /** Stands in for corrupt streams that ObjectInputStream reports with unchecked exceptions. */
    private static class Exploding implements Serializable {
        private Object readResolve() {
            throw new IllegalStateException("corrupt");
        }
    }
}
