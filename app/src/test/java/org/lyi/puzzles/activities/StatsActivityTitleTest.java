/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles.activities;

import androidx.appcompat.widget.Toolbar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lyi.puzzles.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class StatsActivityTitleTest {
    @Test public void toolbarNamesTheScreenInsteadOfTheApp() {
        try (ActivityController<StatsActivity> controller = Robolectric.buildActivity(StatsActivity.class).setup()) {
            StatsActivity activity = controller.get();
            Toolbar toolbar = activity.findViewById(R.id.toolbar);
            assertEquals(activity.getString(R.string.action_stat), String.valueOf(toolbar.getTitle()));
        }
    }
}
