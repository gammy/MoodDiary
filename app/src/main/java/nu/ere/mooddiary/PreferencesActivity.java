package nu.ere.mooddiary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

// Note: Any key prefixed with "junk_" will *not* be used by the app, and is considered a
//       necessary evil.

public class PreferencesActivity extends ThemedPreferenceActivity {
    private static final String LOG_PREFIX = "PreferencesActivity";
    private ORM orm;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    PreferenceScreen prefReminders,
                     prefEventTypes;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        prefReminders =
                (PreferenceScreen) findPreference("preference_select_reminders");
        prefEventTypes =
                (PreferenceScreen) findPreference("preference_select_event_types");

        createEventTypePreferences(); // When user clicks Settings -> Measurement Types, this is shown
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
    public void createEventTypePreferences() {
        Log.d(LOG_PREFIX, "Enter createEventTypePreferences");

        MeasurementTypes measurementTypes = orm.getMeasurementTypes();

        for(int i = 0; i < measurementTypes.types.size(); i++) {
            MeasurementType e = measurementTypes.types.get(i);
            CheckBoxPreference cb = new CheckBoxPreference(this);
            cb.setKey("visible_event_" + Long.toString(e.id));
            cb.setTitle(e.name);
            cb.setChecked(e.enabled == 1);
            prefEventTypes.addPreference(cb);
        }
    }

    // "Level 2"
    public void createReminderPreferences() {
        Log.d(LOG_PREFIX, "Enter createReminderPreferences");

        // Create categories
        PreferenceCategory oldCategory = new PreferenceCategory(this);
        PreferenceCategory addCategory = new PreferenceCategory(this);
        oldCategory.setTitle(getApplicationContext().getString(R.string.title_category_old_reminders));
        addCategory.setTitle(getApplicationContext().getString(R.string.title_category_new));

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
                    in.putExtra(BundleExtraKey.REMINDER_MODE, ReminderEditMode.CHANGE);
                    in.putExtra(BundleExtraKey.REMINDER_TIME_ID, this.reminderTimeID);
                    startActivityForResult(in, ReminderEditMode.CHANGE);
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
                    in.putExtra(BundleExtraKey.REMINDER_MODE, ReminderEditMode.CREATE);
                    startActivityForResult(in, ReminderEditMode.CREATE);
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
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show(); // FIXME hardcoded
            return;
        }

        Bundle bundle = data.getExtras();
        Log.d(LOG_PREFIX, "Received bundle!");
        Log.d(LOG_PREFIX, "Hour, Minute = " +
                Integer.toString(bundle.getInt(BundleExtraKey.REMINDER_HOUR)) + ":" +
                Integer.toString(bundle.getInt(BundleExtraKey.REMINDER_MINUTE)));

        switch (requestCode) {
            case ReminderEditMode.CREATE:
                Log.d(LOG_PREFIX, "CREATE reminder");
                Util.addReminder(this, bundle);
                Toast.makeText(this, "TODO: Create", Toast.LENGTH_SHORT).show();
                break;

            case ReminderEditMode.CHANGE:
                Log.d(LOG_PREFIX, "UPDATE reminder");
                Util.updateReminder(this, bundle);
                break;

            default:
                Log.d(LOG_PREFIX, "Unknown request: " + Integer.toString(requestCode));
                break;
        }
    }
}