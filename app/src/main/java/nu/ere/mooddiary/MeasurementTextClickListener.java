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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nu.ere.mooddiary;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MeasurementTextClickListener implements OnClickListener {
    public Activity activity;
    public MeasurementType measurementType;
    public int dialogThemeID;
    public TextView textView = null;

    public MeasurementTextClickListener(Activity activity,
                                        TextView textView,
                                        MeasurementType measurementType,
                                        int dialogThemeID) {
        this.activity = activity;
        this.textView = textView;
        this.measurementType = measurementType;
        this.dialogThemeID = dialogThemeID;
    }

    @Override
    public void onClick(View v)
    {
        CustomDialogs.showNumberDialog(activity, textView, measurementType, dialogThemeID);
    }


}
