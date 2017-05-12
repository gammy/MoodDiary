package nu.ere.mooddiary;

import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.TextView;

class DialogNumberClickListener implements DialogInterface.OnClickListener {
    private static final String LOG_PREFIX = "DialogNumberClickLis..";

    private TextView view;
    //private TextInputEditText view;
    private NumberPicker numberPicker;

    public DialogNumberClickListener(TextView view, NumberPicker numberPicker) {
    //public DialogNumberClickListener(TextInputEditText view, NumberPicker numberPicker) {
        this.view = view;
        this.numberPicker = numberPicker;
    }

    public void onClick(DialogInterface dialog, int which) {
        Log.d(LOG_PREFIX, "Enter onClick");
        Log.d(LOG_PREFIX, "Which = " + Integer.toString(which));

        switch (which)
        {
            case DialogInterface.BUTTON_POSITIVE:
                view.setText(Integer.toString(numberPicker.getValue()));
                break;
            default:
                break;
        }
    }
}

