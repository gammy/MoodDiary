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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import java.util.ArrayList;

public class SaveClickListener implements OnClickListener {
    private static final String LOG_PREFIX = "SaveClickListener";

    private AnimationSet animationSet;
    private Activity activity;
    private ImageView view;
    ArrayList<MeasurementType> measurementTypes;

    public SaveClickListener(Activity activity,
                             ArrayList<MeasurementType> measurementTypes,
                             ImageView view) {
        Log.d(LOG_PREFIX, "Enter SaveClickListener");

        this.activity = activity;
        this.measurementTypes = measurementTypes;
        this.view = view;

        animationSet = new AnimationSet(true);
        animationSet.addAnimation(animationBegin());
        animationSet.addAnimation(animationEnd());
    }

    @Override
    public void onClick(View v) {
        Log.d(LOG_PREFIX, "CLICK, save!");
        view.startAnimation(this.animationSet);
    }

    private AnimationSet animationBegin() {
        Animation alpha = new AlphaAnimation(0.0f, 1.0f);
        Animation scale = new ScaleAnimation(
                0.1f, 2.0f,
                0.1f, 2.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        Animation translate = new TranslateAnimation(
                0.5f, 0.5f,
                0.5f, -140.0f);

        alpha.setDuration(400);
        scale.setDuration(650);
        translate.setDuration(650);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(alpha);
        set.addAnimation(scale);
        set.addAnimation(translate);

        return set;
    }

    private AnimationSet animationEnd() {

        Animation alpha = new AlphaAnimation(1.0f, 0.0f);
        Animation scale = new ScaleAnimation(
                2.0f, 1.5f,
                2.0f, 1.5f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);

        AnimationSet set = new AnimationSet(true);

        alpha.setDuration(200);
        scale.setDuration(200);

        set.addAnimation(alpha);
        set.addAnimation(scale);

        ReminderSaveAnimationListener outAnimationListener = new ReminderSaveAnimationListener();
        outAnimationListener.activity = activity;
        outAnimationListener.measurementTypes = measurementTypes;

        set.setStartOffset(650);
        set.setAnimationListener(outAnimationListener);

        return set;
    }
}
