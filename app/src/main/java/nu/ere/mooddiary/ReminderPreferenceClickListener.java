package nu.ere.mooddiary;

import android.preference.Preference;

import java.util.prefs.PreferenceChangeListener;

class OnReminderTimePreferenceClickListener implements Preference.OnPreferenceClickListener {
    public int reminderTimeID = 0;

    public OnReminderTimePreferenceClickListener(int reminderTimeID) {
        this.reminderTimeID = reminderTimeID;
    }

    public int getReminderTimeId() {
        return reminderTimeID;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return true;
    }
}


