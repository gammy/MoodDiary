package nu.ere.mooddiary;

import android.preference.Preference;

class OnReminderTimePreferenceClickListener implements Preference.OnPreferenceClickListener {
    public int reminderTimeId = 0;

    public OnReminderTimePreferenceClickListener(int reminderTimeId) {
        this.reminderTimeId = reminderTimeId;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return true;
    }
}


