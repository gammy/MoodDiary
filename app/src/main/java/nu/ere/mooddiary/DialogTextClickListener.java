package nu.ere.mooddiary;

import android.app.Activity;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

class DialogTextClickListener implements DialogInterface.OnClickListener {
    private static final String LOG_PREFIX = "DialogTextClickLis..";

    private TextView view = null;

    private EditText editText = null;
    private Activity activity = null;
    private MeasurementType measurementType = null;

    public DialogTextClickListener(Activity activity, EditText editText) {
        this.activity = activity;
        this.editText = editText;
    }

    public void onClick(DialogInterface dialog, int which) {
        Log.d(LOG_PREFIX, "Enter onClick");
        Log.d(LOG_PREFIX, "Which = " + Integer.toString(which));

        String value = editText.getText().toString();

        switch (which)
        {
            case DialogInterface.BUTTON_POSITIVE:
                setViewText(view, value);
                break;
            default:
                break;
        }

        // Ugly, but it works. This saves a single entry from the main view
        if(this.view == null) {
            Util.saveSingleEntry(this.activity, this.measurementType, value);
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
    }
}

