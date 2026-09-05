/*
 This file is part of YiPuzzles.

 Privacy Friendly Sudoku is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly Sudoku is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Sudoku. If not, see <http://www.gnu.org/licenses/>.
 */
package org.lyi.puzzles

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.work.Configuration
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import org.lyi.puzzles.backup.BackupCreator
import org.lyi.puzzles.backup.BackupRestorer
import org.secuso.privacyfriendlybackup.api.pfa.BackupManager

class PF2048 : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        BackupManager.backupCreator = BackupCreator()
        BackupManager.backupRestorer = BackupRestorer()

        applyNightMode(this)

        // Material You: pick up the wallpaper based colour scheme on Android 12+.
        // The precondition is evaluated for every activity, so toggling the preference
        // takes effect as soon as the activities are recreated.
        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions.Builder()
                .setPrecondition { _, _ -> isDynamicColorEnabled(this) }
                .build()
        )
    }

    override val workManagerConfiguration: Configuration =
        Configuration.Builder().setMinimumLoggingLevel(Log.INFO).build()

    companion object {
        const val PREF_THEME = "currentTheme"
        const val PREF_DYNAMIC_COLORS = "pref_dynamic_colors"

        @JvmStatic
        fun isDynamicColorEnabled(context: Context): Boolean =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_DYNAMIC_COLORS, true)

        /** Applies the light / dark / system setting stored in the preferences. */
        @JvmStatic
        fun applyNightMode(context: Context) {
            val mode = when (
                PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_THEME, "system")
            ) {
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}
