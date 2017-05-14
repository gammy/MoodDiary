package nu.ere.mooddiary;

import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.view.Gravity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Util {
    private static final String LOG_PREFIX = "Util";

    /**
     * Walk through all minutes and reset (zero) any Views.
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

    public static void saveEvents(Activity activity, ArrayList<MeasurementType> measurementTypes) {
        Log.d(LOG_PREFIX, "Enter saveEvents");
        ORM orm = ORM.getInstance(activity);

        ArrayList<Entry> entries = new ArrayList<>();
        int evCount = measurementTypes.size();

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
            entries.add(new nu.ere.mooddiary.Entry(measurementType.id, value));
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

    public static void saveSingleEntry(Activity activity,
                                       MeasurementType measurementType,
                                       String value) {
        // Ugly, but it works. This saves a single entry from the main view
        Entry entry = new Entry(measurementType.id, value);
        ORM orm = ORM.getInstance(activity);
        ArrayList<Entry> entryList = new ArrayList<>();
        entryList.add(entry);
        orm.addEntries(entryList, true);
        String text = activity.getString(R.string.toast_saved);
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }
}
