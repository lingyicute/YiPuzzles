/*
 This file is part of YiPuzzles. This app implements the functions of the
 game 2048 in a privacy friendly version.

 YiPuzzles is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly App Example is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly App Example. If not, see <http://www.gnu.org/licenses/>.
 */


package org.lyi.puzzles.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.util.TypedValue;
import android.view.View;

import org.lyi.puzzles.R;
import org.lyi.puzzles.helpers.TileColors;

/**
 * This class extends the android.support.v7.widget.AppCompatButton class and represents a box on the game field.
 * This element contains different features like the presented value (number) and the position of this value in the playing field (posX, posY).
 * Because of the animation a distinction is made between the calculated value and the displayed value.
 * Furthermore there are more features like the color of the box and the font size of the value.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180910
 */

public class Element extends androidx.appcompat.widget.AppCompatButton {
    public int number = 0;
    public int dNumber = 0;
    public int posX = 0;
    public int posY = 0;
    public int dPosX = 0;
    public int dPosY = 0;
    public boolean activated;
    public boolean animateMoving = false;
    Context context;
    int color;


    @SuppressLint("RestrictedApi")
    public Element(Context c) {
        super(c);
        context = c;
        setAllCaps(false);
        // Material 3 tile: flat rounded surface, no elevation / press animation
        setBackgroundResource(R.drawable.game_brick);
        setStateListAnimator(null);
        setElevation(0f);
        int pad = Math.round(4 * c.getResources().getDisplayMetrics().density);
        setPadding(pad, pad, pad, pad);
        setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        setColor(TileColors.forNumber(context, 0).getBackground());
        setMaxLines(1);
        setAutoSizeTextTypeUniformWithConfiguration(1,
                100,
                1,
                TypedValue.COMPLEX_UNIT_SP);
    }

    public void drawItem() {
        dNumber = number;
        activated = (number != 0);
        if (number == 0) {
            setVisibility(View.INVISIBLE);
            setText("");
        } else {
            setText("" + number);
            if (getVisibility() != View.VISIBLE)
                setVisibility(View.VISIBLE);
        }

        TileColors.Tile tile = TileColors.forNumber(context, number);
        setColor(tile.getBackground());
        setTextColor(tile.getText());
    }

    private void setColor(int c) {
        color = c;
        Drawable background = getBackground();
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable) background).getPaint().setColor(c);
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(c);
        } else if (background instanceof ColorDrawable) {
            ((ColorDrawable) background).setColor(c);
        }
    }

    public String toString() {
        return "number: " + number;
    }

    public int getNumber() {
        return number;
    }

    public void setDPosition(int i, int j) {
        dPosX = i;
        dPosY = j;
    }

    public void setNumber(int i) {
        number = i;
    }

    public int getdPosX() {
        return dPosX;
    }

    public int getdPosY() {
        return dPosY;
    }

    public int getdNumber() {
        return dNumber;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public Element copy() {
        Element temp = new Element(context);
        temp.number = number;
        temp.dNumber = dNumber;
        temp.posX = posX;
        temp.posY = posY;
        temp.dPosX = dPosX;
        temp.dPosY = dPosY;
        temp.activated = activated;
        temp.animateMoving = animateMoving;
        temp.color = color;
        //temp.setBackgroundResource(backGroundResource);
        temp.setColor(color);
        temp.setVisibility(getVisibility());
        temp.setLayoutParams(getLayoutParams());
        return temp;
    }
}
