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

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter onCreate");
        super.onCreate(savedInstanceState);

        start();
    }

    public void importDatabase() {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter importDatabase");

        if(filename == null) {
            Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "filename is NULL, cancel");
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
            Toast.makeText(this, getString(R.string.import_bad_database), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        File newDB = new File(sourcePath);
        File oldDB  = new File(targetPath);

        try {
            FileChannel src = new FileInputStream(newDB).getChannel();
            FileChannel dst = new FileOutputStream(oldDB).getChannel();
            Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, sourcePath + " -> " + targetPath);
            long fuck = dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, Long.toString(fuck) + " bytes copied");
        } catch (Exception e) {
            Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        orm.reload(this);
        Toast.makeText(this, getString(R.string.import_good_database), Toast.LENGTH_SHORT).show();
        finish();
    }

    public boolean verifyDatabase() {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter verifyDatabase");
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
                Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Checking table: " + table);
                Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + table, null);
                cursor.moveToFirst();
                cursor.getColumnCount();
                cursor.close();
            }
            db.close();
        } catch(SQLiteException e) {
            Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "EXCEPTION!");
            Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, e.getMessage());
            //database does't exist yet.
            return false;
        }

        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Database verified");
        return true;
    }

    // Based on http://stackoverflow.com/a/19093736/417115
    public boolean start() {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter exportDatabase");

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1 /* Callback id */);

        // http://stackoverflow.com/a/6942735/417115
        String state = Environment.getExternalStorageState();
        Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "sdcard state: " + state);

        if(Environment.MEDIA_MOUNTED.equals(state)) {
            Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "sdcard mounted and writable");
        } else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "sdcard mounted readonly");
        }

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "READ_EXTERNAL_STORAGE != GRANTED");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "(Should show request permission rationale here)");
            }
        } else {
            Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "READ_EXTERNAL_STORAGE granted, I think");
            Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "-------- Request permission ---------");
            if(PermissionUtils.hasSelfPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showDialog();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, // ???
                        1 /* Code */);
            }
            Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "-------- End Request permission ---------");
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showDialog();
                    Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "We have read permissions");

                } else {
                    Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "NEIN!!!");
                }
                break;

            default:
                Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Unhandled requestCode: " + Integer.toString(requestCode));
        }
    }

    private void showDialog() {
        File mPath = new File(Environment.getExternalStorageDirectory() + "//Download//");
        FileSelectDialog fileDialog = new FileSelectDialog(this, mPath, ".sqlite3");
        fileDialog.addFileListener(new FileSelectDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                Logger.log(Logger.LOGLEVEL_1, getClass().getName(), "selected file " + file.toString());
                filename = file.toString();
                importDatabase();
            }
        });

        Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "Showing dialog");
        fileDialog.showDialog();
    }

}