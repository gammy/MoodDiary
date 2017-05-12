/* New Reminder menu, containing a list of event types, a time, and a save button */
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
    private ORM orm;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    PreferenceScreen reminderScreen;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventTypes eventTypes = orm.eventTypes;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        // Create the main context
        reminderScreen = getPreferenceManager().createPreferenceScreen(this);

        // Create categories
        PreferenceCategory timeCategory = new PreferenceCategory(this);
        PreferenceCategory typeCategory = new PreferenceCategory(this);
        PreferenceCategory saveCategory = new PreferenceCategory(this);

        timeCategory.setTitle(getApplicationContext().getString(R.string.title_category_time));
        typeCategory.setTitle(getApplicationContext().getString(R.string.title_category_types));
        saveCategory.setTitle(getApplicationContext().getString(R.string.title_category_save));

        reminderScreen.addPreference(timeCategory);
        reminderScreen.addPreference(typeCategory);
        reminderScreen.addPreference(saveCategory);

        // Add a time view
        TimePreference timePref = new TimePreference(this, null);
        timePref.setKey("junk_reminder_edit_time");
        timeCategory.addPreference(timePref);

        // Add checkboxes for all enabled event types, and configure them appropriately if
        // we are editing an existing reminder
        for(int i = 0; i < eventTypes.types.size(); i++) {
            EventType e = eventTypes.types.get(i);
            CheckBoxPreference cb = new CheckBoxPreference(this);
            cb.setKey("junk_" + Long.toString(e.id)); // FIXME should load state from db / reminder obj, not event types
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
                // TODO collate all the stupid properties
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