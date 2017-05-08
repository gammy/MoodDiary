package nu.ere.mooddiary;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

public class PreferencesActivity extends PreferenceActivity {
    PreferenceScreen prefReminders,
                     prefEventTypes;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        prefReminders =
                (PreferenceScreen) findPreference("preference_select_reminders");
        prefEventTypes =
                (PreferenceScreen) findPreference("preference_select_event_types");

        createEventTypePreferences();
        createReminderPreferences();
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
            Log.d("createEventTypePr..", "ITERATE EventType");
        }

    }

    public void createReminderPreferences() {
        Log.d("PreferenceActivity", "Enter createReminderPreferences");

        // List a correctly initialized TimePreference for each
        // current Reminder

        Reminders r = MainActivity.reminders;
        for (int i = 0; i < r.reminders.size(); i++) {
            Log.d("createReminderP..", "ITERATE Reminder");
        }


        /*
        Preference button = new Preference(this);
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //code for what you want it to do
                TimePreference tp = new TimePreference(this);
                tp.
                return true;
            }
        });

        */
        // Add a button to add another reminder
        TimePreference tp = new TimePreference(this);
        tp.setTitle("Add a reminder");
        tp.setKey("new_reminder");

        Preference.OnPreferenceChangeListener listener =
            new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Code goes here
                    return true;
                }
            };
        tp.setOnPreferenceChangeListener(listener);

        this.prefReminders.addPreference(tp);
        //newFragment.show(getSupportFragmentManager(), "timePicker");
    }

}