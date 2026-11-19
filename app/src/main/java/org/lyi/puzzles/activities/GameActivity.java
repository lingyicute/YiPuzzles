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

package org.lyi.puzzles.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.lyi.puzzles.R;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.lyi.puzzles.activities.helper.BaseActivityWithoutNavBar;
import org.lyi.puzzles.activities.helper.GameState;
import org.lyi.puzzles.activities.helper.GameStorage;
import org.lyi.puzzles.activities.helper.GameStatistics;
import org.lyi.puzzles.activities.helper.Gestures;


/**
 * This activity contains the entire game and draws the game field depending on the selected mode and the screen size.
 * Also the Intent influences, when a new game is generated or a previous game status is loaded.
 * Therefore a two dimensional field named elements is filled with instances of the class Element depending on the selected mode (4x4, 5x5, 6x6, 7x7).
 * The function of the two buttons restart and undo are also implemented.
 * With the help of the class Gesture the user inputs are recognized and the corresponding method is retrieved.
 * This method contains the important game logic for 2048.
 * With loops the values of the elements will be added and the whole elements will be moved in the selected direction.
 * After each move (only if something has been moved) a new element appears on the game field with the help of the method addNumber().
 * This new element is placed randomly on a free spot of the game field.
 * If the activity is paused or stopped, the game status is saved in the GameState and the GameStatistics will be updated.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180910
 */

public class GameActivity extends BaseActivityWithoutNavBar {
    private int n;
    public TextView textFieldPoints;
    public TextView textFieldRecord;
    private int numberFieldSize;
    Element[][] elements;
    Element[][] last_elements;
    Element[][] backgroundElements;
    private GameState gameState;

    RelativeLayout number_field;
    RelativeLayout number_field_background;
    RelativeLayout touch_field;
    MaterialButton restartButton;
    MaterialButton undoButton;
    private int points;
    private int last_points;
    private long record;

    private static final long ADDINGSPEED = 100;
    private static final long MOVINGSPEED = 80;
    private static final long SCALINGSPEED = 100;
    private static final float SCALINGFACTOR = 1.1f;

    private boolean moved;
    private boolean newGame;
    private boolean won2048;
    private boolean winPending;
    private AlertDialog roundDialog;
    private boolean gameOver;
    private boolean undo;
    private boolean animationActivated;
    private boolean saveState = true;

    private static final int WINTHRESHOLD = 2048;
    private static final int MAX_TILE = 1 << 30;
    private static final double PROBABILITY_FOR_TWO = 0.9;
    private View.OnTouchListener swipeListener;
    private GameStatistics gameStatistics;
    private long startingTime = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        n = requestedBoardSize();
        gameStatistics = GameStorage.readStatistics(getFilesDir(), n);
        record = gameStatistics.getRecord();
        // A recreated activity resumes its round even if the original launch started a new one.
        initializeState(savedInstanceState == null && getIntent().getBooleanExtra("new", true));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        animationActivated = sharedPref.getBoolean("pref_animationActivated", true);
        if (sharedPref.getBoolean("switch_preference_1", true)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        setContentView(R.layout.activity_game);
        setupToolbar();
        initResources();
    }

    @Override
    public void onResume() {
        super.onResume();
        startingTime = SystemClock.elapsedRealtime();
        presentRoundDialog();
    }

    @Override
    protected void onPause() {
        if (startingTime >= 0) {
            gameStatistics.addTimePlayed(SystemClock.elapsedRealtime() - startingTime);
            startingTime = -1;
        }
        saveStateToFile(gameState);
        saveStatisticsToFile(gameStatistics);
        super.onPause();
    }

    private void handleUndo() {
        undoButton.setVisibility(View.INVISIBLE);
        if (undo && last_elements != null) {
            undo = false;
            gameStatistics.undo();
            elements = last_elements;
            points = last_points;
            number_field.removeAllViews();
            textFieldPoints.setText("" + points);
            setDPositions(false);
            for (Element[] i : elements) {
                for (Element j : i) {
                    j.setVisibility(View.INVISIBLE);
                    number_field.addView(j);
                    j.drawItem();
                }
            }
            for (int i = 0; i < elements.length; i++) {
                for (int j = 0; j < elements[i].length; j++) {
                    elements[i][j].setOnTouchListener(swipeListener);
                    backgroundElements[i][j].setOnTouchListener(swipeListener);
                }
            }
            updateGameState();
            drawAllElements(elements);
            number_field.refreshDrawableState();
        }
    }
    /** Material 3 top app bar with up navigation and the board size as subtitle. */
    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_activity_game);
            actionBar.setSubtitle(n + "x" + n);
        }
    }

    public void initResources() {
        number_field = (RelativeLayout) findViewById(R.id.number_field);
        number_field_background = (RelativeLayout) findViewById(R.id.number_field_background);
        touch_field = (RelativeLayout) findViewById(R.id.touch_field);
        textFieldPoints = (TextView) findViewById(R.id.points);
        textFieldRecord = (TextView) findViewById(R.id.record);
        restartButton = (MaterialButton) findViewById(R.id.restartButton);
        restartButton.setOnClickListener(v -> createNewGame());
        undoButton = findViewById(R.id.undoButton);
        undoButton.setVisibility(View.INVISIBLE);
        undoButton.setOnClickListener(v -> handleUndo());

        // Directions are physical left/right, regardless of the surrounding UI language.
        number_field.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        number_field_background.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        touch_field.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) number_field.getLayoutParams();
            int width = right - left - margins.leftMargin - margins.rightMargin;
            int height = bottom - top - margins.topMargin - margins.bottomMargin;
            int side = Math.min(width, height);
            if (side >= n && side != numberFieldSize) {
                numberFieldSize = side;
                resizeBoardLayer(number_field);
                resizeBoardLayer(number_field_background);
                initialize();
            }
        });
    }

    private void resizeBoardLayer(View layer) {
        ViewGroup.LayoutParams params = layer.getLayoutParams();
        params.width = numberFieldSize;
        params.height = numberFieldSize;
        layer.setLayoutParams(params);
    }

    private void createNewGame() {
        saveStatisticsToFile(gameStatistics);
        initializeState(true);
        if (numberFieldSize > 0) initialize();
    }

    private int requestedBoardSize() {
        int size = getIntent().getIntExtra("n", 4);
        return size >= 4 && size <= 7 ? size : 4;
    }

    private void initializeState(boolean startFresh) {
        GameState stored = startFresh ? null : GameStorage.readState(getFilesDir(), n);
        newGame = stored == null;
        gameState = newGame ? new GameState(n) : stored;
        points = gameState.points;
        if (points > record) {
            // The statistics may have been reset while this round was still in progress.
            record = points;
            gameStatistics.setRecord(record);
        }
        last_points = gameState.last_points;
        undo = gameState.undo;
        won2048 = false;
        winPending = gameState.winPending;
        gameOver = false;
    }

    public void drawAllElements(Element[][] e) {
        for (Element[] i : e) {
            for (Element j : i) {
                j.drawItem();
            }
        }
    }

    public void updateGameState() {
        gameState = new GameState(elements, last_elements);
        gameState.n = n;
        gameState.points = points;
        gameState.last_points = last_points;
        gameState.undo = undo;
        boolean hasWon = hasWinningTile();
        winPending = hasWon && (!won2048 || winPending);
        won2048 = hasWon;
        gameState.winPending = winPending;
        gameOver = !canMove();
        updateHighestNumber();

    }

    public void initialize() {
        elements = new Element[n][n];
        backgroundElements = new Element[n][n];
        int desiredGap = Math.round(10 * getResources().getDisplayMetrics().density);
        int gap = Math.min(desiredGap, (numberFieldSize - n) / (n + 1));
        int number_size = (numberFieldSize - (n + 1) * gap) / n;

        textFieldRecord.setText("" + record);
        textFieldPoints.setText("" + points);
        if (undo)
            undoButton.setVisibility(View.VISIBLE);
        else
            undoButton.setVisibility(View.INVISIBLE);

        number_field_background.removeAllViews();
        number_field.removeAllViews();
        for (int i = 0; i < elements.length; i++) {
            for (int j = 0; j < elements[i].length; j++) {
                //background elements
                backgroundElements[i][j] = new Element(this);

                elements[i][j] = new Element(this);
                elements[i][j].setNumber(gameState.getNumber(i, j));

                elements[i][j].drawItem();
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(number_size, number_size);
                lp.leftMargin = gap + j * (number_size + gap);
                lp.topMargin = gap + i * (number_size + gap);
                elements[i][j].setDPosition(lp.leftMargin, lp.topMargin);
                elements[i][j].setLayoutParams(lp);
                backgroundElements[i][j].setLayoutParams(new RelativeLayout.LayoutParams(lp));
                number_field_background.addView(backgroundElements[i][j]);
                number_field.addView(elements[i][j]);
            }
        }
        last_elements = deepCopy(elements);
        if (undo) {
            for (int i = 0; i < elements.length; i++) {
                for (int j = 0; j < elements[i].length; j++) {
                    last_elements[i][j].setNumber(gameState.getLastNumber(i, j));
                }
            }
        }
        if (newGame) {
            addNumber();
            addNumber();
            newGame = false;
        }
        // Existing wins from older archives are already acknowledged.
        won2048 = hasWinningTile();
        updateGameState();
        setListener();
        presentRoundDialog();
    }

    public void switchElementPositions(Element e1, Element e2) {
        int i = e1.getdPosX();
        int j = e1.getdPosY();

        e1.animateMoving = true;
        e1.setDPosition(e2.getdPosX(), e2.getdPosY());
        e2.animateMoving = false;
        e2.setDPosition(i, j);

    }

    public Element[][] deepCopy(Element[][] e) {
        Element[][] r = new Element[e.length][];
        for (int i = 0; i < r.length; i++) {
            r[i] = new Element[e[i].length];
            for (int j = 0; j < r[i].length; j++) {
                r[i][j] = e[i][j].copy();
            }
        }
        return r;
    }

    private boolean handleSwipeTop() {
        if (winPending || gameOver || roundDialog != null) return false;
        settleAnimations();
        Element[][] temp = deepCopy(elements);
        int temp_points = points;
        moved = false;
        Element s = new Element(this);

        for (int i = 0; i < elements.length; i++) {
            s.number = elements[0][i].number;
            s.posX = 0;
            s.posY = i;


            for (int j = 1; j < elements[i].length; j++) {
                if (elements[j][i].number != 0 && (s.number == 0 || canMerge(s.number, elements[j][i].number))) {
                    moved = true;
                    elements[j][i].setNumber(s.number + elements[j][i].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[j][i], elements[s.posX][s.posY]);
                    Element z = elements[j][i];
                    elements[j][i] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;
                    if (s.number != 0)
                        points = (int) Math.min(Integer.MAX_VALUE, (long) points + elements[s.posX][s.posY].number);
                    if (s.number != 0)
                        s.posX++;
                    j = s.posX;
                    s.number = elements[j][i].number;

                } else if (elements[j][i].number != 0) {
                    s.number = elements[j][i].number;
                    s.posX = j;
                    s.posY = i;
                }
            }

        }
        for (int i = 0; i < elements.length; i++) {
            s.number = elements[0][i].number;
            s.posX = 0;
            s.posY = i;


            for (int j = 1; j < elements[i].length; j++) {
                if (elements[j][i].number != 0 && s.number == 0) {
                    moved = true;
                    elements[j][i].setNumber(s.number + elements[j][i].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[j][i], elements[s.posX][s.posY]);
                    Element z = elements[j][i];
                    elements[j][i] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;
                    j = s.posX;
                    s.number = elements[j][i].number;

                } else if (s.number != 0) {
                    s.number = elements[j][i].number;
                    s.posX = j;
                    s.posY = i;
                }
            }

        }
        return finishMove(temp, temp_points, gameStatistics::moveT);
    }

    private boolean handleSwipeRight() {
        if (winPending || gameOver || roundDialog != null) return false;
        settleAnimations();
        Element[][] temp = deepCopy(elements);
        int temp_points = points;
        moved = false;
        Element s = new Element(this);
        for (int i = 0; i < elements.length; i++) {
            s.number = elements[i][elements[i].length - 1].number;
            s.posX = i;
            s.posY = elements[i].length - 1;


            for (int j = elements[i].length - 2; j >= 0; j--) {
                if (elements[i][j].number != 0 && (s.number == 0 || canMerge(s.number, elements[i][j].number))) {
                    moved = true;

                    elements[i][j].setNumber(s.number + elements[i][j].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[i][j], elements[s.posX][s.posY]);
                    Element z = elements[i][j];
                    elements[i][j] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;

                    if (s.number != 0)
                        points = (int) Math.min(Integer.MAX_VALUE, (long) points + elements[s.posX][s.posY].number);
                    if (s.number != 0)
                        s.posY--;
                    j = s.posY;
                    s.number = elements[i][j].number;
                } else if (elements[i][j].number != 0) {
                    s.number = elements[i][j].number;
                    s.posX = i;
                    s.posY = j;
                }
            }

        }
        for (int i = 0; i < elements.length; i++) {
            s.number = elements[i][elements[i].length - 1].number;
            s.posX = i;
            s.posY = elements[i].length - 1;


            for (int j = elements[i].length - 2; j >= 0; j--) {
                if (elements[i][j].number != 0 && s.number == 0) {
                    moved = true;

                    elements[i][j].setNumber(s.number + elements[i][j].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[i][j], elements[s.posX][s.posY]);
                    Element z = elements[i][j];
                    elements[i][j] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;


                    j = s.posY;
                    s.number = elements[i][j].number;
                } else if (s.number != 0) {
                    s.number = elements[i][j].number;
                    s.posX = i;
                    s.posY = j;
                }
            }

        }
        return finishMove(temp, temp_points, gameStatistics::moveR);
    }

    private boolean handleSwipeLeft() {
        if (winPending || gameOver || roundDialog != null) return false;
        settleAnimations();
        Element[][] temp = deepCopy(elements);
        int temp_points = points;
        moved = false;
        Element s = new Element(this);
        for (int i = 0; i < elements.length; i++) {
            s.number = elements[i][0].number;
            s.posX = i;
            s.posY = 0;


            for (int j = 1; j < elements[i].length; j++) {
                if (elements[i][j].number != 0 && (s.number == 0 || canMerge(s.number, elements[i][j].number))) {
                    moved = true;


                    elements[i][j].setNumber(s.number + elements[i][j].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[i][j], elements[s.posX][s.posY]);
                    Element z = elements[i][j];
                    elements[i][j] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;

                    if (s.number != 0)
                        points = (int) Math.min(Integer.MAX_VALUE, (long) points + elements[s.posX][s.posY].number);
                    if (s.number != 0)
                        s.posY++;
                    j = s.posY;
                    s.number = elements[i][j].number;
                } else if (elements[i][j].number != 0) {
                    s.number = elements[i][j].number;
                    s.posX = i;
                    s.posY = j;
                }
            }

        }
        for (int i = 0; i < elements.length; i++) {
            s.number = elements[i][0].number;
            s.posX = i;
            s.posY = 0;

            for (int j = 1; j < elements[i].length; j++) {
                if (elements[i][j].number != 0 && s.number == 0) {
                    moved = true;

                    elements[i][j].setNumber(s.number + elements[i][j].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[i][j], elements[s.posX][s.posY]);
                    Element z = elements[i][j];
                    elements[i][j] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;

                    j = s.posY;
                    s.number = elements[i][j].number;
                } else if (s.number != 0) {
                    s.number = elements[i][j].number;
                    s.posX = i;
                    s.posY = j;
                }
            }

        }
        return finishMove(temp, temp_points, gameStatistics::moveL);
    }

    private boolean handleSwipeBottom() {
        if (winPending || gameOver || roundDialog != null) return false;
        settleAnimations();
        Element[][] temp = deepCopy(elements);
        int temp_points = points;
        moved = false;
        Element s = new Element(this);
        for (int i = 0; i < elements.length; i++) {
            s.number = elements[elements[i].length - 1][i].number;
            s.posX = elements[i].length - 1;
            s.posY = i;


            for (int j = elements[i].length - 2; j >= 0; j--) {
                if (elements[j][i].number != 0 && (s.number == 0 || canMerge(s.number, elements[j][i].number))) {
                    moved = true;

                    elements[j][i].setNumber(s.number + elements[j][i].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[j][i], elements[s.posX][s.posY]);
                    Element z = elements[j][i];
                    elements[j][i] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;

                    if (s.number != 0)
                        points = (int) Math.min(Integer.MAX_VALUE, (long) points + elements[s.posX][s.posY].number);
                    if (s.number != 0)
                        s.posX--;
                    j = s.posX;
                    s.number = elements[j][i].number;
                } else if (elements[j][i].number != 0) {
                    s.number = elements[j][i].number;
                    s.posX = j;
                    s.posY = i;
                }
            }

        }
        for (int i = 0; i < elements.length; i++) {
            s.number = elements[elements[i].length - 1][i].number;
            s.posX = elements[i].length - 1;
            s.posY = i;


            for (int j = elements[i].length - 2; j >= 0; j--) {
                if (elements[j][i].number != 0 && s.number == 0) {
                    moved = true;

                    elements[j][i].setNumber(s.number + elements[j][i].number);
                    elements[s.posX][s.posY].setNumber(0);
                    switchElementPositions(elements[j][i], elements[s.posX][s.posY]);
                    Element z = elements[j][i];
                    elements[j][i] = elements[s.posX][s.posY];
                    elements[s.posX][s.posY] = z;

                    j = s.posX;
                    s.number = elements[j][i].number;
                } else if (s.number != 0) {
                    s.number = elements[j][i].number;
                    s.posX = j;
                    s.posY = i;
                }
            }

        }
        return finishMove(temp, temp_points, gameStatistics::moveD);
    }

    private boolean finishMove(Element[][] previous, int previousPoints, Runnable countDirection) {
        if (!moved) return false;
        gameStatistics.addMoves(1);
        countDirection.run();
        last_points = previousPoints;
        last_elements = previous;
        undo = true;
        undoButton.setVisibility(View.VISIBLE);
        setDPositions(animationActivated);
        addNumber();
        if (points > record) {
            record = points;
            gameStatistics.setRecord(record);
            textFieldRecord.setText(String.valueOf(record));
        }
        textFieldPoints.setText(String.valueOf(points));
        updateGameState();
        presentRoundDialog();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (elements == null) return super.onKeyDown(keyCode, event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                handleSwipeTop();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                handleSwipeRight();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                handleSwipeLeft();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                handleSwipeBottom();
                return true;
            case KeyEvent.KEYCODE_Z:
                if (event.isCtrlPressed()) {
                    handleUndo();
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setListener() {
        swipeListener = new Gestures(this) {
            public boolean onSwipeTop() {
                return handleSwipeTop();
            }

            public boolean onSwipeRight() {
                return handleSwipeRight();
            }

            public boolean onSwipeLeft() {
                return handleSwipeLeft();
            }

            public boolean onSwipeBottom() {
                return handleSwipeBottom();
            }

        };
        touch_field.setOnTouchListener(swipeListener);
        number_field.setOnTouchListener(swipeListener);
        for (int i = 0; i < elements.length; i++) {
            for (int j = 0; j < elements[i].length; j++) {
                elements[i][j].setOnTouchListener(swipeListener);
                backgroundElements[i][j].setOnTouchListener(swipeListener);
            }
        }
    }

    public void updateHighestNumber() {
        for (Element[] row : elements) {
            for (Element tile : row) gameStatistics.setHighestNumber(tile.number);
        }
    }

    private boolean hasWinningTile() {
        for (Element[] row : elements) {
            for (Element tile : row) if (tile.number >= WINTHRESHOLD) return true;
        }
        return false;
    }

    private static boolean canMerge(int first, int second) {
        return first < MAX_TILE && first == second;
    }

    private boolean canMove() {
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                int value = elements[y][x].number;
                if (value == 0 || (x + 1 < n && canMerge(value, elements[y][x + 1].number))
                        || (y + 1 < n && canMerge(value, elements[y + 1][x].number))) return true;
            }
        }
        return false;
    }

    private void settleAnimations() {
        if (!animationActivated) return;
        for (Element[] row : elements) {
            for (Element tile : row) {
                tile.animate().setListener(null).cancel();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tile.getLayoutParams();
                tile.setTranslationX(tile.dPosX - params.leftMargin);
                tile.setTranslationY(tile.dPosY - params.topMargin);
                tile.setAlpha(1f);
                tile.setScaleX(1f);
                tile.setScaleY(1f);
                if (tile.dNumber != tile.number) tile.drawItem();
            }
        }
    }

    public void setDPositions(boolean animation) {
        for (Element[] i : elements) {
            for (Element j : i) {
                if (j.dPosX != j.getX()) {
                    if (j.animateMoving && animation) {
                        if (j.number != j.dNumber)
                            j.animate().x(j.dPosX).setDuration(MOVINGSPEED).setStartDelay(0).setInterpolator(new LinearInterpolator()).setListener(new MovingListener(j, true)).start();
                        else
                            j.animate().x(j.dPosX).setDuration(MOVINGSPEED).setStartDelay(0).setInterpolator(new LinearInterpolator()).setListener(new MovingListener(j, false)).start();

                    } else {
                        if (!animation) {
                            ViewGroup.MarginLayoutParams lp1 = (ViewGroup.MarginLayoutParams) j.getLayoutParams();
                            lp1.leftMargin = j.dPosX;
                            j.setLayoutParams(lp1);
                            j.drawItem();
                        } else
                            j.animate().x(j.dPosX).setDuration(0).setStartDelay(MOVINGSPEED).setInterpolator(new LinearInterpolator()).setListener(new MovingListener(j, false)).start();

                    }

                }
                if (j.dPosY != j.getY()) {
                    if (j.animateMoving && animation) {
                        if (j.number != j.dNumber)
                            j.animate().y(j.dPosY).setDuration(MOVINGSPEED).setStartDelay(0).setInterpolator(new LinearInterpolator()).setListener(new MovingListener(j, true)).start();
                        else
                            j.animate().y(j.dPosY).setDuration(MOVINGSPEED).setStartDelay(0).setInterpolator(new LinearInterpolator()).setListener(new MovingListener(j, false)).start();

                    } else {
                        if (!animation) {
                            ViewGroup.MarginLayoutParams lp1 = (ViewGroup.MarginLayoutParams) j.getLayoutParams();
                            lp1.topMargin = j.dPosY;
                            j.setLayoutParams(lp1);
                            j.drawItem();
                        } else
                            j.animate().y(j.dPosY).setDuration(0).setStartDelay(MOVINGSPEED).setInterpolator(new LinearInterpolator()).setListener(new MovingListener(j, false)).start();

                    }


                }
            }
        }
    }

    class MovingListener extends AnimatorListenerAdapter {
        Element e;
        long SCALINGSPEED;
        float scalingFactor;
        boolean scale;

        public MovingListener(Element e, boolean scale) {
            super();
            this.e = e;
            this.SCALINGSPEED = GameActivity.SCALINGSPEED;
            this.scalingFactor = GameActivity.SCALINGFACTOR;
            this.scale = scale;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);
            animation.setupEndValues();
            if (e != null)
                e.drawItem();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if (e != null) {
                e.drawItem();
                if (scale)
                    e.animate().scaleX(scalingFactor).scaleY(scalingFactor).setDuration(SCALINGSPEED).setStartDelay(0).setInterpolator(new LinearInterpolator()).setListener(new ScalingListener(e)).start();
            }

        }
    }

    class ScalingListener extends AnimatorListenerAdapter {
        Element e;

        public ScalingListener(Element e) {
            super();
            this.e = e;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);
            animation.setupEndValues();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if (e != null) {
                e.animate().scaleX(1.0f).scaleY(1.0f).setDuration(SCALINGSPEED).setStartDelay(0).setInterpolator(new LinearInterpolator()).setListener(new AnimatorListenerAdapter() {}).start();
            }

        }
    }

    private void addNumber() {
        Element[] empty = new Element[n * n];
        int count = 0;
        for (Element[] row : elements) {
            for (Element tile : row) if (tile.number == 0) empty[count++] = tile;
        }
        if (count == 0) return;
        Element tile = empty[(int) (Math.random() * count)];
        tile.setNumber(Math.random() < PROBABILITY_FOR_TWO ? 2 : 4);
        tile.drawItem();
        if (animationActivated) {
            tile.setAlpha(0);
            tile.animate().alpha(1).setInterpolator(new LinearInterpolator())
                    .setStartDelay(MOVINGSPEED).setDuration(ADDINGSPEED).start();
        }
    }

    private void presentRoundDialog() {
        if ((!winPending && !gameOver) || roundDialog != null || elements == null
                || startingTime < 0 || isFinishing()) return;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this).setCancelable(false);
        if (winPending) {
            builder.setTitle(R.string.Titel_V_Message)
                    .setMessage(R.string.Winning_Message)
                    .setNegativeButton(R.string.No_Message, (dialog, which) -> {
                        acknowledgeWin();
                        finish();
                    })
                    .setPositiveButton(R.string.Yes_Message, (dialog, which) -> acknowledgeWin());
        } else {
            builder.setTitle(getString(R.string.Titel_L_Message, points))
                    .setMessage(getString(R.string.Lost_Message, points))
                    .setNegativeButton(R.string.No_Message, (dialog, which) -> {
                        saveState = false;
                        GameStorage.deleteState(getFilesDir(), n);
                        finish();
                    })
                    .setPositiveButton(R.string.Yes_Message, (dialog, which) -> createNewGame());
            if (undo) builder.setNeutralButton(R.string.undo, (dialog, which) -> handleUndo());
        }

        saveStatisticsToFile(gameStatistics);
        roundDialog = builder.create();
        roundDialog.setOnDismissListener(dialog -> {
            roundDialog = null;
            // The winning move can also fill the last playable cell.
            presentRoundDialog();
        });
        roundDialog.show();
    }

    private void acknowledgeWin() {
        winPending = false;
        gameState.winPending = false;
    }

    @Override
    protected void onDestroy() {
        if (roundDialog != null) {
            roundDialog.setOnDismissListener(null);
            roundDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        saveStateToFile(gameState);
    }

    private void saveStateToFile(GameState state) {
        if (saveState && !newGame && state != null) GameStorage.writeState(getFilesDir(), n, state);
    }

    private void saveStatisticsToFile(GameStatistics statistics) {
        GameStorage.writeStatistics(getFilesDir(), n, statistics);
    }
}
