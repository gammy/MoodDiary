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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class OverviewActivity extends ThemedActivity {
    private static final String LOG_PREFIX = "OverviewActivity";
    private ORM orm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter Create");
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setContentView(R.layout.content_overview);

        loadInfo();
    }

    private void loadInfo() {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter loadInfo");

        TextView view = (TextView) findViewById(R.id.overviewText);
        SQLiteDatabase db = orm.db;

        SQLiteStatement s;

        s = db.compileStatement("SELECT COUNT(*) FROM Events");
        long entryCount = s.simpleQueryForLong();
        s.releaseReference();

        long eventTypeCount = orm.getMeasurementTypes().types.size();
        long reminderCount = orm.getReminderTimes().reminderTimes.size();
        long entityPrimitiveCount = orm.getPrimitives().entities.size();

        /*
        s = db.compileStatement("SELECT COUNT(*) FROM MeasurementTypes");
        long eventTypeCount = s.simpleQueryForLong();
        s.releaseReference();

        s = db.compileStatement("SELECT COUNT(*) FROM EntityPrimitives");
        long entityPrimitiveCount = s.simpleQueryForLong();
        s.releaseReference();

        s = db.compileStatement("SELECT COUNT(*) FROM ReminderTimes");
        long reminderCount = s.simpleQueryForLong();
        s.releaseReference();
        */

        String text =
                Long.toString(entryCount) + " entries\n" +
                Long.toString(reminderCount) + " reminders\n" +
                Long.toString(eventTypeCount) + " measurement types\n" +
                Long.toString(entityPrimitiveCount) + " entity primitives\n\n";


        //////////////////

        String sql;
        sql = "SELECT " +
                  "date, " +
                  "value, " +
                  "type, " +
                  "MeasurementTypes.name AS name " +
              "FROM " +
                  "Events " +
              "LEFT OUTER JOIN " +
                  "MeasurementTypes " +
              "ON " +
                  "Events.type = MeasurementTypes.id " +
              "ORDER BY " +
                  "date DESC";

        Cursor cursor = orm.db.rawQuery(sql, null);

        while(cursor.moveToNext()) {
            long unixTime = cursor.getLong(cursor.getColumnIndex("date"));
            String value  = cursor.getString(cursor.getColumnIndex("value"));
            String type   = cursor.getString(cursor.getColumnIndex("type"));
            String name   = cursor.getString(cursor.getColumnIndex("name"));

            java.util.Date time = new java.util.Date(unixTime * 1000);

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm");
            text += dateFormat.format(time) + ": " + name + " (" + type +  ")  \"" + value + "\"\n";
        }
        cursor.close();

        view.setText(text);
    }
}