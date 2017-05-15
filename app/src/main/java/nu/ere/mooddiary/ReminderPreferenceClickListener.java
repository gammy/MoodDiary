package nu.ere.mooddiary;

import android.preference.Preference;

class OnReminderTimePreferenceClickListener implements Preference.OnPreferenceClickListener {
    public int reminderTimeID = 0;

    public OnReminderTimePreferenceClickListener(int reminderTimeID) {
        this.reminderTimeID = reminderTimeID;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return true;
    }
}


