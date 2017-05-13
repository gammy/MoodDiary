package nu.ere.mooddiary;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

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
        // The saveClickListener below will terminate this activity once it's done.
        saveButton.setOnClickListener(new SaveClickListener(this, thanksView, true));

        Util.renderEntryTypes(this, R.id.reminderLayout, dialogThemeID);
    }
}
