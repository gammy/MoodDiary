package nu.ere.mooddiary;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
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

// TODO: get database objects from MainActivity, perhaps with:
//http://stackoverflow.com/questions/2906925/how-do-i-pass-an-object-from-one-activity-to-another-on-android

public class ReminderActivity extends ThemedActivity {
    private static final String LOG_PREFIX = "ReminderActivity";
    private ORM orm;
    private long reminderID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Create");
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance(this);

        // So, getting the *correct* intent (i.e the one sent from the alarm) is a bit
        // tricky: If another of our app activities (say MainActivity) is still running,
        // and this (ReminderActivity) is raised, the intent will come from Main.
        // I ... I think.

        // FIXME all of this is broken.
        /*
        Intent intent = getIntent();
        reminderID = intent.getLongExtra("reminder_id", -1);
//        Toast.makeText(this,
//                "Reminder ID: " + Long.toString(reminderID),
//                Toast.LENGTH_LONG);
        */
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Bundle extras = intent.getExtras();
            if(extras == null) {
                reminderID = -1;
            } else {
                reminderID = extras.getLong("reminder_id");
                Log.d(LOG_PREFIX, "GOT EXTRA!");
            }
        } else {
            Log.d(LOG_PREFIX, "Disappoint: getting serializable copy of intent extra");
            reminderID = Long.parseLong((String) savedInstanceState.getSerializable("reminder_id"));
        }
        Log.d(LOG_PREFIX, "Reminder ID: " + Long.toString(reminderID));

        /** Notification test ***********************/

        Intent resultIntent = new Intent(this, ReminderActivity.class);
        // ..
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("My notification")
                    .setContentText("Hello World!");

        mBuilder.setContentIntent(pendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

        initUI();
    }

    public void initUI() {
        Log.d(LOG_PREFIX, "Enter initUI" );

        setContentView(R.layout.coordinator_reminders);
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
        saveButton.setOnClickListener(new SaveClickListener(this, thanksView));

        renderEntryTypes();
    }
    public void renderEntryTypes() {
        Log.d(LOG_PREFIX, "Enter renderEntryTypes");
        // FIXME move some of this back into initUI; this function is only called ONCE.

        // Get our main (scrollable) view, where we are to programmatically add our EntryTypes
        LinearLayout entryLayout = (LinearLayout) findViewById(R.id.reminderLayout);

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
            EntityPrimitive primitive = etype.getPrimitive(orm.getPrimitives());
            Log.d(LOG_PREFIX, "Rendering event id " + Long.toString(etype.id) + ", type " + Long.toString(etype.entity));

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

                    // Can't find an easier way to do this - insane
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
                            new EventNumberClickListener(ReminderActivity.this, number, etype, dialogThemeID);
                    number.setOnClickListener(listener);
                    row.addView(number, rowParams);
                    break;

                case "text":
                    TextInputEditText text = new TextInputEditText(this);
                    etype.setView(text);
                    row.addView(text, rowParams);
                    break;

                default:
                    Log.d(LOG_PREFIX, "Hm, not sure how to render this: " + etype.name);
                    break;

            }

            table.addView(row);
        }

        entryLayout.addView(table);
    }
}
