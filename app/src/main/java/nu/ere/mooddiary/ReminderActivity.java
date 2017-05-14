package nu.ere.mooddiary;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.Toolbar;
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

public class ReminderActivity extends ThemedActivity {
    private static final String LOG_PREFIX = "ReminderActivity";
    private ORM orm;
    private int reminderID;
    private ArrayList<MeasurementType> measurementTypes = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Create");
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance(this);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            //PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Bundle extras = intent.getExtras();
            if(extras == null) {
                reminderID = -1;
            } else {
                reminderID = extras.getInt("reminder_id");
            }
        } else {
            Log.d(LOG_PREFIX, "Disappoint: getting serializable copy of intent extra");
            // FIXME causes crash on screen reorient: ticket 6b000d4cb67470a302b1241a83ee09e0bdf4a327 (85)
            // reminderID = Integer.parseInt((String) savedInstanceState.getSerializable("reminder_id"));
        }

        Log.d(LOG_PREFIX, "Reminder ID: " + Integer.toString(reminderID));
        Toast.makeText(this, "ID: " + Integer.toString(reminderID), Toast.LENGTH_SHORT).show();
        // TODO errorhandling (probably just abort on -1)
        measurementTypes = orm.getReminderTimes().getTypesByReminderTimeID(reminderID);

        initUI();
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

        renderReminderEventTypes(R.id.reminderLayout, dialogThemeID);

        // The saveClickListener below will terminate this activity once it's done.
        saveButton.setOnClickListener(new SaveClickListener(this, measurementTypes, thanksView, true));
    }

    /**
     * Generate the entire page layout for a Reminder.
     * This involves creating the encasing widgets (tables, etc), instantiating Views based on
     * minute primitives, and rendering everything.
     *
     * @param layout The layout resource to be used as main container where all Views are created
     * @param dialogThemeID Theme (style) id to pass to any dialog click listeners
     */
    public void renderReminderEventTypes(int layout, int dialogThemeID) {
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

        // Walk our measurement types and create the appropriate text and entry widget (slider, etc).
        // Add them to the main layout.
        for(int i = 0; i < measurementTypes.size(); i++) {
            //MeasurementType measurementType = orm.getMeasurementTypes().types.get(i);
            MeasurementType measurementType = measurementTypes.get(i);
            EntityPrimitive primitive = measurementType.getPrimitive(orm.getPrimitives());

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
                    measurementType.setView(seekBar);
                    // The drawable resource name (i.e 'res/drawable/range_center.xml') matches
                    // the database EntityPrimitive name.
                    int styleID = resources.getIdentifier(primitive.name,
                            "drawable", this.getPackageName());
                    seekBar.setProgressDrawable(
                            ResourcesCompat.getDrawable(resources, styleID, null));
                    seekBar.setMax(measurementType.totalValues);
                    seekBar.setProgress(measurementType.normalDefault);
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
                    measurementType.setView(number);
                    number.setGravity(Gravity.CENTER_HORIZONTAL);
                    TextViewCompat.setTextAppearance(number,
                            android.R.style.TextAppearance_DeviceDefault_Medium);
                    //number.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);

                    number.setText(Long.toString(measurementType.normalDefault));
                    MeasurementTextClickListener listener =
                            new MeasurementTextClickListener(this, number, measurementType, dialogThemeID);
                    number.setOnClickListener(listener);
                    row.addView(number, rowParams);
                    break;

                case "text":
                    TextInputEditText text = new TextInputEditText(this);
                    TextViewCompat.setTextAppearance(text,
                            android.R.style.TextAppearance_DeviceDefault_Medium);
                    measurementType.setView(text);
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
