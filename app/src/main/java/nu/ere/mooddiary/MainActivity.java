package nu.ere.mooddiary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

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

        renderEventSelect(R.id.entryLayout);
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

    /**
     *
     * @param layout The layout resource to be used as main container where all Views are created
     */
    public void renderEventSelect(int layout) {
        Log.d(LOG_PREFIX, "Enter renderEventSelect");
        Resources resources = this.getResources();
        ORM orm = ORM.getInstance(this);

        // Get our main (scrollable) view, where we are to programmatically add our EntryTypes
        LinearLayout entryLayout = (LinearLayout) this.findViewById(layout);

        /*
        // Create a table
        TableLayout table = new TableLayout(this);
        table.setColumnStretchable(1, true); // Stretch the rightmost column (holding sliders etc)

        TableRow rowTitle = new TableRow(this);
        rowTitle.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        TableRow.LayoutParams rowParams = new TableRow.LayoutParams();
        rowParams.span = 1;

        rowParams.topMargin    = (int) resources.getDimension(R.dimen.entry_padding_top);
        rowParams.bottomMargin = (int) resources.getDimension(R.dimen.entry_padding_bottom);
        */

        // Walk our measurement types and create the appropriate text and entry widget (slider, etc).
        // Add them to the main layout.
        for(int i = 0; i < orm.getMeasurementTypes().types.size(); i++) {
            MeasurementType measurementType = orm.getMeasurementTypes().types.get(i);
            EntityPrimitive primitive = measurementType.getPrimitive(orm.getPrimitives());

            AppCompatButton measurementButton = new AppCompatButton(this);
            measurementButton.setText(measurementType.name);

            MeasurementButtonClickListener listener =
                    new MeasurementButtonClickListener(this, measurementType, dialogThemeID);
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
