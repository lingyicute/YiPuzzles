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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.lyi.puzzles.BuildConfig;
import org.lyi.puzzles.R;
import org.lyi.puzzles.activities.helper.BaseActivity;
import org.lyi.puzzles.helpers.EdgeToEdgeHelper;

import com.google.android.material.appbar.MaterialToolbar;

public class AboutActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdgeHelper.enableEdgeToEdgeDisplay(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(R.string.about);
        }

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            EdgeToEdgeHelper.applyBottomSystemBarPadding(mainContent);
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(BaseActivity.MAIN_CONTENT_FADEIN_DURATION);
        }

        overridePendingTransition(0, 0);

        ((TextView) findViewById(R.id.textFieldVersionName)).setText(getString(R.string.version_number, BuildConfig.VERSION_NAME));

        setupLinkButton(R.id.buttonHomepage, R.string.about_homepage, getString(R.string.url_homepage));
        setupLinkButton(R.id.buttonGithub, R.string.about_github, getString(R.string.url_github));
    }

    /**
     * Wires one of the author card link buttons to an external URL. A tooltip is
     * set as well so the icon-only portrait buttons still convey their meaning.
     */
    private void setupLinkButton(int buttonId, int tooltipTextRes, final String url) {
        View button = findViewById(buttonId);
        if (button == null) {
            return;
        }
        androidx.appcompat.widget.TooltipCompat.setTooltipText(button, getString(tooltipTextRes));
        button.setOnClickListener(v -> openUrl(url));
    }

    /** Opens the given URL in an external browser, ignoring the click if none is available. */
    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException ignored) {
            // No app can handle the link; nothing sensible to do here.
        }
    }

}

