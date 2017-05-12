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

// Note: Any key prefixed with "junk_" will *not* be used by the app, and is considered a
//       necessary evil.

public class PreferencesActivity extends ThemedPreferenceActivity {
    private ORM orm;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    PreferenceScreen prefReminders,
                     prefEventTypes;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orm = new ORM();

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

        EventTypes eventTypes = orm.eventTypes;

        for(int i = 0; i < eventTypes.types.size(); i++) {
            EventType e = eventTypes.types.get(i);
            CheckBoxPreference cb = new CheckBoxPreference(this);
            cb.setKey("visible_event_" + Long.toString(e.id));
            cb.setTitle(e.name);
            cb.setChecked(e.enabled == 1);
            prefEventTypes.addPreference(cb);
            Log.d("PreferenceActivity", "ITERATE EventType");
        }
    }

    // "Level 2"
    public void createReminderPreferences() {
        Log.d("PreferenceActivity", "Enter createReminderPreferences");

        // Create categories
        PreferenceCategory oldCategory = new PreferenceCategory(this);
        PreferenceCategory addCategory = new PreferenceCategory(this);
        oldCategory.setTitle(getApplicationContext().getString(R.string.title_category_old_reminders));
        addCategory.setTitle(getApplicationContext().getString(R.string.title_category_new));

        this.prefReminders.addPreference(oldCategory);
        this.prefReminders.addPreference(addCategory);

        // Make a list of existing reminders - each entry can be clicked to open up a new/edit submenu
        for(int i = 0; i < 5; i++) {
            Preference oldReminder = new Preference(this);
            oldReminder.setKey("junk_old_reminder_" + Integer.toString(i));
            //oldReminder.setTitle("<time of old reminder>");
            oldReminder.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Initialize the damn thing somehow
                    Intent i = new Intent(PreferencesActivity.this, ReminderPreferencesActivity.class);
                    startActivity(i);
                    return true;
                }
            });
            oldCategory.addPreference(oldReminder);
        }
;

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

        addCategory.addPreference(newReminderButton);
    }
}