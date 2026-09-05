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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;

import org.lyi.puzzles.R;
import org.lyi.puzzles.helpers.EdgeToEdgeHelper;
import org.lyi.puzzles.helpers.FirstLaunchManager;
import org.lyi.puzzles.helpers.TileColors;

/**
 * In this activity a ViewPager is filled with the four pages of the tutorial.
 * The buttons at the bottom differ depending on the current position in the ViewPager.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180910
 */
public class TutorialActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private MaterialButton btnSkip, btnNext;
    private FirstLaunchManager firstLaunchManager;

    // layouts of all welcome sliders
    // add few more layouts if you want
    private int[] layouts = new int[]{
            R.layout.tutorial_slide1,
            R.layout.tutorial_slide2,
            R.layout.tutorial_slide3,
            R.layout.tutorial_slide4,
    };

    private static final String TAG = TutorialActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdgeHelper.enableEdgeToEdgeDisplay(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        EdgeToEdgeHelper.applySystemBarPadding(findViewById(R.id.main_content), true, true);

        firstLaunchManager = new FirstLaunchManager(this);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnSkip = (MaterialButton) findViewById(R.id.btn_skip);
        btnNext = (MaterialButton) findViewById(R.id.btn_next);


        // adding bottom dots
        addBottomDots(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);


        try {
            ImageView imageView = (ImageView) findViewById(R.id.image1);
            Glide.with(TutorialActivity.this).load(R.mipmap.ic_launcher_foreground).into(imageView);//.into(imageView);//@mipmap/ic_launcher_foreground).into(imageView);
        } catch (NullPointerException ne) {

        }

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHomeScreen();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    launchHomeScreen();
                }
            }
        });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        // Material 3 page indicator: primary for the active page, outline variant otherwise
        int activeColor = MaterialColors.getColor(dotsLayout, com.google.android.material.R.attr.colorPrimary);
        int inactiveColor = MaterialColors.getColor(dotsLayout, com.google.android.material.R.attr.colorOutlineVariant);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText("\u2022");
            dots[i].setTextSize(35);
            dots[i].setTextColor(inactiveColor);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(activeColor);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        if (firstLaunchManager.isFirstTimeLaunch()) {
            Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            firstLaunchManager.setFirstTimeLaunch(false);
            startActivity(intent);
        }
        finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {


            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.okay));
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);
            ImageView imageView;
            switch (position) {
                case 0:
                    imageView = (ImageView) findViewById(R.id.image1);
                    Glide.with(TutorialActivity.this).load(R.mipmap.ic_launcher_foreground).into(imageView);
                    break;
                case 1:
                    imageView = (ImageView) findViewById(R.id.image2);
                    if (!TileColors.currentScheme(TutorialActivity.this).equals(TileColors.SCHEME_ORIGINAL))
                        Glide.with(TutorialActivity.this).load(R.drawable.tutorial_move_s).into(imageView);
                    else
                        Glide.with(TutorialActivity.this).load(R.drawable.tutorial_move_o).into(imageView);
                    break;
                case 2:
                    imageView = (ImageView) findViewById(R.id.image3);
                    if (!TileColors.currentScheme(TutorialActivity.this).equals(TileColors.SCHEME_ORIGINAL))
                        Glide.with(TutorialActivity.this).load(R.drawable.tutorial_swipe_s).into(imageView);
                    else
                        Glide.with(TutorialActivity.this).load(R.drawable.tutorial_swipe_o).into(imageView);
                    break;
                case 3:
                    imageView = (ImageView) findViewById(R.id.image4);
                    if (!TileColors.currentScheme(TutorialActivity.this).equals(TileColors.SCHEME_ORIGINAL))
                        Glide.with(TutorialActivity.this).load(R.drawable.tutorial_add_s).into(imageView);
                    else
                        Glide.with(TutorialActivity.this).load(R.drawable.tutorial_add_o).into(imageView);
                    break;
            }
            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
