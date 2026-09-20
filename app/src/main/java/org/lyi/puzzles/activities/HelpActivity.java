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

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.lyi.puzzles.R;
import org.lyi.puzzles.activities.adapter.HelpQuestionAdapter;
import org.lyi.puzzles.activities.adapter.HelpQuestionAdapter.HelpItem;
import org.lyi.puzzles.activities.helper.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Help screen (FAQ).
 * <p>
 * Material 3 implementation: a {@link RecyclerView} list of expandable filled
 * cards (one per question) replaces the legacy {@code ExpandableListView}.
 * The list carries the shared {@code main_content} id, so {@link BaseActivity}
 * applies the standard content fade-in and the bottom system bar padding
 * (edge-to-edge) exactly like on every other screen.
 */
public class HelpActivity extends BaseActivity {

    private static final String STATE_EXPANDED_ITEMS = "help_expanded_items";

    private HelpQuestionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        boolean[] restoredState = savedInstanceState != null
                ? savedInstanceState.getBooleanArray(STATE_EXPANDED_ITEMS)
                : null;

        adapter = new HelpQuestionAdapter(this, buildHelpItems(restoredState));

        RecyclerView helpList = findViewById(R.id.main_content);
        helpList.setLayoutManager(new LinearLayoutManager(this));
        helpList.setAdapter(adapter);

        overridePendingTransition(0, 0);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter != null) {
            outState.putBooleanArray(STATE_EXPANDED_ITEMS, adapter.getExpandedState());
        }
    }

    /**
     * The FAQ entries in display order. Entries that were expanded before a
     * configuration change (rotation, dark mode switch, ...) are restored.
     */
    private List<HelpItem> buildHelpItems(@Nullable boolean[] expandedState) {
        int[][] entries = {
                {R.string.help_permission, R.string.help_permission_answer},
                {R.string.help_play, R.string.help_play_answer},
                {R.string.help_play_how, R.string.help_play_how_answer},
                {R.string.help_play_add, R.string.help_play_add_answer},
                {R.string.help_tip, R.string.help_tip_answer},
                {R.string.help_undo, R.string.help_undo_answer},
                {R.string.help_color, R.string.help_color_answer},
        };

        List<HelpItem> items = new ArrayList<>(entries.length);
        for (int i = 0; i < entries.length; i++) {
            boolean expanded = expandedState != null
                    && i < expandedState.length
                    && expandedState[i];
            items.add(new HelpItem(getString(entries[i][0]), getString(entries[i][1]), expanded));
        }
        return items;
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_help;
    }

}
