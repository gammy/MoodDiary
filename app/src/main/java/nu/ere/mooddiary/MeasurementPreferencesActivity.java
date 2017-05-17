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

/* New Reminder menu, containing a list of measurement types, a time, and a save button */
package nu.ere.mooddiary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class MeasurementPreferencesActivity extends ThemedPreferenceActivity {
    private static final String LOG_PREFIX = "MeasurePref..Activity";
    private ORM orm;

    private int tempHour, tempMinute;
    private Calendar cal = null;
    private HashMap<Integer, CheckBoxPreference> tempTypeMap = null;
    private Bundle bundle;
    PreferenceScreen measurementScreen;
    private int oldID = -1;

    /* What we need to show:
     *          Name: Farts        (EditBox)
     *          Type: range_center (Popup radio buttons?)
     * Minimum value: 0_           (numberbox)
     * Maximum value: 100          (numberbox)
     * Default value: 0            (numberbox)
     */
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Enter onCreate");
        super.onCreate(savedInstanceState);
        orm = orm.getInstance(this);

        MeasurementType mType = null;
        Intent intent = getIntent();
        int editMode = intent.getIntExtra(BundleExtraKey.MEASUREMENT_TYPE_MODE, -1);

        switch(editMode) {
            default:
                throw new NoSuchElementException(Integer.toString(editMode) + ": Invalid mode");

            case PreferenceEditMode.MEASUREMENT_TYPE_CREATE:
                Log.d(LOG_PREFIX, "Our mission: CREATE measurement type");
                break;

            case PreferenceEditMode.MEASUREMENT_TYPE_CHANGE:
            case PreferenceEditMode.MEASUREMENT_TYPE_DELETE:
                Log.d(LOG_PREFIX, "Our mission: EDIT / DELETE measurement type");
                oldID = intent.getIntExtra(BundleExtraKey.MEASUREMENT_TYPE_ID, -1);
                Log.d(LOG_PREFIX, "oldID: " + Integer.toString(oldID));
                if(oldID == -1) {
                    throw new NoSuchElementException(Integer.toString(oldID) + ": An existing" +
                        "measurement type id needs to be passed to this intent in this mode");
                }
                mType = orm.getMeasurementTypes().getByID(oldID);
                Toast.makeText(this, "mType: " + Integer.toString(oldID), Toast.LENGTH_SHORT).show();
                break;

        }

        bundle = new Bundle();

        // Create the main context
        measurementScreen = getPreferenceManager().createPreferenceScreen(this);

        // Create categories
        PreferenceCategory timeCategory = new PreferenceCategory(this);
        PreferenceCategory typeCategory = new PreferenceCategory(this);
        PreferenceCategory saveCategory = new PreferenceCategory(this);
        PreferenceCategory delCategory  = new PreferenceCategory(this);

        timeCategory.setTitle(getApplicationContext().getString(R.string.title_category_time));
        typeCategory.setTitle(getApplicationContext().getString(R.string.title_category_types));
        saveCategory.setTitle(getApplicationContext().getString(R.string.title_category_save));
        delCategory.setTitle(getApplicationContext().getString(R.string.title_category_delete));

        measurementScreen.addPreference(timeCategory);
        measurementScreen.addPreference(typeCategory);
        if(editMode == PreferenceEditMode.MEASUREMENT_TYPE_CHANGE) {
            measurementScreen.addPreference(delCategory);
        }
        measurementScreen.addPreference(saveCategory);

        if(mType != null) {
            // FIXME
            //tempHour   = reminderTime.hour;
            //tempMinute = reminderTime.minute;
        } else {
            // FIXME
        }

        //timeCategory.addPreference(timePref);

        // If we're editing an existing reminder, add the option to delete it with a button
        if(editMode == PreferenceEditMode.MEASUREMENT_TYPE_CHANGE) {
            Preference delButton = new Preference(this);
            delButton.setTitle(R.string.delete);
            delButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.d(LOG_PREFIX, "Enter onPreferenceClick pre-delete bundle");
                    Intent rIntent = getIntent();
                    bundle.putInt(BundleExtraKey.MEASUREMENT_TYPE_MODE,
                            PreferenceEditMode.MEASUREMENT_TYPE_DELETE);
                    bundle.putInt(BundleExtraKey.MEASUREMENT_TYPE_ID, oldID);
                    rIntent.putExtras(bundle);
                    setResult(Activity.RESULT_OK, rIntent);
                    finish();
                    return true;
                }
            });
            delCategory.addPreference(delButton);
        }

        // Add save button (hitting 'back' without a save should just cancel)
        Preference saveButton = new Preference(this);
        saveButton.setTitle(R.string.submit);
        saveButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Log.d(LOG_PREFIX, "Enter onPreferenceClick pre-bundle");
                Intent rIntent = getIntent();
                bundle.putInt(BundleExtraKey.MEASUREMENT_TYPE_ID, oldID);

                // FIXME more properties here

                rIntent.putExtras(bundle);
                setResult(Activity.RESULT_OK, rIntent);
                finish();
                return true;
            }
        });

        saveCategory.addPreference(saveButton);
        setPreferenceScreen(measurementScreen);
    }
}