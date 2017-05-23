/* Mood Diary, a free Android mood tracker
 * Copyright (C) 2017 Kristian Gunstone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nu.ere.mooddiary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class ImportActivity extends ThemedPreferenceActivity {
    private static final String LOG_PREFIX = "ImportActivity";

    private ORM orm;
    private String filename = null;

    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Enter onCreate");
        super.onCreate(savedInstanceState);

        File mPath = new File(Environment.getExternalStorageDirectory() + "//Download//");
        ShitDialog fileDialog = new ShitDialog(this, mPath, ".sqlite3");
        fileDialog.addFileListener(new ShitDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                Log.d(getClass().getName(), "selected file " + file.toString());
                filename = file.toString();
                importDatabase();
            }
        });

        fileDialog.showDialog();
    }

    public void importDatabase() {
        Log.d(LOG_PREFIX, "Enter importDatabase");

        if(filename == null) {
            Log.d(LOG_PREFIX, "filename is NULL, cancel");
            finish();
        }

        // Test the DB (check table names and version)
        // Close DB
        // Close ORM
        ORM orm = ORM.getInstance(this);
        orm.close();
        // Delete the old DB (required)
        this.deleteDatabase(orm.getDatabaseName());
        // Do straight file copy
        // Reopen ORM

        String sourcePath = filename;
        String targetPath =
                "//data//data//" + this.getPackageName() + "//databases//" + orm.getDatabaseName();

        if(verifyDatabase() == false) {
            Toast.makeText(this, "Import: Incompatible database!", Toast.LENGTH_LONG).show(); // FIXME hardcoded
            finish();
            return;
        }

        File newDB = new File(sourcePath);
        File oldDB  = new File(targetPath);

        try {
            FileChannel src = new FileInputStream(newDB).getChannel();
            FileChannel dst = new FileOutputStream(oldDB).getChannel();
            Log.d(LOG_PREFIX, sourcePath + " -> " + targetPath);
            long fuck = dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            Log.d(LOG_PREFIX, Long.toString(fuck) + " bytes copied");
        } catch (Exception e) {
            Log.d(LOG_PREFIX, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        orm.reload(this);
        Toast.makeText(this, "Imported!", Toast.LENGTH_SHORT).show(); // FIXME hardcoded
        finish();
    }

    public boolean verifyDatabase() {
        Log.d(LOG_PREFIX, "Enter verifyDatabase");
        SQLiteDatabase db = null;
        String[] tables = {
                "android_metadata",
                "Events",
                "EntityPrimitives",
                "MeasurementTypes",
                "ReminderGroups",
                "ReminderTimes"
        };

        // Quick integrity / compatibility check
        try {
            db = SQLiteDatabase.openDatabase(filename, null, SQLiteDatabase.OPEN_READONLY);
            for (String table: tables) {
                Log.d(LOG_PREFIX, "Checking table: " + table);
                Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + table, null);
                cursor.moveToFirst();
                cursor.getColumnCount();
                cursor.close();
            }
            db.close();
        } catch(SQLiteException e) {
            Log.d(LOG_PREFIX, "EXCEPTION!");
            Log.d(LOG_PREFIX, e.getMessage());
            //database does't exist yet.
            return false;
        }

        Log.d(LOG_PREFIX, "Database verified");
        return true;
    }

}