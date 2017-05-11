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
import org.bostonandroid.preference.TimePreference;

public class ReminderPreferencesActivity extends ThemedPreferenceActivity {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    PreferenceScreen reminderScreen;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventTypes eventTypes = MainActivity.eventTypes;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        // Create the main context
        reminderScreen = getPreferenceManager().createPreferenceScreen(this);

        // Create categories
        PreferenceCategory timeCategory = new PreferenceCategory(this);
        PreferenceCategory typeCategory = new PreferenceCategory(this);
        PreferenceCategory saveCategory = new PreferenceCategory(this);

        timeCategory.setTitle("Time"); // FIXME hardcoded
        typeCategory.setTitle("Event types"); // FIXME hardcoded
        saveCategory.setTitle("Save"); // FIXME hardcoded

        reminderScreen.addPreference(timeCategory);
        reminderScreen.addPreference(typeCategory);
        reminderScreen.addPreference(saveCategory);

        // Add a time view
        TimePreference timePref = new TimePreference(this, null);
        timePref.setTitle("(time should be here)");
        timePref.setKey("reminder_edit_time");
        timeCategory.addPreference(timePref);

        // TODO figure out how to get the damn time

        //timePref.setOnPreferenceChangeListener(listener);
        //addPreferencesFromResource(R.xml.preferences);
        //PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Add checkboxes for all enabled event types, and configure them appropriately if
        // we are editing an existing reminder
        for(int i = 0; i < eventTypes.types.size(); i++) {
            EventType e = eventTypes.types.get(i);
            CheckBoxPreference cb = new CheckBoxPreference(this);
            cb.setKey(Long.toString(e.id)); // FIXME should load state from db / reminder obj, not event types
            cb.setTitle(e.name);
            cb.setChecked(e.enabled == 1); // FIXME should load state from db / reminder obj, not event types
            typeCategory.addPreference(cb);
            Log.d("PreferenceActivity", "ITERATE EventType");
        }

        // Add save button (hitting 'back' without a save should just cancel)
        Preference saveButton = new Preference(this);
        saveButton.setTitle(R.string.submit);
        saveButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Save data to database
                // TODO Reload
                //timePref.onActivityDestroy();
                ReminderPreferencesActivity.this.finish();
                return true;
            }
        });

        saveCategory.addPreference(saveButton);
        setPreferenceScreen(reminderScreen);
    }
}