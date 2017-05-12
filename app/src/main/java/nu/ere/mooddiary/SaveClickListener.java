package nu.ere.mooddiary;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

// So stupid..
public class SaveClickListener implements OnClickListener {
    Activity activity;
    TextView view;

    Animation in, out;
    SaveSplash splash;

    public SaveClickListener(Activity activity, TextView view) {

        this.activity = activity;
        this.view = view;

        out = AnimationUtils.loadAnimation(activity, R.anim.text_fade_out); // opaque to invisible
        in  = AnimationUtils.loadAnimation(activity, R.anim.text_fade_in); // invisible to opaque

        splash = new SaveSplash();
        splash.animation = out;
        splash.view = view;
    }

    @Override
    public void onClick(View v)
    {
        in.setAnimationListener(splash);
        view.startAnimation(in);
        view.setVisibility(View.VISIBLE);

        // activity.saveEvents(); // Where the magic happens FIXME need activity-agnostic method
        //activity.resetEntries(); // FIXME need activity-agnostic method
    }


}
