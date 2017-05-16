package nu.ere.mooddiary;

import android.preference.Preference;

class OnMeasurementPreferenceClickListener implements Preference.OnPreferenceClickListener {
    public int measurementTypeId = 0;

    public OnMeasurementPreferenceClickListener(int measurementTypeId) {
        this.measurementTypeId = measurementTypeId;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return true;
    }
}


