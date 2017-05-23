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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nu.ere.mooddiary;
import org.bostonandroid.preference.DatePreference;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class ExportActivity extends ThemedPreferenceActivity {
    private static final String LOG_PREFIX = "ExportActivity";

    private ORM orm;
    PreferenceScreen prefEventTypes;
    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.export);
        }
    }
    */

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    PreferenceScreen prefCSV,
                     prefSQL;

    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Enter onCreate");
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        addPreferencesFromResource(R.xml.export);
        PreferenceManager.setDefaultValues(this, R.xml.export, false);

        prefSQL = (PreferenceScreen) findPreference("export_sql");
        prefCSV = (PreferenceScreen) findPreference("export_csv");

        createSQLPreferences(); // When user clicks Export -> Export SQL, this is shown
        createCSVPreferences();  // When user clicks Export -> Export CSV, this is shown
    }

    public void createSQLPreferences() {
        Log.d(LOG_PREFIX, "Enter createSQLPreferences");
        prefSQL.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return exportDatabase();
            }
        });
    }

    public void createCSVPreferences() {
        Log.d(LOG_PREFIX, "Enter createCSVPreferences");

        // Create categories
        PreferenceCategory typesCategory = new PreferenceCategory(this);
        PreferenceCategory datesCategory = new PreferenceCategory(this);
        PreferenceCategory saveCategory = new PreferenceCategory(this);

        typesCategory.setTitle(getString(R.string.title_category_types));
        datesCategory.setTitle(getString(R.string.title_category_dates));
        saveCategory.setTitle(getString(R.string.title_category_save));

        prefCSV.addPreference(datesCategory);
        prefCSV.addPreference(typesCategory);
        prefCSV.addPreference(saveCategory);

        // Date begin widget
        DatePreference datePrefBeg = new DatePreference(this, null);
        datePrefBeg.setKey("csv_edit_time_beg");
        datePrefBeg.setPositiveButtonText(R.string.submit);
        datePrefBeg.setNegativeButtonText(R.string.cancel);
        datePrefBeg.setTitle(getString(R.string.title_date_from));
        datesCategory.addPreference(datePrefBeg);

        // Date end widget
        DatePreference datePrefEnd = new DatePreference(this, null);
        datePrefEnd.setKey("csv_edit_time_end");
        datePrefEnd.setPositiveButtonText(R.string.submit);
        datePrefEnd.setNegativeButtonText(R.string.cancel);
        datePrefEnd.setTitle(getString(R.string.title_date_to));
        datesCategory.addPreference(datePrefEnd);

        // minute list
        createEventTypePreferences(typesCategory);
        prefCSV.addPreference(typesCategory);

        // Save button (hitting 'back' without a save should just cancel)
        Preference saveButton = new Preference(this);
        saveButton.setTitle(R.string.submit);
        saveButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Produce the CSV based on the "csv_*" preferences
                ExportActivity.this.finish();
                return true;
            }
        });
        saveCategory.addPreference(saveButton);

    }

    // Function copied near-verbatim from PreferencesActivity :(
    public void createEventTypePreferences(PreferenceCategory screen) {
        Log.d(LOG_PREFIX, "Enter createMeasurementTypePreferences");

        MeasurementTypes measurementTypes = orm.getMeasurementTypes();

        Log.d(LOG_PREFIX, "minute count: " + Integer.toString(measurementTypes.types.size()));
        for(int i = 0; i < measurementTypes.types.size(); i++) {
            MeasurementType e = measurementTypes.types.get(i);
            CheckBoxPreference cb = new CheckBoxPreference(this);
            cb.setKey("csv_type_" + Long.toString(e.id));
            cb.setTitle(e.name);
            cb.setChecked(e.enabled == 1);
            screen.addPreference(cb);
        }
    }

    // Based on http://stackoverflow.com/a/19093736/417115
    public boolean exportDatabase() {
        Log.d(LOG_PREFIX, "Enter exportDatabase");

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1 /* Callback id */);

        // http://stackoverflow.com/a/6942735/417115
        String state = Environment.getExternalStorageState();
        Log.d(LOG_PREFIX, "sdcard state: " + state);

        if(Environment.MEDIA_MOUNTED.equals(state)) {
            Log.d(LOG_PREFIX, "sdcard mounted and writable");
        } else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Log.d(LOG_PREFIX, "sdcard mounted readonly");
        }

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    1 /* Code */);
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_PREFIX, "We have write permissions");

                    String name = this.orm.getDatabaseName();

                    String sourcePath = "//data//" + this.getPackageName() + "//databases//" + name;
                    String targetPath = "Download" + "/" + name + ".sqlite3";

                    File sd = Environment.getExternalStorageDirectory();
                    File data = Environment.getDataDirectory();

                    File currentDB = new File(data, sourcePath);
                    File backupDB  = new File(sd,   targetPath);

                    if (currentDB.exists()) {
                        try {
                            FileChannel src = new FileInputStream(currentDB).getChannel();
                            FileChannel dst = new FileOutputStream(backupDB).getChannel();
                            dst.transferFrom(src, 0, src.size());
                            src.close();
                            dst.close();
                        } catch (Exception e) {
                            Log.d(LOG_PREFIX, e.getMessage());
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else{
                        Log.d(LOG_PREFIX, "FIXME: " + sourcePath + ": No such file");
                    }

                    Toast.makeText(this, "Exported!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(LOG_PREFIX, "NEIN!!!");
                }
            }
        }
    }
}