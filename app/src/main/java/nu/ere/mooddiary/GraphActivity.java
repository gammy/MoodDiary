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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class GraphActivity extends ThemedActivity {
    private static final String LOG_PREFIX = "GraphActivity";
    private ORM orm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Enter onCreate");
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

        // Setup some nice colors
        ArrayList<Integer> colorTable = new ArrayList<>();

        colorTable.add(Color.rgb( 52, 122,  78)); // 1
        colorTable.add(Color.rgb(212, 110,  40)); // 2
        colorTable.add(Color.rgb( 93,  88, 123)); // 3
        colorTable.add(Color.rgb(121, 189, 198)); // 4
        colorTable.add(Color.rgb(206, 188, 103)); // 5
        colorTable.add(Color.rgb( 72,  99, 142)); // 6
        colorTable.add(Color.rgb( 79, 250, 234)); // 7
        colorTable.add(Color.rgb(249, 110, 104)); // 8
        colorTable.add(Color.rgb( 93, 255,  28)); // 9
        colorTable.add(Color.rgb(160,  69, 142)); // 10
        colorTable.add(Color.rgb(129, 168,  55)); // 11
        colorTable.add(Color.rgb( 64, 191, 157)); // 12
        colorTable.add(Color.rgb(214, 219, 148)); // 13

        ArrayList<MeasurementType> mTypes = orm.getMeasurementTypes().getEnabledTypes();

        GraphView graph = (GraphView) findViewById(R.id.graph);
        // I don't think DOTTED/DASHED lines work at all on real android hardware at this time.
        // From what I've read, android can't do any kind of dot/dash in hardware accelerated
        // mode, and graphView requires hardware acceleration. I see no way around it.

        // Custom paint simply never ever works:
        // Paint paint = new Paint();
        // paint.setStyle(Paint.Style.STROKE);
        // paint.setStrokeWidth(10);
        // paint.setPathEffect(new DashPathEffect(new float[]{8, 5}, 0));

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

            LinkedHashMap<Date, Integer> values = getValues(cursor, mType);
            cursor.close();
            LinkedHashMap<String, Integer> counts = getMinMaxAvg(values);

            Log.d(LOG_PREFIX, "Creating new series for " + mType.name);

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
            series.setTitle(mType.name);
            series.setDrawDataPoints(true);
            series.setColor(colorTable.get((mType.id) % colorTable.size()));

            for(Map.Entry entry: values.entrySet()) {
                Date k = (Date) entry.getKey();
                Integer v = (Integer) entry.getValue();
                DataPoint point = new DataPoint(k, v);
                series.appendData(point, true, maxPoints);
            }

            if(mType.min < 0) {
                Log.d(LOG_PREFIX,
                        String.format("minimum(%d) < 0: Making BarGraph", counts.get("minimum")));
                graph.getSecondScale().setMinY(mType.min);
                graph.getSecondScale().setMaxY(mType.max);
                series.setThickness(14);
                series.setDataPointsRadius(20);
                graph.getSecondScale().addSeries(series);
                //series.setCustomPaint(paint); // Never works unfortunately
            } else {
                Log.d(LOG_PREFIX,
                        String.format("minimum(%d) >= 0: Making LineGraph", counts.get("minimum")));
                //series.setDrawBackground(true);
                series.setThickness(8);
                series.setDataPointsRadius(10);
               // series.setDrawBackground(true);
                //series.setBackgroundColor(Color.rgb(64, 64, 64));
                graph.addSeries(series);
            }

            /*
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
            */

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

        // set date label formatter
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE");
        DateAsXAxisLabelFormatter formatter = new DateAsXAxisLabelFormatter(this, dateFormat);
        graph.getGridLabelRenderer().setLabelFormatter(formatter);
        graph.getGridLabelRenderer().setNumHorizontalLabels(6);
        graph.getGridLabelRenderer().setHumanRounding(false);

        graph.getLegendRenderer().setVisible(true);
        //graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.MIDDLE);
        graph.getLegendRenderer().setFixedPosition(10, 10);
        graph.getLegendRenderer().setBackgroundColor(Color.argb(25, 64, 64, 64));

        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);
        //graph.getViewport().setScalableY(true);

        //graph.getViewport().setXAxisBoundsManual(true);

        Calendar tempcal = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        long oneWeekAgo = calendar.getTimeInMillis();
        calendar.setTime(new Date());
        long rightNow = calendar.getTimeInMillis();

        graph.getViewport().setMinX(oneWeekAgo);
        graph.getViewport().setMaxX(rightNow);

        //for(LineGraphSeries series: seriesList) {
        //    Log.d(LOG_PREFIX, "Adding a series to the graph");
        //    graph.addSeries(series);
        //}
    }

    private LinkedHashMap<Date, Integer> getValues(Cursor cursor, MeasurementType mType) {
        Log.d(LOG_PREFIX, "Enter getValues");
        LinkedHashMap<Date, Integer> list = new LinkedHashMap<>();

        while(cursor.moveToNext()) {
            long longTime = cursor.getLong(cursor.getColumnIndex("date"));
            Date time = new Date(longTime * 1000);
            Integer value = Integer.parseInt(cursor.getString(cursor.getColumnIndex("value")));
            Log.d(LOG_PREFIX, "Adding event " + mType.name
                    + ", time " + Long.toString(longTime)
                    + ", value " + Integer.toString(value));
            list.put(time, value);
        }
        return(list);
    }

    private LinkedHashMap<String, Integer> getMinMaxAvg(LinkedHashMap<Date, Integer> values) {
        Log.d(LOG_PREFIX, "Enter getMinMaxAvg");

        int minimum = 0;
        int maximum = 0;
        int average = 0;
        LinkedHashMap<String, Integer> minMaxAvg = new LinkedHashMap<>();

        for(Map.Entry v: values.entrySet()) {
            Integer val = (Integer) v.getValue();
            if(val < minimum) {
                minimum = val;
            }
            if(val > maximum) {
                maximum = val;
            }
            average += val;
        }

        if(values.size() > 0) {
            average /= values.size(); // FIXME double!
        }

        minMaxAvg.put("minimum", minimum);
        minMaxAvg.put("maximum", maximum);
        minMaxAvg.put("average", average);

        Log.d(LOG_PREFIX,
                String.format("Minimum: %d, Maximum: %d, Average: %d", minimum, maximum, average));

        return(minMaxAvg);
    }
}
