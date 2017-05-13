package nu.ere.mooddiary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.WindowId;
import nu.ere.mooddiary.Util;

// Note: Any key prefixed with "junk_" will *not* be used by the app, and is considered a
//       necessary evil.

public class PreferencesActivity extends ThemedPreferenceActivity {
    private static final String LOG_PREFIX = "PreferencesActivity";
    private ORM orm;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    PreferenceScreen prefReminders,
                     prefEventTypes;

    public int preferenceTimeIDToEdit = -1; // FIXME hack omg

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

        createEventTypePreferences(); // When user clicks Settings -> Event Types, this is shown
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

        EventTypes eventTypes = orm.getEventTypes();

        for(int i = 0; i < eventTypes.types.size(); i++) {
            EventType e = eventTypes.types.get(i);
            CheckBoxPreference cb = new CheckBoxPreference(this);
            cb.setKey("visible_event_" + Long.toString(e.id));
            cb.setTitle(e.name);
            cb.setChecked(e.enabled == 1);
            prefEventTypes.addPreference(cb);
            Log.d(LOG_PREFIX, "ITERATE EventType");
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
            Preference oldReminder = new Preference(this);
            this.preferenceTimeIDToEdit = reminder.id;
            oldReminder.setKey("junk_old_reminder_" + Integer.toString(i)); // FIXME
            oldReminder.setTitle(Util.toHumanTime(this, reminder.hour, reminder.minute));
            oldReminder.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent in = new Intent(PreferencesActivity.this, ReminderPreferencesActivity.class);
                    in.putExtra("newReminder", false);
                    in.putExtra("reminderTimeID", PreferencesActivity.this.preferenceTimeIDToEdit); // FIXME awkward
                    startActivityForResult(in, 1338); // FIXME const - 1338 - EDIT
                    return true;
                }
            });
            oldCategory.addPreference(oldReminder);
        }

        Preference newReminderButton = new Preference(this);
        newReminderButton.setTitle(R.string.action_preference_reminders);
        newReminderButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Finally we load Level 3, the add/edit dialog. It contains:
                // - Time view / select dialog
                // - List of event type checkboxes
                // - Save button
                // onActivityResult in this class will be called on save or cancel/back
                //code for what you want it to do
                Intent in = new Intent(PreferencesActivity.this, ReminderPreferencesActivity.class);
                in.putExtra("newReminder", true);
                startActivityForResult(in, 1337); // FIXME const
                return true;
            }
        });

        addCategory.addPreference(newReminderButton);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_PREFIX, "Enter onActivityResult");
        Log.d(LOG_PREFIX, "requestCode " + Integer.toString(requestCode) +
                ", resultCode " + Integer.toString(resultCode));

        if(resultCode != RESULT_OK) {
            Log.d(LOG_PREFIX, "Bad resultCode: do nothing");
            return;
        }

        if(requestCode == 1337) { // Create (FIXME const)
            Log.d(LOG_PREFIX, "result: OK - CREATE reminder");
        } else
        if (requestCode == 1338){ // Edit (FIXME const)
            Log.d(LOG_PREFIX, "result: OK - UPDATE reminder");

        }
    }
}