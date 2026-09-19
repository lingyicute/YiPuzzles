package org.lyi.puzzles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.robolectric.RobolectricTestRunner;
import androidx.viewpager.widget.ViewPager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lyi.puzzles.activities.MainActivity;
import org.lyi.puzzles.views.BoardPreviewView;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

/**
 * Measures the "choose board size" page of the main screen: the ViewPager slide that holds the
 * grey MaterialCardView with the board preview drawn inside it.
 *
 * The user-visible requirement is that the grey card hugs the square board in every orientation.
 */
@RunWith(RobolectricTestRunner.class)
public class PreviewBoardSquareTest {

    private static class Holder {
        View card;
        BoardPreviewView preview;
        TextView label;
        View pageRoot;
    }

    private MainActivity launch() {
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class);
        MainActivity activity = controller.setup().get();
        View content = activity.findViewById(android.R.id.content);
        Configuration cfg = activity.getResources().getConfiguration();
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        int w = Math.round(cfg.screenWidthDp * dm.density);
        int h = Math.round(cfg.screenHeightDp * dm.density);
        content.measure(View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY));
        content.layout(0, 0, w, h);
        return activity;
    }

    private Holder measurePage(MainActivity activity, int page) {
        ViewPager pager = activity.findViewById(R.id.view_pager);
        assertNotNull(pager);
        View slide = ((ViewGroup) pager).getChildAt(page);
        assertNotNull("pager has no instantiated child for page " + page, slide);

        Holder h = new Holder();
        h.pageRoot = slide;
        walk(slide, h);
        assertNotNull("no BoardPreviewView found on page " + page, h.preview);
        return h;
    }

    private void walk(View v, Holder h) {
        if (v instanceof BoardPreviewView && h.preview == null) {
            h.preview = (BoardPreviewView) v;
            h.card = (View) v.getParent();
            ViewGroup group = (ViewGroup) h.card.getParent();
            for (int i = 0; i < group.getChildCount(); i++) {
                if (group.getChildAt(i) instanceof TextView) {
                    h.label = (TextView) group.getChildAt(i);
                }
            }
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                walk(g.getChildAt(i), h);
            }
        }
    }

    private void report(String tag, MainActivity activity, int page) {
        ViewPager pager = activity.findViewById(R.id.view_pager);
        Holder h = measurePage(activity, page);
        int cw = h.card.getWidth();
        int ch = h.card.getHeight();
        int pw = h.preview.getWidth();
        int ph = h.preview.getHeight();
        int padL = h.card.getPaddingLeft();
        int padT = h.card.getPaddingTop();
        int padR = h.card.getPaddingRight();
        int padB = h.card.getPaddingBottom();

        // grey background area that is NOT covered by the board
        int greyAbove = (ph + padT + padB) == 0 ? 0 : (h.preview.getTop() - padT);
        int greyBelow = ch - padB - h.preview.getBottom();

        System.out.println(String.format(
                "### %-26s slide=%dx%d pager=%dx%d label=%dx%d%n"
              + "### %-26s card=%dx%d preview=%dx%d padding=%d/%d/%d/%d "
              + "greyBandAbove=%d greyBandBelow=%d totalGrey=%d%n"
              + "### %-26s cardH/cardW=%.3f -> %s",
                tag, h.pageRoot.getWidth(), h.pageRoot.getHeight(),
                pager.getWidth(), pager.getHeight(),
                h.label == null ? 0 : h.label.getWidth(), h.label == null ? 0 : h.label.getHeight(),
                tag, cw, ch, pw, ph, padL, padT, padR, padB,
                Math.abs(greyAbove), Math.abs(greyBelow), Math.abs(greyAbove) + Math.abs(greyBelow),
                tag, ch / (float) cw, (cw == ch ? "SQUARE (ok)" : "RECTANGLE (bug)")));
    }

    private void assertSquare(String tag, MainActivity activity, int page) {
        Holder h = measurePage(activity, page);
        assertEquals(tag + ": grey card behind the board must be square"
                + " (card=" + h.card.getWidth() + "x" + h.card.getHeight()
                + ", preview=" + h.preview.getWidth() + "x" + h.preview.getHeight() + ")",
                h.card.getWidth(), h.card.getHeight());
    }

    private void assertHugsBoard(String tag, MainActivity activity, int page) {
        Holder h = measurePage(activity, page);
        int expectedW = h.preview.getWidth() + h.card.getPaddingLeft() + h.card.getPaddingRight();
        int expectedH = h.preview.getHeight() + h.card.getPaddingTop() + h.card.getPaddingBottom();
        assertEquals(tag + ": card width must hug the board", expectedW, h.card.getWidth());
        assertEquals(tag + ": card height must hug the board", expectedH, h.card.getHeight());
    }

    private MainActivity launchAndReport(String tag) {
        MainActivity a = launch();
        ViewPager pager = a.findViewById(R.id.view_pager);
        for (int page = 0; page < pager.getChildCount(); page++) {
            report(tag + " p" + page, a, page);
        }
        return a;
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-port-mdpi")
    public void portrait_411x891() {
        MainActivity a = launchAndReport("portrait 411x891");
        for (int page = 0; page < 2; page++) {
            assertSquare("portrait page" + page, a, page);
            assertHugsBoard("portrait page" + page, a, page);
        }
    }

    @Test
    @Config(qualifiers = "w360dp-h800dp-port-mdpi")
    public void portrait_360x800() {
        MainActivity a = launchAndReport("portrait 360x800");
        assertSquare("portrait 360x800", a, 0);
        assertHugsBoard("portrait 360x800", a, 0);
    }

    @Test
    @Config(qualifiers = "w412dp-h915dp-port-420dpi")
    public void portrait_412x915_xxhdpi() {
        MainActivity a = launchAndReport("portrait 412x915 xxhdpi");
        assertSquare("portrait 412x915", a, 0);
        assertHugsBoard("portrait 412x915", a, 0);
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp-port-mdpi")
    public void portrait_360x640() {
        MainActivity a = launchAndReport("portrait 360x640");
        assertSquare("portrait 360x640", a, 0);
        assertHugsBoard("portrait 360x640", a, 0);
    }

    @Test
    @Config(qualifiers = "w891dp-h411dp-land-mdpi")
    public void landscape_891x411() {
        MainActivity a = launchAndReport("landscape 891x411");
        assertSquare("landscape 891x411", a, 0);
        assertHugsBoard("landscape 891x411", a, 0);
    }

    @Test
    @Config(qualifiers = "w800dp-h360dp-land-mdpi")
    public void landscape_800x360() {
        MainActivity a = launchAndReport("landscape 800x360");
        assertSquare("landscape 800x360", a, 0);
        assertHugsBoard("landscape 800x360", a, 0);
    }

    @Test
    @Config(qualifiers = "w800dp-h1280dp-port-xhdpi")
    public void tablet_portrait_800x1280() {
        MainActivity a = launchAndReport("tablet portrait 800x1280");
        assertSquare("tablet portrait", a, 0);
        assertHugsBoard("tablet portrait", a, 0);
    }

    @Test
    @Config(qualifiers = "w600dp-h600dp-port-mdpi")
    public void squareWindow_600x600() {
        MainActivity a = launchAndReport("portrait 600x600");
        assertSquare("600x600", a, 0);
        assertHugsBoard("600x600", a, 0);
    }

    @Test
    @Config(qualifiers = "w320dp-h480dp-port-mdpi")
    public void smallScreen_320x480() {
        MainActivity a = launchAndReport("portrait 320x480");
        assertSquare("320x480", a, 0);
        assertHugsBoard("320x480", a, 0);
    }
}
