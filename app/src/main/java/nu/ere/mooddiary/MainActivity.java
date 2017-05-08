package nu.ere.mooddiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.NumberPicker;
import android.database.sqlite.SQLiteDatabase;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.support.v4.widget.TextViewCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public Database dbh;
    public static SQLiteDatabase db;
    public EntityPrimitives entityPrimitives;
    public EventTypes eventTypes;
    public long lastSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Main", "Create");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initDB();
        initUI();
    }

    public void initUI() {
        Log.d("Main", "Enter initUI" );

        lastSave = 0;

        Button saveButton = (Button) findViewById(R.id.saveButton);
        TextView thanksView = (TextView) findViewById(R.id.thanksTextView);
        saveButton.setOnClickListener(new SaveClickListener(this, thanksView));

        renderEntryTypes();
    }

    public void initDB() {
        dbh = new Database(this);
        db = dbh.getWritableDatabase();

        //dbh.onUpgrade(db, 0, 0); // XXX Debugging - trash db to force creation

        entityPrimitives = new EntityPrimitives(db);
        eventTypes = new EventTypes(db);
    }

    public void showNumberDialog(Activity activity, TextView view, EventType eventType){
        final NumberPicker numberPicker = new NumberPicker(activity);

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue((int) eventType.totalValues);
        numberPicker.setValue(Integer.parseInt(view.getText().toString()));
        numberPicker.setWrapSelectorWheel(false);

        /*
        numberPicker.setLayoutParams(new RelativeLayout.LayoutParams(
                                        RelativeLayout.LayoutParams.MATCH_PARENT,
                                        RelativeLayout.LayoutParams.MATCH_PARENT));
        */

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

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
        for(int i = 0; i < eventTypes.types.size(); i++) {
            EventType etype = eventTypes.types.get(i);
            EntityPrimitive primitive = etype.getPrimitive(entityPrimitives);

            // Make a label
            TextView label = new TextView(this);
            TextViewCompat.setTextAppearance(label,
                    android.R.style.TextAppearance_DeviceDefault_Small);
            label.setGravity(Gravity.LEFT);
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
                            "drawable", getApplicationContext().getPackageName());
                    seekBar.setProgressDrawable(
                            ResourcesCompat.getDrawable(getResources(), styleID, null));
                    seekBar.setMax((int) etype.totalValues);
                    seekBar.setProgress((int) etype.normalDefault);
                    row.addView(seekBar, rowParams);
                    break;

                case "number":
                    TextView number = new TextView(this);
                    etype.setView(number);
                    number.setGravity(Gravity.CENTER_HORIZONTAL);
                    TextViewCompat.setTextAppearance(number,
                            android.R.style.TextAppearance_DeviceDefault_Medium);
                    number.setText(Long.toString(etype.normalDefault));
                    EventNumberClickListener listener =
                            new EventNumberClickListener(MainActivity.this, number, etype);
                    number.setOnClickListener(listener);
                    row.addView(number, rowParams);
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

        int evCount = eventTypes.types.size();

        for (int i = 0; i < evCount; i++) {
            EventType etype = eventTypes.types.get(i);

            switch (etype.getPrimitive(entityPrimitives).name) {
                case "range_center":
                case "range_normal":
                    SeekBar seekBar = (SeekBar) etype.view;
                    seekBar.setMax((int) etype.totalValues);
                    seekBar.setProgress((int) etype.normalDefault);
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

        ArrayList<Entry> entries = new ArrayList<Entry>();
        int evCount = eventTypes.types.size();

        // - Save widget data down to the entrylist
        for(int i = 0; i < evCount; i++) {
            EventType etype = eventTypes.types.get(i);
            String value;

            // Parse
            switch(etype.getPrimitive(entityPrimitives).name) {
                case "range_center":
                case "range_normal":
                    value = Integer.toString(((SeekBar) etype.view).getProgress());
                    break;
                case "number":
                default:
                    value = ((TextView) etype.view).getText().toString();
                    break;
            }

            // Account for normals (important!)
            value = Long.toString(etype.min + Long.parseLong(value, 10));

            // Add
            entries.add(new nu.ere.mooddiary.Entry((int) etype.id, value));
        }

        lastSave = System.currentTimeMillis();
        dbh.addEntries(db, entries, true);
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
        int id = item.getItemId();
        Intent i;

        switch (item.getItemId()) {
            // Load the Settings screen ("Preferences")
            case R.id.action_settings:
                i = new Intent(MainActivity.this, PreferencesActivity.class);
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
