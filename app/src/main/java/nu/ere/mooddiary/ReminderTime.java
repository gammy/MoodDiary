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

import android.preference.Preference;
import android.view.View;

public class ReminderTime {
    public int id;
    public int group;
    public int hour;
    public int minute;

    public View view;
    public Preference preference;

    public ReminderTime(int id, int group, int hour, int minute) {
        this.id     = id;
        this.group  = group;
        this.hour   = hour;
        this.minute = minute;
    }

    public View getView() {
        return(this.view);
    }
    public void setView(View view) {
        this.view = view;
    }
    public Preference getPreference() {
        return this.preference;
    }
    public void setPreference(Preference preference) {
        this.preference = preference;
    }
}

