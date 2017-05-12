package nu.ere.mooddiary;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.NumberPicker;
import android.database.sqlite.SQLiteDatabase;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.support.v4.widget.TextViewCompat;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends ThemedActivity {

    public ORM orm;
    SharedPreferences sharedPrefs;
    public long lastSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Main", "Create");
        installAlarms();
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance();
        initUI();
    }

    public void initUI() {
        Log.d("Main", "Enter initUI" );

        lastSave = 0;

        setContentView(R.layout.coordinator_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        saveButton.setOnClickListener(new SaveClickListener(this, thanksView));

        renderEntryTypes();
    }

    public void installAlarms() {

        Intent reminderIntent = new Intent(MainActivity.this , ReminderActivity.class);
        reminderIntent.putExtra("reminder_id", 1234); // TODO (can add Bundle / Parceable as well?)
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, reminderIntent, 0);

        // TODO: walk through all the Reminders and set all the timers.
        //       This will need to be done each time we add or change an existing reminder as well.
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 00);
        calendar.set(Calendar.MINUTE, 22);
        calendar.set(Calendar.SECOND, 0);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
        //                          calendar.getTimeInMillis(),
        //        3600000 /* One hour in ms */, pendingIntent);

    }

    public void showNumberDialog(Activity activity, TextView view, EventType eventType){
        Log.d("NumberDialog", "dialogThemeID: " + Integer.toString(dialogThemeID));
        final NumberPicker numberPicker =
                new NumberPicker(new ContextThemeWrapper(activity, dialogThemeID));

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue((int) eventType.totalValues);
        numberPicker.setValue(Integer.parseInt(view.getText().toString()));
        numberPicker.setWrapSelectorWheel(false);

        // FIXME numberPicker styling (font size) regression
        /*
        numberPicker.setLayoutParams(new RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.MATCH_PARENT));
        */

        AlertDialog.Builder builder =
                new AlertDialog.Builder(new ContextThemeWrapper(activity, dialogThemeID));

        DialogNumberClickListener listener = new DialogNumberClickListener(view, numberPicker);

        builder.setPositiveButton(R.string.submit, listener);
        builder.setNegativeButton(R.string.cancel, listener);
        builder.setView(numberPicker);

        AlertDialog dialog = builder.create();

        dialog.setTitle(eventType.name);
        dialog.show();
    }

    public void renderEntryTypes() {
        Log.d("MainActivity", "Enter renderEntryTypes");
        // FIXME move some of this back into initUI; this function is only called ONCE.

        // Get our main (scrollable) view, where we are to programmatically add our EntryTypes
        LinearLayout entryLayout = (LinearLayout) findViewById(R.id.entryLayout);

        // Create a table
        TableLayout table = new TableLayout(this);
        table.setColumnStretchable(1, true); // Stretch the rightmost column (holding sliders etc)

        TableRow rowTitle = new TableRow(this);
        rowTitle.setGravity(Gravity.CENTER_HORIZONTAL);

        TableRow.LayoutParams rowParams = new TableRow.LayoutParams();
        rowParams.span = 1;

        rowParams.topMargin    = (int) getResources().getDimension(R.dimen.entry_padding_top);
        rowParams.bottomMargin = (int) getResources().getDimension(R.dimen.entry_padding_bottom);

        // Walk our event types and create the appropriate text and entry widget (slider, etc).
        // Add them to the main layout.
        for(int i = 0; i < orm.getEventTypes().types.size(); i++) {
            EventType etype = orm.getEventTypes().types.get(i);
            EntityPrimitive primitive = etype.getPrimitive(orm.entityPrimitives);

            // Make a label
            TextView label = new TextView(this);
            TextViewCompat.setTextAppearance(label,
                    android.R.style.TextAppearance_DeviceDefault_Small);
            label.setGravity(Gravity.START);
            label.setText(etype.name);

            TableRow row = new TableRow(this);
            // row.setBackgroundColor(Color.BLUE); // (debugging)
            row.addView(label, rowParams);

            // Make the appropriate widget
            switch(primitive.name) {

                case "range_center":
                case "range_normal":
                    SeekBar seekBar = new SeekBar(this);
                    etype.setView(seekBar);
                    // The drawable resource name (i.e 'res/drawable/range_center.xml') matches
                    // the database EntityPrimitive name.
                    int styleID = getResources().getIdentifier(primitive.name,
                            "drawable", this.getPackageName());
                    seekBar.setProgressDrawable(
                            ResourcesCompat.getDrawable(getResources(), styleID, null));
                    seekBar.setMax((int) etype.totalValues);
                    seekBar.setProgress((int) etype.normalDefault);
                    row.addView(seekBar, rowParams);
                    break;

                case "number":
                    TextView number = new TextView(this);
                    //number.setPaintFlags(number.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    //number.setBackground(android:background="?attr/editTextBackground"

                    /* Can't find an easier way to do this - insane */
                    int[] attrs = new int[] { R.attr.editTextBackground};
                    TypedArray ta = this.obtainStyledAttributes(attrs);
                    Drawable drawableFromTheme = ta.getDrawable(0);
                    ta.recycle();
                    number.setBackgroundDrawable(drawableFromTheme);

                    //TextInputEditText number = new TextInputEditText(this);
                    etype.setView(number);
                    number.setGravity(Gravity.CENTER_HORIZONTAL);
                    TextViewCompat.setTextAppearance(number,
                            android.R.style.TextAppearance_DeviceDefault_Medium);
                    //number.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);

                    number.setText(Long.toString(etype.normalDefault));
                    EventNumberClickListener listener =
                            new EventNumberClickListener(MainActivity.this, number, etype);
                    number.setOnClickListener(listener);
                    row.addView(number, rowParams);
                    break;

                case "text":
                    TextInputEditText text = new TextInputEditText(this);
                    etype.setView(text);
                    row.addView(text, rowParams);
                    break;

                default:
                    break;

            }

            table.addView(row);
        }

        entryLayout.addView(table);
    }

    public void resetEntries() {
        Log.d("MainActivity", "Enter resetEntries");

        // FIXME need to refactor this stuff, it's in too many places already but it's
        //       02:20am, so it will have to wait!

        int evCount = orm.getEventTypes().types.size();

        for (int i = 0; i < evCount; i++) {
            EventType etype = orm.getEventTypes().types.get(i);

            switch (etype.getPrimitive(orm.getPrimitives()).name) {
                case "range_center":
                case "range_normal":
                    SeekBar seekBar = (SeekBar) etype.view;
                    seekBar.setMax((int) etype.totalValues);
                    seekBar.setProgress((int) etype.normalDefault);
                    break;

                case "text":
                    TextInputEditText textInputEditText = (TextInputEditText) etype.view;
                    textInputEditText.setText("");
                    break;

                case "number":
                default:
                    TextView textView = (TextView) etype.view;
                    textView.setText(Long.toString(etype.normalDefault));
                    break;
            }
        }
    }

    public void saveEvents() {
        Log.d("MainActivity", "Enter saveEvents");

        ArrayList<Entry> entries = new ArrayList<>();
        int evCount = orm.getEventTypes().types.size();

        // - Save widget data down to the entrylist
        for(int i = 0; i < evCount; i++) {
            EventType etype = orm.getEventTypes().types.get(i);
            String value;

            // Parse
            switch(etype.getPrimitive(orm.getPrimitives()).name) {
                case "range_center":
                case "range_normal":
                    value = Integer.toString(((SeekBar) etype.view).getProgress());
                    value = Long.toString(etype.min + Long.parseLong(value, 10));
                    break;

                case "text":
                    value = ((TextInputEditText) etype.view).getText().toString();
                    break;

                case "number":
                default:
                    value = ((TextView) etype.view).getText().toString();
                    value = Long.toString(etype.min + Long.parseLong(value, 10));
                    break;
            }

            // Add
            entries.add(new nu.ere.mooddiary.Entry((int) etype.id, value));
        }

        lastSave = System.currentTimeMillis();
        orm.getHelper().addEntries(entries, true);
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

            default:
        }

        // Fallthrough (do nothing - let super handle it)
        return super.onOptionsItemSelected(item);
    }

}
