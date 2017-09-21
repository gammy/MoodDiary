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
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Util {
    private static final String LOG_PREFIX = "Util";

    /**
     * Raise a notification and flash the LED
     *
     * @param activity Calling activity
     */
    public static void raiseNotification(Activity activity) {
        log(LOGLEVEL_1, LOG_PREFIX, "Enter raiseNotification");
        Intent resultIntent = new Intent(activity, ReminderActivity.class);
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
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle(activity.getString(R.string.notification_title))
                        .setContentText(activity.getString(R.string.notification_text));

        mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
        mBuilder.setLights(0xFFFF00FF, 500, 500); // FIXME hardcoded
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

    /**
     * Provide a fancypants string representing time in the user's desired locale
     *
     * @param context Calling context
     * @param hour Hour in 24hr format (0-23)
     * @param minute Minute in 24hr format (0-59)
     * @return String
     */
    public static String toHumanTime(Context context, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new java.util.Date());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        /*
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "toHumanTime: Alarm set for: " +
                String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.valueOf(calendar.get(Calendar.MINUTE)) + ":" +
                String.valueOf(calendar.get(Calendar.SECOND))
        );
        */

        long millis = calendar.getTimeInMillis();
        Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "in milliseconds: " + Long.toString(millis));
        String timeString = DateUtils.formatDateTime(context, millis, DateUtils.FORMAT_SHOW_TIME);
        Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "Human: " + timeString);

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

    /**
     * Walk through the provided measurement types and add them to the database
     *
     * @param activity Calling activity
     * @param types  Arraylist of measurement types
     */
    public static void saveEvents(Activity activity, ArrayList<MeasurementType> types) {
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter saveEvents");
        ORM orm = ORM.getInstance(activity);

        for(MeasurementType type: types) {
            Util.log(Util.LOGLEVEL_3, LOG_PREFIX, " mType " + Integer.toString(type.id) + ", " +
                    "View " + type.view.toString());

        }

        Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "COMMENCING VIEW PARSE LOOP");

        ArrayList<Entry> entries = new ArrayList<>();

        // - Save widget data down to the entrylist
        for(MeasurementType type: types) {
            String value;

            Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "Get entity name");
            String entity_name = type.getPrimitive(orm.getPrimitives()).name;
            Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "Got it: " + entity_name);

            // Parse
            switch(entity_name) {
                default:
                    value = "Default"; // TODO
                    break;
                case "range_center":
                case "range_normal":
//                    value = ""; // TODO
                    SeekBar sBar = (SeekBar) type.view;
                    int progress = sBar.getProgress();
                    value = Long.toString(type.min + progress);
                    //value = "0";
                    break;

                case "text":
                    TextInputEditText tView = (TextInputEditText) type.view;
                    value = tView.getText().toString();
                    break;

                case "number":
                    TextView nView = (TextView) type.view;
                    value = nView.getText().toString();
                    value = Long.toString(Long.parseLong(value, 10));
                    break;

                case "toggle":
                    CheckBox cView = (CheckBox) type.view;
                    value = cView.isChecked() ? "1" : "0";
            }

            // Add
            entries.add(new nu.ere.mooddiary.Entry(type.id, value));
        }

        orm.addEntries(entries, true);
    }

    /**
     * Add a reminderTime to the database and reload the alarms
     *
     * @param activity Calling preference activity
     * @param bundle The bundle containing reminder metadata
     * @return boolean always true
     */
    public static boolean addReminder(PreferencesActivity activity, Bundle bundle) {
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter addReminder");
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

    /**
     * Update(change) a reminderTime and reload the alarms
     *
     * @param activity Calling preference activity
     * @param bundle The bundle containing reminder metadata
     * @return boolean always true
     */
    public static boolean updateReminder(PreferencesActivity activity, Bundle bundle) {
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter updateReminder");
        ORM orm = ORM.getInstance(activity);

        int reminderTimeId = bundle.getInt(BundleExtraKey.REMINDER_TIME_ID);
        int hour           = bundle.getInt(BundleExtraKey.REMINDER_HOUR);
        int minute         = bundle.getInt(BundleExtraKey.REMINDER_MINUTE);

        int changeMode = bundle.getInt(BundleExtraKey.REMINDER_MODE);

        if(changeMode == PreferenceEditMode.REMINDER_CHANGE) {
            Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "About to update reminderTimeId " + Integer.toString(reminderTimeId));
            ArrayList<Integer> types = bundle.getIntegerArrayList(BundleExtraKey.REMINDER_TYPES);
            Toast.makeText(activity, activity.getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
            orm.changeReminder(reminderTimeId, hour, minute, types);
        } else if(changeMode == PreferenceEditMode.REMINDER_DELETE) {
            Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "About to DELETE reminderTimeId " + Integer.toString(reminderTimeId));
            Toast.makeText(activity, activity.getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
            orm.deleteReminder(reminderTimeId);
        }

        orm.getReminderTimes().reload();
        Alarms.installAlarms(activity);

        return(true);
    }

    /**
     * Add a measurementType to the database
     *
     * @param activity Calling preference activity
     * @param bundle The bundle containing reminder metadata
     * @return boolean always true
     */
    public static boolean addMeasurementType(PreferencesActivity activity, Bundle bundle) {
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter addMeasurementType");
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

    /**
     * Update(change) a measurementType
     *
     * @param activity Calling preference activity
     * @param bundle The bundle containing reminder metadata
     * @return boolean always true
     */
    public static boolean updateMeasurementType(PreferencesActivity activity, Bundle bundle) {
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter updateMeasurementType");
        ORM orm = ORM.getInstance(activity);

        int mTypeId    = bundle.getInt(BundleExtraKey.MEASUREMENT_TYPE_ID);
        int changeMode = bundle.getInt(BundleExtraKey.MEASUREMENT_TYPE_MODE);

        String name = bundle.getString(BundleExtraKey.MEASUREMENT_TYPE_NAME);
        int order   =    bundle.getInt(BundleExtraKey.MEASUREMENT_TYPE_ORDER);
        int enabled =    bundle.getInt(BundleExtraKey.MEASUREMENT_TYPE_ENABLED);

        if(changeMode == PreferenceEditMode.MEASUREMENT_TYPE_CHANGE) {
            Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "About to update measurementTypeId " + Integer.toString(mTypeId));
            Toast.makeText(activity, activity.getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
            orm.changeMeasurementType(mTypeId, name, order, enabled);
        } else if(changeMode == PreferenceEditMode.MEASUREMENT_TYPE_DELETE) {
            Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "About to DELETE mTypeId " + Integer.toString(mTypeId));
            Toast.makeText(activity, activity.getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show();
            orm.deleteMeasurementType(mTypeId);
        }

        orm.reload(activity);

        return(true);
    }

    public static final int LOGLEVEL_QUIET = 0;
    public static final int LOGLEVEL_1 = 1;
    public static final int LOGLEVEL_2 = 2;
    public static final int LOGLEVEL_3 = 3;

    private static final int CURRENT_LOGLEVEL = LOGLEVEL_3;

    /**
     * Add a message to the logfile
     * Based on https://stackoverflow.com/questions/1756296/android-writing-logs-to-text-file
     *
     * Example: log(LOGLEVEL_INFO, "Renderer", "Copying memory to framebuffer");
     *
     * @param logLevel The minimum log level required to store the message
     * @param logPrefix A string which is automatically prepended to the message, separated by ': '
     * @param text The message to save
     */
    public static void log(Integer logLevel, String logPrefix, String text) {
        Log.d(logPrefix, text);

        if(CURRENT_LOGLEVEL < logLevel) {
            return;
        }

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/YY HH:mm: ");

        String targetPath = "Download" + "/MoodDiary.log";
        File sd = Environment.getExternalStorageDirectory();
        File logFile  = new File(sd,   targetPath);

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(dateFormat.format(currentTime));
            buf.append(logPrefix + ": ");
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}