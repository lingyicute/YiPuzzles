package org.lyi.puzzles;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class SquareFrameLayout extends FrameLayout {

    public SquareFrameLayout(Context context) {
        super(context);
    }

    public SquareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width;
        int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).getLayoutParams().height = height;
        }
        
        super.onMeasure(widthMeasureSpec, heightSpec);
    }
} 