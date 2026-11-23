/*
 This file is part of YiPuzzles. This app implements the functions of the
 game 2048 in a privacy friendly version.

 YiPuzzles is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 YiPuzzles is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YiPuzzles. If not, see <http://www.gnu.org/licenses/>.
 */


package org.lyi.puzzles.activities.helper;

import java.io.Serializable;

/**
 * The current statistics in each modes is defined in this class.
 * It contains the highest reached number, the amount of swipes (total and for each direction), playing time, highest score and the amount of using the undo button.
 * For saving this data in a file, the interface java.io.Serializable is implemented.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180910
 */
public class GameStatistics implements Serializable {
    /**
     * Fixed id: the default value is computed from the class members, so every change to this
     * class would otherwise make the statistics files written by earlier versions unreadable.
     */
    private static final long serialVersionUID = -3651969648725498397L;

    private long moves = 0;
    private long timePlayed = 0;
    private long highestNumber = 2;
    private String filename;
    private long record = 0;
    private int undo = 0;
    private int moves_l = 0;
    private int moves_r = 0;
    private int moves_t = 0;
    private int moves_d = 0;

    public GameStatistics(int n) {
        filename = "statistics" + n + ".txt";
    }


    public long getHighestNumber() {
        return highestNumber;
    }

    public void setHighestNumber(long highestNumber) {
        if (this.highestNumber < highestNumber)
            this.highestNumber = highestNumber;
    }

    public long getTimePlayed() {
        return timePlayed;
    }

    public void addTimePlayed(long timePlayed) {
        if (timePlayed > 0) {
            this.timePlayed = Long.MAX_VALUE - this.timePlayed < timePlayed
                    ? Long.MAX_VALUE : this.timePlayed + timePlayed;
        }
    }

    public long getMoves() {
        return moves;
    }

    public void addMoves(long moves) {
        if (moves > 0) {
            this.moves = Long.MAX_VALUE - this.moves < moves
                    ? Long.MAX_VALUE : this.moves + moves;
        }
    }

    void sanitize(int n) {
        filename = "statistics" + n + ".txt";
        moves = Math.max(0L, moves);
        timePlayed = Math.max(0L, timePlayed);
        record = Math.max(0L, record);
        if (highestNumber < 2L || (highestNumber & (highestNumber - 1L)) != 0L) {
            highestNumber = 2L;
        }
        undo = Math.max(0, undo);
        moves_l = Math.max(0, moves_l);
        moves_r = Math.max(0, moves_r);
        moves_t = Math.max(0, moves_t);
        moves_d = Math.max(0, moves_d);
    }

    public String getFilename() {
        return filename;
    }

    public long getRecord() {
        return record;
    }

    public void setRecord(long record) {
        this.record = record;
    }

    public void undo() {
        undo++;
    }

    public void moveL() {
        moves_l++;
    }

    public void moveR() {
        moves_r++;
    }

    public void moveT() {
        moves_t++;
    }

    public void moveD() {
        moves_d++;
    }

    public int getUndo() {
        return undo;
    }

    public int getMoves_d() {
        return moves_d;
    }

    public int getMoves_t() {
        return moves_t;
    }

    public int getMoves_r() {
        return moves_r;
    }

    public int getMoves_l() {
        return moves_l;
    }
}
