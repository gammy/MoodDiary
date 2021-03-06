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
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;

public class MainActivity extends ThemedActivity {
    private static final String LOG_PREFIX = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.requestAccess(this);
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter Create");
        super.onCreate(savedInstanceState);
        initUI();
        Alarms.installAlarms(this);
    }

    // If the user adds a new measurement type, we need to re-render the buttons.
    @Override
    protected void onRestart() {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter onRestart");
        super.onRestart();
        initUI();
    }

    public void initUI() {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter initUI" );

        setContentView(R.layout.coordinator_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        // Ensure that no programmatically generated view within our ScrollView forces the
        // view to scroll down: We want the initial to view always to be at the top:
        // http://stackoverflow.com/a/35071620
        ScrollView view = (ScrollView) findViewById(R.id.mainScrollView);
        view.setFocusableInTouchMode(true);
        view.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

        renderEventSelect(R.id.entryLayout);
    }

    /**
     *
     * @param layout The layout resource to be used as main container where all Views are created
     */
    public void renderEventSelect(int layout) {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter renderEventSelect");
        ORM orm = ORM.getInstance(this);

        // Get our main (scrollable) view, where we are to programmatically add our EntryTypes
        LinearLayout entryLayout = (LinearLayout) this.findViewById(layout);

        // Walk our measurement types and create the appropriate text and entry widget (slider, etc).
        // Add them to the main layout.
        ArrayList<MeasurementType> types = orm.getMeasurementTypes().getEnabledTypes();
        for(MeasurementType type: types) {
            AppCompatButton measurementButton = new AppCompatButton(this);
            measurementButton.setText(type.name);

            MeasurementButtonClickListener listener =
                    new MeasurementButtonClickListener(this, type, dialogThemeID);
            measurementButton.setOnClickListener(listener);

            entryLayout.addView(measurementButton);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent i;

        switch (item.getItemId()) {
            // Load the Settings screen ("Preferences")
            case R.id.action_settings:
                i = new Intent(MainActivity.this, PreferencesActivity.class);
                PreferencesActivity p = new PreferencesActivity();
                startActivity(i);
                return true;

            // Load the statistics screen
            case R.id.action_graph:
                i = new Intent(MainActivity.this, GraphActivity.class);
                startActivity(i);
                return true;

            // Load the Overview screen
           case R.id.action_overview:
                i = new Intent(MainActivity.this, OverviewActivity.class);
                startActivity(i);
                return true;

            // Load the About screen
            case R.id.action_about:
                i = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(i);
                return true;

            case R.id.action_test_reminder_activity:
                Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Test mode: Picking the first available reminderTime");
                i = new Intent(MainActivity.this, ReminderActivity.class);
                i.putExtra("reminder_id",
                        ORM.getInstance(this).getReminderTimes().getFirstReminderTimeId());
                startActivity(i);
                return true;

            case R.id.action_test_reminder_activity_nosave:
                Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Test mode (nosave): Picking the first available reminderTime");
                i = new Intent(MainActivity.this, ReminderActivity.class);
                i.putExtra("nosave", true);
                i.putExtra("reminder_id",
                        ORM.getInstance(this).getReminderTimes().getFirstReminderTimeId());
                startActivity(i);
                return true;

            case R.id.action_test_alarm:
                Alarms.alarmTest(this);
                return true;

            default:
        }

        // Fallthrough (do nothing - let super handle it)
        return super.onOptionsItemSelected(item);
    }

}
