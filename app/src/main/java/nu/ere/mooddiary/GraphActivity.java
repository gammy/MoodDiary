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
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter onCreate");
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance(this);
        initUI();
    }

    public void initUI() {
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter initUI");

        setContentView(R.layout.coordinator_graph);

        // Populate it with some data
        int maxPoints = 50000; // Arbitrary limit

        /* We need to
           - figure out how to get ONE MONTH worth of data
           - get that data for each registered primitive (if any exists)
           - create graph series for each primitive that has data
           - populate & render
         */

        // Get UNIX Timestamp equalling one month ago
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -5); // Get 5 years' worth of data
        long graphTimeBegin = calendar.getTimeInMillis() / 1000;

        /************************************************************************************/

        // Setup some nice colors
        ArrayList<Integer> colorTable = new ArrayList<>();

        colorTable.add(Color.rgb(0x63, 0x39, 0x74)); // 1
        colorTable.add(Color.rgb(0xD9, 0x88, 0x80)); // 2
        colorTable.add(Color.rgb(0x85, 0xC1, 0xE9)); // 3
        colorTable.add(Color.rgb(0x1A, 0xBC, 0x9C)); // 4
        colorTable.add(Color.rgb(0xF7, 0xDC, 0x6F)); // 5
        colorTable.add(Color.rgb(0xA0, 0x40, 0x00)); // 6
        colorTable.add(Color.rgb(0xCA, 0xCF, 0xD2)); // 7
        colorTable.add(Color.rgb(0x56, 0x65, 0x73)); // 8
        colorTable.add(Color.rgb(0x7E, 0x51, 0x09)); // 9
        colorTable.add(Color.rgb(0xF5, 0xB7, 0xB1)); // 10
        colorTable.add(Color.rgb(0x11, 0x78, 0x64)); // 11
        colorTable.add(Color.rgb(0x4D, 0x56, 0x56)); // 12
        colorTable.add(Color.rgb(0xFE, 0xF9, 0xE7)); // 13
        colorTable.add(Color.rgb(0xE8, 0xDA, 0xEF)); // 14
        colorTable.add(Color.rgb(0x34, 0x98, 0xDB)); // 15
        colorTable.add(Color.rgb(0x1A, 0x52, 0x76)); // 16

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

        for(int i = 0; i < mTypes.size(); i++) {
            MeasurementType mType = mTypes.get(i);
            Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "Walking Measurement Types: " + mType.name);

            EntityPrimitive primitive = orm.getPrimitives().getByID(mType.entity);
            if(primitive.isNumber == 0) {
                Util.log(Util.LOGLEVEL_3, LOG_PREFIX, "This type is not a number: Skipping");
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
                             "date >= " + Long.toString(graphTimeBegin) + " " +
                         "ORDER BY " +
                             "date ASC " +
                         "LIMIT " + Integer.toString(maxPoints);
            Util.log(Util.LOGLEVEL_3, LOG_PREFIX, sql);

            Cursor cursor = orm.db.rawQuery(sql, null);

            LinkedHashMap<Date, Integer> values = getValues(cursor, mType);
            cursor.close();
            LinkedHashMap<String, Integer> counts = getMinMaxAvg(values);

            Util.log(Util.LOGLEVEL_2, LOG_PREFIX, "Creating new series for " + mType.name);

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
            series.setTitle(mType.name);
            series.setDrawDataPoints(true);
            series.setColor(colorTable.get(i % (colorTable.size() - 1)));

            for(Map.Entry entry: values.entrySet()) {
                Date k = (Date) entry.getKey();
                Integer v = (Integer) entry.getValue();
                DataPoint point = new DataPoint(k, v);
                series.appendData(point, true, maxPoints);
            }

            if(mType.min < 0) {
                Util.log(Util.LOGLEVEL_3, LOG_PREFIX,
                        String.format("minimum(%d) < 0: Making BarGraph", counts.get("minimum")));
                graph.getSecondScale().setMinY(mType.min);
                graph.getSecondScale().setMaxY(mType.max);
                series.setThickness(14);
                series.setDataPointsRadius(20);
                graph.getSecondScale().addSeries(series);
                //series.setCustomPaint(paint); // Never works unfortunately
            } else {
                Util.log(Util.LOGLEVEL_3, LOG_PREFIX,
                        String.format("minimum(%d) >= 0: Making LineGraph", counts.get("minimum")));
                //series.setDrawBackground(true);
                series.setThickness(8);
                series.setDataPointsRadius(10);
               // series.setDrawBackground(true);
                //series.setBackgroundColor(Color.rgb(64, 64, 64));
                graph.addSeries(series);
            }

        }

        Util.log(Util.LOGLEVEL_2, LOG_PREFIX, "Rendering graph");

        // set date label formatter
        // TODO: Add some kind of handler to catch if the graph scale changed,
        //       and dynamically update the below properties to fit the scale?
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE\nd/M\n" );
        DateAsXAxisLabelFormatter formatter = new DateAsXAxisLabelFormatter(this, dateFormat);
        graph.getGridLabelRenderer().setLabelFormatter(formatter);
        graph.getGridLabelRenderer().setNumHorizontalLabels(6); // = 1 week :P
        graph.getGridLabelRenderer().setNumVerticalLabels(0);
        graph.getGridLabelRenderer().setHumanRounding(false);
        graph.getGridLabelRenderer().setTextSize(16);

        graph.getLegendRenderer().setVisible(true);
        //graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.MIDDLE);
        graph.getLegendRenderer().setFixedPosition(10, 10);
        graph.getLegendRenderer().setBackgroundColor(Color.argb(192, 240, 240, 240));

        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalable(true);

        updateViewPort(graph);
    }

    private void updateViewPort(GraphView graph) {
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(new Date());
        calBegin.add(Calendar.WEEK_OF_YEAR, -1);
        long viewPortBegin = calBegin.getTimeInMillis();

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(new Date());
        long viewPortEnd = calEnd.getTimeInMillis();

        graph.getViewport().setMinX(viewPortBegin);
        graph.getViewport().setMaxX(viewPortEnd);

        SimpleDateFormat foo = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date beg = new Date(viewPortBegin);
        Date end = new Date(viewPortEnd);

        Util.log(Util.LOGLEVEL_2, LOG_PREFIX, String.format("Beg: %s (%d)", foo.format(beg), viewPortBegin));
        Util.log(Util.LOGLEVEL_2, LOG_PREFIX, String.format("End: %s (%d)", foo.format(end), viewPortEnd));
    }

    private LinkedHashMap<Date, Integer> getValues(Cursor cursor, MeasurementType mType) {
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter getValues");
        LinkedHashMap<Date, Integer> list = new LinkedHashMap<>();

        while(cursor.moveToNext()) {
            long longTime = cursor.getLong(cursor.getColumnIndex("date"));
            Date time = new Date(longTime * 1000);
            Integer value = Integer.parseInt(cursor.getString(cursor.getColumnIndex("value")));
            Util.log(Util.LOGLEVEL_2, LOG_PREFIX, "Adding event " + mType.name
                    + ", time " + Long.toString(longTime)
                    + ", value " + Integer.toString(value));
            list.put(time, value);
        }
        return(list);
    }

    private LinkedHashMap<String, Integer> getMinMaxAvg(LinkedHashMap<Date, Integer> values) {
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter getMinMaxAvg");

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

        Util.log(Util.LOGLEVEL_2, LOG_PREFIX,
                String.format("Minimum: %d, Maximum: %d, Average: %d", minimum, maximum, average));

        return(minMaxAvg);
    }
}
