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

import android.os.Bundle;
import android.widget.ExpandableListView;

import org.lyi.puzzles.R;
import org.lyi.puzzles.activities.adapter.HelpExpandableListAdapter;
import org.lyi.puzzles.activities.helper.BaseActivity;
import org.lyi.puzzles.helpers.EdgeToEdgeHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class HelpActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        LinkedHashMap<String, List<String>> expandableListDetail = buildData();

        ExpandableListView generalExpandableListView = findViewById(R.id.generalExpandableListView);
        HelpExpandableListAdapter adapter = new HelpExpandableListAdapter(this, new ArrayList<>(expandableListDetail.keySet()), expandableListDetail);
        generalExpandableListView.setAdapter(adapter);
        // keep the list clear of the navigation bar (edge-to-edge)
        EdgeToEdgeHelper.applyBottomSystemBarPadding(generalExpandableListView);

        overridePendingTransition(0, 0);
    }

    private LinkedHashMap<String, List<String>> buildData() {
        LinkedHashMap<String, List<String>> expandableListDetail = new LinkedHashMap<String, List<String>>();

        expandableListDetail.put(getString(R.string.help_whatis), Collections.singletonList(getString(R.string.help_whatis_answer)));
        expandableListDetail.put(getString(R.string.help_privacy), Collections.singletonList(getString(R.string.help_privacy_answer)));
        expandableListDetail.put(getString(R.string.help_permission), Collections.singletonList(getString(R.string.help_permission_answer)));
        expandableListDetail.put(getString(R.string.help_play), Collections.singletonList(getString(R.string.help_play_answer)));
        expandableListDetail.put(getString(R.string.help_play_how), Collections.singletonList(getString(R.string.help_play_how_answer)));
        expandableListDetail.put(getString(R.string.help_play_add), Collections.singletonList(getString(R.string.help_play_add_answer)));
        expandableListDetail.put(getString(R.string.help_tip), Collections.singletonList(getString(R.string.help_tip_answer)));
        expandableListDetail.put(getString(R.string.help_undo), Collections.singletonList(getString(R.string.help_undo_answer)));
        expandableListDetail.put(getString(R.string.help_color), Collections.singletonList(getString(R.string.help_color_answer)));


        return expandableListDetail;
    }

    protected int getNavigationDrawerID() {
        return R.id.nav_help;
    }

}
