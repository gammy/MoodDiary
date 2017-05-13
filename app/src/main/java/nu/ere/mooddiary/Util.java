package nu.ere.mooddiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.view.Gravity;

import java.util.ArrayList;

public class Util {
    private static final String LOG_PREFIX = "MainActivity";

    /**
     *
     * @param activity The calling activity (i.e `this` in your Activity)
     * @param eventType
     * @param dialogThemeID Theme (style) id to pass to any dialog click listeners
     * @return
     */
    public static NumberPicker showNumberDialog(Activity activity,
                                                EventType eventType,
                                                int dialogThemeID){
        TextView view = (TextView) eventType.view;
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

    /**
     * Generate the entire page layout for a Reminder.
     * This involves creating the encasing widgets (tables, etc), instantiating Views based on
     * event type primitives, and rendering everything.
     *
     * @param activity The calling activity (i.e `this` in your Activity)
     * @param layout The layout resource to be used as main container where all Views are created
     * @param dialogThemeID Theme (style) id to pass to any dialog click listeners
     */
    public static void renderEntryTypes(Activity activity, int layout, int dialogThemeID) {
        Log.d(LOG_PREFIX, "Enter renderEntryTypes");
        Resources resources = activity.getResources();
        ORM orm = ORM.getInstance(activity);

        // Get our main (scrollable) view, where we are to programmatically add our EntryTypes
        LinearLayout entryLayout = (LinearLayout) activity.findViewById(layout);

        // Create a table
        TableLayout table = new TableLayout(activity);
        table.setColumnStretchable(1, true); // Stretch the rightmost column (holding sliders etc)

        TableRow rowTitle = new TableRow(activity);
        rowTitle.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        TableRow.LayoutParams rowParams = new TableRow.LayoutParams();
        rowParams.span = 1;

        rowParams.topMargin    = (int) resources.getDimension(R.dimen.entry_padding_top);
        rowParams.bottomMargin = (int) resources.getDimension(R.dimen.entry_padding_bottom);

        // Walk our event types and create the appropriate text and entry widget (slider, etc).
        // Add them to the main layout.
        for(int i = 0; i < orm.getEventTypes().types.size(); i++) {
            EventType etype = orm.getEventTypes().types.get(i);
            EntityPrimitive primitive = etype.getPrimitive(orm.getPrimitives());

            // Make a label
            TextView label = new TextView(activity);
            TextViewCompat.setTextAppearance(label,
                    android.R.style.TextAppearance_DeviceDefault_Small);
            label.setGravity(Gravity.START);
            label.setText(etype.name);

            TableRow row = new TableRow(activity);
            // row.setBackgroundColor(Color.BLUE); // (debugging)
            row.addView(label, rowParams);

            // Make the appropriate widget
            switch(primitive.name) {

                case "range_center":
                case "range_normal":
                    SeekBar seekBar = new SeekBar(activity);
                    etype.setView(seekBar);
                    // The drawable resource name (i.e 'res/drawable/range_center.xml') matches
                    // the database EntityPrimitive name.
                    int styleID = resources.getIdentifier(primitive.name,
                            "drawable", activity.getPackageName());
                    seekBar.setProgressDrawable(
                            ResourcesCompat.getDrawable(resources, styleID, null));
                    seekBar.setMax((int) etype.totalValues);
                    seekBar.setProgress((int) etype.normalDefault);
                    row.addView(seekBar, rowParams);
                    break;

                case "number":
                    TextView number = new TextView(activity);
                    //number.setPaintFlags(number.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    //number.setBackground(android:background="?attr/editTextBackground"

                    /* Can't find an easier way to do this - insane */
                    int[] attrs = new int[] { R.attr.editTextBackground};
                    TypedArray ta = activity.obtainStyledAttributes(attrs);
                    Drawable drawableFromTheme = ta.getDrawable(0);
                    ta.recycle();
                    number.setBackgroundDrawable(drawableFromTheme);

                    //TextInputEditText number = new TextInputEditText(this);
                    etype.setView(number);
                    number.setGravity(Gravity.CENTER_HORIZONTAL);
                    TextViewCompat.setTextAppearance(number,
                            android.R.style.TextAppearance_DeviceDefault_Medium);
                    //number.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);

                    number.setText(Long.toString(etype.normalDefault));
                    EventNumberClickListener listener =
                            new EventNumberClickListener(activity, etype, dialogThemeID);
                    number.setOnClickListener(listener);
                    row.addView(number, rowParams);
                    break;

                case "text":
                    TextInputEditText text = new TextInputEditText(activity);
                    etype.setView(text);
                    row.addView(text, rowParams);
                    break;

                default:
                    break;

            }

            table.addView(row);
        }

        entryLayout.addView(table);
    }

    /**
     * Walk through all event types and reset (zero) any Views.
     * Expected to be called after the views have actually been instantiated
     * (i.e via renderEntryTypes)
     *
     * @param activity The calling activity (i.e `this` in your Activity)
     */
    public static void resetEntries(Activity activity) {
        Log.d(LOG_PREFIX, "Enter resetEntries");
        Resources resources = activity.getResources();
        ORM orm = ORM.getInstance(activity);

        int evCount = orm.getEventTypes().types.size();

        for (int i = 0; i < evCount; i++) {
            EventType etype = orm.getEventTypes().types.get(i);
            Log.d(LOG_PREFIX, "resetEntries: to reset etype " +
                    Long.toString(etype.id) + ", view id " + Long.toString(etype.view.getId()));


            switch (etype.getPrimitive(orm.getPrimitives()).name) {
                case "range_center":
                case "range_normal":
                    SeekBar seekBar = (SeekBar) etype.view;
                    seekBar.setMax((int) etype.totalValues);
                    seekBar.setProgress((int) etype.normalDefault);
                    break;

                case "text":
                    TextInputEditText textInputEditText = (TextInputEditText) etype.view;
                    textInputEditText.setText("");
                    break;

                case "number":
                default:
                    TextView textView = (TextView) etype.view;
                    textView.setText(Long.toString(etype.normalDefault));
                    break;
            }
        }
    }

    public static void saveEvents(Activity activity) {
        Log.d(LOG_PREFIX, "Enter saveEvents");
        ORM orm = ORM.getInstance(activity);

        ArrayList<Entry> entries = new ArrayList<>();
        int evCount = orm.getEventTypes().types.size();

        // - Save widget data down to the entrylist
        for(int i = 0; i < evCount; i++) {
            EventType etype = orm.getEventTypes().types.get(i);
            String value;

            // Parse
            switch(etype.getPrimitive(orm.getPrimitives()).name) {
                case "range_center":
                case "range_normal":
                    value = Integer.toString(((SeekBar) etype.view).getProgress());
                    value = Long.toString(etype.min + Long.parseLong(value, 10));
                    break;

                case "text":
                    value = ((TextInputEditText) etype.view).getText().toString();
                    break;

                case "number":
                default:
                    value = ((TextView) etype.view).getText().toString();
                    value = Long.toString(etype.min + Long.parseLong(value, 10));
                    break;
            }

            // Add
            entries.add(new nu.ere.mooddiary.Entry((int) etype.id, value));
        }

        orm.lastSave = System.currentTimeMillis();
        orm.addEntries(entries, true);
    }

}
