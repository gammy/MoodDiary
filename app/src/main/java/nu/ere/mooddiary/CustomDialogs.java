package nu.ere.mooddiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

public class CustomDialogs {
    private static final String LOG_PREFIX = "CustomDialog";

    public static void showDialog(Activity activity,
                                  TextInputEditText view,
                                  MeasurementType measurementType,
                                  int dialogThemeID) {

        ORM orm = ORM.getInstance(activity);
        EntityPrimitive primitive = measurementType.getPrimitive(orm.getPrimitives());

        switch(primitive.name) {
            case "range_normal":
            case "range_center":
                showNormalRangeDialog(activity, null, measurementType, dialogThemeID, primitive);
                break;

            case "number":
                showNumberDialog(activity, null, measurementType, dialogThemeID);
                break;

            case "text":
                showTextDialog(activity, null, measurementType, dialogThemeID);
                break;
        }
    }

    public static SeekBar showNormalRangeDialog(Activity activity,
                                                SeekBar view,
                                                MeasurementType measurementType,
                                                int dialogThemeID,
                                                EntityPrimitive primitive) {
        Log.d(LOG_PREFIX, "Enter showNormalRangeDialog");
        final SeekBar seekBar = new SeekBar(new ContextThemeWrapper(activity, dialogThemeID));

        // This is stupid: The code assumes that the caller view is a number or somesuch,
        // to set the correct 'default' (startup) value of the number box. But in the MainActivity
        // the caller view is a button, and has nothing to do with any default value; the button
        // has the *name* of the measurement type..
        //if(view != null) {
        //    editText.setText(view.getText().toString()); // FIXME regression with abstraction of TextView
        //}

        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(activity, dialogThemeID));

        DialogRangeClickListener listener = new DialogRangeClickListener(activity, seekBar);
        listener.setView(view);
        listener.setMeasurementType(measurementType);

        builder.setPositiveButton(R.string.submit, listener);
        builder.setNegativeButton(R.string.cancel, listener);
        builder.setView(seekBar);

        AlertDialog dialog = builder.create();

        dialog.setTitle(measurementType.name);
        dialog.show();

        // The drawable resource name (i.e 'res/drawable/range_center.xml') matches
        // the database EntityPrimitive name.
        Resources resources = activity.getResources();
        Log.d(LOG_PREFIX, "primitive: " + primitive.name);
        int styleID = resources.getIdentifier(primitive.name, "drawable", activity.getPackageName());
        Log.d(LOG_PREFIX, "styleID: " + Integer.toString(styleID));
        seekBar.setProgressDrawable(ResourcesCompat.getDrawable(resources, styleID, null));
        seekBar.setMax(measurementType.totalValues);
        seekBar.setProgress(measurementType.normalDefault);

        return(seekBar);
    }

    public static EditText showTextDialog(Activity activity,
                                          EditText view,
                                          MeasurementType measurementType,
                                          int dialogThemeID) {
        Log.d(LOG_PREFIX, "Enter showTextDialog");
        final EditText editText =
                new EditText(new ContextThemeWrapper(activity, dialogThemeID));

        // This is stupid: The code assumes that the caller view is a number or somesuch,
        // to set the correct 'default' (startup) value of the number box. But in the MainActivity
        // the caller view is a button, and has nothing to do with any default value; the button
        // has the *name* of the measurement type..
        if(view != null) {
            editText.setText(view.getText().toString()); // FIXME regression with abstraction of TextView
        }

        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(activity, dialogThemeID));

        DialogTextClickListener listener = new DialogTextClickListener(activity, editText);
        listener.setView(view);
        listener.setMeasurementType(measurementType);

        builder.setPositiveButton(R.string.submit, listener);
        builder.setNegativeButton(R.string.cancel, listener);
        builder.setView(editText);

        AlertDialog dialog = builder.create();

        dialog.setTitle(measurementType.name);
        dialog.show();

        return(editText);
    }
    /**
     *
     * @param activity The calling activity (i.e `this` in your Activity)
     * @param measurementType
     * @param dialogThemeID Theme (style) id to pass to any dialog click listeners
     * @return
     */
    public static NumberPicker showNumberDialog(Activity activity,
                                                TextView view,
                                                MeasurementType measurementType,
                                                int dialogThemeID){
        Log.d(LOG_PREFIX, "Enter showNumberDialog");
        Log.d(LOG_PREFIX, "dialogThemeID: " + Integer.toString(dialogThemeID));
        final NumberPicker numberPicker =
                new NumberPicker(new ContextThemeWrapper(activity, dialogThemeID));

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(measurementType.totalValues);
        // This is stupid: The code assumes that the caller view is a number or somesuch,
        // to set the correct 'default' (startup) value of the number box. But in the MainActivity
        // the caller view is a button, and has nothing to do with any default value; the button
        // has the *name* of the measurement type..
        if(view != null) {
            numberPicker.setValue(Integer.parseInt(view.getText().toString())); // FIXME regression with abstraction of TextView
        }
        numberPicker.setWrapSelectorWheel(false);

        // FIXME numberPicker styling (font size) regression
        /*
        numberPicker.setLayoutParams(new RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.MATCH_PARENT));
        */

        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(activity, dialogThemeID));

        DialogNumberClickListener listener = new DialogNumberClickListener(activity, numberPicker);
        listener.setView(view);
        listener.setMeasurementType(measurementType);

        builder.setPositiveButton(R.string.submit, listener);
        builder.setNegativeButton(R.string.cancel, listener);
        builder.setView(numberPicker);

        AlertDialog dialog = builder.create();

        dialog.setTitle(measurementType.name);
        dialog.show();

        return(numberPicker);
    }
}
