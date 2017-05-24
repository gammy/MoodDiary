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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nu.ere.mooddiary;

import android.app.Activity;
import android.app.Notification;
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
                    .setContentTitle(activity.getString(R.string.notification_title))
                    .setContentText(activity.getString(R.string.notification_text));

       mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
       mBuilder.setLights(0xFFFF00FF, 500, 500);
       mBuilder.setDefaults(Notification.DEFAULT_SOUND |
                            Notification.DEFAULT_VIBRATE);

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
        Alarms.installAlarms(activity);

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

        if(changeMode == PreferenceEditMode.REMINDER_CHANGE) {
            Log.d(LOG_PREFIX, "About to update reminderTimeId " + Integer.toString(reminderTimeId));
            ArrayList<Integer> types = bundle.getIntegerArrayList(BundleExtraKey.REMINDER_TYPES);
            Toast.makeText(activity, activity.getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
            orm.changeReminder(reminderTimeId, hour, minute, types);
        } else if(changeMode == PreferenceEditMode.REMINDER_DELETE) {
            Log.d(LOG_PREFIX, "About to DELETE reminderTimeId " + Integer.toString(reminderTimeId));
            Toast.makeText(activity, activity.getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
            orm.deleteReminder(reminderTimeId);
        }

        orm.getReminderTimes().reload();
        Alarms.installAlarms(activity);

        return(true);
    }

    public static boolean addMeasurementType(PreferencesActivity activity, Bundle bundle) {
        Log.d(LOG_PREFIX, "Enter addMeasurementType");
        ORM orm = ORM.getInstance(activity);

        String name = bundle.getString(BundleExtraKey.MEASUREMENT_TYPE_NAME);
        int entity  =    bundle.getInt(BundleExtraKey.MEASUREMENT_TYPE_ENTITY);
        int order   =    bundle.getInt(BundleExtraKey.MEASUREMENT_TYPE_ORDER);
        int min     =    bundle.getInt(BundleExtraKey.MEASUREMENT_TYPE_MINIMUM);
        int max     =    bundle.getInt(BundleExtraKey.MEASUREMENT_TYPE_MAXIMUM);
        int dfl     =    bundle.getInt(BundleExtraKey.MEASUREMENT_TYPE_DEFAULT);

        orm.addMeasurementType(entity, order, name, min, max, dfl, "");
        orm.reload(activity);

        Toast.makeText(activity, activity.getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
        return(true);
    }

    public static boolean updateMeasurementType(PreferencesActivity activity, Bundle bundle) {
        Log.d(LOG_PREFIX, "Enter updateMeasurementType");
        ORM orm = ORM.getInstance(activity);

        int mTypeId    = bundle.getInt(BundleExtraKey.MEASUREMENT_TYPE_ID);
        int changeMode = bundle.getInt(BundleExtraKey.MEASUREMENT_TYPE_MODE);

        String name = bundle.getString(BundleExtraKey.MEASUREMENT_TYPE_NAME);
        int order   =    bundle.getInt(BundleExtraKey.MEASUREMENT_TYPE_ORDER);

        if(changeMode == PreferenceEditMode.MEASUREMENT_TYPE_CHANGE) {
            Log.d(LOG_PREFIX, "About to update measurementTypeId " + Integer.toString(mTypeId));
            Toast.makeText(activity, activity.getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
            orm.changeMeasurementType(mTypeId, name, order);
        } else if(changeMode == PreferenceEditMode.MEASUREMENT_TYPE_DELETE) {
            Log.d(LOG_PREFIX, "About to DELETE mTypeId " + Integer.toString(mTypeId));
            Toast.makeText(activity, activity.getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
            orm.deleteMeasurementType(mTypeId);
        }

        orm.reload(activity);

        return(true);
    }
}