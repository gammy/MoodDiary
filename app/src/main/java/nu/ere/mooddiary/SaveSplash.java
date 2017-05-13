package nu.ere.mooddiary;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.IntentCompat;
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