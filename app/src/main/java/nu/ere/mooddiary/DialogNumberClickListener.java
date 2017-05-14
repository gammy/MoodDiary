package nu.ere.mooddiary;

import android.app.Activity;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

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
                setViewText(view, value);
                break;
            default:
                break;
        }

        // Ugly, but it works. This saves a single entry from the main view
        if(view == null) {
            Entry entry = new Entry(measurementType.id, value);
            ORM orm = ORM.getInstance(activity);
            ArrayList<Entry> entryList = new ArrayList<>();
            entryList.add(entry);
            orm.addEntries(entryList, true);
            String text = activity.getString(R.string.toast_saved);
            Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
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

