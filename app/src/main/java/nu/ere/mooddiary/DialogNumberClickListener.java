package nu.ere.mooddiary;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.TextView;

class DialogNumberClickListener implements DialogInterface.OnClickListener {
    private static final String LOG_PREFIX = "DialogNumberClickLis..";

    private TextView view = null;

    private NumberPicker numberPicker = null;
    private Activity activity = null;
    private MeasurementType measurementType = null;

    public DialogNumberClickListener(Activity activity, NumberPicker numberPicker) {
        this.activity = activity;
        this.numberPicker = numberPicker;
    }

    public void onClick(DialogInterface dialog, int which) {
        Log.d(LOG_PREFIX, "Enter onClick");
        Log.d(LOG_PREFIX, "Which = " + Integer.toString(which));

        String value = Integer.toString(numberPicker.getValue());

        switch (which)
        {
            case DialogInterface.BUTTON_POSITIVE:
                // Ugly, but it works. This saves a single entry from the main view
                if(view == null) {
                    Util.saveSingleEntry(activity, measurementType, value);
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
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(measurementType.name, numberPicker.getValue());
        editor.apply();
    }
}

