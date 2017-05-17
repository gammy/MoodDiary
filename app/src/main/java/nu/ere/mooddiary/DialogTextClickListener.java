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
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

class DialogTextClickListener implements DialogInterface.OnClickListener {
    private static final String LOG_PREFIX = "DialogTextClickLis..";

    private TextView view = null;

    private EditText editText = null;
    private Activity activity = null;
    private MeasurementType measurementType = null;

    public DialogTextClickListener(Activity activity, EditText editText) {
        this.activity = activity;
        this.editText = editText;
    }

    public void onClick(DialogInterface dialog, int which) {
        Log.d(LOG_PREFIX, "Enter onClick");
        Log.d(LOG_PREFIX, "Which = " + Integer.toString(which));

        String value = editText.getText().toString();

        switch (which)
        {
            case DialogInterface.BUTTON_POSITIVE:
                if(view == null) {
                    Util.saveSingleEntry(this.activity, this.measurementType, value);
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
    }
}

