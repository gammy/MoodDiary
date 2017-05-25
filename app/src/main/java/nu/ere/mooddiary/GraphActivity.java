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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;

public class GraphActivity extends ThemedActivity {
    private static final String LOG_PREFIX = "GraphActivity";
    private ORM orm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Create");
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance(this);
        initUI();
    }

    public void initUI() {
        Log.d(LOG_PREFIX, "Enter initUI");

        setContentView(R.layout.coordinator_graph);
        Toolbar toolbar = (Toolbar) findViewById(R.id.graphToolbar);
        setSupportActionBar(toolbar);

        // Populate it with some data
        int maxPoints = 100;

        /************************************************************************************/
        /*
        SELECT
                date,
                value,
                type,
                MeasurementTypes.name AS name
        FROM
                Events
        LEFT OUTER JOIN
                MeasurementTypes
        ON
                Events.type = MeasurementTypes.id
        LEFT OUTER JOIN
                EntityPrimitives
        ON
                MeasurementTypes.entity = EntityPrimitives.id
        WHERE
                EntityPrimitives.isNumber = 1
        ORDER BY
                date DESC
        LIMIT 100
                */
        // Fetch `maxPoints` rows of events whose primitive is a number
        String sql = "SELECT " +
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
                     "LEFT OUTER JOIN " +
                         "EntityPrimitives " +
                     "ON " +
                         "MeasurementTypes.entity = EntityPrimitives.id " +
                     "WHERE " +
                         "EntityPrimitives.isNumber = 1 " +
                     "ORDER BY " +
                         "date ASC " +
                     "LIMIT " + Integer.toString(maxPoints);

        Cursor cursor = orm.db.rawQuery(sql, null);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        // Some testing - just one set of data
        LineGraphSeries<DataPoint> moodSeries = new LineGraphSeries<>();

        while(cursor.moveToNext()) {
            long unixTime = cursor.getLong(cursor.getColumnIndex("date"));
            int value     = Integer.parseInt(cursor.getString(cursor.getColumnIndex("value")));
            int type      = cursor.getInt(cursor.getColumnIndex("type"));
            String name   = cursor.getString(cursor.getColumnIndex("name"));

            java.util.Date time = new java.util.Date(unixTime * 1000);

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm");

            if(type == 1) {
                Log.d(LOG_PREFIX, "Datapoint: " + Long.toString(unixTime) + ", type " + type + ": " + Integer.toString(value));
                DataPoint point = new DataPoint(unixTime, value);
                moodSeries.appendData(point, true, maxPoints);
            }
        }

        cursor.close();
        /************************************************************************************/

        /*
        DataPoint point = new DataPoint(5, 5);
        series.appendData(point, true, 7);

        point = new DataPoint(6, 1);
        series.appendData(point, true, 7);

        point = new DataPoint(7, 5);
        series.appendData(point, true, 7);
        */

        Log.d(LOG_PREFIX, "Rendering graph");
        graph.addSeries(moodSeries);
    }
}
