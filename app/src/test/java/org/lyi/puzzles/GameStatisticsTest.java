/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles;

import org.junit.Test;
import org.lyi.puzzles.activities.helper.GameStatistics;
import java.lang.reflect.Field;
import java.util.function.IntSupplier;
import static org.junit.Assert.assertEquals;

public class GameStatisticsTest {
    @Test public void integerCountersSaturateWithoutLosingTheirHistory() throws Exception {
        for (int initial : new int[]{0, Integer.MAX_VALUE - 1, Integer.MAX_VALUE}) {
            GameStatistics statistics = new GameStatistics(4);
            String[] fields = {"undo", "moves_l", "moves_r", "moves_t", "moves_d"};
            Runnable[] increment = {statistics::undo, statistics::moveL, statistics::moveR,
                    statistics::moveT, statistics::moveD};
            IntSupplier[] read = {statistics::getUndo, statistics::getMoves_l, statistics::getMoves_r,
                    statistics::getMoves_t, statistics::getMoves_d};
            for (int i = 0; i < fields.length; i++) {
                Field field = GameStatistics.class.getDeclaredField(fields[i]);
                field.setAccessible(true);
                field.setInt(statistics, initial);
                increment[i].run();
                increment[i].run();
                assertEquals(fields[i], Math.min(Integer.MAX_VALUE, (long) initial + 2), read[i].getAsInt());
            }
        }
    }

    @Test public void longTotalsSaturateAndIgnoreNegativeIncrements() {
        GameStatistics statistics = new GameStatistics(4);
        statistics.addMoves(Long.MAX_VALUE);
        statistics.addMoves(1);
        statistics.addMoves(-1);
        statistics.addTimePlayed(Long.MAX_VALUE);
        statistics.addTimePlayed(1);
        statistics.addTimePlayed(-1);
        assertEquals(Long.MAX_VALUE, statistics.getMoves());
        assertEquals(Long.MAX_VALUE, statistics.getTimePlayed());
    }
}
