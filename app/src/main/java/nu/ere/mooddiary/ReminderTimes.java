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

import android.database.Cursor;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

public final class ReminderTimes {
    private static final String LOG_PREFIX = "ReminderTimes";
    public ArrayList<ReminderTime> reminderTimes;
    private ORM orm = null;

    public ReminderTimes(ORM orm) {
        Log.d(LOG_PREFIX, "Enter ReminderTimes");
        this.orm = orm;
        reload();
    }

    public void reload() {
        Log.d(LOG_PREFIX, "Enter reload");

        // Populate ReminderTimes
        Cursor cursor;
        Log.d(LOG_PREFIX, "SELECT id, reminderGroup, hour, minute FROM ReminderTimes");
        cursor = orm.db.rawQuery("SELECT id, reminderGroup, hour, minute FROM ReminderTimes", null);

        reminderTimes = new ArrayList<>();

        // Walk through each reminder and collate the associated measurementTypes
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            int group = cursor.getInt(cursor.getColumnIndex("reminderGroup"));
            int hour = cursor.getInt(cursor.getColumnIndex("hour"));
            int minute = cursor.getInt(cursor.getColumnIndex("minute"));

            ReminderTime reminderTime = new ReminderTime(id, group, hour, minute);
            reminderTimes.add(reminderTime);

            Log.d(LOG_PREFIX,
                    "  ReminderTime " + Integer.toString(id) + ", " +
                            "group " + Integer.toString(group) + ", " +
                            "time = " + Integer.toString(hour) + ":" + Integer.toString(minute));
        }

        cursor.close();
    }

    // FIXME dangerous and kind of useless, isn't it? The id will be either invalid or point to the wrong thing!
    // FIXME what's using this right now?
    // FIXME Are the above statements true? I don't think so. It works just fine.
    public ReminderTime getByID(int id) {
        for (int i = 0; i < reminderTimes.size(); i++) {
            ReminderTime r = reminderTimes.get(i);
            if (r.id == id) {
                return (r);
            }
        }
        throw new NoSuchElementException("Unable to find an ReminderTime with id " +
                Long.toString(id));
    }

    public ArrayList<MeasurementType> getTypesByReminderTimeID(int reminderTimeID) {
        Log.d(LOG_PREFIX, "Enter getTypesByReminderTimeID");
        ArrayList<MeasurementType> reminderMeasurementTypes = new ArrayList<>();

        Cursor cursor;
        String iStr = Integer.toString(reminderTimeID);
        String sql;

        /* First get reminder *TIME* ID from ReminderTimes, *THEN* look for that */
        sql = "SELECT reminderGroup FROM ReminderTimes WHERE id = ?";
        cursor = orm.db.rawQuery(sql, new String[]{iStr});
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            throw new IndexOutOfBoundsException(
                    "Found no reminderGroup in ReminderTimes for id " +
                            Integer.toString(reminderTimeID));
        }
        int reminderGroup = cursor.getInt(cursor.getColumnIndex("reminderGroup"));
        String gStr = Integer.toString(reminderGroup);

        // This query is a bit more complex, as we need the result in the correct listOrder so that
        // it displays in the specified order on the screen.
        sql = "SELECT " +
                "type " +
                "FROM " +
                "ReminderGroups " +
                "LEFT OUTER JOIN " +
                "MeasurementTypes " +
                "ON " +
                "ReminderGroups.type = MeasurementTypes.id " +
                "WHERE " +
                "ReminderGroups.reminderGroup = ?" +
                "ORDER BY " +
                "MeasurementTypes.listOrder ASC";

        cursor = orm.db.rawQuery(sql, new String[]{gStr});

        // Get all measurement types associated with this reminder
        while (cursor.moveToNext()) {
            int measurementTypeId = cursor.getInt(cursor.getColumnIndex("type"));
            Log.d(LOG_PREFIX, "  associated type: " + Integer.toString(measurementTypeId));
            MeasurementType measurementType = orm.getMeasurementTypes().getByID(measurementTypeId);
            reminderMeasurementTypes.add(measurementType);
        }

        cursor.close();

        return (reminderMeasurementTypes);
    }

    public int getFirstReminderGroupId() {
        Log.d(LOG_PREFIX, "Enter getFirstReminderGroupId");
        Log.d(LOG_PREFIX, "SELECT MIN (reminderGroup) FROM ReminderTimes");
        Cursor cursor =
                orm.db.rawQuery("SELECT MIN (reminderGroup) AS first FROM ReminderTimes", null);
        cursor.moveToFirst();
        int id = cursor.getInt(cursor.getColumnIndex("first"));
        Log.d(LOG_PREFIX, "id: " + Integer.toString(id));
        cursor.close();
        return id;
    }

    public ArrayList<ReminderTime> getSorted() {
        Log.d(LOG_PREFIX, "Enter getSorted");

        ArrayList<ReminderTime> list = orm.getReminderTimes().reminderTimes;

        Collections.sort(list, new Comparator<ReminderTime>() {
            @Override
            public int compare(ReminderTime o1, ReminderTime o2) {
                Date o1Date = null;
                Date o2Date = null;
                DateFormat fmt = new SimpleDateFormat("HH:mm");

                try {
                    o1Date = fmt.parse(String.format("%02d:%02d", o1.hour, o1.minute));
                    o2Date = fmt.parse(String.format("%02d:%02d", o2.hour, o2.minute));
                } catch(Exception e) {
                    Log.d(LOG_PREFIX, "Dateparser sez NO. Boooooooooooooom");
                }

                return o1Date.compareTo(o2Date);
            }
        });

        for(ReminderTime r: reminderTimes) {
            String timeText = String.format("%02d:%02d", r.hour, r.minute);
            Log.d(LOG_PREFIX, "  reminder " + Integer.toString(r.id) + ": " + timeText);
        }

        return(list);
    }
}
