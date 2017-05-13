package nu.ere.mooddiary;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

// So stupid..
public class SaveClickListener implements OnClickListener {
    private static final String LOG_PREFIX = "SaveClickListener";

    Activity activity;
    TextView view;

    Animation in, out;
    SaveSplash splash;

    public SaveClickListener(Activity activity, TextView view, boolean harakiri) {
        Log.d(LOG_PREFIX, "Enter SaveClickListener");

        this.activity = activity;
        this.view = view;

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
        Log.d(LOG_PREFIX, "alarm: CLICK");

        Util.saveEvents(activity);

        in.setAnimationListener(splash);
        view.startAnimation(in);
        view.setVisibility(View.VISIBLE);

        Util.resetEntries(activity);
    }
}
