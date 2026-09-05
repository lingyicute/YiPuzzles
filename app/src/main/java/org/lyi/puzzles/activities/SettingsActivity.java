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


import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.DynamicColors;

import org.lyi.puzzles.PF2048;
import org.lyi.puzzles.R;
import org.lyi.puzzles.activities.helper.BaseActivity;
import org.lyi.puzzles.helpers.TileColors;


/**
 * As the name suggests the settings for the app are located in this activity.
 * Here you can change options like the colour palette of the tiles, the light/dark theme,
 * Material You dynamic colours, animations and the screen lock.
 * <p>
 * Implemented with AndroidX {@link PreferenceFragmentCompat} which renders Material 3
 * switches, list dialogs and typography.
 *
 * @author Julian Wadephul and Saskia Jacob
 * @version 20180910
 */
public class SettingsActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        super.mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        setContentView(R.layout.activity_settings);

        overridePendingTransition(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == null) return;

        switch (key) {
            case PF2048.PREF_THEME:
                // AppCompat recreates the activity for us
                PF2048.applyNightMode(this);
                break;
            case PF2048.PREF_DYNAMIC_COLORS:
                // The DynamicColors precondition is evaluated on activity creation, so a
                // simple recreate() is enough to switch between wallpaper and baseline colours.
                TileColors.invalidate();
                recreate();
                break;
            case TileColors.PREF_KEY:
                TileColors.invalidate();
                break;
            default:
                break;
        }
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_settings;
    }

    /**
     * Shows the general preferences (see {@code res/xml/pref_general.xml}).
     */
    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            setPreferencesFromResource(R.xml.pref_general, rootKey);

            // Dynamic colours only exist on Android 12+
            SwitchPreferenceCompat dynamicColors = findPreference(PF2048.PREF_DYNAMIC_COLORS);
            if (dynamicColors != null) {
                boolean available = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && DynamicColors.isDynamicColorAvailable();
                dynamicColors.setEnabled(available);
                if (!available) {
                    dynamicColors.setChecked(false);
                }
            }

            ListPreference colorScheme = findPreference(TileColors.PREF_KEY);
            if (colorScheme != null) {
                colorScheme.setSummaryProvider(new Preference.SummaryProvider<ListPreference>() {
                    @Override
                    public CharSequence provideSummary(ListPreference preference) {
                        CharSequence entry = preference.getEntry();
                        if (TileColors.SCHEME_MATERIAL_YOU.equals(preference.getValue())) {
                            return entry + " · " + getString(R.string.settings_color_summary_dynamic);
                        }
                        return entry;
                    }
                });
            }
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            // bottom inset padding is applied to the fragment container by BaseActivity
            RecyclerView list = getListView();
            if (list != null) {
                list.setClipToPadding(false);
            }
        }
    }
}
