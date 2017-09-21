/* Mood Diary, a free Android mood tracker
 * Copyright (C) 2017 Kristian Gunstone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nu.ere.mooddiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

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

            case "toggle":
                showToggleDialog(activity, null, measurementType, dialogThemeID);
                break;
        }
    }

    public static SeekBar showNormalRangeDialog(Activity activity,
                                                SeekBar view,
                                                MeasurementType measurementType,
                                                int dialogThemeID,
                                                EntityPrimitive primitive) {
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter showNormalRangeDialog");
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
        Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "primitive: " + primitive.name);
        int styleID = resources.getIdentifier(primitive.name, "drawable", activity.getPackageName());
        Util.log(Util.LOGLEVEL_2, LOG_PREFIX, "styleID: " + Integer.toString(styleID));
        seekBar.setProgressDrawable(ResourcesCompat.getDrawable(resources, styleID, null));
        seekBar.setMax(measurementType.totalValues);
        seekBar.setProgress(measurementType.normalDefault);

        return(seekBar);
    }

    public static EditText showTextDialog(Activity activity,
                                          EditText view,
                                          MeasurementType measurementType,
                                          int dialogThemeID) {
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter showTextDialog");
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
     * @param view
     * @param measurementType
     * @param dialogThemeID Theme (style) id to pass to any dialog click listeners
     * @return NumberPicker
     */
    public static NumberPicker showNumberDialog(Activity activity,
                                                TextView view,
                                                final MeasurementType measurementType,
                                                int dialogThemeID){
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter showNumberDialog");
        Util.log(Util.LOGLEVEL_2, LOG_PREFIX, "dialogThemeID: " + Integer.toString(dialogThemeID));
        final NumberPicker numberPicker =
                new NumberPicker(new ContextThemeWrapper(activity, dialogThemeID));

        // The NumberPicker only likes values from 0 and up. We can work around this for negative
        // values by displaying an array of strings with whatever we like. The alternative is to
        // override the NumberPicker formatter but I can't get that to work properly. So fuck it.
        String[] printValues = new String[measurementType.totalValues + 1];
        for(int i = 0; i < measurementType.totalValues + 1; i++) {
            printValues[i] = String.format("%d", i + measurementType.min);
        }

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(measurementType.totalValues);

        int value = measurementType.dfl - measurementType.min;
        if(view != null) {
            // Set the previously set (non-default) value in the view
            //value = Integer.parseInt(view.getText().toString()) - measurementType.min
            Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "Previous Value string from view: " + view.getText().toString());
            value = Integer.parseInt(view.getText().toString()) - measurementType.min;
            Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "Setting Value to: " + Integer.toString(value));
        }

        Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "Value = " + value);
        numberPicker.setValue(value);

        numberPicker.setDisplayedValues(printValues);
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

    /**
     *
     * @param activity The calling activity (i.e `this` in your Activity)
     * @param view
     * @param measurementType
     * @param dialogThemeID Theme (style) id to pass to any dialog click listeners
     * @return CheckBox
     */
    public static CheckBox showToggleDialog(Activity activity,
                                            CheckBox view,
                                            MeasurementType measurementType,
                                            int dialogThemeID){
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter showToggleDialog");
        Util.log(Util.LOGLEVEL_2, LOG_PREFIX, "dialogThemeID: " + Integer.toString(dialogThemeID));
        final CheckBox checkBox =
                new CheckBox(new ContextThemeWrapper(activity, dialogThemeID));
        checkBox.setGravity(Gravity.END);

        checkBox.setChecked(measurementType.dfl == 1);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(activity, dialogThemeID));

        DialogToggleClickListener listener = new DialogToggleClickListener(activity, checkBox);
        listener.setView(view);
        listener.setMeasurementType(measurementType);

        builder.setPositiveButton(R.string.submit, listener);
        builder.setNegativeButton(R.string.cancel, listener);
        builder.setView(checkBox);

        AlertDialog dialog = builder.create();

        dialog.setTitle(measurementType.name);
        dialog.show();

        return(checkBox);
    }
}
