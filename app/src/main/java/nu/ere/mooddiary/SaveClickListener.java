package nu.ere.mooddiary;

import android.app.Activity;
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

        for(int i = 0; i < measurementTypes.size(); i++) {
            MeasurementType measurementType = measurementTypes.get(i);
            Log.d(LOG_PREFIX, " mType " + Integer.toString(measurementType.id) + ", " +
                "View " + measurementType.view.toString());

        }
        Util.saveEvents(activity, measurementTypes);

        in.setAnimationListener(splash);
        view.startAnimation(in);
        view.setVisibility(View.VISIBLE);

        //Util.resetEntries(activity);
    }
}
