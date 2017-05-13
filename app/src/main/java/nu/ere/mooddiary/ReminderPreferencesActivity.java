/* New Reminder menu, containing a list of event types, a time, and a save button */
package nu.ere.mooddiary;

import android.app.Activity;
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

import java.util.Calendar;
import java.util.Date;

public class ReminderPreferencesActivity extends ThemedPreferenceActivity {
    private static final String LOG_PREFIX = "ReminderPref..Activity";
    private ORM orm;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    PreferenceScreen reminderScreen;

    private int oldID = -1;

    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Enter onCreate");
        super.onCreate(savedInstanceState);
        orm = orm.getInstance(this);

        Reminder reminder = null;

        boolean makeNew = getIntent().getBooleanExtra("newReminder", false);
        if(makeNew) {
            Log.d(LOG_PREFIX, "Our mission: CREATE reminder");
        } else {
            Log.d(LOG_PREFIX, "Our mission: EDIT reminder");
            oldID = getIntent().getIntExtra("reminderTimeID", -1);
            reminder = orm.getReminderTimes().getReminderByID(oldID);
        }

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
        if(reminder != null) {
            timePref.setTitle(Util.toHumanTime(this, reminder.hour, reminder.minute));
        } else {
            // Set a brand-new date to one hour from now
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.MINUTE, 60);
            timePref.setTitle(Util.toHumanTime(this,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE)));
            // FIXME set actual time also (in the dialog)
        }
        timeCategory.addPreference(timePref);

        // Whether we're creating or changing, we need to view *all* of the event types available.
        // later on, if changing, get all the associated reminderGroups. Then, when rendering
        // the Views, set each default value to whatever they were
        EventTypes eventTypes = orm.getEventTypes();
        // prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // editor = prefs.edit();

        // Add checkboxes for all enabled event types, and configure them appropriately if
        // we are editing an existing reminder
        for(int i = 0; i < eventTypes.types.size(); i++) {
            EventType e = eventTypes.types.get(i);
            CheckBoxPreference cb = new CheckBoxPreference(this);
            cb.setKey("junk_" + Long.toString(e.id)); // FIXME should load state from db / reminder obj, not event types
            cb.setTitle(e.name);
            cb.setChecked(e.enabled == 1); // FIXME should load state from db / reminder obj, not event types
            typeCategory.addPreference(cb);
            Log.d(LOG_PREFIX, "ITERATE EventType");
        }

        // Add save button (hitting 'back' without a save should just cancel)
        Preference saveButton = new Preference(this);
        saveButton.setTitle(R.string.submit);
        saveButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent rIntent = getIntent();
                setResult(Activity.RESULT_OK, rIntent);
                finish();
                return true;
            }
        });

        saveCategory.addPreference(saveButton);
        setPreferenceScreen(reminderScreen);
    }

}