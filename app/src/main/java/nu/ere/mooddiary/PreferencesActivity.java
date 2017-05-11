package nu.ere.mooddiary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

public class PreferencesActivity extends ThemedPreferenceActivity {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    PreferenceScreen prefReminders,
                     prefEventTypes;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        Log.d("PreferenceActivity", "Enter createEventTypePreferences");

        EventTypes eventTypes = MainActivity.eventTypes;

        for(int i = 0; i < eventTypes.types.size(); i++) {
            EventType e = eventTypes.types.get(i);
            CheckBoxPreference cb = new CheckBoxPreference(this);
            cb.setKey(Long.toString(e.id));
            cb.setTitle(e.name);
            cb.setChecked(e.enabled == 1);
            prefEventTypes.addPreference(cb);
            Log.d("PreferenceActivity", "ITERATE EventType");
        }
    }

    // "Level 2"
    public void createReminderPreferences() {
        Log.d("PreferenceActivity", "Enter createReminderPreferences");

        // Make a list of existing reminders - each entry can be clicked to open up a new/edit submenu
        //Reminders r = MainActivity.reminders;
        //for (int i = 0; i < r.reminders.size(); i++) {
        //    Log.d("createReminderP..", "ITERATE Reminder");
        //}

        // Add a button to add another reminder, which also opens a new/edit submenu
        /*
        TimePreference tp = new TimePreference(this);
        tp.setTitle("Add a reminder");
        tp.setKey("new_reminder");

        Preference.OnPreferenceChangeListener listener =
            new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Code goes here
                //    this.setTitle("lol");
                    return true;
                }
            };
        tp.setOnPreferenceChangeListener(listener);
        */

        Preference newReminderButton = new Preference(this);
        newReminderButton.setTitle(R.string.action_preference_reminders);
        newReminderButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Finally we load Level 3, the add/edit dialog. It contains:
                // - Time view / select dialog
                // - List of event type checkboxes
                // - Save button, taking the user back to reminderPreferences
                //code for what you want it to do
                Intent i = new Intent(PreferencesActivity.this, ReminderPreferencesActivity.class);
                startActivity(i);

                return true;
            }
        });

        this.prefReminders.addPreference(newReminderButton);
        //newFragment.show(getSupportFragmentManager(), "timePicker");
    }

}