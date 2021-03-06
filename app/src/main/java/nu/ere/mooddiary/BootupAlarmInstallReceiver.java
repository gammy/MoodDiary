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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootupAlarmInstallReceiver extends BroadcastReceiver {
    private static final String LOG_PREFIX = "BootupAlarmInst..";

    public void onReceive(Context context, Intent intent) {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter onReceive");

        //Intent aIntent = new Intent(context.getApplicationContext(), AlarmInstallerActivity.class);
        Intent aIntent = new Intent();

        aIntent.setClassName("nu.ere.mooddiary", "nu.ere.mooddiary.AlarmInstallerActivity");
        aIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "Starting a new activity (FLAG_ACTIVITY_NEW_TASK): "
                + aIntent.getClass().getName());

//        aIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
//                       | Intent.FLAG_ACTIVITY_CLEAR_TOP
//                       | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(aIntent);
    }
}
