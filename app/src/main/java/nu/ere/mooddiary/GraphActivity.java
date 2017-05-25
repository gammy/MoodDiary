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

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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

        int maxPoints = 1000;

        /* We need to
           - figure out how to get ONE MONTH worth of data
           - get that data for each registered primitive (if any exists)
           - create graph series for each primitive that has data
           - populate & render
         */

        // Get UNIX Timestamp equalling one month ago
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, -1);
        long oneMonthAgoUnixTime = calendar.getTimeInMillis() / 1000;
        Log.d(LOG_PREFIX, "A month ago = " + Long.toString(oneMonthAgoUnixTime));

        /************************************************************************************/

        ArrayList<Integer> colorTable = new ArrayList<>();

        colorTable.add(Color.rgb( 52, 122,  78)); // 1
        colorTable.add(Color.rgb(212, 110,  40)); // 2
        colorTable.add(Color.rgb( 43,  68,  53)); // 3
        colorTable.add(Color.rgb(121, 189, 198)); // 4
        colorTable.add(Color.rgb(206, 188, 103)); // 5
        colorTable.add(Color.rgb( 72,  59,  42)); // 6
        colorTable.add(Color.rgb( 49, 250, 234)); // 7
        colorTable.add(Color.rgb(249, 110, 104)); // 8
        colorTable.add(Color.rgb( 93, 255,  28)); // 9
        colorTable.add(Color.rgb(160,  69, 142)); // 10
        colorTable.add(Color.rgb(129, 168,  55)); // 11
        colorTable.add(Color.rgb( 64, 191, 157)); // 12
        colorTable.add(Color.rgb(214, 219, 148)); // 13

        ArrayList<LineGraphSeries<DataPoint>> seriesList = new ArrayList<>();
        ArrayList<MeasurementType> mTypes = orm.getMeasurementTypes().getEnabledTypes();

        for(MeasurementType mType: mTypes ) {
            Log.d(LOG_PREFIX, "Walking Measurement Types: " + mType.name);

            EntityPrimitive primitive = orm.getPrimitives().getByID(mType.entity);
            if(primitive.isNumber == 0) {
                Log.d(LOG_PREFIX, "This type is not a number: Skipping");
                continue;
            }

            String sql = "SELECT " +
                             "date, " +
                             "value " +
                         "FROM " +
                             "Events " +
                         "WHERE " +
                             "type = " + Integer.toString(mType.id) + " " +
                         "AND " +
                             "date >= " + Long.toString(oneMonthAgoUnixTime) + " " +
                         "ORDER BY " +
                             "date ASC " +
                         "LIMIT " + Integer.toString(maxPoints);
            Log.d(LOG_PREFIX, sql);

            Cursor cursor = orm.db.rawQuery(sql, null);

            // Create a new series for this type
            Log.d(LOG_PREFIX, "Creating new series for " + mType.name);
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
            series.setTitle(mType.name);
            series.setDrawDataPoints(true);
            //series.setDrawBackground(true);
            series.setDataPointsRadius(8);
            series.setColor(colorTable.get((mType.id) % 13));
            series.setThickness(4 + mType.order);
            //series.setBackgroundColor(Color.rgb(testR, testG, testB));

            // Add events to the series
            while(cursor.moveToNext()) {
                long longTime = cursor.getLong(cursor.getColumnIndex("date"));
                Date time = new Date(longTime * 1000);
                int value = Integer.parseInt(cursor.getString(cursor.getColumnIndex("value")));
                //String value  = cursor.getString(cursor.getColumnIndex("value"));
                Log.d(LOG_PREFIX, "Adding event " + mType.name
                                + ", time " + Long.toString(longTime)
                                + ", value " + Integer.toString(value));
                value -= mType.min;
                DataPoint point = new DataPoint(time, value);
                series.appendData(point, true, maxPoints);
            }

            // Add this series to the list
            seriesList.add(series);
        }
        /*
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
                     "AND " +
                         "date >= " + Long.toString(oneMonthAgoUnixTime) + " " +
                     "ORDER BY " +
                         "date ASC " +
                     "LIMIT " + Integer.toString(maxPoints);
        */

        /************************************************************************************/

        Log.d(LOG_PREFIX, "Rendering graph");

        GraphView graph = (GraphView) findViewById(R.id.graph);

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        graph.getGridLabelRenderer().setNumHorizontalLabels(6);
        graph.getGridLabelRenderer().setHumanRounding(false);

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        //graph.getViewport().setXAxisBoundsManual(true);

        Calendar tempcal = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        long oneWeekAgo = calendar.getTimeInMillis();
        calendar.setTime(new Date());
        long rightNow = calendar.getTimeInMillis();

        graph.getViewport().setMinX(oneWeekAgo);
        graph.getViewport().setMaxX(rightNow);

        for(LineGraphSeries series: seriesList) {
            Log.d(LOG_PREFIX, "Adding a series to the graph");
            graph.addSeries(series);
        }
    }
}