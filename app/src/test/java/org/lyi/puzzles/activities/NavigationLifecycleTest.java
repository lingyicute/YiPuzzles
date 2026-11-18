/* SPDX-License-Identifier: GPL-3.0-or-later */
package org.lyi.puzzles.activities;

import android.os.Looper;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lyi.puzzles.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import java.time.Duration;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class NavigationLifecycleTest {
    @Test public void stoppedActivityDoesNotLaunchPendingNavigation() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            NavigationView navigation = activity.findViewById(R.id.nav_view);
            activity.onNavigationItemSelected(navigation.getMenu().findItem(R.id.nav_settings));
            controller.pause().stop();
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(300));
            assertNull(shadowOf(activity).getNextStartedActivity());
            controller.start().resume();
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(300));
            View content = activity.findViewById(R.id.main_content);
            assertEquals(1f, content.getAlpha(), 0f);
            assertTrue(navigation.getMenu().findItem(R.id.nav_example).isChecked());
        }
    }

    @Test public void reselectingCurrentPageCancelsPendingNavigation() {
        try (ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            NavigationView navigation = activity.findViewById(R.id.nav_view);
            activity.onNavigationItemSelected(navigation.getMenu().findItem(R.id.nav_settings));
            activity.onNavigationItemSelected(navigation.getMenu().findItem(R.id.nav_example));
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(300));
            assertNull(shadowOf(activity).getNextStartedActivity());
            assertEquals(1f, activity.findViewById(R.id.main_content).getAlpha(), 0f);
        }
    }
}
