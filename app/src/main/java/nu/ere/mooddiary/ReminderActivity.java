package nu.ere.mooddiary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class ReminderActivity extends ThemedActivity {
    private static final String LOG_PREFIX = "ReminderActivity";
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor prefEditor;
    private ORM orm;
    private int reminderID = -1;
    private ArrayList<MeasurementType> measurementTypes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Create");
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance(this);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefEditor = sharedPrefs.edit();

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if(extras == null) {
                reminderID = -1;
            } else {
                reminderID = extras.getInt("reminder_id");
                prefEditor.putInt("reminder_id", reminderID);
                prefEditor.apply();
            }
        } else {
            reminderID = sharedPrefs.getInt("reminder_id", -1);
        }

        Log.d(LOG_PREFIX, "Reminder ID: " + Integer.toString(reminderID));

        if(reminderID == -1) {
            throw new NoSuchElementException("Caller didn't provide a reminderid");
        }

        Toast.makeText(this, "ID: " + Integer.toString(reminderID), Toast.LENGTH_SHORT).show();
        measurementTypes = orm.getReminderTimes().getTypesByReminderTimeID(reminderID);

        initUI();
        //Util.raiseNotification(this);
    }

    public void initUI() {
        Log.d(LOG_PREFIX, "Enter initUI" );

        setContentView(R.layout.content_reminders);
        Toolbar toolbar = (Toolbar) findViewById(R.id.reminderToolbar);
        setSupportActionBar(toolbar);

        // Ensure that no programmatically generated view within our ScrollView forces the
        // view to scroll down: We want the initial to view always to be at the top:
        // http://stackoverflow.com/a/35071620
        ScrollView view = (ScrollView) findViewById(R.id.reminderScrollView);
        view.setFocusableInTouchMode(true);
        view.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

        // Set up the save button, which, on click, saves the event and runs an animation
        Button saveButton = (Button) findViewById(R.id.reminderSaveButton);
        TextView thanksView = (TextView) findViewById(R.id.reminderThanksTextView);

        renderReminderEventTypes(R.id.reminderLayout);

        // The saveClickListener below will terminate this activity once it's done.
        saveButton.setOnClickListener(
                new SaveClickListener(this, measurementTypes, thanksView, true));
    }

    /**
     * Generate the entire page layout for a Reminder.
     * This involves creating the encasing widgets (tables, etc), instantiating Views based on
     * minute primitives, and rendering everything.
     *
     * @param layout The layout resource to be used as main container where all Views are created
     */
    public void renderReminderEventTypes(int layout) {
        Log.d(LOG_PREFIX, "Enter renderReminderEventTypes");
        Resources resources = getResources();

        // Get our main (scrollable) view, where we are to programmatically add our EntryTypes
        LinearLayout entryLayout = (LinearLayout) this.findViewById(layout);

        // Create a table
        TableLayout table = new TableLayout(this);
        table.setColumnStretchable(1, true); // Stretch the rightmost column (holding sliders etc)

        TableRow rowTitle = new TableRow(this);
        rowTitle.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        TableRow.LayoutParams rowParams = new TableRow.LayoutParams();
        rowParams.span = 1;

        rowParams.topMargin    = (int) resources.getDimension(R.dimen.entry_padding_top);
        rowParams.bottomMargin = (int) resources.getDimension(R.dimen.entry_padding_bottom);

        // FIXME not guaranteed correct order (list isn't sorted?)
        // Walk our measurement types and create the appropriate text and entry widget (slider, etc).
        // Add them to the main layout.
        for(int i = 0; i < measurementTypes.size(); i++) {
            //MeasurementType measurementType = orm.getMeasurementTypes().types.get(i);
            MeasurementType measurementType = measurementTypes.get(i);
            EntityPrimitive primitive = measurementType.getPrimitive(orm.getPrimitives());
            Log.d(LOG_PREFIX, "Renderer: primitive to render: " + primitive.name);

            // Make a label
            TextView label = new TextView(this);
            TextViewCompat.setTextAppearance(label,
                    android.R.style.TextAppearance_DeviceDefault_Small);
            label.setGravity(Gravity.START);
            label.setText(measurementType.name);

            TableRow row = new TableRow(this);
            // row.setBackgroundColor(Color.BLUE); // (debugging)
            row.addView(label, rowParams);

            // Make the appropriate widget
            switch(primitive.name) {

                case "range_center":
                case "range_normal":
                    SeekBar seekBar = new SeekBar(this);
                    Log.d(LOG_PREFIX, "Renderer: Range: Original View    : " + seekBar.toString());
                    measurementType.setView(seekBar);
                    // The drawable resource name (i.e 'res/drawable/range_center.xml') matches
                    // the database EntityPrimitive name.
                    int styleID = resources.getIdentifier(primitive.name,
                            "drawable", this.getPackageName());
                    seekBar.setProgressDrawable(
                            ResourcesCompat.getDrawable(resources, styleID, null));
                    seekBar.setMax(measurementType.totalValues);
                    seekBar.setProgress(sharedPrefs.getInt(measurementType.name,
                            measurementType.normalDefault));

                    SeekBarChangeListener seekBarChangeListener = new SeekBarChangeListener();
                    seekBarChangeListener.setPreference(prefEditor, measurementType.name);
                    seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
                    Log.d(LOG_PREFIX, "Renderer: Range: Assigning mType " +
                            Integer.toString(measurementType.id) + ": " +
                            "View " + measurementType.view.toString());

                    row.addView(seekBar, rowParams);
                    break;

                case "number":
                    TextView number = new TextView(this);
                    measurementType.setView(number);
                    Log.d(LOG_PREFIX, "Renderer: Number: Original View    : " + number.toString());

                    /* Can't find an easier way to do this - insane */
                    int[] attrs = new int[] { R.attr.editTextBackground};
                    TypedArray ta = this.obtainStyledAttributes(attrs);
                    Drawable drawableFromTheme = ta.getDrawable(0);
                    ta.recycle();
                    number.setBackgroundDrawable(drawableFromTheme);

                    number.setGravity(Gravity.CENTER_HORIZONTAL);
                    TextViewCompat.setTextAppearance(number,
                            android.R.style.TextAppearance_DeviceDefault_Medium);
                    //number.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);

                    number.setText(Integer.toString(
                            sharedPrefs.getInt(measurementType.name,
                                               measurementType.normalDefault)));
                    MeasurementTextClickListener listener =
                            new MeasurementTextClickListener(this, number, measurementType, themeID);
                    number.setOnClickListener(listener);
                    //TextInputEditText number = new TextInputEditText(this);
                    Log.d(LOG_PREFIX, "Renderer: Number: Assigning mType " +
                            Integer.toString(measurementType.id) + ": " +
                            "View " + measurementType.view.toString());

                    row.addView(number, rowParams);
                    break;

                case "text":
                    TextInputEditText text = new TextInputEditText(this);
                    measurementType.setView(text);
                    Log.d(LOG_PREFIX, "Renderer: Text: Original View    : " + text.toString());
                    //measurementType.setView(text);
                    text.setText(sharedPrefs.getString(measurementType.name, ""));
                    TextChangedListener textChangedListener = new TextChangedListener();
                    textChangedListener.setPreference(prefEditor, measurementType.name);
                    text.addTextChangedListener(textChangedListener);
                    TextViewCompat.setTextAppearance(text,
                            android.R.style.TextAppearance_DeviceDefault_Medium);
                    Log.d(LOG_PREFIX, "Renderer: Text: Assigning mType " +
                            Integer.toString(measurementType.id) + ": " +
                            "View " + measurementType.view.toString());
                    row.addView(text, rowParams);
                    break;

                default:
                    break;

            }

            table.addView(row);
        }

        entryLayout.addView(table);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        /*
        final View view = getWindow().getDecorView();
        final WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();

        lp.gravity = Gravity.CENTER;

        lp.width = 1000;
        lp.height = 1600;
        getWindowManager().updateViewLayout(view, lp);
        */
    }
}
