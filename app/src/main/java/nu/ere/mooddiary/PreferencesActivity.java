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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class PreferencesActivity extends ThemedPreferenceActivity {
    private static final String LOG_PREFIX = "PreferencesActivity";
    private ORM orm;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    PreferenceScreen prefReminders,
                     prefMeasurementTypes;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        prefReminders =
                (PreferenceScreen) findPreference("preference_select_reminders");
        prefMeasurementTypes =
                (PreferenceScreen) findPreference("preference_select_measurement_types");

        PreferenceScreen prefExport = (PreferenceScreen) findPreference("preference_select_export");
        prefExport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent in = new Intent(PreferencesActivity.this, ExportActivity.class);
                startActivity(in);
                finish();
                return true;
            }
        });

        PreferenceScreen prefImport = (PreferenceScreen) findPreference("preference_select_import");
        prefImport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent in = new Intent(PreferencesActivity.this, ImportActivity.class);
                startActivity(in);
                finish();
                return true;
            }
        });
        createReminderPreferences();  // When user clicks Settings -> Reminders, this is shown
        createMeasurementTypePreferences(); // When user clicks Settings -> Measurement Types, this is shown
    }

    // "Level 2"
    public void createMeasurementTypePreferences() {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter createMeasurementTypePreferences");

        MeasurementTypes measurementTypes = orm.getMeasurementTypes();

        // Create categories
        PreferenceCategory oldCategory = new PreferenceCategory(this);
        PreferenceCategory addCategory = new PreferenceCategory(this);

        oldCategory.setTitle(getApplicationContext().getString(R.string.title_category_old_types));
        addCategory.setTitle(getApplicationContext().getString(R.string.title_category_new_type));

        this.prefMeasurementTypes.addPreference(oldCategory);
        this.prefMeasurementTypes.addPreference(addCategory);

        // Make a list of existing measurement types - each entry can be clicked to open up a new/edit submenu

        for(MeasurementType mType: measurementTypes.types) {
            Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "Load old type id: " + Integer.toString(mType.id));

            Preference oldType = new Preference(this);

            oldType.setTitle(mType.name);
            if(mType.enabled == 0) {
                oldType.setSummary(getString(R.string.summary_is_disabled));
            }

            Preference.OnPreferenceClickListener listener =
                    new OnMeasurementPreferenceClickListener(mType.id) {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Intent in = new Intent(PreferencesActivity.this,
                                    MeasurementPreferencesActivity.class);
                            in.putExtra(BundleExtraKey.MEASUREMENT_TYPE_MODE,
                                    PreferenceEditMode.MEASUREMENT_TYPE_CHANGE);
                            in.putExtra(BundleExtraKey.MEASUREMENT_TYPE_ID, this.measurementTypeId);
                            Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX,
                                    "Sending mTypeId: " + Integer.toString(this.measurementTypeId));
                            startActivityForResult(in, PreferenceEditMode.MEASUREMENT_TYPE_CHANGE);
                            return true;
                        }
                    };
            oldType.setOnPreferenceClickListener(listener);
            oldCategory.addPreference(oldType);
        }

        Preference newTypeButton = new Preference(this);
        newTypeButton.setTitle(R.string.action_preference_measurements);

        Preference.OnPreferenceClickListener listener =
                new OnReminderTimePreferenceClickListener(-1) {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent in = new Intent(PreferencesActivity.this,
                                MeasurementPreferencesActivity.class);
                        in.putExtra(BundleExtraKey.MEASUREMENT_TYPE_MODE,
                                PreferenceEditMode.MEASUREMENT_TYPE_CREATE);
                        startActivityForResult(in, PreferenceEditMode.MEASUREMENT_TYPE_CREATE);
                        return true;
                    }
                };
        newTypeButton.setOnPreferenceClickListener(listener);
        addCategory.addPreference(newTypeButton);
    }

    // "Level 2"
    public void createReminderPreferences() {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter createReminderPreferences");

        // Create categories
        PreferenceCategory oldCategory = new PreferenceCategory(this);
        PreferenceCategory addCategory = new PreferenceCategory(this);
        oldCategory.setTitle(getString(R.string.title_category_old_reminders));
        addCategory.setTitle(getString(R.string.title_category_new_reminder));

        this.prefReminders.addPreference(oldCategory);
        this.prefReminders.addPreference(addCategory);

        // Make a list of existing reminders - each entry can be clicked to open up a new/edit submenu
        // FIXME order by hhmm!
        ArrayList<ReminderTime> sortedReminders = orm.getReminderTimes().getSorted();
        //ArrayList<ReminderTime> sortedReminders = orm.getReminderTimes().reminderTimes;
        for(ReminderTime reminder: sortedReminders) {
            Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Load old reminderTimeId: " + Integer.toString(reminder.id));

            Preference oldReminder = new Preference(this);
             // oldReminder.setKey("junk_old_reminder_" + Integer.toString(i)); // FIXME

            oldReminder.setTitle(Util.toHumanTime(this, reminder.hour, reminder.minute));
            Preference.OnPreferenceClickListener listener =
                    new OnReminderTimePreferenceClickListener(reminder.id) {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent in = new Intent(PreferencesActivity.this,
                            ReminderPreferencesActivity.class);
                    in.putExtra(BundleExtraKey.REMINDER_MODE,
                            PreferenceEditMode.REMINDER_CHANGE);
                    in.putExtra(BundleExtraKey.REMINDER_TIME_ID, this.reminderTimeId);
                    Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX,
                            "Sending reminderTimeId: " + Integer.toString(this.reminderTimeId));
                    startActivityForResult(in, PreferenceEditMode.REMINDER_CHANGE);
                    return true;
                }
            };
            oldReminder.setOnPreferenceClickListener(listener);
            oldCategory.addPreference(oldReminder);
        }

        Preference newReminderButton = new Preference(this);
        newReminderButton.setTitle(R.string.action_preference_reminders);

        Preference.OnPreferenceClickListener listener =
            new OnReminderTimePreferenceClickListener(-1) {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent in = new Intent(PreferencesActivity.this,
                            ReminderPreferencesActivity.class);
                    in.putExtra(BundleExtraKey.REMINDER_MODE,
                            PreferenceEditMode.REMINDER_CREATE);
                    startActivityForResult(in, PreferenceEditMode.REMINDER_CREATE);
                    return true;
                }
            };
        newReminderButton.setOnPreferenceClickListener(listener);
        addCategory.addPreference(newReminderButton);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter onActivityResult");
        Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "requestCode " + Integer.toString(requestCode) +
                ", resultCode " + Integer.toString(resultCode));

        if (resultCode != RESULT_OK) {
            Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "Bad resultCode: do nothing");
            Toast.makeText(this,
                    this.getString(R.string.toast_cancelled), Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = data.getExtras();
        Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "Received bundle!");

        switch (requestCode) {
            case PreferenceEditMode.REMINDER_CREATE:
                Util.addReminder(this, bundle);
                prefReminders.removeAll(); // For redraw
                createReminderPreferences();
                break;

            case PreferenceEditMode.REMINDER_CHANGE:
                Util.updateReminder(this, bundle);
                prefReminders.removeAll(); // For redraw
                createReminderPreferences();
                break;

            case PreferenceEditMode.MEASUREMENT_TYPE_CREATE:
                Util.addMeasurementType(this, bundle);
                prefMeasurementTypes.removeAll(); // For redraw
                createMeasurementTypePreferences();
                break;

            case PreferenceEditMode.MEASUREMENT_TYPE_CHANGE:
                Util.updateMeasurementType(this, bundle);
                prefMeasurementTypes.removeAll(); // For redraw
                createMeasurementTypePreferences();
                break;

            default:
                Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "Unknown request: " + Integer.toString(requestCode));
                break;
        }
    }
}