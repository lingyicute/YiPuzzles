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

package org.lyi.puzzles.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Karola Marky
 * @version 20161223
 * Structure based on http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
 * accessed at 16th June 2016
 * <p>
 * Defines the (empty) database that is written into the backups created by
 * {@link org.lyi.puzzles.backup.BackupCreator}; the game itself stores its state in files and
 * in the shared preferences, so nothing is ever read from or written to this database.
 */

public class PFASQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    /**
     * Use the following pattern for the name of the database
     * PF_[Name of the app]_DB
     */
    public static final String DATABASE_NAME = "PF_EXAMPLE_DB";

    //Names of table in the database
    private static final String TABLE_SAMPLEDATA = "SAMPLE_DATA";

    //Names of columns in the databases in this example we only use one table
    private static final String KEY_ID = "id";
    private static final String KEY_DOMAIN = "domain";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_LENGTH = "length";

    public PFASQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /**
         * Create the table sample data on the first start
         * Be careful with the final line of the query and the SQL syntax that is used in the String.
         */
        String CREATE_SAMPLEDATA_TABLE = "CREATE TABLE " + TABLE_SAMPLEDATA +
                "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_DOMAIN + " TEXT NOT NULL," +
                KEY_USERNAME + " TEXT NOT NULL," +
                KEY_LENGTH + " INTEGER);";

        sqLiteDatabase.execSQL(CREATE_SAMPLEDATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_SAMPLEDATA);

        onCreate(sqLiteDatabase);
    }


}
