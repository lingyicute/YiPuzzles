/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles.activities.helper;

import android.util.AtomicFile;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/** Reads existing save files and replaces them without truncating the previous copy. */
public final class GameStorage {
    private static final String TAG = "GameStorage";

    private GameStorage() {}

    public static GameState readState(File directory, int size) {
        GameState state = read(file(directory, "state", size), GameState.class);
        if (state == null || !state.hasValidBoard(size)) return null;

        state.points = Math.max(0, state.points);
        if (!state.hasValidUndo()) state.undo = false;
        return state;
    }

    public static GameStatistics readStatistics(File directory, int size) {
        GameStatistics statistics = read(file(directory, "statistics", size), GameStatistics.class);
        if (statistics == null) return new GameStatistics(size);
        statistics.sanitize(size);
        return statistics;
    }

    public static void writeState(File directory, int size, GameState state) {
        write(file(directory, "state", size), state);
    }

    public static void writeStatistics(File directory, int size, GameStatistics statistics) {
        write(file(directory, "statistics", size), statistics);
    }

    public static synchronized void deleteState(File directory, int size) {
        file(directory, "state", size).delete();
    }

    public static synchronized void deleteStatistics(File directory, int size) {
        file(directory, "statistics", size).delete();
    }

    private static AtomicFile file(File directory, String prefix, int size) {
        return new AtomicFile(new File(directory, prefix + size + ".txt"));
    }

    private static synchronized <T> T read(AtomicFile file, Class<T> type) {
        try (FileInputStream stream = file.openRead(); ObjectInputStream in = new ObjectInputStream(stream)) {
            Object data = in.readObject();
            return type.isInstance(data) ? type.cast(data) : null;
        } catch (FileNotFoundException missing) {
            return null;
        } catch (IOException | ClassNotFoundException | RuntimeException malformed) {
            Log.w(TAG, "Could not read " + file.getBaseFile().getName(), malformed);
            return null;
        }
    }

    private static synchronized void write(AtomicFile file, Serializable value) {
        FileOutputStream stream = null;
        try {
            stream = file.startWrite();
            ObjectOutputStream out = new ObjectOutputStream(stream);
            out.writeObject(value);
            out.flush();
            file.finishWrite(stream);
            stream = null;
        } catch (IOException failure) {
            Log.w(TAG, "Could not write " + file.getBaseFile().getName(), failure);
        } finally {
            if (stream != null) file.failWrite(stream);
        }
    }
}
