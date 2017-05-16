package nu.ere.mooddiary;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class Util {
    private static final String LOG_PREFIX = "Util";

    /**
     * Walk through all measurement types and reset (zero) any Views.
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

        for(int i = 0; i < measurementTypes.size(); i++) {
            MeasurementType measurementType = measurementTypes.get(i);
            Log.d(LOG_PREFIX, " mType " + Integer.toString(measurementType.id) + ", " +
                    "View " + measurementType.view.toString());

        }

        Log.d(LOG_PREFIX, "COMMENCING VIEW PARSE LOOP");

        ArrayList<Entry> entries = new ArrayList<>();
        int evCount = measurementTypes.size();

        // - Save widget data down to the entrylist
        for(int i = 0; i < evCount; i++) {
            Log.d(LOG_PREFIX, "Event " + Integer.toString(i));

            MeasurementType measurementType = measurementTypes.get(i);
            String value;

            Log.d(LOG_PREFIX, "Get entity name");
            String entity_name = measurementType.getPrimitive(orm.getPrimitives()).name;
            Log.d(LOG_PREFIX, "Got it: " + entity_name);

            // Parse
            switch(entity_name) {
                default:
                    value = "Default"; // TODO
                    break;
                case "range_center":
                case "range_normal":
//                    value = ""; // TODO
                    SeekBar sBar = (SeekBar) measurementType.view;
                    Log.d(LOG_PREFIX, "About to read range getProgress on " + sBar.toString());
                    int progress = sBar.getProgress();
                    value = Long.toString(measurementType.min + progress);
                    //value = "0";
                    break;

                case "text":
                    TextInputEditText tView = (TextInputEditText) measurementType.view;
                    Log.d(LOG_PREFIX, "About to read text getText on " + tView.toString());
                    value = tView.getText().toString();
                    break;

                case "number":
                    TextView nView = (TextView) measurementType.view;
                    Log.d(LOG_PREFIX, "About to read number getText on " + nView.toString());
                    value = nView.getText().toString();
                    value = Long.toString(measurementType.min + Long.parseLong(value, 10));
            }

            // Add
            entries.add(new nu.ere.mooddiary.Entry(measurementType.id, value));
        }

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

        /*
        Log.d(LOG_PREFIX, "toHumanTime: Alarm set for: " +
                String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.valueOf(calendar.get(Calendar.MINUTE)) + ":" +
                String.valueOf(calendar.get(Calendar.SECOND))
        );
        */

        long millis = calendar.getTimeInMillis();
        // Log.d(LOG_PREFIX, "in milliseconds: " + Long.toString(millis));
        String timeString = DateUtils.formatDateTime(context, millis, DateUtils.FORMAT_SHOW_TIME);
        //Log.d(LOG_PREFIX, "Human: " + timeString);

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

    // FIXME untested!
    public static void clearAlarms(Activity activity) {
        Log.d(LOG_PREFIX, "Enter clearAlarms" );
        ORM orm = ORM.getInstance(activity);
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);
        ArrayList<ReminderTime> reminderTimes = orm.getReminderTimes().reminderTimes;
        for(int i = 0; i < reminderTimes.size(); i++) {
            Intent reminderIntent = new Intent(activity, ReminderActivity.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(activity, i /* Alarm ID */,
                            reminderIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.cancel(pendingIntent);
        }
        Log.d(LOG_PREFIX, Long.toString(reminderTimes.size()) + " alarms installed");
    }

    public static void installAlarms(Activity activity) {
        Log.d(LOG_PREFIX, "Enter installAlarms" );
        clearAlarms(activity);
        ORM orm = ORM.getInstance(activity);
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);

        ArrayList<ReminderTime> reminderTimes = orm.getReminderTimes().reminderTimes;

        for(int i = 0; i < reminderTimes.size(); i++) {
            ReminderTime reminderTime = reminderTimes.get(i);

            Intent reminderIntent = new Intent(activity, ReminderActivity.class);
            Log.d(LOG_PREFIX, " reminder_id " + Integer.toString(reminderTime.id));
            reminderIntent.putExtra("reminder_id", reminderTime.id);

            // Set up time (and possibly adjust)
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new java.util.Date());

            calendar.set(Calendar.HOUR_OF_DAY, reminderTime.hour);
            calendar.set(Calendar.MINUTE,      reminderTime.minute);
            calendar.set(Calendar.SECOND, 0);

            java.util.Date pDate = new java.util.Date();
            Log.d(LOG_PREFIX, "  Time now : " + pDate.toString());

            // Adjust if the time's already passed
            if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1); // Tomorrow
                Log.d(LOG_PREFIX, "    Time has already passed: setting alarm for tomorrow");
            }

            // Set up the alarm
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(activity, i /* Alarm ID */,
                            reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                                      calendar.getTimeInMillis(),
                                      AlarmManager.INTERVAL_DAY, pendingIntent);

            pDate = new java.util.Date(calendar.getTimeInMillis());
            Log.d(LOG_PREFIX, "  Alarm SET: " + pDate.toString());
        }

        Log.d(LOG_PREFIX, Long.toString(reminderTimes.size()) + " alarms installed");
    }

    public static void alarmTest(Activity activity) {
        Log.d(LOG_PREFIX, "Enter installAlarms" );
        ORM orm = ORM.getInstance(activity);

        Intent reminderIntent = new Intent(activity, ReminderActivity.class);
        reminderIntent.putExtra("reminder_id", 1);

        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(activity, 0 /* "Unique" Request code */,
                        reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();

        Calendar now = Calendar.getInstance();
        now.setTime(new java.util.Date());

        Log.d(LOG_PREFIX, "alarm: Current time is : " +
                String.valueOf(now.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.valueOf(now.get(Calendar.MINUTE)) + ":" +
                String.valueOf(now.get(Calendar.SECOND))
        );

        now.add(Calendar.SECOND, 10);

        Log.d(LOG_PREFIX, "alarm: Setting alarm to: " +
                String.valueOf(now.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.valueOf(now.get(Calendar.MINUTE)) + ":" +
                String.valueOf(now.get(Calendar.SECOND))
        );

        calendar.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, now.get(Calendar.SECOND));
        Toast.makeText(activity, "Alarm test in progress (10 seconds)",
                Toast.LENGTH_LONG).show();
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent); // FIXME
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
        //                          calendar.getTimeInMillis(),
        //        3600000 /* One hour in ms */, pendingIntent);

    }

    public static String buildIntString(ArrayList<Integer> list) {
        // The String object in Java < 8 doesn't have a .join method, so we have to do this
        // shit instead.
        StringBuilder stringBuilder = new StringBuilder();
        String separator = ""; // No separator on first iteration
        for(Integer integer : list) {
            stringBuilder.append(integer.toString());
            stringBuilder.append(separator);
            separator = ",";
        }

        return stringBuilder.toString();
    }

    public static boolean addReminder(PreferencesActivity activity, Bundle bundle) {
        Log.d(LOG_PREFIX, "Enter addReminder");
        ORM orm = ORM.getInstance(activity);

        int hour   = bundle.getInt(BundleExtraKey.REMINDER_HOUR);
        int minute = bundle.getInt(BundleExtraKey.REMINDER_MINUTE);
        ArrayList<Integer> types = bundle.getIntegerArrayList(BundleExtraKey.REMINDER_TYPES);

        orm.addReminder(hour, minute, types);
        orm.getReminderTimes().reload();
        installAlarms(activity);

        Toast.makeText(activity, activity.getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
        return(true);
    }

    public static boolean updateReminder(PreferencesActivity activity, Bundle bundle) {
        Log.d(LOG_PREFIX, "Enter updateReminder");
        ORM orm = ORM.getInstance(activity);

        int reminderTimeId = bundle.getInt(BundleExtraKey.REMINDER_TIME_ID);
        int hour           = bundle.getInt(BundleExtraKey.REMINDER_HOUR);
        int minute         = bundle.getInt(BundleExtraKey.REMINDER_MINUTE);

        int changeMode = bundle.getInt(BundleExtraKey.REMINDER_MODE);

        if(changeMode == ReminderEditMode.CHANGE) {
            Log.d(LOG_PREFIX, "About to update reminderTimeId " + Integer.toString(reminderTimeId));
            ArrayList<Integer> types = bundle.getIntegerArrayList(BundleExtraKey.REMINDER_TYPES);
            Toast.makeText(activity, activity.getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
            orm.changeReminder(reminderTimeId, hour, minute, types);
        } else if(changeMode == ReminderEditMode.DELETE) {
            Log.d(LOG_PREFIX, "About to DELETE reminderTimeId " + Integer.toString(reminderTimeId));
            Toast.makeText(activity, activity.getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
            orm.deleteReminder(reminderTimeId);
        }

        orm.getReminderTimes().reload();
        installAlarms(activity);

        return(true);
    }

}
