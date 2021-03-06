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
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class Alarms {
    private static final String LOG_PREFIX = "Alarms";

    public static void clearAlarms(Activity activity) {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter clearAlarms");
        ORM orm = ORM.getInstance(activity);
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);

        /*
        Intent updateServiceIntent = new Intent(activity, ReminderActivity.class);
        PendingIntent pendingUpdateIntent =
                PendingIntent.getService(activity, 0, updateServiceIntent, 0);
        try {
            alarmManager.cancel(pendingUpdateIntent);
        } catch (Exception e) {
            Log.e(LOG_PREFIX, "AlarmManager exception during cancel: " + e.toString());
        }
        */

        ArrayList<ReminderTime> reminderTimes = orm.getReminderTimes().reminderTimes;
        for(int i = 0; i < reminderTimes.size(); i++) {
            Intent reminderIntent = new Intent(activity, ReminderActivity.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(activity, i , // id
                            reminderIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "  Cancelling alarm id " + Integer.toString(i));
            alarmManager.cancel(pendingIntent);
        }
        Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, Long.toString(reminderTimes.size()) + " alarms cancelled");
    }

    public static void installAlarms(Activity activity) {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter installAlarms");

        clearAlarms(activity);
        ORM orm = ORM.getInstance(activity);
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);

        ArrayList<ReminderTime> reminderTimes = orm.getReminderTimes().reminderTimes;

        java.util.Date pDate = new java.util.Date();
        Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "Time now : " + pDate.toString());

        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Installing alarms");

        for(int i = 0; i < reminderTimes.size(); i++) {
            ReminderTime reminderTime = reminderTimes.get(i);

            Intent reminderIntent = new Intent(activity, ReminderActivity.class);
            Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "  reminderTimeId "
                    + Integer.toString(reminderTime.id)
                    + " (alarm id " + Integer.toString(i) + ")");
            reminderIntent.putExtra("reminder_id", reminderTime.id);

            // Set up time (and possibly adjust)
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new java.util.Date());

            calendar.set(Calendar.HOUR_OF_DAY, reminderTime.hour);
            calendar.set(Calendar.MINUTE,      reminderTime.minute);
            calendar.set(Calendar.SECOND, 0);

            // Adjust if the time's already passed
            if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1); // Tomorrow
                Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "  Time has already passed: setting alarm for tomorrow");
            }

            Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "  Create intent: FLAG_UPDATE_CURRENT");
            // Set up the alarm
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(activity, i /* Alarm ID */,
                            reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
            //        calendar.getTimeInMillis(),
            //        AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "  On KITKAT or newer, using setExact()");
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else{
                Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "  On older than KITKAT, using set()");
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }

            pDate = new java.util.Date(calendar.getTimeInMillis());
            Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "  Alarm SET: " + pDate.toString());
        }

        Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, Long.toString(reminderTimes.size()) + " alarms installed");
    }

    public static void alarmTest(Activity activity) {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter installAlarms" );
        ORM orm = ORM.getInstance(activity);

        Intent reminderIntent = new Intent(activity, ReminderActivity.class);
        reminderIntent.putExtra("reminder_id", orm.getReminderTimes().getFirstReminderTimeId());

        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(activity, 0 /* "Unique" Request code */,
                        reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();

        Calendar now = Calendar.getInstance();
        now.setTime(new java.util.Date());

        Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "alarm: Current time is : " +
                String.valueOf(now.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.valueOf(now.get(Calendar.MINUTE)) + ":" +
                String.valueOf(now.get(Calendar.SECOND))
        );

        now.add(Calendar.SECOND, 10);

        Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "alarm: Setting alarm to: " +
                String.valueOf(now.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.valueOf(now.get(Calendar.MINUTE)) + ":" +
                String.valueOf(now.get(Calendar.SECOND))
        );

        calendar.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, now.get(Calendar.SECOND));
        Toast.makeText(activity, "Alarm test in progress (10 seconds)",
                Toast.LENGTH_LONG).show();
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Alarm test in progress (10 seconds)");

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "   On KITKAT or newer, using setExact()");
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
        } else{
            Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "   On older than KITKAT, using set()");
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

}
