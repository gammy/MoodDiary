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
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

// So stupid..
public class SaveSplash implements AnimationListener {

    public Animation animation; // "out"
    public TextView view;
    public Activity activity = null;

    public void onAnimationEnd(Animation animation) {
        animation = this.animation; // Resistance is futile.
        view.startAnimation(animation);
        view.setVisibility(View.INVISIBLE); // "out"
        if(activity != null) {
            Log.d("SaveSplash", "alarm: Bye! Ueaugh");
            ActivityCompat.finishAffinity(activity);
        }

    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {

    }
}