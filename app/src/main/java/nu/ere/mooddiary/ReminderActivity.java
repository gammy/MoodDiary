package nu.ere.mooddiary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ReminderActivity extends ThemedActivity {
    private static final String LOG_PREFIX = "ReminderActivity";
    private ORM orm;
    private int reminderID;

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
        // The saveClickListener below will terminate this activity once it's done.
        saveButton.setOnClickListener(new SaveClickListener(this, thanksView, true));

        Util.renderReminderEventTypes(this, reminderID, R.id.reminderLayout, dialogThemeID);
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
