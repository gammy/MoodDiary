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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;

// So stupid..
public class SaveClickListener implements OnClickListener {
    private static final String LOG_PREFIX = "SaveClickListener";

    private Activity activity;
    private TextView view;

    private Animation in, out;
    private SaveSplash splash;

    private ArrayList<MeasurementType> measurementTypes = null;

    public SaveClickListener(Activity activity,
                             ArrayList<MeasurementType> measurementTypes,
                             TextView view,
                             boolean harakiri) {
        Log.d(LOG_PREFIX, "Enter SaveClickListener");


        this.activity = activity;
        this.view = view;
        this.measurementTypes = measurementTypes;

        out = AnimationUtils.loadAnimation(activity, R.anim.text_fade_out); // opaque to invisible
        in  = AnimationUtils.loadAnimation(activity, R.anim.text_fade_in); // invisible to opaque

        splash = new SaveSplash();
        splash.animation = out;
        splash.view = view;
        splash.activity = harakiri ? activity : null;
    }

    @Override
    public void onClick(View v)
    {
        Log.d(LOG_PREFIX, "CLICK, save!");
        Log.d(LOG_PREFIX, "Number of types to save: " + Integer.toString(measurementTypes.size()));

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        for(int i = 0; i < measurementTypes.size(); i++) {
            MeasurementType measurementType = measurementTypes.get(i);
            editor.remove(measurementType.name);

            Log.d(LOG_PREFIX, " mType " + Integer.toString(measurementType.id) + ", " +
                "View " + measurementType.view.toString());

        }
        Util.saveEvents(activity, measurementTypes);
        editor.apply();

        in.setAnimationListener(splash);
        view.startAnimation(in);
        view.setVisibility(View.VISIBLE);

        //Util.resetEntries(activity);
    }
}
