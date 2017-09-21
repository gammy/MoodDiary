/* Mood Diary, a free Android mood tracker
 * Copyright (C) 2017 Kristian Gunstone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nu.ere.mooddiary;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.TextView;

class DialogNumberClickListener implements DialogInterface.OnClickListener {
    private static final String LOG_PREFIX = "DialogNumberClickLis..";

    private TextView view = null;

    private NumberPicker numberPicker = null;
    private Activity activity = null;
    private MeasurementType measurementType = null;

    public DialogNumberClickListener(Activity activity, NumberPicker numberPicker) {
        this.activity = activity;
        this.numberPicker = numberPicker;
    }

    public void onClick(DialogInterface dialog, int which) {
        Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "Enter onClick");
        Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "Which = " + Integer.toString(which));

        String value = Integer.toString(numberPicker.getValue() + measurementType.min);
        Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "Value = " + value);

        switch (which)
        {
            case DialogInterface.BUTTON_POSITIVE:
                // Ugly, but it works. This saves a single entry from the main view
                if(view == null) {
                    Util.saveSingleEntry(activity, measurementType, value);
                }
                setViewText(view, value);
                break;

            default:
                break;
        }
    }

    public void setView(TextView view) {
        this.view = view;
    }

    public void setMeasurementType(MeasurementType measurementType) {
        this.measurementType = measurementType;
    }

    private void setViewText(TextView view, String text) {
        if(view == null) {
            return;
        }

        view.setText(text);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(measurementType.name, numberPicker.getValue());
        editor.apply();
    }
}

