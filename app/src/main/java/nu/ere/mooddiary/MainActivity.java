package nu.ere.mooddiary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class MainActivity extends ThemedActivity {
    private static final String LOG_PREFIX = "MainActivity";
    private ORM orm;
    SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Create");
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance(this);
        initUI();
        Util.installAlarms(this);
    }

    public void initUI() {
        Log.d(LOG_PREFIX, "Enter initUI" );

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
        Log.d(LOG_PREFIX, "Enter renderEventSelect");
        ORM orm = ORM.getInstance(this);

        // Get our main (scrollable) view, where we are to programmatically add our EntryTypes
        LinearLayout entryLayout = (LinearLayout) this.findViewById(layout);

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

            case R.id.action_test_reminder_activity:
                i = new Intent(MainActivity.this, ReminderActivity.class);
                i.putExtra("reminder_id", 1);
                startActivity(i);
                return true;

            case R.id.action_test_alarm:
                Util.alarmTest(this);
                return true;

            default:
        }

        // Fallthrough (do nothing - let super handle it)
        return super.onOptionsItemSelected(item);
    }

}
