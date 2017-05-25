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

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class AlarmInstallerActivity extends ThemedActivity {
    private static final String LOG_PREFIX = "AlarmInstallerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Create");
        super.onCreate(savedInstanceState);
        Log.d(LOG_PREFIX, "Installing alarms");
        Alarms.installAlarms(this);
        ActivityCompat.finishAffinity(this);
    }
}
