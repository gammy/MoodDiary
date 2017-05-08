package nu.ere.mooddiary;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

// So stupid..
public class SaveSplash implements AnimationListener {

    public Animation animation; // "out"
    public TextView view;

    public void onAnimationEnd(Animation animation) {
        animation = this.animation; // Resistance is futile.
        view.startAnimation(animation);
        view.setVisibility(View.INVISIBLE); // "out"
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public void onAnimationStart(Animation animation) {

    }
}