package nu.ere.mooddiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.NumberPicker;
import android.widget.TextView;

public class Util {
    private static final String LOG_PREFIX = "MainActivity";

    public static NumberPicker showNumberDialog(Activity activity,
                                         TextView view,
                                         EventType eventType,
                                         int dialogThemeID){
        Log.d(LOG_PREFIX, "Enter showNumberDialog");
        Log.d(LOG_PREFIX, "dialogThemeID: " + Integer.toString(dialogThemeID));
        final NumberPicker numberPicker =
                new NumberPicker(new ContextThemeWrapper(activity, dialogThemeID));

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue((int) eventType.totalValues);
        numberPicker.setValue(Integer.parseInt(view.getText().toString()));
        numberPicker.setWrapSelectorWheel(false);

        // FIXME numberPicker styling (font size) regression
        /*
        numberPicker.setLayoutParams(new RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.MATCH_PARENT));
        */

        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(activity, dialogThemeID));

        DialogNumberClickListener listener = new DialogNumberClickListener(view, numberPicker);

        builder.setPositiveButton(R.string.submit, listener);
        builder.setNegativeButton(R.string.cancel, listener);
        builder.setView(numberPicker);

        AlertDialog dialog = builder.create();

        dialog.setTitle(eventType.name);
        dialog.show();

        return(numberPicker);
    }

}
