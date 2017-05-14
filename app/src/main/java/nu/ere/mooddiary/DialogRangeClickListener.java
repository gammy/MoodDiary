package nu.ere.mooddiary;

import android.app.Activity;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

class DialogRangeClickListener implements DialogInterface.OnClickListener {
    private static final String LOG_PREFIX = "DialogRangeClickLis..";

    private SeekBar view = null;
    private SeekBar seekBar = null;
    private Activity activity = null;
    private MeasurementType measurementType = null;

    public DialogRangeClickListener(Activity activity, SeekBar seekBar) {
        this.activity = activity;
        this.seekBar = seekBar;
    }

    public void onClick(DialogInterface dialog, int which) {
        Log.d(LOG_PREFIX, "Enter onClick");
        Log.d(LOG_PREFIX, "Which = " + Integer.toString(which));

        String value = Integer.toString(seekBar.getProgress());
        value = Long.toString(measurementType.min + Long.parseLong(value, 10));

        switch (which)
        {
            case DialogInterface.BUTTON_POSITIVE:
                setViewText(view, "Eh?");
                break;
            default:
                break;
        }

        // Ugly, but it works. This saves a single entry from the main view
        if(this.view == null) {
            Util.saveSingleEntry(this.activity, this.measurementType, value);
        }


    }

    public void setView(SeekBar view) {
        this.view = view;
    }

    public void setMeasurementType(MeasurementType measurementType) {
        this.measurementType = measurementType;
    }

    private void setViewText(SeekBar view, String text) {
        if(view == null) {
            return;
        }

        Log.d(LOG_PREFIX, "setviewText: STUB"); // FIXME or WONTFIX
        //view.setText(text);
    }
}

