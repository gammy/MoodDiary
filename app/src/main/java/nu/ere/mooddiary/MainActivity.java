package nu.ere.mooddiary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Calendar;

public class MainActivity extends ThemedActivity {
    private static final String LOG_PREFIX = "MainActivity";
    private ORM orm;
    SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Create");
        //installAlarms();
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance(this);
        initUI();

        orm.testReminders();
    }

    public void initUI() {
        Log.d(LOG_PREFIX, "Enter initUI" );

        orm.lastSave = 0;

        setContentView(R.layout.coordinator_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        // Ensure that no programmatically generated view within our ScrollView forces the
        // view to scroll down: We want the initial to view always to be at the top:
        // http://stackoverflow.com/a/35071620
        ScrollView view = (ScrollView) findViewById(R.id.mainScrollView);
        view.setFocusableInTouchMode(true);
        view.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

        // Set up the save button, which, on click, saves the event and runs an animation
        Button saveButton = (Button) findViewById(R.id.saveButton);
        TextView thanksView = (TextView) findViewById(R.id.thanksTextView);
        saveButton.setOnClickListener(new SaveClickListener(this, thanksView, false));

        Util.renderEntryTypes(this, R.id.entryLayout, dialogThemeID);
    }

    public void installAlarms() {
        Log.d(LOG_PREFIX, "Enter installAlarms" );

        Intent reminderIntent = new Intent(MainActivity.this , ReminderActivity.class);
        reminderIntent.putExtra("reminder_id", (long) 1234);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(MainActivity.this, 0 /* "Unique" Request code */,
                        reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // TODO: walk through all the Reminders and set all the timers.
        //       This will need to be done each time we add or change an existing reminder as well.
        Calendar calendar = Calendar.getInstance();

        Calendar now = Calendar.getInstance();
        now.setTime(new java.util.Date());

        Log.d(LOG_PREFIX, "alarm: Current time is : " +
                String.valueOf(now.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.valueOf(now.get(Calendar.MINUTE)) + ":" +
                String.valueOf(now.get(Calendar.SECOND))
        );

        now.add(Calendar.SECOND, 30); // 30 seconds from now

        Log.d(LOG_PREFIX, "alarm: Setting alarm to: " +
                String.valueOf(now.get(Calendar.HOUR_OF_DAY)) + ":" +
                String.valueOf(now.get(Calendar.MINUTE)) + ":" +
                String.valueOf(now.get(Calendar.SECOND))
        );

        calendar.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, now.get(Calendar.SECOND));
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
        //                          calendar.getTimeInMillis(),
        //        3600000 /* One hour in ms */, pendingIntent);

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

            // Load the Export screen
            case R.id.action_export:
                i = new Intent(MainActivity.this, ExportActivity.class);
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

            // Load TEST
            case R.id.action_TEST:
                i = new Intent(MainActivity.this, ReminderActivity.class);
                startActivity(i);
                return true;

            default:
        }

        // Fallthrough (do nothing - let super handle it)
        return super.onOptionsItemSelected(item);
    }

}
