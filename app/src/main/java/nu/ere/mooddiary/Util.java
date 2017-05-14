package nu.ere.mooddiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.TextViewCompat;
import android.text.format.DateUtils;
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
import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Util {
    private static final String LOG_PREFIX = "Util";

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

    /**
     * Generate the entire page layout for a Reminder.
     * This involves creating the encasing widgets (tables, etc), instantiating Views based on
     * event type primitives, and rendering everything.
     *
     * @param activity The calling activity (i.e `this` in your Activity)
     * @param layout The layout resource to be used as main container where all Views are created
     * @param dialogThemeID Theme (style) id to pass to any dialog click listeners
     */
    public static void renderReminderEventTypes(Activity activity,
                                                int reminderGroup, int layout, int dialogThemeID) {
        Log.d(LOG_PREFIX, "Enter renderReminderEventTypes");
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
        for(int i = 0; i < orm.getMeasurementTypes().types.size(); i++) {
            MeasurementType measurementType = orm.getMeasurementTypes().types.get(i);
            EntityPrimitive primitive = measurementType.getPrimitive(orm.getPrimitives());

            // Make a label
            TextView label = new TextView(activity);
            TextViewCompat.setTextAppearance(label,
                    android.R.style.TextAppearance_DeviceDefault_Small);
            label.setGravity(Gravity.START);
            label.setText(measurementType.name);

            TableRow row = new TableRow(activity);
            // row.setBackgroundColor(Color.BLUE); // (debugging)
            row.addView(label, rowParams);

            // Make the appropriate widget
            switch(primitive.name) {

                case "range_center":
                case "range_normal":
                    SeekBar seekBar = new SeekBar(activity);
                    measurementType.setView(seekBar);
                    // The drawable resource name (i.e 'res/drawable/range_center.xml') matches
                    // the database EntityPrimitive name.
                    int styleID = resources.getIdentifier(primitive.name,
                            "drawable", activity.getPackageName());
                    seekBar.setProgressDrawable(
                            ResourcesCompat.getDrawable(resources, styleID, null));
                    seekBar.setMax(measurementType.totalValues);
                    seekBar.setProgress(measurementType.normalDefault);
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
                    measurementType.setView(number);
                    number.setGravity(Gravity.CENTER_HORIZONTAL);
                    TextViewCompat.setTextAppearance(number,
                            android.R.style.TextAppearance_DeviceDefault_Medium);
                    //number.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);

                    number.setText(Long.toString(measurementType.normalDefault));
                    MeasurementTextClickListener listener =
                            new MeasurementTextClickListener(activity, number, measurementType, dialogThemeID);
                    number.setOnClickListener(listener);
                    row.addView(number, rowParams);
                    break;

                case "text":
                    TextInputEditText text = new TextInputEditText(activity);
                    measurementType.setView(text);
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
     * (i.e via renderReminderEventTypes)
     *
     * @param activity The calling activity (i.e `this` in your Activity)
     */
    public static void resetEntries(Activity activity) {
        Log.d(LOG_PREFIX, "Enter resetEntries");
        Resources resources = activity.getResources();
        ORM orm = ORM.getInstance(activity);

        int evCount = orm.getMeasurementTypes().types.size();

        for (int i = 0; i < evCount; i++) {
            MeasurementType measurementType = orm.getMeasurementTypes().types.get(i);
            Log.d(LOG_PREFIX, "resetEntries: to reset etype " +
                    Long.toString(measurementType.id) + ", view id " + Long.toString(measurementType.view.getId()));


            switch (measurementType.getPrimitive(orm.getPrimitives()).name) {
                case "range_center":
                case "range_normal":
                    SeekBar seekBar = (SeekBar) measurementType.view;
                    seekBar.setMax(measurementType.totalValues);
                    seekBar.setProgress(measurementType.normalDefault);
                    break;

                case "text":
                    TextInputEditText textInputEditText = (TextInputEditText) measurementType.view;
                    textInputEditText.setText("");
                    break;

                case "number":
                default:
                    TextView textView = (TextView) measurementType.view;
                    textView.setText(Long.toString(measurementType.normalDefault));
                    break;
            }
        }
    }

    public static void saveEvents(Activity activity) {
        Log.d(LOG_PREFIX, "Enter saveEvents");
        ORM orm = ORM.getInstance(activity);

        ArrayList<Entry> entries = new ArrayList<>();
        int evCount = orm.getMeasurementTypes().types.size();

        // - Save widget data down to the entrylist
        for(int i = 0; i < evCount; i++) {
            MeasurementType measurementType = orm.getMeasurementTypes().types.get(i);
            String value;

            // Parse
            switch(measurementType.getPrimitive(orm.getPrimitives()).name) {
                case "range_center":
                case "range_normal":
                    value = Integer.toString(((SeekBar) measurementType.view).getProgress());
                    value = Long.toString(measurementType.min + Long.parseLong(value, 10));
                    break;

                case "text":
                    value = ((TextInputEditText) measurementType.view).getText().toString();
                    break;

                case "number":
                default:
                    value = ((TextView) measurementType.view).getText().toString();
                    value = Long.toString(measurementType.min + Long.parseLong(value, 10));
                    break;
            }

            // Add
            entries.add(new nu.ere.mooddiary.Entry((int) measurementType.id, value));
        }

        orm.lastSave = System.currentTimeMillis();
        orm.addEntries(entries, true);
    }

    public static void raiseNotification(Activity activity) {
        /** Notification test ***********************/

        Intent resultIntent = new Intent(activity, ReminderActivity.class);
        // ..
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        activity,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(activity)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("My notification")
                    .setContentText("Hello World!");

        mBuilder.setContentIntent(pendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 1;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) activity.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    public static String toHumanTime(Context context, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new java.util.Date());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Log.d(LOG_PREFIX, "toHumanTime: Alarm set for: " +
                String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.valueOf(calendar.get(Calendar.MINUTE)) + ":" +
                String.valueOf(calendar.get(Calendar.SECOND))
        );

        long millis = calendar.getTimeInMillis();
        Log.d(LOG_PREFIX, "in milliseconds: " + Long.toString(millis));
        String timeString = DateUtils.formatDateTime(context, millis, DateUtils.FORMAT_SHOW_TIME);
        Log.d(LOG_PREFIX, "Human: " + timeString);

        return timeString;
    }

}
