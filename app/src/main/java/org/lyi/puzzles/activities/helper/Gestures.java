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

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

/**
 * This class implements the Gestures Listener for swiping in the game
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180807
 */
public class Gestures implements View.OnTouchListener {

    private final float swipeThreshold;
    private int activePointer = -1;
    private float startX;
    private float startY;

    public Gestures(Context context) {
        swipeThreshold = 24 * context.getResources().getDisplayMetrics().density;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                activePointer = event.getPointerId(0);
                // Screen coordinates stay stable even when the touched tile is animating.
                startX = event.getRawX();
                startY = event.getRawY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                activePointer = -1;
                break;
            case MotionEvent.ACTION_UP:
                if (activePointer == -1 || event.getPointerId(0) != activePointer) break;
                activePointer = -1;
                float diffX = event.getRawX() - startX;
                float diffY = event.getRawY() - startY;
                if (Math.max(Math.abs(diffX), Math.abs(diffY)) < swipeThreshold) {
                    view.performClick();
                } else if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (diffX > 0) onSwipeRight();
                    else onSwipeLeft();
                } else {
                    if (diffY > 0) onSwipeBottom();
                    else onSwipeTop();
                }
                break;
            default:
                break;
        }
        // Own the entire gesture, including slow drags and moves that cannot change the board.
        return true;
    }

    public boolean onSwipeRight() {
        return false;
    }

    public boolean onSwipeLeft() {
        return false;
    }

    public boolean onSwipeTop() {
        return false;
    }

    public boolean onSwipeBottom() {
        return false;
    }

}


