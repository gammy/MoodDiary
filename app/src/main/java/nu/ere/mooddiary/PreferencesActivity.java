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

        prefReminders = (PreferenceScreen) findPreference("preference_select_reminders");
        prefMeasurementTypes = (PreferenceScreen) findPreference("preference_select_measurement_types");

        createMeasurementTypePreferences(); // When user clicks Settings -> Measurement Types, this is shown
        createReminderPreferences();  // When user clicks Settings -> Reminders, this is shown
    }

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
            new MyPreferenceFragment()).commit();
    }


    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            createReminderPreferences();
            // PreferenceManager.setDefaultValues(this, R.xml.preferences, false); // ?
        }
    }
    */

    // "Level 2"
    public void createMeasurementTypePreferences() {
        Log.d(LOG_PREFIX, "Enter createMeasurementTypePreferences");

        MeasurementTypes measurementTypes = orm.getMeasurementTypes();
        /*

        for(int i = 0; i < measurementTypes.types.size(); i++) {
            MeasurementType e = measurementTypes.types.get(i);
            CheckBoxPreference cb = new CheckBoxPreference(this);
            cb.setKey("visible_event_" + Long.toString(e.id));
            cb.setTitle(e.name);
            cb.setChecked(e.enabled == 1);
            prefMeasurementTypes.addPreference(cb);
        }
        */
        // Create categories
        PreferenceCategory oldCategory = new PreferenceCategory(this);
        PreferenceCategory addCategory = new PreferenceCategory(this);

        oldCategory.setTitle(getApplicationContext().getString(R.string.title_category_old_types));
        addCategory.setTitle(getApplicationContext().getString(R.string.title_category_new_type));

        this.prefMeasurementTypes.addPreference(oldCategory);
        this.prefMeasurementTypes.addPreference(addCategory);

        // Make a list of existing measurement types - each entry can be clicked to open up a new/edit submenu
        for(int i = 0; i < measurementTypes.types.size(); i++) {
            MeasurementType mType = measurementTypes.types.get(i);
            Log.d(LOG_PREFIX, "Load old type id: " + Integer.toString(mType.id));

            Preference oldType = new Preference(this);

            oldType.setTitle(mType.name);
            Preference.OnPreferenceClickListener listener =
                    new OnMeasurementPreferenceClickListener(mType.id) { // FIXME
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Intent in = new Intent(PreferencesActivity.this, MeasurementPreferencesActivity.class);
                            in.putExtra(BundleExtraKey.MEASUREMENT_TYPE_MODE, PreferenceEditMode.MEASUREMENT_TYPE_CHANGE);
                            in.putExtra(BundleExtraKey.MEASUREMENT_TYPE_ID, this.measurementTypeId);
                            Log.d(LOG_PREFIX, "Sending mTypeId: " + Integer.toString(this.measurementTypeId));
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
                        Intent in = new Intent(PreferencesActivity.this, MeasurementPreferencesActivity.class);
                        in.putExtra(BundleExtraKey.MEASUREMENT_TYPE_MODE, PreferenceEditMode.MEASUREMENT_TYPE_CREATE);
                        startActivityForResult(in, PreferenceEditMode.MEASUREMENT_TYPE_CREATE);
                        return true;
                    }
                };
        newTypeButton.setOnPreferenceClickListener(listener);
        addCategory.addPreference(newTypeButton);
    }

    // "Level 2"
    public void createReminderPreferences() {
        Log.d(LOG_PREFIX, "Enter createReminderPreferences");

        // Create categories
        PreferenceCategory oldCategory = new PreferenceCategory(this);
        PreferenceCategory addCategory = new PreferenceCategory(this);
        oldCategory.setTitle(getApplicationContext().getString(R.string.title_category_old_reminders));
        addCategory.setTitle(getApplicationContext().getString(R.string.title_category_new_reminder));

        this.prefReminders.addPreference(oldCategory);
        this.prefReminders.addPreference(addCategory);

        // Make a list of existing reminders - each entry can be clicked to open up a new/edit submenu
        for(int i = 0; i < orm.getReminderTimes().reminderTimes.size(); i++) {
            ReminderTime reminder = orm.getReminderTimes().reminderTimes.get(i);
            Log.d(LOG_PREFIX, "Load old reminderTimeId: " + Integer.toString(reminder.id));

            Preference oldReminder = new Preference(this);
             // oldReminder.setKey("junk_old_reminder_" + Integer.toString(i)); // FIXME

            oldReminder.setTitle(Util.toHumanTime(this, reminder.hour, reminder.minute));
            Preference.OnPreferenceClickListener listener =
                    new OnReminderTimePreferenceClickListener(reminder.id) {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent in = new Intent(PreferencesActivity.this, ReminderPreferencesActivity.class);
                    in.putExtra(BundleExtraKey.REMINDER_MODE, PreferenceEditMode.REMINDER_CHANGE);
                    in.putExtra(BundleExtraKey.REMINDER_TIME_ID, this.reminderTimeId);
                    Log.d(LOG_PREFIX, "Sending reminderTimeId: " + Integer.toString(this.reminderTimeId));
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
                    Intent in = new Intent(PreferencesActivity.this, ReminderPreferencesActivity.class);
                    in.putExtra(BundleExtraKey.REMINDER_MODE, PreferenceEditMode.REMINDER_CREATE);
                    startActivityForResult(in, PreferenceEditMode.REMINDER_CREATE);
                    return true;
                }
            };
        newReminderButton.setOnPreferenceClickListener(listener);
        addCategory.addPreference(newReminderButton);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_PREFIX, "Enter onActivityResult");
        Log.d(LOG_PREFIX, "requestCode " + Integer.toString(requestCode) +
                ", resultCode " + Integer.toString(resultCode));

        if (resultCode != RESULT_OK) {
            Log.d(LOG_PREFIX, "Bad resultCode: do nothing");
            Toast.makeText(this, this.getString(R.string.toast_cancelled), Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = data.getExtras();
        Log.d(LOG_PREFIX, "Received bundle!");

        switch (requestCode) {
            case PreferenceEditMode.REMINDER_CREATE:
                Log.d(LOG_PREFIX, "CREATE reminder");
                Util.addReminder(this, bundle);
                prefReminders.removeAll();
                createReminderPreferences();
                break;

            case PreferenceEditMode.REMINDER_CHANGE:
                Log.d(LOG_PREFIX, "UPDATE reminder");
                Util.updateReminder(this, bundle);
                prefReminders.removeAll();
                createReminderPreferences();
                break;

            default:
                Log.d(LOG_PREFIX, "Unknown request: " + Integer.toString(requestCode));
                break;
        }
    }
}